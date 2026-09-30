local grid   = require("grid")
local log    = require("log")
local config = require("config")
local logMsg = log.logMsg

local function generateBuildQueue()
  local gridTable     = grid.getGrid()
  local meta          = grid.getMeta()
  local MAP_SIZE      = meta.MAP_SIZE
  local totalY        = meta.totalY
  local tx            = meta.tx
  local tz            = meta.tz
  local geolyzerYGrid = meta.geolyzerYGrid
  local r             = config.ANALYSIS_RADIUS

  local startX, startY, startZ = tx, geolyzerYGrid + 1, tz
  local reachable = grid.computeReachable(startX, startY, startZ)

  local function isReachable(x, y, z)
    return reachable[x .. "," .. y .. "," .. z] == true
  end

  local blueprintMap = {}
  local function setBP(x, y, z)
    if x < 1 or x > MAP_SIZE or z < 1 or z > MAP_SIZE then return end
    if y < 1 or y > totalY                              then return end
    blueprintMap[x .. "," .. y .. "," .. z] = { x = x, y = y, z = z }
  end

  local function isPillboxWall(dx, dz, outerR, innerR)
    local distSq  = dx * dx + dz * dz
    local inOuter = (math.abs(dx) + math.abs(dz) <= outerR * 1.35) and (distSq <= outerR * outerR)
    local inInner = (math.abs(dx) + math.abs(dz) <= innerR * 1.35) and (distSq <= innerR * innerR)
    return inOuter and not inInner
  end

  local function isPillboxFloor(dx, dz, outerR)
    local distSq = dx * dx + dz * dz
    return (math.abs(dx) + math.abs(dz) <= outerR * 1.35) and (distSq <= outerR * outerR)
  end

  local mainOuterR = math.min(r, 6)
  local mainInnerR = math.max(2, mainOuterR - 2)

  local function matchesFootprint(dx, dz)
    if isPillboxFloor(dx, dz, mainOuterR)        then return true end
    if math.abs(dz) <= 2 and math.abs(dx) <= r  then return true end
    if math.abs(dx) <= 2 and math.abs(dz) <= r  then return true end
    if math.sqrt((dx - r)^2 + dz^2)       <= 3.5 then return true end
    if math.sqrt((dx + r)^2 + dz^2)       <= 3.5 then return true end
    if math.sqrt(dx^2 + (dz - r)^2)       <= 3.5 then return true end
    if math.sqrt(dx^2 + (dz + r)^2)       <= 3.5 then return true end
    return false
  end

  local ty = geolyzerYGrid

  for dx = -r, r do
    for dz = -r, r do
      if matchesFootprint(dx, dz) then
        local targetX = tx + dx
        local targetZ = tz + dz
        if targetX >= 1 and targetX <= MAP_SIZE and targetZ >= 1 and targetZ <= MAP_SIZE then
          local groundY = 1
          for ny = ty, 1, -1 do
            if gridTable[targetX][ny][targetZ] then
              groundY = ny
              break
            end
          end
          for ny = groundY, ty do
            setBP(targetX, ny, targetZ)
          end
        end
      end
    end
  end

  local wallBase   = ty + 1
  local wallHeight = 4
  for ny = wallBase, wallBase + wallHeight do
    local relY = ny - wallBase
    for dx = -mainOuterR, mainOuterR do
      for dz = -mainOuterR, mainOuterR do
        local isDoorwayY      = (relY <= 1)
        local isCorridorDoorX = isDoorwayY and math.abs(dz) <= 1 and math.abs(dx) >= mainInnerR
        local isCorridorDoorZ = isDoorwayY and math.abs(dx) <= 1 and math.abs(dz) >= mainInnerR
        local isEmbrasureY     = (relY == 1)
        local isEmbrasureAngle = (math.abs(dx) == math.abs(dz)) or (dx == 0) or (dz == 0)

        if isPillboxWall(dx, dz, mainOuterR, mainInnerR) then
          if not isCorridorDoorX and not isCorridorDoorZ then
            if not (isEmbrasureY and isEmbrasureAngle) then
              setBP(tx + dx, ny, tz + dz)
            end
          end
        end
      end
    end
  end

  if r > 6 then
    for ny = ty + 1, ty + 3 do
      for d = mainOuterR - 1, r do
        setBP(tx + d, ny, tz - 2)
        setBP(tx + d, ny, tz + 2)
        setBP(tx - d, ny, tz - 2)
        setBP(tx - d, ny, tz + 2)

        setBP(tx - 2, ny, tz + d)
        setBP(tx + 2, ny, tz + d)
        setBP(tx - 2, ny, tz - d)
        setBP(tx + 2, ny, tz - d)

        if ny == ty + 3 then
          for w = -2, 2 do
            setBP(tx + d, ny, tz + w)
            setBP(tx - d, ny, tz + w)
            setBP(tx + w, ny, tz + d)
            setBP(tx + w, ny, tz - d)
          end
        end
      end
    end

    local cardinalOffsets = {
      { x =  r, z =  0, dir = "EAST"  },
      { x = -r, z =  0, dir = "WEST"  },
      { x =  0, z =  r, dir = "SOUTH" },
      { x =  0, z = -r, dir = "NORTH" },
    }

    local outpostR = 3.5

    for _, offset in ipairs(cardinalOffsets) do
      local cx = tx + offset.x
      local cz = tz + offset.z

      for ny = ty + 1, ty + 3 do
        for dx = -4, 4 do
          for dz = -4, 4 do
            local distSq = dx * dx + dz * dz

            local isWall = distSq <= outpostR * outpostR
                       and distSq >= (outpostR - 1.5) * (outpostR - 1.5)

            local isSlit = (ny == ty + 2)
                       and (math.abs(dx) <= 1 or math.abs(dz) <= 1)

            local isPassageY     = (ny <= ty + 2)
            local isEntryDoorway = false
            if isPassageY then
              if offset.dir == "EAST"  and dx <= 0 and math.abs(dz) <= 1 then isEntryDoorway = true end
              if offset.dir == "WEST"  and dx >= 0 and math.abs(dz) <= 1 then isEntryDoorway = true end
              if offset.dir == "SOUTH" and dz <= 0 and math.abs(dx) <= 1 then isEntryDoorway = true end
              if offset.dir == "NORTH" and dz >= 0 and math.abs(dx) <= 1 then isEntryDoorway = true end
            end

            if isWall and not isSlit and not isEntryDoorway then
              setBP(cx + dx, ny, cz + dz)
            end

            if ny == ty + 3 and distSq <= outpostR * outpostR then
              setBP(cx + dx, ny, cz + dz)
            end
          end
        end
      end
    end
  end

  local rawQueue = {}
  for _, block in pairs(blueprintMap) do
    if not gridTable[block.x][block.y][block.z] then
      if isReachable(block.x, block.y + 1, block.z) then
        table.insert(rawQueue, block)
      end
    end
  end

  local buildQueue = {}
  local pool = rawQueue

  while #pool > 0 do
    local progressMade = false
    for i = 1, #pool do
      local candidate = pool[i]
      if grid.hasSupport(candidate.x, candidate.y, candidate.z) then
        table.insert(buildQueue, candidate)
        grid.setBlock(candidate.x, candidate.y, candidate.z, true)
        table.remove(pool, i)
        progressMade = true
        break
      end
    end
    if not progressMade then break end
  end

  for _, b in ipairs(buildQueue) do
    grid.setBlock(b.x, b.y, b.z, false)
  end

  local homePos = { x = tx, y = geolyzerYGrid + 1, z = tz }
  local safeY   = math.min(totalY - 1, ty + wallHeight + 3)

  return buildQueue, homePos, safeY
end

return {
  generateBuildQueue = generateBuildQueue,
}

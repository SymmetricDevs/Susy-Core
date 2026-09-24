local component = require("component")
local config = require("config")
local log = require("log")
local logMsg = log.logMsg

local MAP_SIZE = config.MAP_SIZE
local ox, oz = -16, -16
local starty, stopy = -5, 26
local totalY = 1 + stopy - starty

local TILE = 4

local grid = {}
for x = 1, MAP_SIZE do
  grid[x] = {}
  for y = 1, totalY do
    grid[x][y] = {}
    for z = 1, MAP_SIZE do
      grid[x][y][z] = false
    end
  end
end

local geolyzer = component.isAvailable("geolyzer") and component.geolyzer or nil

local tx, tz, geolyzerYGrid

local function scanTerrain()
  if not geolyzer then
    error("scanTerrain called but no Geolyzer is available on this server.")
  end
  logMsg("Scanning terrain via Geolyzer (tiled volume)...")

  for oy = 0, totalY - 1, TILE do
    for oz_offset = 0, MAP_SIZE - 1, TILE do
      for ox_offset = 0, MAP_SIZE - 1, TILE do

        local sx = math.min(TILE, MAP_SIZE - ox_offset)
        local sy = math.min(TILE, totalY - oy)
        local sz = math.min(TILE, MAP_SIZE - oz_offset)

        local worldX = ox + ox_offset
        local worldY = starty + oy
        local worldZ = oz + oz_offset

        local ok, data = pcall(geolyzer.scan, worldX, worldZ, worldY, sx, sz, sy)
        if not ok or not data then
          error("geolyzer.scan failed at ("..worldX..","..worldY..","..worldZ..")")
        end

        local i = 1
        for y = 0, sy - 1 do
          for z = 0, sz - 1 do
            for x = 0, sx - 1 do
              local gx = ox_offset + x + 1
              local gy = oy + y + 1
              local gz = oz_offset + z + 1
              local hardness = data[i]
              grid[gx][gy][gz] = (hardness and hardness ~= 0)
              i = i + 1
            end
          end
        end

      end
    end
    os.sleep(0.1)
  end

  logMsg("Scan completed.")

  tx = 1 - ox
  tz = 1 - oz
  geolyzerYGrid = 1 - starty
  logMsg(string.format("Geolyzer mapped to grid centre: (%d, %d, %d)",
    tx, geolyzerYGrid, tz))
end

local function setMeta(new_tx, new_tz, new_geolyzerYGrid)
  tx            = new_tx
  tz            = new_tz
  geolyzerYGrid = new_geolyzerYGrid
end

local function hasSupport(bX, bY, bZ)
  if bY > 1 and grid[bX][bY - 1][bZ] then return true end
  if bX > 1 and grid[bX - 1][bY][bZ] then return true end
  if bX < MAP_SIZE and grid[bX + 1][bY][bZ] then return true end
  if bZ > 1 and grid[bX][bY][bZ - 1] then return true end
  if bZ < MAP_SIZE and grid[bX][bY][bZ + 1] then return true end
  return false
end

local function computeReachable(startX, startY, startZ)
  local visited = {}
  local queue = { {x=startX, y=startY, z=startZ} }
  visited[startX .. "," .. startY .. "," .. startZ] = true

  local directions = {
    {1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}
  }

  while #queue > 0 do
    local p = table.remove(queue, 1)
    for _, d in ipairs(directions) do
      local nx, ny, nz = p.x + d[1], p.y + d[2], p.z + d[3]
      if nx >= 1 and nx <= MAP_SIZE
      and ny >= 1 and ny <= totalY
      and nz >= 1 and nz <= MAP_SIZE then
        local key = nx .. "," .. ny .. "," .. nz
        if not visited[key] and not grid[nx][ny][nz] then
          visited[key] = true
          table.insert(queue, {x=nx, y=ny, z=nz})
        end
      end
    end
  end

  return visited
end

local function sealUnreachable(startX, startY, startZ)
  local reachable = computeReachable(startX, startY, startZ)
  local sealed = 0
  for x = 1, MAP_SIZE do
    for y = 1, totalY do
      for z = 1, MAP_SIZE do
        if not grid[x][y][z] then
          local key = x .. "," .. y .. "," .. z
          if not reachable[key] then
            grid[x][y][z] = true
            sealed = sealed + 1
          end
        end
      end
    end
  end
  return sealed
end

local function getGrid() return grid end

local function getMeta()
  return {
    MAP_SIZE      = MAP_SIZE,
    totalY        = totalY,
    tx            = tx,
    tz            = tz,
    geolyzerYGrid = geolyzerYGrid,
  }
end

local function setBlock(x, y, z, val)
  grid[x][y][z] = val
end

return {
  scanTerrain      = scanTerrain,
  setMeta          = setMeta,
  hasSupport       = hasSupport,
  computeReachable = computeReachable,
  sealUnreachable  = sealUnreachable,
  getGrid          = getGrid,
  getMeta          = getMeta,
  setBlock         = setBlock,
}

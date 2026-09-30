local grid   = require("grid")
local log    = require("log")
local logMsg = log.logMsg

local function heuristic(a, b)
  return math.abs(a.x - b.x) + math.abs(a.y - b.y) + math.abs(a.z - b.z)
end

local function heapPush(h, item)
  local i = #h + 1
  h[i] = item
  while i > 1 do
    local p = math.floor(i / 2)
    if h[p].f <= h[i].f then break end
    h[p], h[i] = h[i], h[p]
    i = p
  end
end

local function heapPop(h)
  local top = h[1]
  local n   = #h
  h[1]      = h[n]
  h[n]      = nil
  local n2  = n - 1
  local i   = 1
  while true do
    local l, r, s = 2 * i, 2 * i + 1, i
    if l <= n2 and h[l].f < h[s].f then s = l end
    if r <= n2 and h[r].f < h[s].f then s = r end
    if s == i then break end
    h[i], h[s] = h[s], h[i]
    i = s
  end
  return top
end

local DIRS = {
  {x=1,y=0,z=0},{x=-1,y=0,z=0},
  {x=0,y=1,z=0},{x=0,y=-1,z=0},
  {x=0,y=0,z=1},{x=0,y=0,z=-1},
}

local function findPath(startPos, goalPos)
  logMsg(string.format("[A* Start] Calc Path: (%d,%d,%d) -> (%d,%d,%d)",
    startPos.x, startPos.y, startPos.z,
    goalPos.x,  goalPos.y,  goalPos.z))

  if startPos.x == goalPos.x
  and startPos.y == goalPos.y
  and startPos.z == goalPos.z then
    logMsg("[A* Done] Already at destination.")
    return {}
  end

  local gridTable = grid.getGrid()
  local meta      = grid.getMeta()
  local MAP_SIZE  = meta.MAP_SIZE
  local totalY    = meta.totalY

  local heap     = {}
  local gScore   = {}
  local cameFrom = {}

  local function key(p) return p.x .. "," .. p.y .. "," .. p.z end

  local sk = key(startPos)
  gScore[sk] = 0
  heapPush(heap, {pos=startPos, dir=nil, f=heuristic(startPos,goalPos), g=0})

  local iterations = 0

  while #heap > 0 do
    iterations = iterations + 1
    local cur    = heapPop(heap)
    local curKey = key(cur.pos)

    if cur.g > gScore[curKey] then
    elseif cur.pos.x == goalPos.x
       and cur.pos.y == goalPos.y
       and cur.pos.z == goalPos.z then
      local path = {}
      local ck   = curKey
      while cameFrom[ck] do
        local rec = cameFrom[ck]
        table.insert(path, 1, rec.pos)
        ck = rec.parentKey
      end
      logMsg(string.format("[A* Success] Path found (%d steps, %d iterations)",
        #path, iterations))
      return path
    else
      for _, d in ipairs(DIRS) do
        local nb = { x=cur.pos.x+d.x, y=cur.pos.y+d.y, z=cur.pos.z+d.z }
        if nb.x >= 1 and nb.x <= MAP_SIZE
        and nb.y >= 1 and nb.y <= totalY
        and nb.z >= 1 and nb.z <= MAP_SIZE
        and not gridTable[nb.x][nb.y][nb.z] then
          local isTurn = cur.dir ~= nil
            and (cur.dir.x ~= d.x or cur.dir.y ~= d.y or cur.dir.z ~= d.z)
          local g2  = cur.g + 1.0 + (isTurn and 3.0 or 0.0)
          local nbk = key(nb)
          if g2 < (gScore[nbk] or math.huge) then
            gScore[nbk]   = g2
            cameFrom[nbk] = { pos=nb, parentKey=curKey }
            heapPush(heap, {pos=nb, dir=d, f=g2+heuristic(nb,goalPos), g=g2})
          end
        end
      end
    end
  end

  logMsg(string.format("[A* Fail] No path found after %d iterations.", iterations))
  return nil
end

local function compressPath(path, startPos)
  if #path == 0 then return {} end

  local moves = {}
  local currentPos = { x=startPos.x, y=startPos.y, z=startPos.z }

  local lastDx = path[1].x - currentPos.x
  local lastDy = path[1].y - currentPos.y
  local lastDz = path[1].z - currentPos.z
  local accumX, accumY, accumZ = lastDx, lastDy, lastDz

  for i = 2, #path do
    local dx = path[i].x - path[i-1].x
    local dy = path[i].y - path[i-1].y
    local dz = path[i].z - path[i-1].z
    if dx == lastDx and dy == lastDy and dz == lastDz then
      accumX = accumX + dx
      accumY = accumY + dy
      accumZ = accumZ + dz
    else
      table.insert(moves, { x=accumX, y=accumY, z=accumZ })
      lastDx, lastDy, lastDz = dx, dy, dz
      accumX, accumY, accumZ = dx, dy, dz
    end
  end
  table.insert(moves, { x=accumX, y=accumY, z=accumZ })
  return moves
end

return {
  findPath     = findPath,
  compressPath = compressPath,
}

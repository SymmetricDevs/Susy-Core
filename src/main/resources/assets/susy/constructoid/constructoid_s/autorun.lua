local component   = require("component")
local event       = require("event")
local computer    = require("computer")
local config      = require("config")
local log         = require("log")
local grid        = require("grid")
local pathfinding = require("pathfinding")
local logMsg      = log.logMsg

local modem  = component.modem
local PORT   = config.PORT
local myAddr = computer.address()

modem.open(PORT)
modem.setStrength(400)
logMsg("=== Slave Server Started === addr=" .. myAddr)

local masterModemAddr = nil

local function toMaster(...)
  if masterModemAddr then
    modem.send(masterModemAddr, PORT, ...)
  else
    modem.broadcast(PORT, ...)
  end
end

local flights = {}

local function droneCmd(droneId, cmd, ...)
  modem.broadcast(PORT, cmd, droneId, ...)
end

local function dispatchNextMove(droneId)
  local fs = flights[droneId]
  if not fs then return end
  if #fs.moveBuffer > 0 then
    local mv         = table.remove(fs.moveBuffer, 1)
    fs.pendingMove   = mv
    fs.pendingMoveAt = computer.uptime()
    fs.status        = "moving"
    droneCmd(droneId, "move", mv.x, mv.y, mv.z)
  else
    if fs.afterCtx == "place" then
      fs.settleUntil = computer.uptime() + 1.5
      fs.status      = "settling"
    else
      toMaster("flight_done", droneId,
        fs.pos.x, fs.pos.y, fs.pos.z, fs.moveCount, fs.afterCtx)
      flights[droneId] = nil
    end
  end
end

local function startFlight(droneId, fromPos, goalPos, ctx, blockPos)
  if flights[droneId] then
    logMsg(string.format("[Slave] WARNING: overwriting flight for drone %d", droneId))
    flights[droneId] = nil
  end

  local path = pathfinding.findPath(fromPos, goalPos)

  if not path then
    if blockPos and blockPos.x >= 0 then
      toMaster("no_path", droneId, blockPos.x, blockPos.y, blockPos.z,
        fromPos.x, fromPos.y, fromPos.z)
    else
      toMaster("flight_done", droneId,
        fromPos.x, fromPos.y, fromPos.z, 0, ctx)
    end
    return
  end

  if #path == 0 then
    if ctx == "place" then
      flights[droneId] = {
        pos        = { x = fromPos.x, y = fromPos.y, z = fromPos.z },
        moveBuffer = {}, pendingMove = nil, pendingMoveAt = 0,
        moveCount  = 0, afterCtx = ctx, blockPos = blockPos,
        status     = "settling", settleUntil = computer.uptime() + 1.5,
        placingAt  = 0,
      }
    else
      toMaster("flight_done", droneId,
        fromPos.x, fromPos.y, fromPos.z, 0, ctx)
    end
    return
  end

  local moves = pathfinding.compressPath(path, fromPos)
  flights[droneId] = {
    pos          = { x = fromPos.x, y = fromPos.y, z = fromPos.z },
    moveBuffer   = moves,
    pendingMove  = nil,
    pendingMoveAt= 0,
    moveCount    = 0,
    afterCtx     = ctx,
    blockPos     = blockPos,
    status       = "moving",
    settleUntil  = 0,
    placingAt    = 0,
  }
  dispatchNextMove(droneId)
end

local function deserializeGrid(data, MAP_SIZE, totalY)
  local p2   = {1, 2, 4, 8, 16, 32, 64, 128}
  local idx  = 1
  local bit  = 1
  local byte = string.byte(data, idx) or 0
  for x = 1, MAP_SIZE do
    for y = 1, totalY do
      for z = 1, MAP_SIZE do
        grid.setBlock(x, y, z, (byte % (p2[bit] * 2)) >= p2[bit])
        bit = bit + 1
        if bit > 8 then
          idx  = idx + 1
          byte = string.byte(data, idx) or 0
          bit  = 1
        end
      end
    end
  end
end

modem.broadcast(PORT, "slave_ack", myAddr)
logMsg("Waiting for terrain...")

local terrainReady = false
while not terrainReady do
  local args = {event.pull(30, "modem_message")}
  if not args[1] then
    logMsg("No terrain yet – re-announcing...")
    modem.broadcast(PORT, "slave_ack", myAddr)
  else
    if not masterModemAddr then
      masterModemAddr = args[3]
      logMsg("Master modem addr learned: " .. masterModemAddr)
    end

    local msg = args[6]
    if msg == "identify_slave" then
      toMaster("slave_ack", myAddr)

    elseif msg == "terrain" then
      local data, MAP_SIZE, totalY, tx, tz, gyGrid =
        args[7], args[8], args[9], args[10], args[11], args[12]
      logMsg(string.format("Terrain received (%d×%d×%d). Loading...",
        MAP_SIZE, totalY, MAP_SIZE))
      deserializeGrid(data, MAP_SIZE, totalY)
      grid.setMeta(tx, tz, gyGrid)
      toMaster("terrain_ready", myAddr)
      terrainReady = true
      logMsg("Terrain loaded. Slave operational.")
    end
  end
end

logMsg("=== Slave ready ===")
while true do
  local args = {event.pull(0.1, "modem_message")}

  if args[1] then
    if not masterModemAddr then
      masterModemAddr = args[3]
    end

    local msg = args[6]
    local p   = {}
    for i = 7, #args do p[#p + 1] = args[i] end

    if msg == "identify_slave" then
      toMaster("slave_ack", myAddr)

    elseif msg == "fly" then
      if p[1] ~= myAddr then
      else
        local droneId = p[2]
        local fx, fy, fz = p[3]:match("(-?%d+),(-?%d+),(-?%d+)")
        local gx, gy, gz = p[4]:match("(-?%d+),(-?%d+),(-?%d+)")
        local ctx         = p[5]
        local bx, by, bz  = p[6]:match("(-?%d+),(-?%d+),(-?%d+)")
        local fromPos  = { x = tonumber(fx), y = tonumber(fy), z = tonumber(fz) }
        local goalPos  = { x = tonumber(gx), y = tonumber(gy), z = tonumber(gz) }
        local blockPos = (tonumber(bx) and tonumber(bx) >= 0)
          and { x = tonumber(bx), y = tonumber(by), z = tonumber(bz) } or nil
        logMsg(string.format("[fly] Drone %d: (%s)→(%s) ctx=%s",
          droneId, p[3], p[4], tostring(ctx)))
        startFlight(droneId, fromPos, goalPos, ctx, blockPos)
      end

    elseif msg == "ack" then
      local droneId = p[1]
      local fs      = flights[droneId]
      if fs then
        if fs.status == "moving" and fs.pendingMove then
          fs.pos.x    = fs.pos.x + fs.pendingMove.x
          fs.pos.y    = fs.pos.y + fs.pendingMove.y
          fs.pos.z    = fs.pos.z + fs.pendingMove.z
          fs.moveCount= fs.moveCount + 1
          fs.pendingMove = nil
          dispatchNextMove(droneId)
        elseif fs.status == "placing" then
          local bp      = fs.blockPos
          local success = p[2]

          if success == false then
            logMsg(string.format("[place] Drone %d: place failed at (%d,%d,%d) – re-queuing",
              droneId, bp.x, bp.y, bp.z))
            toMaster("place_failed", droneId, bp.x, bp.y, bp.z,
              fs.pos.x, fs.pos.y, fs.pos.z)
          else
            toMaster("placed", droneId,
              bp.x .. "," .. bp.y .. "," .. bp.z,
              fs.moveCount,
              fs.pos.x .. "," .. fs.pos.y .. "," .. fs.pos.z)
          end
          flights[droneId] = nil
        end
      end

    elseif msg == "sync_block" then
      grid.setBlock(p[1], p[2], p[3], true)
    end
  end

  local toRemove = {}
  for droneId, fs in pairs(flights) do
    if fs.status == "settling" and computer.uptime() >= fs.settleUntil then
      droneCmd(droneId, "place", 0)
      fs.status    = "placing"
      fs.placingAt = computer.uptime()

    elseif fs.status == "placing"
    and fs.placingAt > 0
    and computer.uptime() - fs.placingAt > 4 then
      logMsg(string.format("[timeout] Retrying place for drone %d", droneId))
      droneCmd(droneId, "place", 0)
      fs.placingAt = computer.uptime()

    elseif fs.status == "moving"
    and fs.pendingMove
    and fs.pendingMoveAt > 0
    and computer.uptime() - fs.pendingMoveAt > 15 then
      logMsg(string.format("[timeout] Move ack lost for drone %d – reporting failure", droneId))
      local bp = fs.blockPos
      if bp and bp.x >= 0 then
        toMaster("no_path", droneId, bp.x, bp.y, bp.z,
          fs.pos.x, fs.pos.y, fs.pos.z)
      else
        toMaster("flight_done", droneId,
          fs.pos.x, fs.pos.y, fs.pos.z, fs.moveCount, fs.afterCtx)
      end
      toRemove[#toRemove + 1] = droneId
    end
  end
  for _, id in ipairs(toRemove) do flights[id] = nil end
end

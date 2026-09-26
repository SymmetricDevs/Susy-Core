local component  = require("component")
local event      = require("event")
local computer   = require("computer")
local config     = require("config")
local log        = require("log")
local grid       = require("grid")
local buildqueue = require("buildqueue")
local logMsg     = log.logMsg

if not component.isAvailable("geolyzer") then
  io.stderr:write("Master requires a Geolyzer.\n"); return
end
if not component.isAvailable("modem") then
  io.stderr:write("Master requires a Modem.\n"); return
end

local modem = component.modem
local PORT  = config.PORT
modem.open(PORT)
modem.setStrength(400)
logMsg("=== Master Server Started ===")

grid.scanTerrain()
local meta = grid.getMeta()

local droneStartY = meta.geolyzerYGrid + 1
local sealed = grid.sealUnreachable(meta.tx, droneStartY, meta.tz)
logMsg(string.format("Sealed %d unreachable air cell(s).", sealed))

local function serializeGrid()
  local g    = grid.getGrid()
  local sz   = meta.MAP_SIZE
  local ty   = meta.totalY
  local p2   = {1, 2, 4, 8, 16, 32, 64, 128}
  local out  = {}
  local bit  = 1
  local byte = 0
  for x = 1, sz do
    for y = 1, ty do
      for z = 1, sz do
        if g[x][y][z] then byte = byte + p2[bit] end
        bit = bit + 1
        if bit > 8 then
          out[#out + 1] = string.char(byte)
          byte = 0; bit = 1
        end
      end
    end
  end
  if bit > 1 then out[#out + 1] = string.char(byte) end
  return table.concat(out)
end

local function discover(broadcastMsg, filterFn)
  local ids   = {}
  local addrs = {}
  modem.broadcast(PORT, broadcastMsg)
  local t0 = computer.uptime()
  repeat
    local rem  = config.DISCOVERY_WINDOW - (computer.uptime() - t0)
    if rem <= 0 then break end
    local args = {event.pull(rem, "modem_message")}
    if args[1] then
      local from = args[3]
      local resp = args[6]
      local p1   = args[7]
      local id, addr = filterFn(resp, p1, from)
      if id then
        local dup = false
        for _, v in ipairs(ids) do if v == id then dup = true; break end end
        if not dup then
          ids[#ids + 1]   = id
          addrs[#addrs+1] = addr
          logMsg(string.format("  Found: %s", tostring(id)))
        end
      end
    end
  until computer.uptime() - t0 >= config.DISCOVERY_WINDOW
  return ids, addrs
end

logMsg("=== Drone Discovery ===")
local droneIds = discover("identify", function(resp, p1)
  if resp == "ack" and type(p1) == "number" then return p1, nil end
end)
logMsg(string.format("Drones found: %d", #droneIds))
if #droneIds == 0 then logMsg("No drones. Aborting."); log.closeLog(); return end

logMsg("=== Slave Discovery ===")
local slaveCompAddrs  = {}
local slaveModemAddrs = {}

modem.broadcast(PORT, "identify_slave")
local t1 = computer.uptime()
repeat
  local rem  = config.DISCOVERY_WINDOW - (computer.uptime() - t1)
  if rem <= 0 then break end
  local args = {event.pull(rem, "modem_message")}
  if args[1] then
    local senderModem = args[3]
    local resp        = args[6]
    local p1          = args[7]
    if resp == "slave_ack" and type(p1) == "string" then
      local dup = false
      for _, v in ipairs(slaveCompAddrs) do if v == p1 then dup = true; break end end
      if not dup then
        slaveCompAddrs[#slaveCompAddrs + 1]  = p1
        slaveModemAddrs[#slaveModemAddrs + 1] = senderModem
        logMsg(string.format("  Found slave: %s (modem: %s)", p1, senderModem))
      end
    end
  end
until computer.uptime() - t1 >= config.DISCOVERY_WINDOW

logMsg(string.format("Slaves found: %d", #slaveCompAddrs))
if #slaveCompAddrs == 0 then logMsg("No slaves. Aborting."); log.closeLog(); return end

logMsg("=== Broadcasting Terrain ===")
local terrainData = serializeGrid()
for _, maddr in ipairs(slaveModemAddrs) do
  modem.send(maddr, PORT, "terrain", terrainData,
    meta.MAP_SIZE, meta.totalY, meta.tx, meta.tz, meta.geolyzerYGrid)
end

local readySet = {}
local t2 = computer.uptime()
while computer.uptime() - t2 < 15 do
  local rem  = 15 - (computer.uptime() - t2)
  local args = {event.pull(rem, "modem_message")}
  if args[1] and args[6] == "terrain_ready" then
    local slaveId = args[7]
    if slaveId and not readySet[slaveId] then
      readySet[slaveId] = true
      logMsg("  Slave ready: " .. slaveId)
    end
  end
  local allReady = true
  for _, a in ipairs(slaveCompAddrs) do
    if not readySet[a] then allReady = false; break end
  end
  if allReady then break end
end

logMsg("=== Generating Build Queue ===")
local buildQueue, homePos, safeY = buildqueue.generateBuildQueue()
logMsg(string.format("Build queue: %d blocks", #buildQueue))

local sharedQueue = {}
for _, b in ipairs(buildQueue) do
  sharedQueue[#sharedQueue + 1] = {
    x = b.x, y = b.y, z = b.z,
    placed = false, claimedBy = nil, failCount = 0, placeFailCount = 0,
  }
end

local giveUp = false

local rechargeRequested = false
local rechargeActive    = false

local function queueDone()
  if giveUp then return true end
  for _, e in ipairs(sharedQueue) do if not e.placed then return false end end
  return true
end

local function currentLayerY()
  local minY = math.huge
  for _, e in ipairs(sharedQueue) do
    if not e.placed and e.y < minY then minY = e.y end
  end
  return minY == math.huge and nil or minY
end

local states = {}
for _, id in ipairs(droneIds) do
  states[id] = {
    id           = id,
    pos          = { x = homePos.x, y = homePos.y, z = homePos.z },
    slot         = 1,
    blocksInSlot = 0,
    instructions = 0,
    status       = "idle",
    pendingBlock = nil,
    rechargeUntil= 0,
    assignedAt   = 0,
  }
end

local slaveIdx = 0
local function nextSlaveInfo()
  slaveIdx = (slaveIdx % #slaveCompAddrs) + 1
  return slaveCompAddrs[slaveIdx], slaveModemAddrs[slaveIdx]
end

local function claimBestBlock(st)
  local layerY = currentLayerY()
  if not layerY then return nil end
  local best, bestD = nil, math.huge
  for _, e in ipairs(sharedQueue) do
    if not e.placed and not e.claimedBy and e.y == layerY
    and grid.hasSupport(e.x, e.y, e.z) then
      local d = math.abs(e.x - st.pos.x)
              + math.abs(e.y - st.pos.y)
              + math.abs(e.z - st.pos.z)
      if d < bestD then best, bestD = e, d end
    end
  end
  if best then best.claimedBy = st.id end
  return best
end

local function sendFly(slaveCompAddr, slaveModemAddr, droneId, fromPos, goalPos, ctx, blockPos)
  local bx = blockPos and blockPos.x or -1
  local by = blockPos and blockPos.y or -1
  local bz = blockPos and blockPos.z or -1
  modem.send(slaveModemAddr, PORT, "fly",
    slaveCompAddr,
    droneId,
    fromPos.x .. "," .. fromPos.y .. "," .. fromPos.z,
    goalPos.x .. "," .. goalPos.y .. "," .. goalPos.z,
    ctx,
    bx .. "," .. by .. "," .. bz)
end

local function assignDrone(st)
  if queueDone() then
    logMsg(string.format("  [%d] Queue done – returning home.", st.id))
    local ca, ma = nextSlaveInfo()
    sendFly(ca, ma, st.id, st.pos, homePos, "done", nil)
    st.status     = "assigned"
    st.assignedAt = computer.uptime()
    return
  end

  if st.instructions >= config.RECHARGE_INTERVAL and not rechargeRequested then
    logMsg(string.format("  [%d] Hit recharge threshold – requesting synchronized recharge for the fleet.", st.id))
    rechargeRequested = true
  end

  if rechargeRequested then
    logMsg(string.format("  [%d] RTB for synchronized recharge.", st.id))
    local ca, ma = nextSlaveInfo()
    sendFly(ca, ma, st.id, st.pos, homePos, "recharge", nil)
    st.status     = "assigned"
    st.assignedAt = computer.uptime()
    return
  end

  if st.blocksInSlot >= 64 then
    st.slot = st.slot + 1
    st.blocksInSlot = 0
    logMsg(string.format("  [%d] Slot full → slot %d", st.id, st.slot))
    modem.broadcast(PORT, "select", st.id, st.slot)
    st.instructions = st.instructions + 1
    assignDrone(st)
    return
  end

  local layerY = currentLayerY()
  if layerY and st.pos.y <= layerY then
    logMsg(string.format("  [%d] Lifting to Y=%d before layer %d work",
      st.id, layerY + 1, layerY))
    local ca, ma = nextSlaveInfo()
    sendFly(ca, ma, st.id, st.pos,
      { x = st.pos.x, y = layerY + 1, z = st.pos.z }, "lift", nil)
    st.status     = "assigned"
    st.assignedAt = computer.uptime()
    return
  end

  local entry = claimBestBlock(st)
  if not entry then
    st.status = "waiting"
    return
  end

  st.pendingBlock = entry
  local goal = { x = entry.x, y = math.min(meta.totalY, entry.y + 1), z = entry.z }
  local ca, ma = nextSlaveInfo()
  logMsg(string.format("  [%d] → (%d,%d,%d) via slave %s",
    st.id, entry.x, entry.y, entry.z, ca))
  sendFly(ca, ma, st.id, st.pos, goal, "place", entry)
  st.status     = "assigned"
  st.assignedAt = computer.uptime()
end

for _, id in ipairs(droneIds) do
  modem.broadcast(PORT, "select", id, 1)
end
os.sleep(1.0)

logMsg(string.format("=== Construction: %d drone(s), %d slave(s) ===",
  #droneIds, #slaveCompAddrs))
for _, st in pairs(states) do assignDrone(st) end

local function allDone()
  for _, st in pairs(states) do if st.status ~= "done" then return false end end
  return true
end

while not allDone() do
  local args = {event.pull(0.5, "modem_message")}

  if args[1] then
    local msg = args[6]
    local p   = {}
    for i = 7, #args do p[#p + 1] = args[i] end

    if msg == "flight_done" then
      local droneId, nx, ny, nz, moveCount, ctx =
        p[1], p[2], p[3], p[4], p[5], p[6]
      local st = states[droneId]
      if st then
        st.pos         = { x = nx, y = ny, z = nz }
        st.instructions= st.instructions + (moveCount or 0)
        logMsg(string.format("  [%d] flight_done ctx=%s pos=(%d,%d,%d)",
          droneId, tostring(ctx), nx, ny, nz))

        if ctx == "recharge" then
          st.status = "rtb_wait"
          logMsg(string.format("  [%d] RTB complete – waiting for the rest of the fleet.", droneId))
        elseif ctx == "done" then
          st.status = "done"
          logMsg(string.format("  [%d] Docked. Done.", droneId))
        else
          st.status = "idle"
          assignDrone(st)
        end
      end

    elseif msg == "placed" then
      local droneId   = p[1]
      local bx, by, bz = tostring(p[2]):match("(-?%d+),(-?%d+),(-?%d+)")
      local moveCount = p[3]
      local dx, dy, dz = tostring(p[4]):match("(-?%d+),(-?%d+),(-?%d+)")
      local st = states[droneId]
      if st and st.pendingBlock then
        local entry     = st.pendingBlock
        entry.placed    = true
        entry.claimedBy = nil
        grid.setBlock(entry.x, entry.y, entry.z, true)
        for _, maddr in ipairs(slaveModemAddrs) do
          modem.send(maddr, PORT, "sync_block", entry.x, entry.y, entry.z)
        end
        st.blocksInSlot = st.blocksInSlot + 1
        st.instructions = st.instructions + (moveCount or 0) + 1
        st.pendingBlock = nil
        if dx then
          st.pos = { x = tonumber(dx), y = tonumber(dy), z = tonumber(dz) }
        end
        st.status = "idle"
        logMsg(string.format("  [%d] Placed (%s) now at (%s)",
          droneId, tostring(p[2]), tostring(p[4])))
        assignDrone(st)
      end

    elseif msg == "no_path" then
      local droneId        = p[1]
      local bx, by, bz     = p[2], p[3], p[4]
      local curX, curY, curZ = tonumber(p[5]), tonumber(p[6]), tonumber(p[7])
      local st = states[droneId]
      if st then
        if curX then
          st.pos = { x = curX, y = curY, z = curZ }
        end
        if st.pendingBlock then
          local entry     = st.pendingBlock
          entry.claimedBy = nil
          entry.failCount = entry.failCount + 1
          if entry.failCount >= 10 then
            entry.placed = true
            logMsg(string.format("  [%d] Skipping (%d,%d,%d) after 10 path failures.",
              droneId, bx, by, bz))
          end
          st.pendingBlock = nil
        end
        st.status = "idle"
        assignDrone(st)
      end

    elseif msg == "place_failed" then
      local droneId        = p[1]
      local bx, by, bz     = tonumber(p[2]), tonumber(p[3]), tonumber(p[4])
      local curX, curY, curZ = tonumber(p[5]), tonumber(p[6]), tonumber(p[7])
      local st = states[droneId]
      if st then
        if curX then
          st.pos = { x = curX, y = curY, z = curZ }
        end
        if st.pendingBlock then
          local entry          = st.pendingBlock
          entry.claimedBy      = nil
          entry.placeFailCount = entry.placeFailCount + 1
          if entry.placeFailCount >= 20 then
            entry.placed = true
            logMsg(string.format("  [%d] Giving up (%d,%d,%d) after 20 place failures.",
              droneId, bx, by, bz))
          end
          st.pendingBlock = nil
        end
        st.status = "idle"
        logMsg(string.format("  [%d] Place failed at (%d,%d,%d) – will retry from (%d,%d,%d).",
          droneId, bx or -1, by or -1, bz or -1, st.pos.x, st.pos.y, st.pos.z))
        assignDrone(st)
      end
    end
  end

  if rechargeRequested and not rechargeActive then
    local allParked = true
    for _, st in pairs(states) do
      if st.status ~= "done" and st.status ~= "rtb_wait" then
        allParked = false
        break
      end
    end
    if allParked then
      rechargeActive = true
      local rechargeUntil = computer.uptime() + 5
      for _, st in pairs(states) do
        if st.status == "rtb_wait" then
          st.status        = "recharging"
          st.instructions  = 0
          st.rechargeUntil = rechargeUntil
        end
      end
      logMsg("  [Sync Recharge] Whole fleet home – recharging together (5 s).")
    end
  end

  for _, st in pairs(states) do
    if st.status == "recharging" and computer.uptime() >= st.rechargeUntil then
      logMsg(string.format("  [%d] Recharge done.", st.id))
      st.status = "idle"
    end
    if st.status == "assigned"
    and st.assignedAt > 0
    and computer.uptime() - st.assignedAt > 90 then
      logMsg(string.format("  [%d] Assignment timed out – un-claiming and reassigning.", st.id))
      if st.pendingBlock then
        st.pendingBlock.claimedBy = nil
        st.pendingBlock = nil
      end
      st.status = "idle"
      assignDrone(st)
    end
  end

  if rechargeActive then
    local stillRecharging = false
    for _, st in pairs(states) do
      if st.status == "recharging" then stillRecharging = true; break end
    end
    if not stillRecharging then
      rechargeRequested = false
      rechargeActive    = false
      logMsg("  [Sync Recharge] Fleet recharged – resuming construction.")
    end
  end

  if not giveUp then
    local allWaiting = true
    for _, st in pairs(states) do
      if st.status ~= "waiting" and st.status ~= "done" then
        allWaiting = false; break
      end
    end
    if allWaiting and not queueDone() then
      local rem = 0
      for _, e in ipairs(sharedQueue) do if not e.placed then rem = rem + 1 end end
      logMsg(string.format("[Stall] %d block(s) unreachable. Wrapping up.", rem))
      giveUp = true
      for _, st in pairs(states) do
        if st.status == "waiting" then
          st.status = "idle"
          assignDrone(st)
        end
      end
    end
  end

  for _, st in pairs(states) do
    if st.status == "idle" or st.status == "waiting" then
      assignDrone(st)
    end
  end
end

logMsg("=== Construction complete! ===")
modem.broadcast(123, "place_above")
log.closeLog()

local config = require("config")
local logHandle = io.open(config.LOG_FILE, "w")

local function logMsg(msg)
  local timeStr = os.date("[%H:%M:%S] ")
  if logHandle then
    logHandle:write(timeStr .. msg .. "\n")
    logHandle:flush()
  end
  print(msg)
end

local function closeLog()
  if logHandle then
    logHandle:close()
    logHandle = nil
  end
end

return {
  logMsg   = logMsg,
  closeLog = closeLog,
}

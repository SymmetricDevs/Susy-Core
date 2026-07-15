--made for openos
local args = table.pack(...)

if args.n ~= 1 then
    error("can only parse a single file, got " .. tostring(args.n))
end

local filename = args[1]
local computer = require("computer")

if not filename then
    error("no file provided")
end
local f = io.open(filename, "rb")
if not f then
    error("couldnt read the file")
end
local comp = require("component")
local speaker
for _, name in ipairs({ "speaker_broadcast", "speaker_single" }) do
    if comp.isAvailable(name) then
        speaker = comp[name]
        break
    end
end

if not speaker then
    error("no speaker component")
end

local max_rate = speaker.getMaxRate()
local min_len = speaker.getMinDuration()
local max_len = speaker.getMaxDuration()
print("speaker type: ", speaker.type)
---@cast filename string
if filename:sub(-3) == "wav" then
    local wav_parser = require("wav")
    local cksize, iter = wav_parser(f, 1024 * 21)
    print(cksize, "bytes of data")

    local rate, nAvgBytesPerSec
    for t, d in iter do
        if t == "fmt " then
            if d.wFormatTag ~= 0x01 then
                error("unsupported format, expected PCM data")
            end
            rate = d.nSamplesPerSec
            nAvgBytesPerSec = d.nAvgBytesPerSec
            if rate > max_rate then
                error("audio rate exceeds max rate of " .. tostring(max_rate))
            end
        end
        if t == "data" then
            local buf = ""
            local min_bytes = min_len * nAvgBytesPerSec
            local max_bytes = max_len * nAvgBytesPerSec

            local function fill_buf()
                local i = 0
                while #buf < max_bytes do
                    i = i + 1
                    local chunk = d.iter()
                    if not chunk then
                        return false
                    end
                    buf = buf .. chunk
                end
                -- print("fill_buf got " .. tostring(i) .. " chunks")
                return true
            end

            fill_buf()

            while #buf >= min_bytes do
                local seg = buf:sub(1, math.min(#buf, max_bytes))
                buf = buf:sub(#seg + 1)

                local t0 = computer.uptime()
                local duration_ms, err = speaker.playSoundAsync(rate, seg)
                if not duration_ms then
                    print("broke:", err)
                    error()
                end

                local keep_going = fill_buf()
                local elapsed = computer.uptime() - t0 - 0.01

                local wait = math.max(0, (duration_ms / 1000) - elapsed)
                print(
                    string.format(
                        "elapsed %.2f ttns %s sleeping %.2f",
                        elapsed,
                        duration_ms and string.format("%.2f", duration_ms) or "nil",
                        wait
                    )
                )
                if wait > 0 and keep_going then
                    os.sleep(wait)
                end
            end
        end
    end
else
    error("unsupported")
end

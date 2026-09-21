---@diagnostic disable: redefined-local, lowercase-global, deprecated
---parsing based on https://www.mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
---@param f file*
---@param segment_length? integer
function parse_wav(f, segment_length)
    segment_length = segment_length or (1024 * 8)
    f:seek("set", 0)
    local h = f:read(12)
    if not h then
        return nil, "file too small to be a wav file"
    end
    local magic, cksize, waveid = string.unpack("c4I4c4", h)
    if magic ~= "RIFF" then
        return nil, "not a wav file"
    end
    if waveid ~= "WAVE" then
        return nil, "not a wav file"
    end
    cksize = cksize - 4

    local riffparse = function(data, _)
        --didnt bother
        return data
    end

    local chunkformatlookup = {
        ["LIST"] = riffparse,
        ["RIFF"] = riffparse,
        ["fact"] = function(data, _)
            return string.unpack("L", data)
        end,
        ["data"] = function(_, cksize)
            local remaining = cksize
            local need_pad = cksize % 2 == 1
            local data_iter = function()
                if remaining <= 0 then
                    return nil
                end
                local n = math.min(segment_length, remaining)
                local chunk = f:read(n)
                if not chunk then
                    return nil
                end
                remaining = remaining - n
                if #chunk < n then
                    error("truncated data")
                end
                if remaining <= 0 and need_pad then
                    local pad = f:read(1) -- skip pad byte
                    if pad ~= "\0" then
                        error("tried to skip padding, got something else")
                    end
                end
                return chunk
            end
            return cksize, data_iter
        end,
        ["fmt "] = function(data, _)
            if #data == 16 then
                local wFormatTag, nChannels, nSamplesPerSec, nAvgBytesPerSec, nBlockAlign, wBitsPerSample =
                    string.unpack("HHI4I4HH", data)
                return {
                    ["wFormatTag"] = wFormatTag,
                    ["nChannels"] = nChannels,
                    ["nSamplesPerSec"] = nSamplesPerSec,
                    ["nAvgBytesPerSec"] = nAvgBytesPerSec,
                    ["nBlockAlign"] = nBlockAlign,
                    ["wBitsPerSample"] = wBitsPerSample,
                }
            elseif #data == 18 or #data == 40 then
                local wFormatTag, nChannels, nSamplesPerSec, nAvgBytesPerSec, nBlockAlign, wBitsPerSample, cbSize, next1 =
                    string.unpack("HHI4I4HHH", data)
                local t = {
                    ["wFormatTag"] = wFormatTag,
                    ["nChannels"] = nChannels,
                    ["nSamplesPerSec"] = nSamplesPerSec,
                    ["nAvgBytesPerSec"] = nAvgBytesPerSec,
                    ["nBlockAlign"] = nBlockAlign,
                    ["wBitsPerSample"] = wBitsPerSample,
                    ["cbSize"] = cbSize,
                }

                if cbSize == 22 then
                    local wValidBitsPerSample, dwChannelMask, SubFormat = string.unpack("HIc16", data, next1)
                    t["wValidBitsPerSample"] = wValidBitsPerSample
                    t["dwChannelMask"] = dwChannelMask
                    t["SubFormat"] = SubFormat
                end
                return t
            else
                error("unexpected cksize")
            end
        end,
    }
    setmetatable(chunkformatlookup, {
        ---@diagnostic disable-next-line: unused-local
        __index = function(_, k)
            return function(data, _)
                return data
            end
        end,
    })
    local data_end = nil
    local iter = function()
        if data_end then
            f:seek("set", data_end)
            data_end = nil
        end
        local header = f:read(8)
        if not header then
            return nil
        end
        local ckId, cksize = string.unpack("c4I4", header)

        if ckId == "data" then
            local data_offset = f:seek("cur", 0)
            local data_size, raw_iter = chunkformatlookup["data"](nil, cksize)
            local started
            local data_iter = function()
                if not started then
                    f:seek("set", data_offset)
                    started = true
                end
                return raw_iter()
            end
            data_end = data_offset + cksize + (cksize % 2)
            return ckId, { size = data_size, iter = data_iter }
        else
            local chunkdata = f:read(cksize)
            if not chunkdata then
                error("corrupted/truncated file")
            end
            if cksize % 2 == 1 then
                local pad = f:read(1)
                if pad ~= "\0" then
                    error("tried to skip padding, got something else")
                end
            end
            return ckId, chunkformatlookup[ckId](chunkdata, cksize)
        end
    end
    return cksize, iter
end

return parse_wav

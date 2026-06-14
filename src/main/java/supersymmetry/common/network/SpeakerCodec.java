package supersymmetry.common.network;

import java.net.URL;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFormat;

import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import supersymmetry.api.SusyLog;

// abandon all hope thou who enter here, this file is almost completely clanker generated because i
// have no idea how a codec for this horrible library is meant to look like
// dont touch it as long as it keeps working
public class SpeakerCodec implements ICodec {

    private static final Map<String, Data> DATA = new ConcurrentHashMap<>();

    private Data data;
    private boolean initialized;

    static class Data {

        final Queue<byte[]> buffers = new ConcurrentLinkedQueue<>();
        AudioFormat format;
    }

    public static void register() {
        try {
            SoundSystemConfig.setCodec("speaker", SpeakerCodec.class);
        } catch (SoundSystemException e) {
            SusyLog.logger.error("codec registration blew up with the following error: {}", e);
        }
    }

    public static Data prepare(String id, AudioFormat format) {
        var data = new Data();
        data.format = format;
        DATA.put(id, data);
        return data;
    }

    public static Data get(String id) {
        return DATA.get(id);
    }

    public static void remove(String id) {
        DATA.remove(id);
    }

    @Override
    public void reverseByteOrder(boolean b) {}

    @Override
    public boolean initialize(URL url) {
        data = DATA.get(extractId(url));
        initialized = true;
        return data != null;
    }

    @Override
    public boolean initialized() {
        return initialized;
    }

    @Override
    public SoundBuffer read() {
        if (data == null) return null;
        var buf = data.buffers.poll();
        if (buf == null) return null;
        return new SoundBuffer(buf, data.format);
    }

    @Override
    public SoundBuffer readAll() {
        return read();
    }

    @Override
    public boolean endOfStream() {
        return data != null && data.buffers.isEmpty();
    }

    @Override
    public AudioFormat getAudioFormat() {
        return data != null ? data.format : null;
    }

    @Override
    public void cleanup() {
        initialized = false;
        data = null;
    }

    private static String extractId(URL url) {
        if (url == null || url.getPath() == null) return "";
        var file = url.getPath().replaceFirst("^.*/", "");
        return file.replaceFirst("\\.speaker$", "").replaceFirst("^speaker_", "");
    }
}

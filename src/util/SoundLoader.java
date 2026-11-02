package util;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;

public final class SoundLoader {

    public record Data(AudioFormat format, byte[] pcm, String source) {
        public double seconds() {
            return pcm.length / (double) (format.getFrameSize() * format.getFrameRate());
        }
    }

    private static final String[] EXTENSIONS = {"wav", "aiff", "aif", "au"};
    private static final long MAX_BYTES = 8L * 1024 * 1024;

    private SoundLoader() {}

    public static List<Path> searchDirs() {
        List<Path> dirs = new ArrayList<>();

        try {
            Path loc =
                    Paths.get(
                            SoundLoader.class
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI());

            if (Files.isDirectory(loc)) {
                dirs.add(loc.resolve("sounds"));
                if (loc.getParent() != null) dirs.add(loc.getParent().resolve("sounds"));
            } else if (loc.getParent() != null) {
                dirs.add(loc.getParent().resolve("sounds"));
            }
        } catch (URISyntaxException | RuntimeException e) {

        }

        dirs.add(Paths.get(System.getProperty("user.dir")).resolve("sounds"));
        List<Path> unique = new ArrayList<>();

        for (Path d : dirs) {
            Path n = d.toAbsolutePath().normalize();
            if (!unique.contains(n)) unique.add(n);
        }

        return unique;
    }

    public static Data load(String baseName) {
        for (Path dir : searchDirs()) {
            for (String ext : EXTENSIONS) {
                Path p = dir.resolve(baseName + "." + ext);
                if (!Files.isRegularFile(p)) continue;

                try {
                    if (Files.size(p) > MAX_BYTES)
                        throw new IOException("file is larger than 8 MB");

                    try (InputStream in = new BufferedInputStream(Files.newInputStream(p))) {
                        return decode(in, p.toString());
                    }
                } catch (Exception e) {
                    System.err.println(
                            "[Sfx] Cannot load "
                                    + p
                                    + " ("
                                    + e.getMessage()
                                    + "). Using the built-in sound instead.");
                }
            }
        }

        for (String ext : EXTENSIONS) {
            try (InputStream raw =
                    SoundLoader.class.getResourceAsStream("/sounds/" + baseName + "." + ext)) {
                if (raw != null)
                    return decode(
                            new BufferedInputStream(raw), "jar:/sounds/" + baseName + "." + ext);
            } catch (Exception e) {
                System.err.println(
                        "[Sfx] Cannot load /sounds/"
                                + baseName
                                + "."
                                + ext
                                + " from the jar ("
                                + e.getMessage()
                                + ").");
            }
        }

        return null;
    }

    public static Data decode(InputStream in, String source)
            throws IOException, UnsupportedAudioFileException {
        AudioInputStream ais = AudioSystem.getAudioInputStream(in);
        AudioFormat f = ais.getFormat();
        if (f.getChannels() > 2)
            throw new UnsupportedAudioFileException("only mono/stereo is supported");

        if (f.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
                || f.getSampleSizeInBits() != 16
                || f.isBigEndian()) {
            AudioFormat target =
                    new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            f.getSampleRate(),
                            16,
                            f.getChannels(),
                            f.getChannels() * 2,
                            f.getSampleRate(),
                            false);
            if (!AudioSystem.isConversionSupported(target, f))
                throw new UnsupportedAudioFileException("unsupported audio format: " + f);
            ais = AudioSystem.getAudioInputStream(target, ais);
        }

        byte[] pcm = ais.readAllBytes();
        if (pcm.length == 0) throw new IOException("no audio data");
        return new Data(ais.getFormat(), pcm, source);
    }

    public static Data pitchShift(Data d, double semitones) {
        if (semitones == 0) return d;
        double factor = Math.pow(2, semitones / 12);
        int ch = d.format().getChannels(), frameSize = ch * 2;
        int inFrames = d.pcm().length / frameSize;
        int outFrames = Math.max(1, (int) (inFrames / factor));
        byte[] out = new byte[outFrames * frameSize];

        for (int f = 0; f < outFrames; f++) {
            double pos = f * factor;
            int i0 = Math.min((int) pos, inFrames - 1), i1 = Math.min(i0 + 1, inFrames - 1);
            double frac = pos - (int) pos;

            for (int c = 0; c < ch; c++) {
                int s0 = sample(d.pcm(), i0 * ch + c), s1 = sample(d.pcm(), i1 * ch + c);
                int v = (int) Math.round(s0 + (s1 - s0) * frac);
                out[(f * ch + c) * 2] = (byte) (v & 0xff);
                out[(f * ch + c) * 2 + 1] = (byte) ((v >> 8) & 0xff);
            }
        }

        return new Data(
                d.format(), out, d.source() + String.format(" (%+.0f semitones)", semitones));
    }

    private static int sample(byte[] b, int index) {
        return (short) ((b[index * 2] & 0xff) | (b[index * 2 + 1] << 8));
    }
}

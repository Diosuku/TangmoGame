package util;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.sound.sampled.*;

import model.entity.FruitType;

public final class Sfx {
    public enum Sound {
        DROP("drop"),
        SHOOT("shoot"),
        WIN("win"),
        LOSE("lose"),
        HURT("hurt"),
        CLICK("click"),
        BOSS_SHOOT("boss_shoot");
        private final String file;

        Sound(String file) {
            this.file = file;
        }

        public String getFileName() {
            return file;
        }
    }

    private static final int[] MERGE_SEMITONES = {0, 0, 2, 4, 7, 9};

    private static final float RATE = 44100f;
    private static final AudioFormat FORMAT = new AudioFormat(RATE, 16, 1, true, false);
    private static final int POOL = 4;

    private static final Map<String, Pool> pools = new LinkedHashMap<>();
    private static boolean enabled = true;

    public static java.util.function.Consumer<String> tap;
    private static boolean failed;

    private Sfx() {}

    private static final class Pool {
        private final Clip[] clips = new Clip[POOL];
        private final String source;
        private int next;

        Pool(SoundLoader.Data d) throws LineUnavailableException {
            this.source = d.source();

            for (int i = 0; i < POOL; i++) {
                clips[i] = AudioSystem.getClip();
                clips[i].open(d.format(), d.pcm(), 0, d.pcm().length);
            }
        }

        void play() {
            Clip c = clips[next];
            next = (next + 1) % POOL;
            c.stop();
            c.setFramePosition(0);
            c.start();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void play(Sound s) {
        if (!enabled) return;
        if (tap != null) tap.accept(s.name());
        if (failed) return;

        try {
            pool(s.getFileName(), () -> dataFor(s)).play();
        } catch (Exception | Error e) {
            failed = true;
        }
    }

    public static void playMerge(FruitType made) {
        if (!enabled) return;
        if (tap != null) tap.accept("MERGE_" + made.name());
        if (failed) return;

        try {
            pool("merge_" + made.name().toLowerCase(), () -> mergeDataFor(made)).play();
        } catch (Exception | Error e) {
            failed = true;
        }
    }

    public static void preload() {
        for (Sound s : Sound.values()) {
            play0(s);
        }

        for (FruitType t : FruitType.values()) if (t.ordinal() > 0) play0(t);
    }

    private static void play0(Object what) {
        if (failed) return;

        try {
            if (what instanceof Sound s) pool(s.getFileName(), () -> dataFor(s));
            else if (what instanceof FruitType t)
                pool("merge_" + t.name().toLowerCase(), () -> mergeDataFor(t));
        } catch (Exception | Error e) {
            failed = true;
        }
    }

    private interface DataSupplier {
        SoundLoader.Data get();
    }

    private static synchronized Pool pool(String key, DataSupplier supplier)
            throws LineUnavailableException {
        Pool p = pools.get(key);

        if (p == null) {
            p = new Pool(supplier.get());
            pools.put(key, p);
        }

        return p;
    }

    private static SoundLoader.Data dataFor(Sound s) {
        SoundLoader.Data d = SoundLoader.load(s.getFileName());
        return d != null ? d : builtIn(synth(s), "built-in " + s.getFileName());
    }

    private static SoundLoader.Data mergeDataFor(FruitType made) {
        SoundLoader.Data d = SoundLoader.load("merge_" + made.name().toLowerCase());
        if (d != null) return d;
        d = SoundLoader.load("merge");
        if (d != null)
            return SoundLoader.pitchShift(
                    d, MERGE_SEMITONES[Math.min(made.ordinal(), MERGE_SEMITONES.length - 1)]);
        return builtIn(synthMerge(made.ordinal()), "built-in merge_" + made.name().toLowerCase());
    }

    private static SoundLoader.Data builtIn(short[] samples, String name) {
        byte[] data = new byte[samples.length * 2];

        for (int i = 0; i < samples.length; i++) {
            data[2 * i] = (byte) (samples[i] & 0xff);
            data[2 * i + 1] = (byte) ((samples[i] >> 8) & 0xff);
        }

        return new SoundLoader.Data(FORMAT, data, name);
    }

    public static synchronized Map<String, String> describeSources() {
        Map<String, String> m = new LinkedHashMap<>();
        for (Sound s : Sound.values()) m.put(s.getFileName(), dataFor(s).source());
        for (FruitType t : FruitType.values())
            if (t.ordinal() > 0) m.put("merge_" + t.name().toLowerCase(), mergeDataFor(t).source());
        return m;
    }

    public static short[] samplesFor(Sound s) {
        return synth(s);
    }

    public static short[] samplesForMerge(int fruitIndex) {
        return synthMerge(fruitIndex);
    }

    private static short[] synth(Sound s) {
        return fadeOut(
                switch (s) {
                    case DROP -> drop();
                    case SHOOT -> shoot();
                    case WIN -> win();
                    case LOSE -> lose();
                    case HURT -> hurt();
                    case CLICK -> click();
                    case BOSS_SHOOT -> bossShoot();
                });
    }

    private static short[] fadeOut(short[] b) {
        int n = Math.min(b.length, (int) (RATE * 0.01));
        for (int i = 0; i < n; i++)
            b[b.length - 1 - i] = (short) (b[b.length - 1 - i] * (i / (double) n));
        return b;
    }

    private static short[] buf(double seconds) {
        return new short[(int) (seconds * RATE)];
    }

    private static short pcm(double v) {
        return (short) Math.max(-32767, Math.min(32767, v * 32767));
    }

    private static short[] drop() {
        short[] b = buf(0.16);
        double phase = 0;

        for (int i = 0; i < b.length; i++) {
            double t = i / RATE, f = 330 - 1100 * t;
            phase += 2 * Math.PI * Math.max(120, f) / RATE;
            double env = Math.exp(-t * 22) * Math.min(1, t * 400);
            double click = i < 150 ? (Math.sin(i * 7.3) * 0.4) * (1 - i / 150.0) : 0;
            b[i] = pcm((Math.sin(phase) * 0.7 + click) * env * 0.8);
        }

        return b;
    }

    private static short[] synthMerge(int idx) {
        return fadeOut(mergeRaw(idx));
    }

    private static short[] mergeRaw(int idx) {
        double[] notes = {523.25, 587.33, 659.25, 783.99, 880.0, 1046.5};
        double f0 = notes[Math.max(0, Math.min(notes.length - 1, idx))];
        short[] b = buf(0.28);
        double phase = 0;

        for (int i = 0; i < b.length; i++) {
            double t = i / RATE, f = f0 * (1 + 0.25 * Math.min(1, t / 0.07));
            phase += 2 * Math.PI * f / RATE;
            double env = Math.exp(-t * 11) * Math.min(1, t * 600);
            double v =
                    Math.sin(phase) * 0.75 + Math.sin(phase * 2) * 0.2 + Math.sin(phase * 3) * 0.05;
            b[i] = pcm(v * env * 0.7);
        }

        return b;
    }

    private static short[] shoot() {
        short[] b = buf(0.09);
        double phase = 0;

        for (int i = 0; i < b.length; i++) {
            double t = i / RATE, f = 1150 - 8500 * t;
            phase += 2 * Math.PI * Math.max(380, f) / RATE;
            double env = Math.exp(-t * 28) * Math.min(1, t * 800);
            double tri = 2 / Math.PI * Math.asin(Math.sin(phase));
            b[i] = pcm(tri * env * 0.32);
        }

        return b;
    }

    private static short[] win() {
        double[] notes = {523.25, 659.25, 783.99, 1046.5};
        short[] b = buf(1.15);
        for (int n = 0; n < notes.length; n++) addTone(b, notes[n], n * 0.115, 0.55, 0.36, 5);
        for (double f : new double[] {523.25, 659.25, 783.99, 1046.5})
            addTone(b, f, 0.5, 0.65, 0.2, 3.5);
        return b;
    }

    private static short[] hurt() {
        short[] b = buf(0.24);
        double phase = 0, noiseState = 0x1234;

        for (int i = 0; i < b.length; i++) {
            double t = i / RATE, f = 420 - 1100 * t;
            phase += 2 * Math.PI * Math.max(140, f) / RATE;
            double saw = 2 * ((phase / (2 * Math.PI)) % 1.0) - 1;
            noiseState = (noiseState * 1103515245 + 12345) % 2147483648.0;
            double noise = noiseState / 2147483648.0 * 2 - 1;
            double env = Math.exp(-t * 13) * Math.min(1, t * 700);
            b[i] = pcm((saw * 0.55 + noise * 0.35 * Math.exp(-t * 40)) * env * 0.7);
        }

        return b;
    }

    private static short[] click() {
        short[] b = buf(0.06);
        double phase = 0;

        for (int i = 0; i < b.length; i++) {
            double t = i / RATE;
            phase += 2 * Math.PI * (1500 - 7000 * t) / RATE;
            double env = Math.exp(-t * 70) * Math.min(1, t * 1500);
            b[i] = pcm(Math.sin(phase) * env * 0.5);
        }

        return b;
    }

    private static short[] bossShoot() {
        short[] b = buf(0.22);
        double phase = 0;

        for (int i = 0; i < b.length; i++) {
            double t = i / RATE, f = 130 + 900 * t - 3300 * t * t;
            phase += 2 * Math.PI * Math.max(90, f) / RATE;
            double env = Math.sin(Math.PI * Math.min(1, t / 0.22)) * Math.exp(-t * 6);
            double v =
                    Math.sin(phase) * 0.7
                            + Math.sin(phase * 2) * 0.25
                            + Math.sin(phase * 3 + 1) * 0.1;
            b[i] = pcm(v * env * 0.75);
        }

        return b;
    }

    private static short[] lose() {
        double[] notes = {392.0, 349.23, 311.13, 261.63};
        short[] b = buf(0.95);
        for (int n = 0; n < notes.length; n++) addTone(b, notes[n], n * 0.17, 0.4, 0.4, 5.5);
        return b;
    }

    private static void addTone(
            short[] b, double freq, double startSec, double lenSec, double amp, double decay) {
        int s0 = (int) (startSec * RATE), n = (int) (lenSec * RATE);

        for (int i = 0; i < n && s0 + i < b.length; i++) {
            double t = i / RATE;
            double env = Math.exp(-t * decay) * Math.min(1, t * 500);
            double v =
                    Math.sin(2 * Math.PI * freq * t) * 0.8 + Math.sin(4 * Math.PI * freq * t) * 0.2;
            double mixed = b[s0 + i] / 32767.0 + v * env * amp;
            b[s0 + i] = pcm(mixed);
        }
    }
}

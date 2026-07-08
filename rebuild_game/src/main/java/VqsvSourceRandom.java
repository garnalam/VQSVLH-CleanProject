import java.util.List;
import java.util.Random;

final class VqsvSourceRandom {
    private Random random;
    private boolean initialized;
    private Long injectedSeed;

    private VqsvSourceRandom() {
    }

    static VqsvSourceRandom lazySourceSeeded() {
        return new VqsvSourceRandom();
    }

    static VqsvSourceRandom seeded(long seed) {
        VqsvSourceRandom rng = new VqsvSourceRandom();
        rng.setSeed(seed);
        return rng;
    }

    void setSeed(long seed) {
        injectedSeed = seed;
        random = new Random(seed);
        initialized = true;
    }

    void clearInjectedSeed() {
        injectedSeed = null;
        random = null;
        initialized = false;
    }

    int a(String label, int n, List<String> trace) {
        int raw = nextRaw(label, trace);
        int value = (raw >>> 1) % n;
        log(trace, label, "ae.a(int)", "bound=" + n, raw, value);
        return value;
    }

    int a(String label, List<String> trace) {
        int raw = nextRaw(label, trace);
        int value = -2 + (raw >>> 1) % 4;
        log(trace, label, "ae.a()", "range=-2..1", raw, value);
        return value;
    }

    int b(String label, int min, int max, List<String> trace) {
        int raw = nextRaw(label, trace);
        int value = (raw >>> 1) % (max - min + 1) + min;
        log(trace, label, "ae.b(int,int)", "range=" + min + ".." + max, raw, value);
        return value;
    }

    private int nextRaw(String label, List<String> trace) {
        ensureInitialized(label, trace);
        return random.nextInt();
    }

    private void ensureInitialized(String label, List<String> trace) {
        if (initialized) {
            return;
        }
        long seed = System.currentTimeMillis();
        random = new Random(seed);
        initialized = true;
        logSeed(trace, label, seed, false);
    }

    private void logSeed(List<String> trace, String label, long seed, boolean injected) {
        if (trace != null) {
            trace.add("RNG TRACE " + label + " seed="
                    + seed + " source=" + (injected ? "injected" : "System.currentTimeMillis"));
        }
    }

    private void log(List<String> trace, String label, String helper, String range, int raw, int value) {
        if (trace != null) {
            trace.add("RNG TRACE " + label
                    + " helper=" + helper
                    + " " + range
                    + " raw=" + raw
                    + " return=" + value
                    + " seed=" + (injectedSeed == null ? "runtime" : injectedSeed));
        }
    }
}

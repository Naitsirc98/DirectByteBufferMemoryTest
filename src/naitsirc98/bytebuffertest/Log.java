package naitsirc98.bytebuffertest;

public final class Log {

    private static volatile boolean verbose;

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean verbose) {
        Log.verbose = verbose;
    }

    public static void logVerbose(String message) {
        if(verbose) {
            log(message);
        }
    }

    public static void log(String message) {
        System.out.println(">> ".concat(message));
    }

    public static void info(String message) {
        System.out.println(message);
    }

    private Log() {}
}

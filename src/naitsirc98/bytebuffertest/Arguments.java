package naitsirc98.bytebuffertest;

public class Arguments {

    private int iterations = 10;
    private boolean exception = false;
    private int bufferSize = 1024;
    private int numThreads = 1;
    private boolean free = false;
    private boolean gc = false;
    private boolean crash = false;
    private boolean checkMemAfterGc = false;
    private boolean verbose = false;

    public Arguments(String[] args) {
        parseArguments(args);
    }

    public int iterations() {
        return iterations;
    }

    public boolean exception() {
        return exception;
    }

    public int bufferSize() {
        return bufferSize;
    }

    public int numBuffers() {
        return numThreads;
    }

    public int numThreads() {
        return numThreads;
    }

    public boolean free() {
        return free;
    }

    public boolean gc() {
        return gc;
    }

    public boolean crash() {
        return crash;
    }

    public boolean checkMemAfterGc() {
        return checkMemAfterGc;
    }

    public boolean verbose() {
        return verbose;
    }

    private void parseArguments(String[] args) {
        for (String arg : args) {
            String argument = arg.trim().toLowerCase();
            parse(argument);
        }
    }

    private void parse(String argument) {
        if (argument.startsWith("-iterations")) {
            iterations = parseInt(argument, 10);
        } else if (argument.equals("-exc")) {
            exception = true;
        } else if (argument.startsWith("-size")) {
            bufferSize = parseInt(argument, 1024);
            if (bufferSize > 1024) {
                System.out.println(">> Max size of buffer is 1024, otherwise it could overflow because it is an int." +
                        "If you want to allocate more memory, specify more buffers with -count.");
                bufferSize = 1024;
            }
        } else if (argument.equals("-free")) {
            if (checkMemAfterGc) {
                throw new RuntimeException("free with checkMemAfterGc makes no sense.");
            }
            free = true;
        } else if (argument.equals("-gc")) {
            gc = true;
        } else if (argument.equals("-crash")) {
            if (checkMemAfterGc) {
                throw new RuntimeException("crash with checkMemAfterGc makes no sense.");
            }
            free = true;
            crash = true;
        } else if (argument.equals("-checkmemaftergc")) {
            if (crash || free) {
                throw new RuntimeException("Cannot specify checkMemAfterGc with crash or free");
            }
            checkMemAfterGc = true;
        } else if (argument.startsWith("-threads")) {
            numThreads = parseInt(argument, 1);
        } else if(argument.equalsIgnoreCase("-verbose")) {
            verbose = true;
        }
    }

    private int parseInt(String argument, int defaultValue) {
        try {
            final int value = Integer.parseInt(argument.substring(argument.indexOf('=') + 1).trim());
            if (value <= 0) {
                System.out.println(">> Value of " + argument + " cannot be <= 0. Using default value: " + defaultValue);
                return defaultValue;
            }
            return value;
        } catch (NumberFormatException e) {
            System.err.println(">> Invalid int value in: " + argument + ". Using default:" + defaultValue);
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        return "Arguments{" +
                "iterations=" + iterations +
                ", exception=" + exception +
                ", bufferSize=" + bufferSize + "MB" +
                ", numBuffers=" + numBuffers() +
                ", numThreads=" + numThreads +
                ", free=" + free +
                ", gc=" + gc +
                ", crash=" + crash +
                ", checkMemAfterGc=" + checkMemAfterGc +
                ", verbose=" + verbose +
                '}';
    }
}

package bbt;

import java.nio.ByteBuffer;
import java.util.Random;

import static bbt.MemoryUtils.*;

public class TestDirectByteBuffer {

    public static void main(String[] args) {
        if(args.length > 0 && args[0].equalsIgnoreCase("-help")) {
            help();
            return;
        }
        Args arguments = new Args(args);
        final long bufferAddress = runTest(arguments);
        if(arguments.checkMemAfterGc) {
            System.out.println(">> Checking if native memory was freed by Java...");
            System.out.println(">> Invoking System.gc() and waiting 5 seconds to let Java garbage collect the ByteBuffer reference...");
            System.gc();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println();
            System.out.println(">> [IMPORTANT] Checking access to native memory via the buffer native pointer to memory." +
                    " If native memory is deleted, then this will cause a crash (EXCEPTION_ACCESS_VIOLATION) or an exception.");
            System.out.println();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Byte value = null;
            try {
                value = MemoryUtils.getByte(bufferAddress, 0);
            } catch (Throwable e) {
                System.out.println(">> Access to native memory cause an exception, as expected: " + e + ", \n" + e.getMessage());
            }
            if(value != null) {
                System.out.println(">> Access to native pointer " + bufferAddress + " did not throw an exception. The native memory" +
                        "is still being used by this process...");
            }
        }
        System.out.println(">> Exiting " + TestDirectByteBuffer.class.getSimpleName() + "...");
    }

    private static long runTest(Args args) {

        System.out.println(">> Executing ByteBuffer test " + args);

        System.out.println(">> Allocating native buffer of " + args.bufferSize + " MB...");
        final long bufferSize = (long)args.bufferSize * 1024L * 1024L;
        ByteBuffer buffer = allocBuffer(bufferSize);
        final long address = addressOf(buffer);
        System.out.println(">> Buffer allocated successfully: " + buffer + ", address = " + address);

        System.out.println(">> Starting loop to write and read from native memory...");

        Random random = new Random(System.nanoTime());

        Runtime r = Runtime.getRuntime();

        final long start = System.currentTimeMillis();

        for(int j = 0;j < args.iterations;j++) {
            System.out.println(">> Starting iteration " + j + "...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i = 0;i < 100000;i++) {
                final int pos1 = random.nextInt(buffer.capacity());
                final int pos2 = random.nextInt(buffer.capacity());
                buffer.put(pos1, random.nextBoolean() ? (byte) i : buffer.get(pos2));
                if(args.exception && j >= 2 && random.nextBoolean()) {
                    throw new UserRequestedException("User requested exception: buffer = " + buffer);
                }
            }
            buffer.clear();
            if(args.exception) {
                throw new UserRequestedException("User requested exception: buffer = " + buffer);
            }
        }

        System.out.println(">> Loop terminated in " + (System.currentTimeMillis() - start) / 1000.0 + " seconds.");

        if(args.free) {
            System.out.println(">> Deleting buffer explicitly...");
            free(buffer);
            System.out.println(">> Buffer cleaner invoked " + buffer);
            System.out.println(">> Native memory is supposed to be freed at this point.");
            if(args.crash) {
                System.out.println(">> Provoking an illegal (to deleted) memory access...");
                System.out.println(">> Please notice this could cause either an exception or a program crash");
                Byte b = null;
                try {
                    b = buffer.get(0);
                } catch (Exception e) {
                    System.out.println(">> Accessing deleted memory throws exception, as expected: " + e + "\n" + e.getMessage());
                }
                if(b != null) {
                    throw new IllegalStateException(">> Access to deleted memory did not throw an exception!!");
                }
            }
         }

        if(args.gc) {
            System.out.println(">> Invoking System.gc()...");
            double totalMem = r.totalMemory() / 1024.0 / 1024.0;
            double freeMem = r.freeMemory() / 1024.0 / 1024.0;
            double usedMed = totalMem - freeMem;
            System.out.println(">> Memory used before gc: " + usedMed + " MB");
            System.gc();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted while sleeping after gc", e);
            }
            totalMem = r.totalMemory() / 1024.0 / 1024.0;
            freeMem = r.freeMemory() / 1024.0 / 1024.0;
            usedMed = totalMem - freeMem;
            System.out.println(">> Memory used after gc: " + usedMed + " MB");
        }

        System.out.println(">> Test terminated");

        buffer = null;
        return address;
    }

    private static void help() {
        System.out.println("==> Test Direct Byte Buffers <==");
        System.out.println("Params:");
        System.out.println("  -iterations=<number of iterations>: specify number of iterations (seconds) of the test. Default is 10.");
        System.out.println("  -exc: if you want to throw an exception in the middle of execution.");
        System.out.println("  -size=<bytes>: to specify size in MB. Default is 1024 MB (1GB).");
        System.out.println("  -free: if you want to explicitly invoke cleaner of DirectByteBuffer.");
        System.out.println("  -gc: if you want to call System.gc on exit.");
        System.out.println("  -crash: if you want to crash the execution with an illegal memory access after deleting the buffer.");
        System.out.println("  -checkMemAfterGc: if you want to check if native memory is still available after " +
                "ByteBuffer reference is garbage collected.");
        System.out.println();
    }

    private static class Args {

        private int iterations = 10;
        private boolean exception;
        private int bufferSize = 1024;
        private boolean free;
        private boolean gc;
        private boolean crash;
        private boolean checkMemAfterGc;

        public Args(String[] args) {
            for(String arg : args) {
                String argument = arg.trim().toLowerCase();
                if(argument.startsWith("-iterations")) {
                    iterations = parseInt(argument, iterations);
                } else if(argument.equals("-exc")) {
                    exception = true;
                } else if(argument.contains("size")) {
                    bufferSize = parseInt(argument, bufferSize);
                } else if(argument.equals("-free")) {
                    if(checkMemAfterGc) {
                        throw new RuntimeException("free with checkMemAfterGc makes no sense.");
                    }
                    free = true;
                } else if(argument.equals("-gc")) {
                    gc = true;
                } else if(argument.equals("-crash")) {
                    if(checkMemAfterGc) {
                        throw new RuntimeException("crash with checkMemAfterGc makes no sense.");
                    }
                    free = true;
                    crash = true;
                } else if(argument.equals("-checkmemaftergc")) {
                    if(crash || free) {
                        throw new RuntimeException("Cannot specify checkMemAfterGc with crash or free");
                    }
                    checkMemAfterGc = true;
                }
            }
        }

        private int parseInt(String argument, int defaultValue) {
            try {
                return Integer.parseInt(argument.substring(argument.indexOf('=')+1).trim());
            } catch(NumberFormatException e) {
                System.err.println(">> Invalid int value in: " + argument + ". Using default:" + defaultValue);
            }
            return defaultValue;
        }

        @Override
        public String toString() {
            return "Args{" +
                    "iterations=" + iterations +
                    ", exception=" + exception +
                    ", bufferSize=" + bufferSize + " MB" +
                    ", free=" + free +
                    ", gc=" + gc +
                    ", crash=" + crash +
                    ", checkMemAfterGc=" + checkMemAfterGc +
                    '}';
        }
    }

    private static class UserRequestedException extends RuntimeException {
        public UserRequestedException(String message) {
            super(message);
        }

        public UserRequestedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}

package naitsirc98.bytebuffertest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        if(args.length > 0 && args[0].equalsIgnoreCase("-help")) {
            help();
            return;
        }
        Arguments arguments = new Arguments(args);
        Log.setVerbose(arguments.verbose());
        BufferGroup bufferGroup = new BufferGroup(arguments);
        runTests(arguments, bufferGroup);
        onTestsFinished(arguments, bufferGroup);
    }

    private static void runTests(Arguments arguments, BufferGroup bufferGroup) {
        if(arguments.numThreads() == 1) {
            new DirectByteBufferTest(bufferGroup.get(0), arguments).run();
        } else {
            Log.log("Preparing thread pool of " + arguments.numThreads() + "...");
            ExecutorService threadPool = Executors.newFixedThreadPool(arguments.numThreads());
            for(int i = 0;i < arguments.numThreads();i++) {
                threadPool.submit(new DirectByteBufferTest(bufferGroup.get(i), arguments));
            }
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void onTestsFinished(Arguments arguments, BufferGroup bufferGroup) {
        if(arguments.checkMemAfterGc()) {
            Log.log("Checking if native memory was freed by Java...");
            Log.log("Invoking System.gc() and waiting 5 seconds to let Java garbage collect the ByteBuffer reference...");
            try {
                Thread.sleep(2000);
                System.gc();
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.log("");
            Log.log("[IMPORTANT] Checking access to native memory via the buffer native pointer to memory." +
                    " If native memory is deleted, then this will cause a crash (EXCEPTION_ACCESS_VIOLATION) or an exception.");
            Log.log("");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Byte value = null;
            try {
                value = MemoryUtils.getByte(bufferGroup.getAddress(0), 0);
            } catch (Throwable e) {
                Log.log("Access to native memory cause an exception, as expected: " + e + ", \n" + e.getMessage());
            }
            if(value != null) {
                Log.log("Access to native pointer " + bufferGroup.getAddress(0) + " did not throw an exception. The native memory" +
                        "is still being used by this process...");
            }
        }
        Log.log("Exiting application...");
    }

    private static void help() {
        Log.info("==> Test Direct Byte Buffers <==");
        Log.info("Params:");
        Log.info("  -iterations=<number of iterations>: specify number of iterations (seconds) of the test on each thread." +
                " Default is 10.");
        Log.info("  -exc: if you want to throw an exception in the middle of execution and in any thread.");
        Log.info("  -verbose: if you want to log every event of the application.");
        Log.info("  -size=<MB>: to specify size in MB. Default is 1024 MB (1GB). Max value is 1024MB.");
        Log.info("  -threads=<thread count>: to specify the number of threads to run, and therefore, the number of buffers to create." +
                " Default is 1.");
        Log.info("  -free: if you want to explicitly invoke cleaner of DirectByteBuffer.");
        Log.info("  -gc: if you want to call System.gc on exit.");
        Log.info("  -crash: if you want to crash the execution with an illegal memory access after" +
                " deleting a buffer in any thread.");
        Log.info("  -checkMemAfterGc: if you want to check if native memory is still available after " +
                "ByteBuffer references are garbage collected.");
        Log.info("");
    }

}

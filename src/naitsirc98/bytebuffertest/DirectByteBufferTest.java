package naitsirc98.bytebuffertest;

import java.nio.ByteBuffer;
import java.util.Random;

import static naitsirc98.bytebuffertest.MemoryUtils.*;

public class DirectByteBufferTest implements Runnable {

    private ByteBuffer buffer;
    private final Arguments args;

    public DirectByteBufferTest(ByteBuffer buffer, Arguments args) {
        this.buffer = buffer;
        this.args = args;
    }

    private String thread() {
        return Thread.currentThread().getName();
    }

    public void run() {

        Log.logVerbose(thread() + ": Executing ByteBuffer test " + args);

        Random random = new Random(System.nanoTime());

        Runtime r = Runtime.getRuntime();

        final long start = System.currentTimeMillis();

        for(int j = 0;j < args.iterations();j++) {
            Log.logVerbose(thread() + ": Starting iteration " + j + "...");
            sleep(1000);
            for(int i = 0;i < 100000;i++) {
                final int pos1 = random.nextInt(buffer.capacity());
                final int pos2 = random.nextInt(buffer.capacity());
                buffer.put(pos1, random.nextBoolean() ? (byte) i : buffer.get(pos2));
                if(args.exception() && j >= 2 && random.nextBoolean()) {
                    throw new UserRequestedException(thread() + ": User requested exception: buffer = " + buffer);
                }
            }
            buffer.clear();
            if(args.exception()) {
                throw new UserRequestedException(thread() + ": User requested exception: buffer = " + buffer);
            }
        }

        Log.logVerbose(thread() + ": Loop terminated in " + (System.currentTimeMillis() - start) / 1000.0 + " seconds.");

        if(args.free()) {
            Log.log(thread() + ": Deleting buffer explicitly...");
            sleep(3000);
            free(buffer);
            Log.log(thread() + ": Buffer cleaner invoked " + buffer);
            Log.log(thread() + ": Native memory is supposed to be freed at this point.");
            sleep(3000);
            if(args.crash()) {
                Log.log(thread() + ": Provoking an illegal (to deleted) memory access...");
                Log.log(thread() + ": Please notice this could cause either an exception or a program crash");
                Byte b = null;
                try {
                    b = buffer.get(0);
                } catch (Exception e) {
                    Log.log(thread() + ": Accessing deleted memory throws exception, as expected: " + e + "\n" + e.getMessage());
                }
                if(b != null) {
                    throw new IllegalStateException(thread() + ": Access to deleted memory did not throw an exception!!");
                }
            }
        }

        if(args.gc()) {
            Log.log(thread() + ": Invoking System.gc()...");
            double totalMem = r.totalMemory() / 1024.0 / 1024.0;
            double freeMem = r.freeMemory() / 1024.0 / 1024.0;
            double usedMed = totalMem - freeMem;
            Log.log(thread() + ": JVM Memory used before gc: " + usedMed + " MB");
            sleep(2000);
            System.gc();
            sleep(3000);
            totalMem = r.totalMemory() / 1024.0 / 1024.0;
            freeMem = r.freeMemory() / 1024.0 / 1024.0;
            usedMed = totalMem - freeMem;
            Log.log(thread() + ": JVM Memory used after gc: " + usedMed + " MB");
            sleep(3000);
        }

        Log.logVerbose(thread() + ": Test terminated");

        buffer = null;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

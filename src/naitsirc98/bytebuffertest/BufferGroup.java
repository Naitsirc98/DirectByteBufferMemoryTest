package naitsirc98.bytebuffertest;

import java.nio.ByteBuffer;

import static naitsirc98.bytebuffertest.MemoryUtils.addressOf;
import static naitsirc98.bytebuffertest.MemoryUtils.allocBuffer;

public class BufferGroup {

    private final Arguments arguments;
    private final ByteBuffer[] buffers;
    private final long[] addresses;

    public BufferGroup(Arguments arguments) {
        this.arguments = arguments;
        Log.logVerbose("Allocating " + arguments.numBuffers() + " of " + arguments.bufferSize() + " MB each...");
        buffers = new ByteBuffer[arguments.numBuffers()];
        addresses = new long[arguments.numBuffers()];
        for(int i = 0;i < arguments.numBuffers();i++) {
            buffers[i] = allocBuffer((long)arguments.bufferSize() * 1024L * 1024L);
            addresses[i] = addressOf(buffers[i]);
        }
    }

    public ByteBuffer get(int index) {
        return buffers[index];
    }

    public long getAddress(int index) {
        return addresses[index];
    }
}

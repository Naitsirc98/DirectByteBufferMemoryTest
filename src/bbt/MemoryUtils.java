package bbt;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static java.util.Objects.requireNonNull;

public final class MemoryUtils {
	public static final long NULL = 0L;

	private static final Unsafe UNSAFE;
	private static final Class<? extends ByteBuffer> DIRECT_BUFFER_CLASS;
	private static final long BUFFER_ADDRESS_OFFSET;
	private static final long BUFFER_POSITION_OFFSET;
	private static final long BUFFER_MARK_OFFSET;
	private static final long BUFFER_CAPACITY_OFFSET;
	private static final long BUFFER_LIMIT_OFFSET;

	private static ByteOrder defaultByteOrder = ByteOrder.nativeOrder();

	public static ByteOrder defaultByteOrder() {
		return defaultByteOrder;
	}

	public static void defaultByteOrder(ByteOrder byteOrder) {
		defaultByteOrder = requireNonNull(byteOrder);
	}

	public static MappedByteBuffer map(FileChannel fileChannel, FileChannel.MapMode mode, long baseOffset, long size) {
		try {
			MappedByteBuffer mappedByteBuffer = fileChannel.map(mode, baseOffset, size);
			mappedByteBuffer.order(defaultByteOrder());
			return mappedByteBuffer;
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static ByteBuffer allocBuffer(long size) {
		return allocBuffer(size, defaultByteOrder());
	}

	public static ByteBuffer allocBuffer(long size, ByteOrder order) {
		if (size < 0) {
			throw new IllegalArgumentException("Size is negative or too large");
		}
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Size " + size + " too large for ByteBuffer");
		}
		ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
		buffer.order(order);
		return buffer;
	}

	public static long addressOf(Buffer buffer) {
		if (!buffer.isDirect()) {
			throw new IllegalArgumentException("Buffer is not direct");
		}
		return UNSAFE.getLong(buffer, BUFFER_ADDRESS_OFFSET);
	}

	public static long malloc(long bytes) {
		return UNSAFE.allocateMemory(bytes);
	}

	public static long calloc(long bytes) {
		final long ptr = malloc(bytes);
		memset(ptr, bytes, 0);
		return ptr;
	}

	public static long realloc(long ptr, long bytes) {
		return UNSAFE.reallocateMemory(ptr, bytes);
	}

	public static void memset(long ptr, long bytes, int value) {
		UNSAFE.setMemory(ptr, bytes, (byte) (value & 0xFF));
	}

	public static void memcpy(long src, long dest, long bytes) {
		UNSAFE.copyMemory(src, dest, bytes);
	}

	public static void memcpy(ByteBuffer src, long srcOffset, byte[] dest, long destOffset, long bytes) {
		memcpy(addressOf(src), srcOffset, dest, destOffset, bytes);
	}

	public static void memcpy(long srcAddress, long srcOffset, byte[] dest, long destOffset, long bytes) {
		UNSAFE.copyMemory(null, srcAddress + srcOffset, dest, arrayBaseOffset(dest.getClass()) + destOffset, bytes);
	}

	public static void memcpy(byte[] src, long srcOffset, ByteBuffer dest, long destOffset, long bytes) {
		memcpy(src, srcOffset, addressOf(dest), destOffset, bytes);
	}

	public static void memcpy(byte[] src, long srcOffset, long destAddress, long destOffset, long bytes) {
		UNSAFE.copyMemory(src, arrayBaseOffset(src.getClass()) + srcOffset, null, destAddress + destOffset, bytes);
	}

	public static void free(long ptr) {
		UNSAFE.freeMemory(ptr);
	}

	public static void free(ByteBuffer buffer) {
		UNSAFE.invokeCleaner(buffer);
	}

	public static byte getByte(long ptr, long offset) {
		return UNSAFE.getByte(ptr + offset);
	}

	public static void setByte(long ptr, long offset, int value) {
		UNSAFE.putByte(ptr + offset, (byte) (value & 0xFF));
	}

	public static short getShort(long ptr, long offset) {
		return UNSAFE.getShort(ptr + offset);
	}

	public static void setShort(long ptr, long offset, int value) {
		UNSAFE.putShort(ptr + offset, (short) (value & 0xFFFF));
	}

	public static char getChar(long ptr, long offset) {
		return UNSAFE.getChar(ptr + offset);
	}

	public static void setChar(long ptr, long offset, char value) {
		UNSAFE.putChar(ptr + offset, value);
	}

	public static int getInt(long ptr, long offset) {
		return UNSAFE.getInt(ptr + offset);
	}

	public static void setInt(long ptr, long offset, int value) {
		UNSAFE.putInt(ptr + offset, value);
	}

	public static long getLong(long ptr, long offset) {
		return UNSAFE.getLong(ptr + offset);
	}

	public static void setLong(long ptr, long offset, long value) {
		UNSAFE.putLong(ptr + offset, value);
	}

	public static float getFloat(long ptr, long offset) {
		return UNSAFE.getFloat(ptr + offset);
	}

	public static void setFloat(long ptr, long offset, float value) {
		UNSAFE.putFloat(ptr + offset, value);
	}

	public static double getDouble(long ptr, long offset) {
		return UNSAFE.getDouble(ptr + offset);
	}

	public static void setDouble(long ptr, long offset, double value) {
		UNSAFE.putDouble(ptr + offset, value);
	}

	public static long arrayBaseOffset(Class<?> arrayClass) {
		return UNSAFE.arrayBaseOffset(arrayClass);
	}

	static {
		Unsafe unsafe = null;
		try {
			final Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		UNSAFE = unsafe;
	}

	static {
		DIRECT_BUFFER_CLASS = ByteBuffer.allocateDirect(0).getClass();
	}

	static {
		long offset = 0;
		try {
			Field field = Buffer.class.getDeclaredField("address");
			offset = UNSAFE.objectFieldOffset(field);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BUFFER_ADDRESS_OFFSET = offset;
	}

	static {
		long offset = 0;
		try {
			Field field = Buffer.class.getDeclaredField("mark");
			offset = UNSAFE.objectFieldOffset(field);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BUFFER_MARK_OFFSET = offset;
	}

	static {
		long offset = 0;
		try {
			Field field = Buffer.class.getDeclaredField("position");
			offset = UNSAFE.objectFieldOffset(field);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BUFFER_POSITION_OFFSET = offset;
	}

	static {
		long offset = 0;
		try {
			Field field = Buffer.class.getDeclaredField("capacity");
			offset = UNSAFE.objectFieldOffset(field);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BUFFER_CAPACITY_OFFSET = offset;
	}

	static {
		long offset = 0;
		try {
			Field field = Buffer.class.getDeclaredField("limit");
			offset = UNSAFE.objectFieldOffset(field);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BUFFER_LIMIT_OFFSET = offset;
	}

	private MemoryUtils() {
	}
}

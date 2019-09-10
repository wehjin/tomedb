package com.rubyhuntersky.tomedb.datalog.hamt

import java.nio.ByteBuffer
import java.nio.ByteOrder

object Hamt {

    fun toIndices(key: Long): Sequence<Byte> {
        val hash = UniHash.hashLong(key)
        return indicesFromHash(hash)
    }

    internal fun indicesFromHash(hash: Long): Sequence<Byte> {
        return sequence {
            for (shiftBits in 0..60 step 5) {
                yield(((hash shr shiftBits) and 0x1f).toByte())
            }
        }
    }

    fun bytesFromLong(long: Long): ByteArray {
        return ByteArray(Long.SIZE_BYTES)
            .also {
                val buffer = ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.BIG_ENDIAN)
                buffer.putLong(long)
                buffer.rewind()
                buffer.get(it)
            }
    }

    fun longFromBytes(bytes: ByteArray): Long {
        val buffer = ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.BIG_ENDIAN)
        buffer.put(bytes, 0, Long.SIZE_BYTES)
        return buffer.getLong(0)
    }
}
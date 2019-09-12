package com.rubyhuntersky.tomedb.basics

import java.nio.ByteBuffer
import java.nio.ByteOrder

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
    buffer.rewind()
    return buffer.long
}
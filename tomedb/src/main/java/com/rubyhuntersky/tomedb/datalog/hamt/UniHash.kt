package com.rubyhuntersky.tomedb.datalog.hamt

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.absoluteValue

object UniHash {

    fun hashLong(l: Long) = hashBytes(longToBytes(l))

    private fun longToBytes(long: Long): ByteArray {
        return ByteArray(Long.SIZE_BYTES)
            .also {
                val buffer = ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.BIG_ENDIAN)
                buffer.putLong(long)
                buffer.rewind()
                buffer.get(it)
            }
    }

    private fun hashBytes(key: ByteArray): Long {
        val (hash, _) = key.fold(
            initial = Pair(0L, 31415L),
            operation = { (hash, a), keyByte -> Pair(a * hash + keyByte.toLong(), a * 27183L) }
        )
        return hash.absoluteValue
    }
}
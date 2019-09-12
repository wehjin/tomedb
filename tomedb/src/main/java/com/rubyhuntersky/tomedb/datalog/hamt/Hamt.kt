package com.rubyhuntersky.tomedb.datalog.hamt

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
}
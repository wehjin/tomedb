package com.rubyhuntersky.tomedb.datalog.hamt

object Hamt : KeyBreaker {

    override fun slotIndex(key: Long, depth: Int): Int {
        // TODO Replace with efficient function
        val first = toIndices(key).drop(depth).firstOrNull()
        return first?.toInt() ?: error("Too few indices for key $key at depth $depth")
    }

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
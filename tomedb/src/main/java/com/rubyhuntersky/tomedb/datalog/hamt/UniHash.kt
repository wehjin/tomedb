package com.rubyhuntersky.tomedb.datalog.hamt

object UniHash {

    fun hashLong(l: Long) = hashBytes(Hamt.bytesFromLong(l))

    private fun hashBytes(key: ByteArray): Long {
        val (hash, _) = key.fold(
            initial = Pair(0L, 31415L),
            operation = { (hash, a), keyByte ->
                val newHash = a * hash + keyByte.toLong()
                Pair(newHash, a * 27183L)
            }
        )
        return hash and (1L shl (Long.SIZE_BITS - 1)).inv()
    }
}
package com.rubyhuntersky.tomedb.datalog.trie

class TrieKey(private val hash: Long) {
    fun toIndices(): Sequence<Byte> {
        return sequence {
            for (shiftBits in 0..60 step 5) {
                yield(((hash shr shiftBits) and 0x1f).toByte())
            }
        }
    }
}
package com.rubyhuntersky.tomedb.datalog.pile

import com.rubyhuntersky.tomedb.basics.longFromBytes
import com.rubyhuntersky.tomedb.datalog.framing.FrameReader

class PileReader(private val topBase: Long, private val frameReader: FrameReader) {

    fun read(): Sequence<ByteArray> {
        return sequence {
            var nextBase = topBase
            while (nextBase != Pile.NULL_BASE) {
                val frameBytes = frameReader.read(nextBase)
                nextBase = longFromBytes(frameBytes)
                yield(frameBytes.sliceArray(Long.SIZE_BYTES until frameBytes.size))
            }
        }
    }
}
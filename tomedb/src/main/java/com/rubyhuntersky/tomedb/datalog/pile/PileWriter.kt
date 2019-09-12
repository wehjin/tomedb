package com.rubyhuntersky.tomedb.datalog.pile

import com.rubyhuntersky.tomedb.basics.bytesFromLong
import com.rubyhuntersky.tomedb.datalog.framing.FramePosition
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import com.rubyhuntersky.tomedb.datalog.pile.Pile.NULL_BASE

class PileWriter(private var topBase: Long?, private val frameWriter: FrameWriter) {

    fun write(layerBytes: ByteArray): FramePosition {
        val lowerBaseBytes = bytesFromLong(topBase?.let { it } ?: NULL_BASE)
        return frameWriter.write(lowerBaseBytes, layerBytes).also { topBase = it }
    }
}


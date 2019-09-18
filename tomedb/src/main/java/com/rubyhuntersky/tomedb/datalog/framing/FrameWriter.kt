package com.rubyhuntersky.tomedb.datalog.framing

import java.io.File
import java.io.RandomAccessFile

class FrameWriter(private val file: RandomAccessFile, private var outputEnd: Long) {
    init {
        file.seek(outputEnd)
    }

    fun write(vararg frameParts: ByteArray): FramePosition {
        val frameLength = frameParts.map { it.size }.sum()
        val recordStart = outputEnd
        file.writeInt(frameLength)
        for (part in frameParts) {
            file.write(part)
        }
        outputEnd += Int.SIZE_BYTES + frameLength
        return recordStart
    }

    companion object {
        fun new(file: File, outputEnd: Long): FrameWriter {
            return FrameWriter(RandomAccessFile(file, "rw"), outputEnd)
        }
    }
}
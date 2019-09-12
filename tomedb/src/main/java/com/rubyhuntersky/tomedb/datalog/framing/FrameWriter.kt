package com.rubyhuntersky.tomedb.datalog.framing

import java.io.OutputStream
import java.nio.ByteBuffer

class FrameWriter(private val outputStream: OutputStream, private var outputEnd: Long) {

    private val byteBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
        .order(FramePolicy.byteOrder)
    private val frameSizeBytes = ByteArray(Int.SIZE_BYTES)

    fun write(vararg frameParts: ByteArray): FramePosition {
        val frameLength = frameParts.map { it.size }.sum()
        val recordStart = outputEnd
        updateFrameSizeBytes(frameLength)
        outputStream.write(frameSizeBytes)
        for (part in frameParts) {
            outputStream.write(part)
        }
        outputEnd += Int.SIZE_BYTES + frameLength
        return recordStart
    }

    private fun updateFrameSizeBytes(frameSize: Int) {
        with(byteBuffer) {
            rewind()
            putInt(frameSize)
            rewind()
            get(frameSizeBytes)
        }
    }
}
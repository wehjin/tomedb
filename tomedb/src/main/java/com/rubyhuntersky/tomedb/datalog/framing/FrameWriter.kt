package com.rubyhuntersky.tomedb.datalog.framing

import com.rubyhuntersky.tomedb.datalog.framing.FramePolicy
import java.io.OutputStream
import java.nio.ByteBuffer

class FrameWriter(private val outputStream: OutputStream, private var outputEnd: Long) {

    private val byteBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
        .order(FramePolicy.byteOrder)
    private val sizeArray = ByteArray(Int.SIZE_BYTES)

    fun write(frame: ByteArray): Long {
        val recordStart = outputEnd
        updateSizeArray(frame)
        outputStream.write(sizeArray)
        outputStream.write(frame)
        outputEnd += Int.SIZE_BYTES + frame.size
        return recordStart
    }

    private fun updateSizeArray(record: ByteArray) {
        with(byteBuffer) {
            rewind()
            putInt(record.size)
            rewind()
            get(sizeArray)
        }
    }
}
package com.rubyhuntersky.tomedb.datalog.framing

import java.io.InputStream
import java.nio.ByteBuffer

class FrameReader(private val inputStream: InputStream) {

    private val byteBuffer = ByteBuffer.allocate(Int.SIZE_BYTES).order(FramePolicy.byteOrder)
    private val sizeArray = ByteArray(Int.SIZE_BYTES)

    init {
        require(inputStream.markSupported())
        inputStream.mark(Int.MAX_VALUE)
    }

    fun read(frameStart: Long): ByteArray {
        moveTo(frameStart)
        val frameSize = readFrameSize()
        return readRecord(frameSize)
    }

    private fun readFrameSize(): Int {
        check(inputStream.read(sizeArray) == sizeArray.size) {
            "Bytes read ${inputStream.read(sizeArray)} failed to match expected count $“sizeArray.size}"
        }
        byteBuffer.rewind()
        byteBuffer.put(sizeArray)
        byteBuffer.rewind()
        return byteBuffer.int
    }

    private fun readRecord(frameSize: Int): ByteArray {
        return ByteArray(frameSize).also { check(inputStream.read(it) == it.size) }
    }

    private fun moveTo(newPosition: Long) {
        inputStream.reset()
        inputStream.skip(newPosition)
    }
}
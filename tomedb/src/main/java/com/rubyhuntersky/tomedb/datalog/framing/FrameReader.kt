package com.rubyhuntersky.tomedb.datalog.framing

import java.io.InputStream
import java.nio.ByteBuffer

class FrameReader(private val inputStream: InputStream, private var position: Long = 0) {

    private val byteBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
        .order(FramePolicy.byteOrder)
    private val sizeArray = ByteArray(Int.SIZE_BYTES)

    fun read(frameStart: Long): ByteArray {
        moveTo(frameStart)
        val frameSize = readFrameSize()
        return readRecord(frameSize)
    }

    private fun readRecord(frameSize: Int): ByteArray {
        return ByteArray(frameSize).also {
            check(inputStream.read(it) == it.size)
            position += it.size
        }
    }

    private fun readFrameSize(): Int {
        check(inputStream.read(sizeArray) == sizeArray.size)
        position += sizeArray.size
        byteBuffer.rewind()
        byteBuffer.put(sizeArray)
        byteBuffer.rewind()
        return byteBuffer.int
    }

    private fun moveTo(newPosition: Long) {
        inputStream.skip(newPosition - position)
        position = newPosition
    }
}
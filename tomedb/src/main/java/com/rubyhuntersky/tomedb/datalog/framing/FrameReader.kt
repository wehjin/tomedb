package com.rubyhuntersky.tomedb.datalog.framing

import java.io.File
import java.io.RandomAccessFile

class FrameReader(private val file: RandomAccessFile) {

    fun read(frameStart: Long): ByteArray {
        file.seek(frameStart)
        val frameSize = file.readInt()
        return readRecord(frameSize)
    }

    private fun readRecord(frameSize: Int): ByteArray {
        return ByteArray(frameSize).also {
            val readBytes = file.read(it)
            check(readBytes == it.size) { "Expected $frameSize bytes in frame, but read $readBytes bytes." }
        }
    }

    companion object {
        fun new(file: File): FrameReader {
            return FrameReader(RandomAccessFile(file, "r"))
        }
    }
}
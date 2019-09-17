package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class HamtWriterTest {
    @Test
    fun variousKeys() {
        val rangeSize = 300L
        val ranges = listOf(
            0L..rangeSize,
            Long.MAX_VALUE / 2 - rangeSize / 2 until Long.MAX_VALUE / 2 + rangeSize / 2,
            Long.MAX_VALUE - rangeSize until Long.MAX_VALUE
        )
        ranges.forEach { range ->
            val outputStream = ByteArrayOutputStream(0)
            val writer = HamtWriter(
                refreshFrameReader = {
                    val writtenBytes = outputStream.toByteArray()
                    FrameReader(ByteArrayInputStream(writtenBytes))
                },
                rootBase = null,
                frameWriter = FrameWriter(outputStream, 0)
            )
            range.forEach { writer[it] = it }

            val reader = HamtReader(
                inputStream = ByteArrayInputStream(outputStream.toByteArray()),
                rootBase = writer.hamtBase
            )
            range.forEach { assertEquals(it, reader[it]) }
        }
    }

    @Test
    fun negativeValues() {
        val outputStream = ByteArrayOutputStream(0)
        val writer = HamtWriter(
            refreshFrameReader = {
                val writtenBytes = outputStream.toByteArray()
                FrameReader(ByteArrayInputStream(writtenBytes))
            },
            rootBase = null,
            frameWriter = FrameWriter(outputStream, 0)
        )
        writer[5] = -5L

        val reader = HamtReader(
            inputStream = ByteArrayInputStream(outputStream.toByteArray()),
            rootBase = writer.hamtBase
        )
        assertEquals(-5L, reader[5])
    }

    @Test
    fun biggerRange() {
        val range = 0L..3000L
        val outputStream = ByteArrayOutputStream(0)
        val writer = HamtWriter(
            refreshFrameReader = {
                val writtenBytes = outputStream.toByteArray()
                FrameReader(ByteArrayInputStream(writtenBytes))
            },
            rootBase = null,
            frameWriter = FrameWriter(outputStream, 0)
        )
        range.forEach { writer[it] = it }

        val reader = HamtReader(
            inputStream = ByteArrayInputStream(outputStream.toByteArray()),
            rootBase = writer.hamtBase
        )
        range.forEach { assertEquals(it, reader[it]) }
    }
}
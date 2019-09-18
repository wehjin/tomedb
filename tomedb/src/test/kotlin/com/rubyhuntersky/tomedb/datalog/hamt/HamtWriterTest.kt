package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.TempDirFixture
import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class HamtWriterTest {

    @Before
    fun setUp() {
        framesFile = TempDirFixture.initDir("basicPileTest").toFile()
            .also { it.mkdirs() }
            .let { File(it, "frames") }
    }

    private lateinit var framesFile: File

    @Test
    fun variousKeys() {
        val rangeSize = 300L
        val ranges = listOf(
            0L..rangeSize,
            Long.MAX_VALUE / 2 - rangeSize / 2 until Long.MAX_VALUE / 2 + rangeSize / 2,
            Long.MAX_VALUE - rangeSize until Long.MAX_VALUE
        )
        ranges.forEach { range ->
            val writer = HamtWriter(
                refreshFrameReader = { FrameReader.new(framesFile) },
                rootBase = null,
                frameWriter = FrameWriter.new(framesFile)
            )
            range.forEach { writer[it] = it }

            val reader = HamtReader(
                frameReader = FrameReader.new(framesFile),
                rootBase = writer.hamtBase
            )
            range.forEach { assertEquals(it, reader[it]) }
        }
    }

    @Test
    fun negativeValues() {
        val writer = HamtWriter(
            refreshFrameReader = { FrameReader.new(framesFile) },
            rootBase = null,
            frameWriter = FrameWriter.new(framesFile)
        )
        writer[5] = -5L

        val reader = HamtReader(
            frameReader = FrameReader.new(framesFile),
            rootBase = writer.hamtBase
        )
        assertEquals(-5L, reader[5])
    }

    @Test
    fun biggerRange() {
        val range = 0L..3000L
        val writer = HamtWriter(
            refreshFrameReader = { FrameReader.new(framesFile) },
            rootBase = null,
            frameWriter = FrameWriter.new(framesFile)
        )
        range.forEach { writer[it] = it }

        val reader = HamtReader(
            frameReader = FrameReader.new(framesFile),
            rootBase = writer.hamtBase
        )
        range.forEach { assertEquals(it, reader[it]) }
    }
}
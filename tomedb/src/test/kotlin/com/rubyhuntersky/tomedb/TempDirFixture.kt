package com.rubyhuntersky.tomedb

import java.io.File
import java.nio.file.Path

object TempDirFixture {

    fun initDir(name: String): Path {
        val tempFolder = System.getProperty("java.io.tmpdir")
        val testFolder = File(tempFolder, name).also {
            it.deleteRecursively()
            println("FOLDER:: ${it.absolutePath}")
        }
        return testFolder.toPath()
    }
}
package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.datalog.FileDatalog
import java.io.File

interface Transactor {
    fun nextEnt(): Long
    fun getDb(): Database
    fun update(updates: List<Update>): List<Fact>
    fun commit()
}

class FileTransactor(dataDir: File) : Transactor {
    private val datalog: Datalog = FileDatalog(dataDir)

    override fun update(updates: List<Update>): List<Fact> {
        return updates.map(this::update).also {
            if (it.isNotEmpty()) {
                commit()
            }
        }
    }

    private fun update(update: Update): Fact {
        val (entity, attr, value, type) = update
        require(value !is TagList)
        return datalog.append(entity, attr, value, type.toStanding())
    }

    override fun commit() = datalog.commit()

    override fun getDb(): Database = DatalogDatabase(datalog.toDatalist())

    private var nextEnt: Long = 1
    override fun nextEnt(): Long = nextEnt++
    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEnt, datalog=$datalog)"
    }
}
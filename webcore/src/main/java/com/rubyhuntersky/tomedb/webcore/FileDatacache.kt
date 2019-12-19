package com.rubyhuntersky.tomedb.webcore

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.Datalist
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.datalog.FileDatalog
import java.io.File

class FileDatacache(dir: File, private val datalog: Datalog) : Datacache {

    private val localDatalog = FileDatalog(dir)
    private val entHeights = mutableMapOf<Long, Long>()

    override fun liftEntToHeight(ent: Long, height: Long): Boolean {
        val localHeight = entHeights[ent] ?: -1
        return if (localHeight < height) {
            val newFacts = datalog.factsOfEnt(ent, localHeight, height)
            localDatalog.addFactsCommit(newFacts)
            entHeights[ent] = height
            true
        } else {
            false
        }
    }

    override fun toDatalist(): Datalist = object : Datalist {

        override val height = datalog.height

        override fun ents(attr: Keyword): Sequence<Long> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun ents(): Sequence<Long> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun factsOfEnt(ent: Long, minHeight: Long, maxHeight: Long): Sequence<Fact> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun attrs(entity: Long): Sequence<Keyword> {
            this@FileDatacache.liftEntToHeight(entity, height)
            // TODO Add height limit to Datalist
            return this@FileDatacache.localDatalog.toDatalist(height).attrs(entity)
        }

        override fun attrs(): Sequence<Keyword> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun values(entity: Long, attr: Keyword): Sequence<Any> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun values(): Sequence<Any> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isAsserted(entity: Long, attr: Keyword, value: Any): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isAsserted(entity: Long, attr: Keyword): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}
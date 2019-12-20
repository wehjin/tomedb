package com.rubyhuntersky.tomedb.webcore

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.Datalist
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.datalog.FileDatalog
import java.io.File

class FileDatacache(dir: File, private val datalog: Datalog) : Datacache {

    private val dataDir = File(dir, "data").also { it.mkdirs() }
    private val localDatalog = FileDatalog(dataDir)

    // TODO Store and restore maps.
    private val entHeights = mutableMapOf<Long, Long>()
    private val attrHeights = mutableMapOf<Keyword, Long>()

    override fun liftEntToHeight(ent: Long, height: Long): Datalist {
        val localHeight = entHeights[ent] ?: -1
        if (localHeight < height) {
            val newFacts = datalog.factsOfEnt(ent, localHeight, height)
            localDatalog.addFactsCommit(newFacts)
            entHeights[ent] = height
        }
        return localDatalog.toDatalist(height)
    }

    override fun liftAttrToHeight(attr: Keyword, height: Long): Datalist {
        val localHeight = attrHeights[attr] ?: -1
        if (localHeight < height) {
            val newFacts = datalog.factsOfAttr(attr, localHeight, height)
            localDatalog.addFactsCommit(newFacts)
            attrHeights[attr] = height
        }
        return localDatalog.toDatalist(height)
    }

    override fun toDatalist(): Datalist = object : Datalist {

        override val height = datalog.height
        private val cache = this@FileDatacache

        override fun factsOfAttr(attr: Keyword, minHeight: Long, maxHeight: Long): Sequence<Fact> =
            cache.liftAttrToHeight(attr, height).factsOfAttr(attr, minHeight, maxHeight)

        override fun ents(attr: Keyword): Sequence<Long> =
            cache.liftAttrToHeight(attr, height).ents(attr)

        override fun ents(): Sequence<Long> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun factsOfEnt(ent: Long, minHeight: Long, maxHeight: Long): Sequence<Fact> =
            cache.liftEntToHeight(ent, height).factsOfEnt(ent, minHeight, maxHeight)

        override fun attrs(entity: Long): Sequence<Keyword> =
            cache.liftEntToHeight(entity, height).attrs(entity)

        override fun attrs(): Sequence<Keyword> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun values(entity: Long, attr: Keyword): Sequence<Any> =
            liftCacheToHeight(entity, attr).values(entity, attr)

        private fun liftCacheToHeight(entity: Long, attr: Keyword): Datalist =
            cache.liftEntToHeight(entity, height).let { cache.liftAttrToHeight(attr, height) }

        override fun values(): Sequence<Any> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isAsserted(entity: Long, attr: Keyword, value: Any): Boolean =
            liftCacheToHeight(entity, attr).isAsserted(entity, attr, value)

        override fun isAsserted(entity: Long, attr: Keyword): Boolean =
            liftCacheToHeight(entity, attr).isAsserted(entity, attr)
    }
}
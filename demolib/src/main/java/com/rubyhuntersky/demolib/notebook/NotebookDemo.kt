package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.data.*
import com.rubyhuntersky.tomedb.scopes.client.ClientScope
import com.rubyhuntersky.tomedb.scopes.query.tomeFromTraitTopic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun main() {
    val demo = NotebookDemo()
    demo.run()
}

object Note : AttributeGroup {

    object CREATED : Attribute {
        override val valueType: ValueType = ValueType.INSTANT
        override val cardinality: Cardinality = Cardinality.ONE
        override val description: String = "The instant a note was created."
    }

    object TEXT : Attribute {
        override val valueType: ValueType = ValueType.STRING
        override val cardinality: Cardinality = Cardinality.ONE
        override val description: String = "The text of the note."
    }
}

class NotebookDemo(
    private val job: Job = Job(),
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job,
    override val dbDir: File = File("data", "notebook"),
    override val dbSpec: List<Attribute> = emptyList()
) : ClientScope {

    private val connectionScope = connectToDatabase()

    data class Mdl(val tome: Tome<TraitKey<Date>>)

    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
        data class DROP(val page: PageTitle<TraitKey<Date>>) : Msg()
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    fun run() {
        println("Running with data in: ${dbDir.absoluteFile}")
        val mdls = Channel<Mdl>(10)
        val actor = actor<Msg> {
            var mdl = initMdl()
            mdls.send(mdl)
            loop@ for (msg in channel) {
                updateMdl(mdl, msg)?.let { mdl = it }
                mdls.send(mdl)
            }
        }
        runBlocking(coroutineContext) {
            renderMdl(mdls, actor)
        }
    }

    private suspend fun initMdl(): Mdl {
        val topic = TomeTopic.Trait<Date>(Note.CREATED)
        return Mdl(tome = connectionScope { tomeFromTraitTopic(topic) })
    }

    private suspend fun updateMdl(mdl: Mdl, msg: Msg): Mdl? = when (msg) {
        is Msg.LIST -> null
        is Msg.ADD -> {
            val date = Date()
            val data = mapOf<Keyword, Any>(
                Note.CREATED to date,
                Note.TEXT to if (msg.text.isBlank()) "Today is $date" else msg.text
            )

            val ent = Ent.of(Note.CREATED, date)
            val key = TraitKey(ent, date)
            val title = mdl.tome.newPageTitle(key)
            val page = pageOf(title, data)
            connectionScope { dbWrite(page) }
            mdl.copy(tome = mdl.tome + page)
        }
        is Msg.DROP -> {
            val page = mdl.tome(msg.page)
            page?.let {
                connectionScope { dbClear(it) }
                mdl.copy(tome = mdl.tome - page)
            }
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun renderMdl(mdls: Channel<Mdl>, actor: SendChannel<Msg>) {
        println("Notebook!")
        println("=========")
        loop@ while (!mdls.isClosedForReceive) {
            val mdl = mdls.receive()
            val pageList = mdl.tome.pageList
            pageList.forEachIndexed { index, page ->
                println("----------")
                println(index + 1)
                val date = page<Date>(Note.CREATED)
                val text = page<String>(Note.TEXT)
                println("Created: $date")
                println("Note: $text")
            }
            if (pageList.isEmpty()) {
                print("--- EMPTY ---\n> ")
            } else {
                print("----------\n> ")
            }
            tailrec fun readUserAndContinue(): Boolean {
                val userLine = readLine()!!
                when {
                    userLine == "list" -> actor.offer(Msg.LIST)
                    userLine.startsWith("add", true) -> {
                        val text = userLine.substring("add".length).trim()
                        actor.offer(Msg.ADD(text))
                    }
                    userLine.startsWith("drop", true) -> {
                        val number = userLine.substring("drop".length).trim().toIntOrNull()
                        val index = number?.let { it - 1 } ?: 0
                        actor.offer(Msg.DROP(page = pageList[index].title))
                    }
                    userLine == "done" -> {
                        actor.close()
                        return false
                    }
                    else -> {
                        print("Sumimasen, mou ichido yukkuri itte kudasai.\n> ")
                        return readUserAndContinue()
                    }
                }
                return true
            }
            if (!readUserAndContinue()) break@loop
        }
        println("\nUser has left the building.")
    }
}

package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.data.*
import com.rubyhuntersky.tomedb.scopes.client.ClientScope
import com.rubyhuntersky.tomedb.scopes.query.dbTome
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
    val demo = NotesDemo()
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

class NotesDemo(
    private val job: Job = Job(),
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job,
    override val dbDir: File = File("data", "notebook"),
    override val dbSpec: List<Attribute> = emptyList()
) : ClientScope {

    private val connectionScope = connectToDatabase()

    data class Mdl(val tome: Tome<Date>)

    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
        data class REVISE(val key: Date, val text: String) : Msg()
        data class DROP(val key: Date) : Msg()
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
        return Mdl(tome = connectionScope { dbTome(topic) })
    }

    private suspend fun updateMdl(mdl: Mdl, msg: Msg): Mdl? = when (msg) {
        is Msg.LIST -> null
        is Msg.ADD -> {
            val date = Date()
            val data = mapOf<Keyword, Any>(
                Note.CREATED to date,
                Note.TEXT to if (msg.text.isBlank()) "Today is $date" else msg.text
            )
            val title = mdl.tome.newPageTitle(date)
            val page = pageOf(title, data)
            connectionScope { dbWrite(page) }
            mdl.copy(tome = mdl.tome + page)
        }
        is Msg.REVISE -> {
            mdl.tome(msg.key)?.let {
                val textLine = lineOf(Note.TEXT, msg.text)
                val nextPage = connectionScope { dbWrite(it, textLine) }
                mdl.copy(tome = mdl.tome + nextPage)
            } ?: mdl
        }
        is Msg.DROP -> {
            mdl.tome(msg.key)?.let {
                connectionScope { dbClear(it) }
                mdl.copy(tome = mdl.tome - msg.key)
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
            if (!sendMsg(pageList, actor)) break@loop
        }
        println("\nUser has left the building.")
    }

    private tailrec fun sendMsg(pageList: List<Page<Date>>, actor: SendChannel<Msg>): Boolean {
        val userLine = readLine()!!
        when {
            userLine == "list" -> actor.offer(Msg.LIST)
            userLine.startsWith("add", true) -> {
                val text = userLine.substring("add".length).trim()
                actor.offer(Msg.ADD(text))
            }
            userLine.startsWith("revise", true) -> {
                val numberAndText = userLine.substring("revise".length).trim()
                val number = numberAndText.substringBefore(' ')
                val index = number.toIntOrNull()?.let { it - 1 }
                val msg = index?.let {
                    val text = numberAndText.substringAfter(' ').trim()
                    Msg.REVISE(key = pageList[index].key, text = text)
                } ?: Msg.LIST
                actor.offer(msg)
            }
            userLine.startsWith("drop", true) -> {
                val number = userLine.substring("drop".length).trim()
                val index = number.toIntOrNull()?.let { it - 1 }
                val msg = index?.let { Msg.DROP(key = pageList[index].key) } ?: Msg.LIST
                actor.offer(msg)
            }
            userLine == "done" -> {
                actor.close()
                return false
            }
            else -> {
                print("Sumimasen, mou ichido yukkuri itte kudasai.\n> ")
                return sendMsg(pageList, actor)
            }
        }
        return true
    }

}

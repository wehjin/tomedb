package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.scopes.client.ClientScope
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


class NotebookDemo(
    private val job: Job = Job(),
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job,
    override val dbDir: File = File("data", "notebook"),
    override val dbSpec: List<Attribute> = emptyList()
) : ClientScope {

    private val connectionScope = connectToDatabase()

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

    data class Mdl(
        val notes: List<Ent>,
        val details: Map<Ent, Map<Keyword, Any>>
    )

    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
        data class DROP(val index: Int) : Msg()
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
        val notes = connectionScope { entsWithAttr(Note.CREATED).toList() }
        return Mdl(notes, connectionScope { notes.associateWith { dbRead(it) } })
    }

    private suspend fun updateMdl(mdl: Mdl, msg: Msg): Mdl? = when (msg) {
        is Msg.LIST -> null
        is Msg.ADD -> {
            val date = Date()
            val ent = Ent.of(Note.CREATED, date)
            val data = mapOf<Keyword, Any>(
                Note.CREATED to date,
                Note.TEXT to if (msg.text.isBlank()) "Today is $date" else msg.text
            )
            connectionScope { dbWrite(data.bind(ent)) }
            val newNotes = mdl.notes + ent
            val newDetails = mdl.details + mapOf(ent to data)
            mdl.copy(notes = newNotes, details = newDetails)
        }
        is Msg.DROP -> {
            if (msg.index >= 0 && msg.index < mdl.notes.size) {
                val ent = mdl.notes[msg.index]
                val date = mdl.details[ent]?.get(Note.CREATED) as Date
                connectionScope { dbClear(ent.long, Note.CREATED.attrName, date) }
                val newNotes = mdl.notes - ent
                val newDetails = mdl.details.minus(ent)
                mdl.copy(notes = newNotes, details = newDetails)
            } else null
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun renderMdl(mdls: Channel<Mdl>, actor: SendChannel<Msg>) {
        println("Notebook!")
        println("=========")
        loop@ while (!mdls.isClosedForReceive) {
            val mdl = mdls.receive()
            mdl.notes.forEachIndexed { index, ent ->
                println("----------")
                println(index + 1)
                val data = mdl.details[ent] ?: error("No details for $ent")
                val date = data[Note.CREATED] as Date
                val text = data[Note.TEXT] as String
                println("Created: $date")
                println("Note: $text")
            }
            if (mdl.notes.isNotEmpty()) {
                print("----------\n> ")
            } else {
                print("--- EMPTY ---\n> ")
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
                        actor.offer(Msg.DROP(index))
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

package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import com.rubyhuntersky.tomedb.basics.CommonKeyword
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.scopes.client.ClientScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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

    private val connScope = connectToDatabase()

    enum class Note : Attribute {
        CREATED {
            override val valueType: ValueType = ValueType.INSTANT
            override val cardinality: Cardinality = Cardinality.ONE
            override val description: String = "The instant a note was created."
        },
        TEXT {
            override val valueType: ValueType = ValueType.STRING
            override val cardinality: Cardinality = Cardinality.ONE
            override val description: String = "The text of the note."
        };

    }

    data class Mdl(val notes: List<Ent>)

    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    fun run() {
        println("Running with data in: ${dbDir.absoluteFile}")
        val mdlChan = Channel<Mdl>(10)
        val actor = actor<Msg> {
            connScope.enter {
                var mdl = Mdl(notes = entsWithAttr(Note.CREATED).toList())
                mdlChan.send(mdl)
                loop@ for (msg in channel) {
                    when (msg) {
                        is Msg.LIST -> Unit
                        is Msg.ADD -> {
                            val date = Date()
                            val text = if (msg.text.isBlank()) "Today is $date" else msg.text
                            val ent = Ent.of(Note.CREATED, date)
                            val data = mapOf<Keyword, Any>(Note.CREATED to date, Note.TEXT to text)
                            dbWrite(data.bindTo(ent))
                            mdl = mdl.copy(notes = mdl.notes + ent)
                        }
                    }
                    mdlChan.send(mdl)
                }
            }
        }
        runBlocking(coroutineContext) {
            println("Notebook!")
            println("=========")

            connScope.enter {
                loop@ while (!mdlChan.isClosedForReceive) {
                    val mdl = mdlChan.receive()

                    mdl.notes.forEachIndexed { index, ent ->
                        val data = dbRead(ent).mapKeys { CommonKeyword(it.key) }
                        println("----------")
                        println(index + 1)
                        val date = data[CommonKeyword(Note.CREATED)]
                        val text = data[CommonKeyword(Note.TEXT)]
                        println("Created: $date")
                        println("Note: $text")
                    }
                    print("----------\n> ")

                    tailrec fun readUserAndContinue(): Boolean {
                        val userLine = readLine()!!
                        when {
                            userLine == "done" -> {
                                actor.close()
                                return false
                            }
                            userLine == "list" -> actor.offer(Msg.LIST)
                            userLine.startsWith("add", true) -> {
                                val text = userLine.substring("add".length).trim()
                                actor.offer(Msg.ADD(text))
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
    }
}

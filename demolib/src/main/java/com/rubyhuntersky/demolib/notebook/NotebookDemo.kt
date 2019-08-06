package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.scopes.client.ClientScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
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


    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    fun run() {
        println("Running with data in: ${dbDir.absoluteFile}")
        val actor = actor<Msg> {
            connScope.enter {
                val notes = entsWithAttr(Note.CREATED).toMutableList()
                this@NotebookDemo.render(notes)
                loop@ for (msg in channel) {
                    when (msg) {
                        Msg.LIST -> Unit
                        is Msg.ADD -> {
                            val date = Date()
                            val text = if (msg.text.isBlank()) "Today is $date" else msg.text
                            val data = mapOf<Keyword, Any>(Note.CREATED to date, Note.TEXT to text)
                            val facts = data.bindTo(Ent.of(Note.CREATED, date))
                            dbWrite(facts)
                            notes.add(Ent.of(Note.CREATED, date))
                        }
                    }
                    this@NotebookDemo.render(notes)
                }
            }
        }
        while (!actor.isClosedForSend) {
            val userLine = readLine()!!
            when {
                userLine == "list" -> actor.offer(Msg.LIST)
                userLine.startsWith("add", true) -> actor.offer(Msg.ADD(userLine.substring("add".length).trim()))
                userLine == "done" -> actor.close()
                else -> print("Sumimasen, mou ichido yukkuri itte kudasai.\n> ")
            }
        }
    }

    private fun render(notes: List<Ent>) {
        print("NOTES: $notes\n> ")
    }
}

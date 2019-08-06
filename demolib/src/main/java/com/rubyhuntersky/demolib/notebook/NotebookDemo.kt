package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import com.rubyhuntersky.tomedb.basics.AttrValue
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.queryOf
import com.rubyhuntersky.tomedb.scopes.client.ClientScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.sendBlocking
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

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

        companion object : AttributeGroup
    }

    fun noteData(created: Date, text: String = "Created: $created"): List<AttrValue<*>> {
        return listOf(
            AttrValue(Note.CREATED, created),
            AttrValue(Note.TEXT, text)
        )
    }

    sealed class Msg {
        object Exit : Msg()
    }

    @ObsoleteCoroutinesApi
    fun run() {
        println("Running with data in: ${dbDir.absoluteFile}")
        val actor = actor<Msg> {
            connScope.enter {
                val attr: Keyword = Note.CREATED
                val eSlot = slot("e")
                val aSlot = slot("a")
                val vSlot = slot("v")
                val query = queryOf {
                    rules = listOf(eSlot has attr, eSlot has aSlot eq vSlot, -eSlot and aSlot and vSlot)
                }
                val result = find(query)
                val notes = result.toProjections(eSlot, aSlot, vSlot)
                println("NOTES: ${notes.toList()}")
            }
            loop@ for (msg in channel) {
                when (msg) {
                    is Msg.Exit -> {
                        println("EXIT")
                        break@loop
                    }
                }
            }
        }
        actor.sendBlocking(Msg.Exit)
    }
}

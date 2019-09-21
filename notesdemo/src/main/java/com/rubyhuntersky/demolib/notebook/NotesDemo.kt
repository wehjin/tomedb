package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.demolib.notebook.NotingStory.Mdl
import com.rubyhuntersky.demolib.notebook.NotingStory.Msg
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.database.Entity
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
    val demo = NotesDemo()
    demo.run()
}

class NotesDemo(
    private val job: Job = Job(),
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job,
    override val dbDir: File = File("data", "notebook"),
    override val dbSpec: List<Attribute> = emptyList()
) : ClientScope {

    private val sessionScope = connectToDatabase()

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    fun run() {
        println("Running with data in: ${dbDir.absoluteFile}")
        val mdls = Channel<Mdl>(10)
        val actor = actor<Msg> {
            val story = NotingStory(sessionScope.sessionChannel)
            var mdl = story.init().also { mdls.send(it) }
            loop@ for (msg in channel) {
                story.update(mdl, msg)?.let { mdl = it }
                mdls.send(mdl)
            }
        }
        runBlocking(coroutineContext) {
            renderMdl(mdls, actor)
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun renderMdl(mdls: Channel<Mdl>, actor: SendChannel<Msg>) {
        NotesPrinter.printSessionHeader()
        loop@ while (!mdls.isClosedForReceive) {
            val mdl = mdls.receive()
            NotesPrinter.printScreenHeader()
            val entities = mdl.entities.toList()
            if (entities.isEmpty()) {
                NotesPrinter.printEmptyNotes()
            } else {
                entities.forEachIndexed { index, entity ->
                    val number = index + 1
                    val date = entity<Date>(Note.CREATED)
                    val text = entity<String>(Note.TEXT)
                    NotesPrinter.printNote(number, date, text)
                }
            }
            NotesPrinter.printScreenFooterAndPrompt()
            if (!sendMsg(entities, actor)) break@loop
        }
        NotesPrinter.printSessionFooter()
    }

    private tailrec fun sendMsg(entities: List<Entity<Date>>, actor: SendChannel<Msg>): Boolean {
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
                    Msg.REVISE(key = entities[index].key, text = text)
                } ?: Msg.LIST
                actor.offer(msg)
            }
            userLine.startsWith("drop", true) -> {
                val number = userLine.substring("drop".length).trim()
                val index = number.toIntOrNull()?.let { it - 1 }
                val msg = index?.let { Msg.DROP(key = entities[index].key) } ?: Msg.LIST
                actor.offer(msg)
            }
            userLine == "done" -> {
                actor.close()
                return false
            }
            else -> {
                print("Sumimasen, mou ichido yukkuri itte kudasai.\n> ")
                return sendMsg(entities, actor)
            }
        }
        return true
    }

}

package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.demolib.notebook.NotesStory.Mdl
import com.rubyhuntersky.demolib.notebook.NotesStory.Msg
import com.rubyhuntersky.tomedb.Owner
import com.rubyhuntersky.tomedb.tomicOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun main() {
    val dir = File("data", "notebook").apply { println("Running with data in: $absoluteFile") }
    val tomic = tomicOf(dir) { emptyList() }
    runBlocking {
        val mdls = Channel<Mdl>(10)
        val actor = actor<Msg> {
            val story = NotesStory(tomic)
            var mdl = story.init().also { mdls.send(it) }
            loop@ for (msg in channel) {
                story.update(mdl, msg)?.let { mdl = it }
                mdls.send(mdl)
            }
        }
        printMdls(mdls, actor)
    }
}

@ExperimentalCoroutinesApi
private suspend fun printMdls(mdls: Channel<Mdl>, actor: SendChannel<Msg>) {
    NotesPrinter.printSessionHeader()
    loop@ while (!mdls.isClosedForReceive) {
        val mdl = mdls.receive()
        NotesPrinter.printScreenHeader()
        val entities = mdl.notes.toList()
        if (entities.isEmpty()) {
            NotesPrinter.printEmptyNotes()
        } else {
            entities.forEachIndexed { index, entity ->
                val number = index + 1
                val date = entity[Note.CREATED]!!
                val text = entity[Note.TEXT]!!
                NotesPrinter.printNote(number, date, text)
            }
        }
        NotesPrinter.printScreenFooterAndPrompt()
        if (!sendMsg(entities, actor)) break@loop
    }
    NotesPrinter.printSessionFooter()
}

private tailrec fun sendMsg(entities: List<Owner<Date>>, actor: SendChannel<Msg>): Boolean {
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
                Msg.REVISE(key = entities[index][Note.CREATED]!!, text = text)
            } ?: Msg.LIST
            actor.offer(msg)
        }
        userLine.startsWith("drop", true) -> {
            val number = userLine.substring("drop".length).trim()
            val index = number.toIntOrNull()?.let { it - 1 }
            val msg = index?.let { Msg.DROP(key = entities[index][Note.CREATED]!!) } ?: Msg.LIST
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

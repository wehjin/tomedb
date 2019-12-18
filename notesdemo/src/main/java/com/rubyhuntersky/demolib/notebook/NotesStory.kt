package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.*
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random


class NotesStory(private val tomic: Tomic) {

    data class Mdl(val notes: List<Owner<Date>>)

    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
        data class REVISE(val key: Date, val text: String) : Msg()
        data class DROP(val key: Date) : Msg()
    }

    fun init(): Mdl = Mdl(notes = tomic.ownerList(Note.CREATED))

    fun update(mdl: Mdl, msg: Msg): Mdl? = when (msg) {
        is Msg.LIST -> null
        is Msg.ADD -> {
            val today = Date()
            val text = if (msg.text.isNotBlank()) msg.text else "Today is $today"
            val ent = Random.nextLong().absoluteValue
            tomic.write(mods = modEnt(ent) {
                Note.CREATED set today
                Note.TEXT set text
            })
            mdl.copy(notes = tomic.ownerList(Note.CREATED))
        }
        is Msg.REVISE -> {
            tomic.modOwnersOf(Note.CREATED) {
                val target = owners.values.firstOrNull { it[Note.CREATED] == msg.key }
                target?.let { owner ->
                    mods = owner.mod { Note.TEXT set msg.text }
                }
                mdl.copy(notes = ownerList)
            }
        }
        is Msg.DROP -> {
            tomic.modOwnersOf(Note.CREATED) {
                val target = owners.values.firstOrNull { it[Note.CREATED] == msg.key }
                target?.let { owner ->
                    mods = owner.mod { Note.CREATED set null }
                }
                mdl.copy(notes = ownerList)
            }
        }
    }
}
package com.rubyhuntersky.demolib.notebook

import java.util.*

object NotesPrinter {
    fun printSessionHeader() {
        println("Notebook!")
        println("=========")
    }

    fun printScreenHeader() {
        println()
    }

    fun printNote(number: Int, date: Date?, text: String?) {
        if (number > 1) println()
        println("($number)")
        println("Created: $date")
        println("Note: $text")
    }

    fun printEmptyNotes() {
        println("[ EMPTY ]")
    }

    fun printScreenFooterAndPrompt() {
        print("\n> ")
    }

    fun printSessionFooter() {
        println("\nUser has left the building.")
    }
}
package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.*
import java.util.*

object Note : AttributeGroup {

    object CREATED : AttributeInObject<Date>() {
        override val description = "The instant a note was created."
        override val scriber: Scriber<Date> = DateScriber
    }

    object TEXT : AttributeInObject<String>() {
        override val description = "The text of the note."
        override val scriber: Scriber<String> = StringScriber
    }
}
package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.AttributeInObject
import com.rubyhuntersky.tomedb.attributes.LongScriber
import com.rubyhuntersky.tomedb.attributes.Scriber

sealed class Counter<T : Any> : AttributeInObject<T>() {

    companion object : AttributeGroup {
        fun attrs() = arrayOf(Count2)
    }

    object Count2 : Counter<Long>() {
        override val description = "The current count of a counter"
        override val scriber: Scriber<Long> = LongScriber
    }
}
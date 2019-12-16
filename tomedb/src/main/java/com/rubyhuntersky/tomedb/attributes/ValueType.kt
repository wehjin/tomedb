package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword
import java.util.*

sealed class ValueType<T : Any> {
    object BOOLEAN : ValueType<Boolean>()
    object LONG : ValueType<Long>()
    object STRING : ValueType<String>()
    object KEYWORD : ValueType<Keyword>()
    object INSTANT : ValueType<Date>()

    val keyword by lazy { Keyword(this::class.java.simpleName, ValueType::class.java.simpleName) }
}
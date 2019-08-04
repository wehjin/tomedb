package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.AnyValue
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import java.math.BigDecimal
import java.util.*

enum class ValueType(val typeId: Int) : Keyword {
    BOOLEAN(1),
    LONG(2),
    STRING(3),
    ATTR(4),
    INSTANT(5),
    DOUBLE(6),
    BIGDEC(7),
    VALUE(8),
    DATA(9);

    fun <T> toValueClass(): Class<T> {
        @Suppress("UNCHECKED_CAST")
        return when (this) {
            BOOLEAN -> Boolean::class.java
            LONG -> Long::class.java
            STRING -> String::class.java
            ATTR -> Keyword::class.java
            INSTANT -> Date::class.java
            DOUBLE -> Double::class.java
            BIGDEC -> BigDecimal::class.java
            VALUE -> AnyValue::class.java
            DATA -> TagList::class.java
        } as Class<T>
    }
}
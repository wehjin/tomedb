package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword

enum class ValueType {
    BOOLEAN,
    LONG,
    STRING,
    ATTR,
    INSTANT,
    DOUBLE,
    BIGDEC,
    VALUE,
    DATA;

    val keyword: Keyword
            by lazy { Keyword(this.name, ValueType::class.java.simpleName) }
}
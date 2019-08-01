package com.rubyhuntersky.tomedb.basics

enum class ValueType(val typeId: Int) : Attr {
    BOOLEAN(1),
    LONG(2),
    STRING(3),
    NAME(4),
    INSTANT(5),
    DOUBLE(6),
    BIGDEC(7),
    VALUE(8),
    DATA(9),
}
package com.rubyhuntersky.tomedb.basics

enum class ValueType(val typeId: Int) : NamedItem {
    REF(1),
    NAME(2),
    INSTANT(3),
    BOOLEAN(4),
    STRING(5),
    LONG(6),
    DOUBLE(7),
    BIGDEC(8),
    VALUE(9),
    DATA(10),
}
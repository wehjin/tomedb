package com.rubyhuntersky.tomedb

sealed class Rule {
    data class EExactA(val entityVar: String, val attribute: Enum<*>) : Rule()
    data class EExactVA(val entityVar: String, val value: Value, val attribute: Enum<*>) : Rule()
    data class EEExactA(val startVar: String, val endVar: String, val attribute: Enum<*>) : Rule()
    data class EVExactA(val entityVar: String, val valueVar: String, val attribute: Enum<*>) : Rule()
}

data class Input(val label: String, val value: Value) {
    constructor(label: String, long: Long) : this(label, Value.LONG(long))
}

sealed class Query {
    data class Find(val rules: List<Rule>, val inputs: List<Input>? = null, val outputs: List<String>) : Query()
}

sealed class Solutions<T> {
    class None<T> : Solutions<T>() {
        override fun toList(): List<T> = emptyList()
    }

    data class One<T>(val item: T) : Solutions<T>() {
        override fun toList(): List<T> = listOf(item)
    }

    data class Some<T>(val items: List<T>) : Solutions<T>() {
        override fun toList(): List<T> = items
    }

    class Any<T> : Solutions<T>() {
        override fun toList(): List<T> = throw Exception("Solutions unknown")
    }

    abstract fun toList(): List<T>

    fun toList(allOptions: () -> List<T>): List<T> =
        if (this is Any) {
            allOptions.invoke()
        } else {
            this.toList()
        }.toSet().toList()

    companion object {
        fun <T> fromList(list: List<T>): Solutions<T> {
            val set = list.toSet()
            return when (set.size) {
                0 -> None()
                1 -> One(set.first())
                else -> Some(set.toList())
            }
        }
    }
}

interface Attribute {
    val attrName: AttrName
        get() = (this as? Enum<*>)?.toAttrName()
            ?: throw NotImplementedError("Attribute::attrName")
    val valueType: ValueType
    val cardinality: Cardinality
    val description: String
}

enum class ValueType {
    REF,
    ATTRNAME,
    DATE,
    BOOLEAN,
    STRING,
    LONG,
    DOUBLE,
    BIGDEC,
    VALUE,
    DATA,
}

enum class Cardinality {
    ONE,
    MANY
}

enum class Scheme {
    NAME,
    VALUETYPE,
    CARDINALITY,
    DESCRIPTION
}

fun Enum<*>.toAttrName(): AttrName = AttrName(this::class.java.simpleName, this.name)

internal fun Input.toBinder(): Binder<*> = when (value) {
    is Value.LONG -> Binder(label, { listOf(value.v) }, Value::LONG, Solutions.One(value.v))
    is Value.REF -> Binder(label, { listOf(value.v) }, Value::REF, Solutions.One(value.v))
    is Value.ATTRNAME -> Binder(label, { listOf(value.v) }, Value::ATTRNAME, Solutions.One(value.v))
    is Value.DATE -> Binder(label, { listOf(value.v) }, Value::DATE, Solutions.One(value.v))
    is Value.BOOLEAN -> Binder(label, { listOf(value.v) }, Value::BOOLEAN, Solutions.One(value.v))
    is Value.STRING -> Binder(label, { listOf(value.v) }, Value::STRING, Solutions.One(value.v))
    is Value.DOUBLE -> Binder(label, { listOf(value.v) }, Value::DOUBLE, Solutions.One(value.v))
    is Value.BIGDEC -> Binder(label, { listOf(value.v) }, Value::BIGDEC, Solutions.One(value.v))
    is Value.VALUE -> Binder(label, { listOf(value.v) }, Value::VALUE, Solutions.One(value.v))
    is Value.DATA -> Binder(label, { listOf(value.v) }, Value::DATA, Solutions.One(value.v))
}

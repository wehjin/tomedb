package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.AttributeInObject
import com.rubyhuntersky.tomedb.attributes.DateScriber
import com.rubyhuntersky.tomedb.attributes.Scriber
import java.util.*

data class Amount(val x: Int) {
    operator fun plus(other: Amount) = Amount(x + other.x)
}

object AmountScriber : Scriber<Amount> {
    override val emptyScript: String = 0.toString()
    override fun scribe(quant: Amount): String = quant.x.toString()
    override fun unscribe(script: String): Amount = Amount(script.toInt())
}

object Wallet {
    object Dollars : AttributeInObject<Amount>() {
        override val description = "A property of an owner"
        override val scriber: Scriber<Amount> = AmountScriber
    }

    object CreationTime : AttributeInObject<Date>() {
        override val description: String = "Time of creation"
        override val scriber: Scriber<Date> = DateScriber
    }
}

fun startTome(name: String, group: String) = tomicOf(
    dir = createTempDir("$name-", ".$group").also { println("Location: $it") },
    init = { emptyList() }
)

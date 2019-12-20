package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.AttributeInObject
import com.rubyhuntersky.tomedb.attributes.DateScriber
import com.rubyhuntersky.tomedb.attributes.EntScriber
import com.rubyhuntersky.tomedb.attributes.Scriber
import com.rubyhuntersky.tomedb.basics.Ent
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

    object Owner : AttributeInObject<Ent>() {
        override val description: String = "The owner of the wallet"
        override val scriber: Scriber<Ent> = EntScriber
    }
}

fun startTome(name: String, group: String) = tomicOf(
    dir = createTempDir("$name-", ".$group").also { println("Location: $it") },
    init = { emptyList() }
)

open class TomeTest(private val group: String) {
    protected fun startTome(name: String) = startTome(name, group)
}
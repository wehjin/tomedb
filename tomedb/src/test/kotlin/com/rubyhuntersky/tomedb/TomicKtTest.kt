package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.*
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.absoluteValue
import kotlin.random.Random

class TomicKtTest {
    data class Amount(val x: Int)

    object Wallet {
        object Dollars : Attribute2<Amount> {
            override val description = "A property of an owner"
            override val scriber: Scriber<Amount> = object : Scriber<Amount> {
                override fun scribe(quant: Amount): String = quant.x.toString()
                override fun unscribe(script: String): Amount = Amount(script.toInt())
            }
            override val itemName: String get() = fallbackItemName
            override val groupName: String get() = fallbackGroupName
        }
    }

    @Test
    fun main() {
        val dir = createTempDir("tomicTest").also { println("Location: $it") }
        val tomic = tomicOf<Unit>(dir) { emptyList() }
        val ent = Random.nextLong().absoluteValue
        tomic.write(ent) {
            bind(Wallet.Dollars, Amount(100))
        }
        val reading = tomic.readLatest().getUntypedDbValue(ent, Wallet.Dollars.toKeyword())
        assertEquals(100, reading)
    }
}
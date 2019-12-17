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
            override val valueType: ValueType<String> = ValueType.STRING
            override val cardinality: Cardinality = Cardinality.ONE
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

        // Construct entity
        val ent = Random.nextLong().absoluteValue
        modEnt(ent) { Wallet.Dollars set Amount(100) }.also { tomic.write(it) }

        // Entity has initial value
        val db1 = tomic.getDb()
        val owner1 = db1.getOwners(Wallet.Dollars)
            .also { assertEquals(1, it.size) }
            .first().also { assertEquals(Amount(100), it[Wallet.Dollars]) }

        // Modify entity
        owner1.getMods { Wallet.Dollars set Amount(200) }.also {
            tomic.write(it)
        }

        // Entity has changed value
        tomic.getDb().getOwners(Wallet.Dollars)
            .also { assertEquals(1, it.size) }
            .first().also { assertEquals(Amount(200), it[Wallet.Dollars]) }

        // Entity in first db remains unchanged
        db1.getOwners(Wallet.Dollars).also { assertEquals(1, it.size) }
            .first().also { assertEquals(Amount(100), it[Wallet.Dollars]) }
    }
}
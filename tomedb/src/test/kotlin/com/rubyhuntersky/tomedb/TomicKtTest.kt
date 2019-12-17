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

    private fun startTome() = tomicOf<Unit>(
        dir = createTempDir("tomicTest").also { println("Location: $it") },
        init = { emptyList() }
    )

    @Test
    fun sequence() {
        val tome = startTome()
        tome.write(listOf(
            modEnt(ent = Random.nextLong().absoluteValue) { Wallet.Dollars set Amount(100) },
            modEnt(ent = Random.nextLong().absoluteValue) { Wallet.Dollars set Amount(200) }
        ).flatten())

        val amounts = sequenceOwners(tome, Wallet.Dollars).map { it[Wallet.Dollars]!! }
        assertEquals(setOf(Amount(100), Amount(200)), amounts.toSet())
    }

    @Test
    fun main() {
        val tome = startTome()
        val ent = Random.nextLong().absoluteValue
        modEnt(ent) { Wallet.Dollars set Amount(100) }.also { tome.write(it) }

        // Entity has initial value
        val hive = tome.collectOwners(Wallet.Dollars) {
            assertEquals(1, owners.size)
            assertEquals(Amount(100), first[Wallet.Dollars])
            mods = first.mod { Wallet.Dollars set Amount(200) }
        }

        // Entity in latest hive has changed value
        val dollars = tome.visitOwners(Wallet.Dollars) {
            assertEquals(1, owners.size)
            first[Wallet.Dollars]
        }
        assertEquals(Amount(200), dollars)

        // Entity in original hive remains unchanged
        hive.visit {
            assertEquals(1, owners.size)
            assertEquals(Amount(100), first[Wallet.Dollars])
        }
    }
}
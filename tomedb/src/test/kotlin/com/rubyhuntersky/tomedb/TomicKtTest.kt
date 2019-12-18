package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.AttributeInObject
import com.rubyhuntersky.tomedb.attributes.DateScriber
import com.rubyhuntersky.tomedb.attributes.Scriber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random

class TomicKtTest {
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

    private fun startTome(name: String) = tomicOf(
        dir = createTempDir("$name-", ".tomicTest").also { println("Location: $it") },
        init = { emptyList() }
    )

    @Test
    fun crud() {
        val tome = startTome("crud")

        val create = modEnt {
            Wallet.Dollars set Amount(1)
            Wallet.CreationTime set Date()
        }
        tome.write(create)

        val read = tome.ownersOf(Wallet.CreationTime).visit {
            owners.values.map { Pair(it.ent, it[Wallet.Dollars] ?: Amount(0)) }
        }
        assertEquals(setOf(Amount(1)), read.map { it.second }.toSet())

        val update = read.first().let { Pair(it.first, it.second + Amount(1)) }
        tome.modOwnersOf(Wallet.CreationTime) {
            val (ent, amount) = update
            val old = owners[ent] ?: error("No ent in owners")
            mods = old.mod { Wallet.Dollars set amount }
            val new = owners[ent] ?: error("No ent in owners")
            assertEquals(amount, new[Wallet.Dollars])
        }
        tome.visitOwnersOf(Wallet.CreationTime) {
            val updated = owners[update.first] ?: error("No ent in owners")
            assertEquals(update.second, updated[Wallet.Dollars])
        }

        val delete = update.first
        tome.modOwnersOf(Wallet.CreationTime) {
            val owner = owners[delete] ?: error("No ent in owner")
            mods = owner.mod { Wallet.CreationTime set null }
            assertNull(owners[delete])
        }
        tome.visitOwnersOf(Wallet.CreationTime) { assertNull(owners[delete]) }
    }

    @Test
    fun immutable() {
        val tome = startTome("immutable")
        val ent = Random.nextLong().absoluteValue
        modEnt(ent) { Wallet.Dollars set Amount(100) }.also { tome.write(it) }
        val oldWallets = tome.ownersOf(Wallet.Dollars)
        // Collective contains value
        oldWallets.visit {
            assertEquals(1, owners.size)
            assertEquals(Amount(100), any!![Wallet.Dollars])
        }

        // Modify entity
        val newAmount = tome.modOwnersOf(Wallet.Dollars) {
            assertEquals(1, owners.size)
            assertEquals(Amount(100), any!![Wallet.Dollars])
            mods = any!!.mod { Wallet.Dollars set Amount(200) }
            assertEquals(1, owners.size)
            any!![Wallet.Dollars]
        }
        // New collective contains new value
        assertEquals(Amount(200), newAmount)
        tome.visitOwnersOf(Wallet.Dollars) {
            assertEquals(1, owners.size)
            assertEquals(Amount(200), any!![Wallet.Dollars])
        }

        // Old collective remains unchanged
        oldWallets.visit {
            assertEquals(1, owners.size)
            assertEquals(Amount(100), any!![Wallet.Dollars])
        }
    }
}
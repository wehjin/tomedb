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
    fun anyWithKey() {
        val tome = startTome("anyWithKey")
        val now = Date(1350)
        tome.write(mods = modEnt {
            Wallet.Dollars set Amount(1)
            Wallet.CreationTime set now
        })

        val dollars = tome.visitPeers(Wallet.CreationTime) {
            val wallet = peersByBadge[now]
            wallet!![Wallet.Dollars]
        }
        assertEquals(Amount(1), dollars)
    }

    @Test
    fun crud() {
        val tome = startTome("crud")

        val create = modEnt {
            Wallet.Dollars set Amount(1)
            Wallet.CreationTime set Date()
        }
        tome.write(create)

        val read = tome.visitPeers(Wallet.CreationTime) {
            peersByEnt.values.map { Pair(it.ent, it[Wallet.Dollars] ?: Amount(0)) }
        }
        assertEquals(setOf(Amount(1)), read.map { it.second }.toSet())

        val update = read.first().let { Pair(it.first, it.second + Amount(1)) }
        tome.modPeers(Wallet.CreationTime) {
            val (ent, amount) = update
            val old = peersByEnt[ent] ?: error("No ent in owners")
            mods = old.mod { Wallet.Dollars set amount }
            val new = peersByEnt[ent] ?: error("No ent in owners")
            assertEquals(amount, new[Wallet.Dollars])
        }
        tome.visitPeers(Wallet.CreationTime) {
            val updated = peersByEnt[update.first] ?: error("No ent in owners")
            assertEquals(update.second, updated[Wallet.Dollars])
        }

        val delete = update.first
        tome.modPeers(Wallet.CreationTime) {
            val owner = peersByEnt[delete] ?: error("No ent in owner")
            mods = owner.mod { Wallet.CreationTime set null }
            assertNull(peersByEnt[delete])
        }
        tome.visitPeers(Wallet.CreationTime) { assertNull(peersByEnt[delete]) }
    }

    @Test
    fun immutable() {
        val tome = startTome("immutable")
        val ent = Random.nextLong().absoluteValue
        modEnt(ent) { Wallet.Dollars set Amount(100) }.also { tome.write(it) }
        val oldWallets = tome.collectPeers(Wallet.Dollars)
        // Collective contains value
        oldWallets.visit {
            assertEquals(1, peersByEnt.size)
            assertEquals(Amount(100), peerOrNull!![Wallet.Dollars])
        }

        // Modify entity
        val newAmount = tome.modPeers(Wallet.Dollars) {
            assertEquals(1, peersByEnt.size)
            assertEquals(Amount(100), peerOrNull!![Wallet.Dollars])
            mods = peerOrNull!!.mod { Wallet.Dollars set Amount(200) }
            assertEquals(1, peersByEnt.size)
            peerOrNull!![Wallet.Dollars]
        }
        // New collective contains new value
        assertEquals(Amount(200), newAmount)
        tome.visitPeers(Wallet.Dollars) {
            assertEquals(1, peersByEnt.size)
            assertEquals(Amount(200), peerOrNull!![Wallet.Dollars])
        }

        // Old collective remains unchanged
        oldWallets.visit {
            assertEquals(1, peersByEnt.size)
            assertEquals(Amount(100), peerOrNull!![Wallet.Dollars])
        }
    }
}
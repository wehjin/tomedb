package com.rubyhuntersky.tomedb

import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random

class PeerKtTest {
    private fun startTome(name: String) = startTome(name, "peerTest")

    @Test
    fun anyWithKey() {
        val tome = startTome("anyWithKey")
        val now = Date(1350)
        tome.write(forms = reformEnt {
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

        val create = tome.reformPeers(Wallet.CreationTime) {
            val badge = Date()
            reforms = formPeer(badge) { Wallet.Dollars set Amount(1) }
            peersByBadge[badge]
        }
        assertNotNull(create)

        val read = tome.visitPeers(Wallet.CreationTime) {
            peersByEnt.values.map {
                val id = it.ent
                val amount = it[Wallet.Dollars] ?: Amount(0)
                Pair(id, amount)
            }.toSet()
        }
        assertEquals(setOf(Amount(1)), read.map { it.second }.toSet())

        val update = read.first().let { (id, amount) ->
            Pair(id, amount + Amount(1))
        }
        tome.reformPeers(Wallet.CreationTime) {
            val (ent, amount) = update
            val old = peersByEnt[ent] ?: error("No ent in owners")
            reforms = old.reform { Wallet.Dollars set amount }
            val new = peersByEnt[ent] ?: error("No ent in owners")
            assertEquals(amount, new[Wallet.Dollars])
        }
        tome.visitPeers(Wallet.CreationTime) {
            val updated = peersByEnt[update.first] ?: error("No ent in owners")
            assertEquals(update.second, updated[Wallet.Dollars])
        }

        val delete = update.first
        tome.reformPeers(Wallet.CreationTime) {
            val owner = peersByEnt[delete] ?: error("No ent in owner")
            reforms = owner.reform { Wallet.CreationTime set null }
            assertNull(peersByEnt[delete])
        }
        tome.visitPeers(Wallet.CreationTime) { assertNull(peersByEnt[delete]) }
    }

    @Test
    fun immutable() {
        val tome = startTome("immutable")
        val ent = Random.nextLong().absoluteValue
        reformEnt(ent) { Wallet.Dollars set Amount(100) }.also { tome.write(it) }
        val oldWallets = tome.collectPeers(Wallet.Dollars)
        // Collective contains value
        oldWallets.visit {
            assertEquals(1, peersByEnt.size)
            assertEquals(Amount(100), peerOrNull!![Wallet.Dollars])
        }

        // Modify entity
        val newAmount = tome.reformPeers(Wallet.Dollars) {
            assertEquals(1, peersByEnt.size)
            assertEquals(Amount(100), peerOrNull!![Wallet.Dollars])
            reforms = peerOrNull!!.reform { Wallet.Dollars set Amount(200) }
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
package com.rubyhuntersky.tomedb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random

class PeerKtTest : TomeTest("peerTest") {

    @Test
    fun anyWithKey() {
        val tome = startTome("anyWithKey")
        val now = Date(1350)
        val ent = Random.nextLong().absoluteValue
        tome.write(reforms = reformEnt(ent) {
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

        val createDate = Date()
        val create = tome.reformPeers(Wallet.CreationTime) {
            reforms = formPeer(createDate) { Wallet.Dollars set Amount(1) }
            peer(createDate)
        }
        assertEquals(createDate, create.badge.quant)

        val read = tome.visitPeers(Wallet.CreationTime) {
            peers.map {
                val badge = it.badge.quant
                val amount = it[Wallet.Dollars] ?: Amount(0)
                Pair(badge, amount)
            }.toSet()
        }
        assertEquals(setOf(Amount(1)), read.map { (_, amount) -> amount }.toSet())

        val (picked, pickedAmount) = read.first()
        val update = tome.reformPeers(Wallet.CreationTime) {
            reforms = peer(picked).reform { Wallet.Dollars set pickedAmount + Amount(1) }
            peer(picked)[Wallet.Dollars]
        }
        assertEquals(Amount(2), update)

        val delete = tome.reformPeers(Wallet.CreationTime) {
            reforms = peer(picked).unform
            peerOrNull(picked)
        }
        assertNull(delete)
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
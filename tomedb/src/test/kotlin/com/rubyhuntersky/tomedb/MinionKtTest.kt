package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.minion.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MinionKtTest : TomeTest("minionTest") {

    @Test
    fun minions() {
        val tome = startTome("crud")
        val leader = Leader(1000, Wallet.Owner)
        tome.reformMinions(leader) {
            reforms = listOf(
                formMinion { Wallet.Dollars set Amount(1) },
                formMinion { Wallet.Dollars set Amount(1) }
            ).flatten()
        }
        val minions = tome.latest.minions(leader)
        assertEquals(2, minions.size)
    }

    @Test
    fun crud() {
        val tome = startTome("crud")
        val leader = Leader(1000, Wallet.Owner)

        val created = tome.formMinion(leader) { Wallet.Dollars set Amount(1) }
        assertEquals(leader, created.leader)

        val read = tome.latest.minionOrNull(leader, created.ent) ?: error("Read failed")
        assertEquals(created.ent, read.ent)
        assertEquals(Amount(1), read[Wallet.Dollars])

        val updated = tome.reformMinion(leader, read.ent) {
            val amount = minion[Wallet.Dollars] ?: error("No amount in wallet")
            Wallet.Dollars set amount + Amount(4)
        }
        assertEquals(created.ent, updated.ent)
        assertEquals(Amount(5), updated[Wallet.Dollars])
        assertEquals(Amount(5), tome.latest[leader, updated.ent, Wallet.Dollars])

        val deleted = tome.unformMinion(leader, updated.ent)
        assertEquals(created.ent, deleted!!.ent)
        assertNull(tome.latest[leader, updated.ent])
    }
}

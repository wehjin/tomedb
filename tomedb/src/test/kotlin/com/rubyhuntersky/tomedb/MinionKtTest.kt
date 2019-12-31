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
    fun filter() {
        val tome = startTome("filter")
        val leader = Leader(1000, Wallet.Owner)
        tome.reformMinions(leader) {
            reforms = listOf(
                formMinion { Wallet.Dollars set Amount(1) },
                formMinion { Wallet.Yen set Amount(100) },
                formMinion { Wallet.Bitcoin set Amount(2) }
            ).flatten()
        }

        val yenLeader = Leader(
            ent = 1000,
            attr = Wallet.Owner,
            filter = BadgeFilter(Wallet.Yen)
        )
        val yenMinions = tome.latest.minions(yenLeader)
        assertEquals(1, yenMinions.size)
        assertEquals(Amount(100), yenMinions.first()[Wallet.Yen])

        val fiatLeader = Leader(
            ent = 1000,
            attr = Wallet.Owner,
            filter = anyOf(BadgeFilter(Wallet.Yen), BadgeFilter(Wallet.Dollars))
        )
        val fiatMinions = tome.latest.minions(fiatLeader)
        assertEquals(2, fiatMinions.size)
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

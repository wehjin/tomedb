package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.attrName
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker

fun Query.Find.Slot.project(results: List<Map<String, Any>>) = results.mapNotNull { it[slotName] }
operator fun Query.Find.Slot.invoke(rows: FindResult): List<Any> = rows(this)
infix fun Query.Find.Slot.has(attr: Keyword): Query.Find.Rule2.SlotAttr =
    Query.Find.Rule2.SlotAttr(this, attr)

infix fun Query.Find.Slot.has(attr: Attribute<*>): Query.Find.Rule2.SlotAttr =
    Query.Find.Rule2.SlotAttr(this, attr.attrName)

infix fun Query.Find.Slot.has(aSlot: Query.Find.Slot): Query.Find.SlotSlot =
    Query.Find.SlotSlot(this, aSlot)

operator fun Query.Find.Slot.unaryMinus() = Query.Find.Rule2.Slide(listOf(this.slotName))
operator fun Query.Find.Slot.unaryPlus() = Query.Find.Slip(this.slotName)

sealed class Query {

    @ScopeTagMarker
    class Find(block: Find.() -> Unit) : Query() {

        lateinit var rules: List<Rule2>

        init {
            block(this)
        }

        sealed class Rule2 {
            data class SlotSlotSlot(val eSlot: Slot, val aSlot: Slot, val vSlot: Slot) : Rule2()
            data class SlotAttrValue(val eSlot: Slot, val attr: Keyword, val value: Any) : Rule2()
            data class SlotAttrSlot(val eSlot: Slot, val attr: Keyword, val vSlot: Slot) : Rule2()
            data class SlotAttrESlot(val eSlot: Slot, val attr: Keyword, val eSlot2: ESlot) :
                Rule2()

            data class SlotAttr(val slot: Slot, val attr: Keyword) : Rule2() {
                infix fun eq(value: Any): SlotAttrValue = SlotAttrValue(this.slot, this.attr, value)
                infix fun eq(slotName: String): SlotAttrSlot = eq(CommonSlot(slotName))
                infix fun eq(slot: Slot): SlotAttrSlot = SlotAttrSlot(this.slot, this.attr, slot)
                infix fun eq(eSlot: ESlot): SlotAttrESlot =
                    SlotAttrESlot(this.slot, this.attr, eSlot)
            }


            data class SlipValue<T : Any>(val slip: Slip, val value: T) : Rule2()
            data class Slide(val keywordNames: List<String>) : Rule2() {
                infix fun and(keywordName: String): Slide = Slide(keywordNames + keywordName)
                infix fun and(slot: Slot): Slide = Slide(keywordNames + slot.slotName)
            }
        }

        data class SlotSlot(val eSlot: Slot, val aSlot: Slot) {
            infix fun eq(vSlot: Slot): Rule2.SlotSlotSlot = Rule2.SlotSlotSlot(eSlot, aSlot, vSlot)
        }

        data class Slip(val name: String) {
            infix fun <T : Any> put(value: T): Rule2.SlipValue<T> = Rule2.SlipValue(this, value)
        }


        interface Slot {
            val slotName: String
        }

        data class ESlot(override val slotName: String) : Slot {
            override fun toString(): String = "ESlot/$slotName"
        }

        infix fun String.has(attr: Attribute<*>): Rule2.SlotAttr =
            CommonSlot(this).has(attr.attrName)

        infix fun String.has(attr: Keyword): Rule2.SlotAttr = CommonSlot(this).has(attr)
        operator fun String.unaryPlus() = Slip(this)
        operator fun String.unaryMinus() = Rule2.Slide(listOf(this))
        operator fun String.not() = ESlot(this)

    }

    data class CommonSlot(override val slotName: String) : Find.Slot {
        override fun toString(): String = "Slot/$slotName"
    }

    companion object {
        fun build(init: Find.() -> Unit): Find = Find(init)
    }
}
package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Ent

/**
 * A title is a entity bound to a topic. It represents an entity
 * found to be described by the topic. When a tome is created from
 * a topic, the pages in the tome are indexed by title.
 */
sealed class PageTitle<KeyT : Any> {

    abstract val topic: TomeTopic<KeyT>
    abstract val dataEnt: Ent
    abstract val dataKey: KeyT

    data class Entity(val ent: Ent, override val topic: TomeTopic.Entity) : PageTitle<Ent>() {
        override val dataEnt: Ent get() = ent
        override val dataKey: Ent get() = ent
    }

    data class Child(val child: Ent, override val topic: TomeTopic.Parent) : PageTitle<Ent>() {
        override val dataEnt: Ent get() = child
        override val dataKey: Ent get() = child
    }

    data class TraitHolder<TraitT : Any>(
        val traitHolder: Ent,
        val traitValue: TraitT,
        override val topic: TomeTopic.Trait<TraitT>
    ) : PageTitle<TraitT>() {
        override val dataEnt: Ent get() = traitHolder
        override val dataKey: TraitT get() = traitValue
    }
}
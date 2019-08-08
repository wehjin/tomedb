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

    data class Entity(val ent: Ent, override val topic: TomeTopic.Entity) : PageTitle<Ent>() {
        override val dataEnt: Ent get() = ent
    }

    data class Child(val child: Ent, override val topic: TomeTopic.Parent) : PageTitle<Ent>() {
        override val dataEnt: Ent get() = child
    }

    data class TraitHolder<TraitT : Any>(
        val traitHolder: Ent,
        val traitValue: Any,
        override val topic: TomeTopic.Trait<TraitT>
    ) : PageTitle<TraitKey<TraitT>>() {
        override val dataEnt: Ent get() = traitHolder
    }
}
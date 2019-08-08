package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent


/**
 * A topic describes a set of entities in the database.
 */
sealed class TomeTopic<KeyT : Any> {

    abstract fun toSubject(key: KeyT): PageSubject<KeyT>

    /**
     * A single-entity topic.  Any tome generated from this topic will
     * contain a single page.
     */
    data class Entity(val ent: Ent) : TomeTopic<Ent>() {
        override fun toSubject(key: Ent): PageSubject.Entity {
            require(key == ent)
            return PageSubject.Entity(key)
        }
    }

    /**
     * A topic composed of entities with the same trait. For example, if
     * the trait is Citizen/DateOfBirth, then the topic describes all
     * citizen entities with a date-of-birth value.
     */
    data class Trait<TraitT : Any>(val attr: Attribute) : TomeTopic<TraitT>() {
        override fun toSubject(key: TraitT): PageSubject.TraitHolder<TraitT> {
            return PageSubject.TraitHolder(traitHolder = Ent.of(attr, key), traitValue = key, topic = this)
        }
    }

    /**
     * A topic composed of entities with a common parent. For example, if
     * the parent is (Norway, Citizen/Country), then the topic describes
     * all entities with a Norway value at Citizen/Country.
     */
    data class Leader(val leader: Ent, val childAttr: Attribute) : TomeTopic<Ent>() {
        override fun toSubject(key: Ent): PageSubject.Follower {
            return PageSubject.Follower(follower = key, topic = this)
        }
    }
}

package com.example.upload.global.entity

import com.example.upload.standard.util.Ut
import jakarta.persistence.*
import org.hibernate.Hibernate

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private var _id: Long? = null // TODO: 추후에 코틀린 전환 과정에서 해결

    var id: Long
        get() = _id ?: 0
        set(value) {
            _id = value
        }

    val modelName: String
        get() = Ut.str.lcfirst(this::class.simpleName!!)

    override fun equals(other: Any?): Boolean {

        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as BaseEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }

}

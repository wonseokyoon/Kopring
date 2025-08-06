package com.example.upload.global.entity

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import lombok.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(
    AuditingEntityListener::class
)
abstract class BaseTime : BaseEntity() {
    @CreatedDate
    lateinit var createdDate: LocalDateTime

    @LastModifiedDate
    lateinit var modifiedDate: LocalDateTime

    fun setCreateDateNow() {
        this.createdDate = LocalDateTime.now()
        this.modifiedDate = createdDate
    }
}
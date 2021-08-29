package study.querydsl.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Hello(
    @Id
    @GeneratedValue
    val id: Long? = null
)

package study.querydsl.repository

import org.springframework.data.jpa.repository.JpaRepository
import study.querydsl.entity.Member

interface MemberRepository : JpaRepository<Member, Long> {

    fun findByUsername(username: String): MutableList<Member>
}

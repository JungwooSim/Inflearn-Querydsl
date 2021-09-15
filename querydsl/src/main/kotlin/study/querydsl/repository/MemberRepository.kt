package study.querydsl.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import study.querydsl.entity.Member

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
    fun findByUsername(username: String): MutableList<Member>
}

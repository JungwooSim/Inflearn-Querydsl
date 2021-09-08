package study.querydsl.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import study.querydsl.entity.Member
import study.querydsl.entity.QMember.member
import java.util.*
import javax.persistence.EntityManager

@Repository
class MemberJpaRepository(val em: EntityManager) {
    val queryFactory: JPAQueryFactory = JPAQueryFactory(em)

    fun save(member: Member) {
        em.persist(member)
    }

    fun findById(id: Long): Optional<Member> {
        val findMember = em.find(Member::class.java, id)
        return Optional.ofNullable(findMember)
    }

    fun findAll(): MutableList<Member> {
        return em.createQuery("select m from Member m", Member::class.java).resultList
    }

    fun findByUsername(username: String): MutableList<Member> {
        return em.createQuery("select m from Member m where m.username = :username", Member::class.java)
            .setParameter("username", username)
            .resultList
    }

    fun findAll_Querydsl(): MutableList<Member> {
        return queryFactory
            .selectFrom(member)
            .fetch()
    }

    fun findByUsername_Querydsl(username: String): MutableList<Member> {
        return queryFactory
            .selectFrom(member)
            .where(member.username.eq(username))
            .fetch()
    }
}

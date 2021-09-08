package study.querydsl.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import org.springframework.util.StringUtils.hasText
import study.querydsl.dto.MemberSearchCondition
import study.querydsl.dto.MemberTeamDto
import study.querydsl.entity.Member
import study.querydsl.entity.QMember.member
import study.querydsl.entity.QTeam.team
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

    fun searchByBuilder(condition: MemberSearchCondition): MutableList<MemberTeamDto> {
        val builder: BooleanBuilder = BooleanBuilder()

        if (hasText(condition.username)) {
            builder.and(member.username.eq(condition.username))
        }

        if (hasText(condition.teamName)) {
            builder.and(team.name.eq(condition.teamName))
        }

        if (condition.ageGoe != null) {
            builder.and(member.age.goe(condition.ageGoe))
        }

        if (condition.ageLoe != null) {
            builder.and(member.age.loe(condition.ageLoe))
        }

        return queryFactory
            .select(
                Projections.constructor(MemberTeamDto::class.java,
                    member.id.`as`("memberId"),
                    member.username,
                    member.age,
                    team.id.`as`("teamId"),
                    team.name.`as`("teamName"),
                )
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(builder)
            .fetch()
    }
}

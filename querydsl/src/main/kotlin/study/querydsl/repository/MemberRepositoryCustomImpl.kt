package study.querydsl.repository

import com.querydsl.core.QueryResults
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.util.StringUtils
import study.querydsl.dto.MemberSearchCondition
import study.querydsl.dto.MemberTeamDto
import study.querydsl.entity.Member
import study.querydsl.entity.QMember
import study.querydsl.entity.QTeam
import javax.persistence.EntityManager

class MemberRepositoryCustomImpl(@Autowired private val em: EntityManager,) : MemberRepositoryCustom {
    val queryFactory: JPAQueryFactory = JPAQueryFactory(em)

    override fun search(condition: MemberSearchCondition): MutableList<MemberTeamDto> {
        return queryFactory
            .select(
                Projections.constructor(MemberTeamDto::class.java,
                    QMember.member.id.`as`("memberId"),
                    QMember.member.username,
                    QMember.member.age,
                    QTeam.team.id.`as`("teamId"),
                    QTeam.team.name.`as`("teamName"),
                )
            )
            .from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(
                usernameEq(condition?.username),
                ageLoe(condition?.ageLoe),
                ageGoe(condition?.ageGoe),
                teamNameEq(condition?.teamName),
            )
            .fetch()
    }

    override fun searchPageSimple(
        condition: MemberSearchCondition,
        pageable: Pageable
    ): Page<MemberTeamDto> {
        val results: QueryResults<MemberTeamDto> = queryFactory
            .select(
                Projections.constructor(
                    MemberTeamDto::class.java,
                    QMember.member.id.`as`("memberId"),
                    QMember.member.username,
                    QMember.member.age,
                    QTeam.team.id.`as`("teamId"),
                    QTeam.team.name.`as`("teamName"),
                )
            )
            .from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(
                usernameEq(condition?.username),
                ageLoe(condition?.ageLoe),
                ageGoe(condition?.ageGoe),
                teamNameEq(condition?.teamName),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetchResults()

        val content: MutableList<MemberTeamDto> = results.results
        val total: Long = results.total

        return PageImpl(content, pageable, total)
    }

    override fun searchPageComplex(
        condition: MemberSearchCondition,
        pageable: Pageable
    ): Page<MemberTeamDto> {
        val content: MutableList<MemberTeamDto> = queryFactory
            .select(
                Projections.constructor(
                    MemberTeamDto::class.java,
                    QMember.member.id.`as`("memberId"),
                    QMember.member.username,
                    QMember.member.age,
                    QTeam.team.id.`as`("teamId"),
                    QTeam.team.name.`as`("teamName"),
                )
            )
            .from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(
                usernameEq(condition?.username),
                ageLoe(condition?.ageLoe),
                ageGoe(condition?.ageGoe),
                teamNameEq(condition?.teamName),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery: JPAQuery<Member> = queryFactory
            .select(QMember.member)
            .from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(
                usernameEq(condition?.username),
                ageLoe(condition?.ageLoe),
                ageGoe(condition?.ageGoe),
                teamNameEq(condition?.teamName),
            )
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount)
    }

    private fun usernameEq(username: String?): BooleanExpression? {
        return if (StringUtils.hasText(username)) QMember.member.username.eq(username) else null
    }

    private fun teamNameEq(teamName: String?): BooleanExpression? {
        return if(StringUtils.hasText(teamName)) QTeam.team.name.eq(teamName) else null
    }

    private fun ageGoe(ageGoe: Int?): BooleanExpression? {
        return if(ageGoe != null) QMember.member.age.goe(ageGoe) else null
    }

    private fun ageLoe(ageLoe: Int?): BooleanExpression? {
        return if(ageLoe != null) QMember.member.age.loe(ageLoe) else null
    }
}

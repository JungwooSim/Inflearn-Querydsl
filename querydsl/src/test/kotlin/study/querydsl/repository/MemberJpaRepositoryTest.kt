package study.querydsl.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import study.querydsl.dto.MemberSearchCondition
import study.querydsl.dto.MemberTeamDto
import study.querydsl.entity.Member
import study.querydsl.entity.Team
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest(
    @Autowired private val em: EntityManager,
    @Autowired private val memberJpaRepository: MemberJpaRepository
) {
    @Test
    fun basicTest() {
        val member = Member(username = "member1", age = 10)
        memberJpaRepository.save(member)

        val findMember = memberJpaRepository.findById(member.id!!).get()
        assertThat(findMember).isEqualTo(member)

        val result1 = memberJpaRepository.findAll_Querydsl()
        assertThat(result1).containsExactly(member)

        val result2 = memberJpaRepository.findByUsername_Querydsl("member1")
        assertThat(result2).containsExactly(member)
    }

    @Test
    fun searchTest() {
        val teamA = Team(name = "teamA")
        val teamB = Team(name = "teamB")
        em.persist(teamA)
        em.persist(teamB)

        val member1: Member = Member(username = "member1", age = 10, team = teamA)
        val member2: Member = Member(username = "member2", age = 20, team = teamA)
        val member3: Member = Member(username = "member3", age = 30, team = teamB)
        val member4: Member = Member(username = "member4", age = 40, team = teamB)

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)

        val condition: MemberSearchCondition = MemberSearchCondition(ageGoe = 35, ageLoe = 40, teamName = "teamB")

        val result: MutableList<MemberTeamDto> = memberJpaRepository.searchByBuilder(condition)

        assertThat(result).extracting("username").containsExactly("member4")
    }
}





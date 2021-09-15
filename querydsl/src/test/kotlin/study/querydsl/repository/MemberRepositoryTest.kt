package study.querydsl.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import study.querydsl.dto.MemberSearchCondition
import study.querydsl.dto.MemberTeamDto
import study.querydsl.entity.Member
import study.querydsl.entity.QMember
import study.querydsl.entity.Team
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
class MemberRepositoryTest(
    @Autowired private val em: EntityManager,
    @Autowired private val memberRepository: MemberRepository) {

    @Test
    fun basicTest() {
        val member = Member(username = "member1", age = 10)
        memberRepository.save(member)

        val findMember = memberRepository.findById(member.id!!).get()
        Assertions.assertThat(findMember).isEqualTo(member)

        val result1 = memberRepository.findAll()
        Assertions.assertThat(result1).containsExactly(member)

        val result2 = memberRepository.findByUsername("member1")
        Assertions.assertThat(result2).containsExactly(member)
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

        val result: MutableList<MemberTeamDto> = memberRepository.search(condition)

        Assertions.assertThat(result).extracting("username").containsExactly("member4")
    }

    @Test
    fun searchPageSimple() {
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

        val condition: MemberSearchCondition = MemberSearchCondition()
        val pageRequest: PageRequest = PageRequest.of(0, 3)

        val result: Page<MemberTeamDto> = memberRepository.searchPageSimple(condition, pageRequest)

        Assertions.assertThat(result.size).isEqualTo(3)
        Assertions.assertThat(result.content).extracting("username").containsExactly("member1", "member2", "member3")
    }

    @Test
    fun querydslPredicateExecutorTest() {
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

        val member = QMember.member

        val result: Iterable<Member> = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")))

        for (findMember in result) {
            println("member1 = $findMember")
        }
    }
}

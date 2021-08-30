package study.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import study.querydsl.entity.Member
import study.querydsl.entity.QMember
import study.querydsl.entity.Team
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
class QuerydslBasicTest(
    @Autowired private val em: EntityManager) {

    lateinit var queryFactory: JPAQueryFactory

    @BeforeEach
    fun before() {
        queryFactory = JPAQueryFactory(em)

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
    }

    @Test
    fun startJPQL() {
        val qlString: String = "select m from Member m where m.username = :username";
        val findMember = em.createQuery(qlString, Member::class.java).setParameter("username", "member1").singleResult
        assertThat(findMember.username).isEqualTo("member1")
    }

    @Test
    fun startQuerydsl() {
        val m = QMember("m")
        val findMember = queryFactory
            .select(m)
            .from(m)
            .where(m.username.eq("member1"))
            .fetchOne()

        assertThat(findMember?.username).isEqualTo("member1")
    }
}

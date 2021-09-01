package study.querydsl

import com.querydsl.core.QueryResults
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import study.querydsl.entity.Member
import study.querydsl.entity.QMember.member
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
        val findMember = queryFactory
            .select(member)
            .from(member)
            .where(member.username.eq("member1"))
            .fetchOne()

        assertThat(findMember?.username).isEqualTo("member1")
    }

    @Test
    fun search() {
        val findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1")
                .and(member.age.between(10, 30)))
            .fetchOne();

        /*
            member.username.eq("member1") // username = 'member1'
            member.username.ne("member1") //username != 'member1'
            member.username.eq("member1").not() // username != 'member1'
            member.username.isNotNull() //이름이 is not null
            member.age.in(10, 20) // age in (10,20)
            member.age.notIn(10, 20) // age not in (10, 20)
            member.age.between(10,30) //between 10, 30
            member.age.goe(30) // age >= 30
            member.age.gt(30) // age > 30
            member.age.loe(30) // age <= 30
            member.age.lt(30) // age < 30
            member.username.like("member%") //like 검색
            member.username.contains("member") // like ‘%member%’ 검색
            member.username.startsWith("member") //like ‘member%’ 검색
         */

        assertThat(findMember?.username).isEqualTo("member1")
    }

    @Test
    fun searchAndParam() {
        val findMember = queryFactory
            .selectFrom(member)
            .where(
                member.username.eq("member1"),
                member.age.between(10, 30)
            )
            .fetchOne();

        assertThat(findMember?.username).isEqualTo("member1")
    }

    @Test
    fun resultFetch() {
        //List
        val fetch: MutableList<Member> = queryFactory
            .selectFrom(member)
            .fetch()

        //단 건
//        val fetchOne: Member? = queryFactory
//            .selectFrom(member)
//            .fetchOne()

        //처음 한 건 조회
        val fetchFirst: Member = queryFactory
            .selectFrom(member)
            .fetchFirst()

        //페이징에서 사용
        val results: QueryResults<Member> = queryFactory
            .selectFrom(member)
            .fetchResults()

        results.total
        val content: MutableList<Member> = results.results

        //count 쿼리
        val total: Long = queryFactory
            .selectFrom(member)
            .fetchCount()
    }

    @Test
    fun sort() {
        val teamA = Team(name = "teamA")
        em.persist(teamA)

        em.persist(Member(username = null, age = 100, team = teamA))
        em.persist(Member(username = "member5", age = 100, team = teamA))
        em.persist(Member(username = "member6", age = 100, team = teamA))

        val result: MutableList<Member> = queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch()

        val member5 = result[0]
        val member6 = result[1]
        val memberNull = result[2]

        assertThat(member5.username).isEqualTo("member5")
        assertThat(member6.username).isEqualTo("member6")
        assertThat(memberNull.username).isNull()
    }
}

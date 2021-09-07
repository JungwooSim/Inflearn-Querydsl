package study.querydsl

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.QueryResults
import com.querydsl.core.Tuple
import com.querydsl.core.types.ExpressionUtils
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import study.querydsl.dto.MemberDto
import study.querydsl.dto.QMemberDto
import study.querydsl.dto.UserDto
import study.querydsl.entity.Member
import study.querydsl.entity.QMember
import study.querydsl.entity.QMember.member
import study.querydsl.entity.QTeam.team
import study.querydsl.entity.Team
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceUnit

@SpringBootTest
@Transactional
class QuerydslBasicTest(
    @Autowired private val em: EntityManager) {

    lateinit var queryFactory: JPAQueryFactory

    @PersistenceUnit
    lateinit var emf: EntityManagerFactory

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

    /**
     *회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
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

    @Test
    fun paging1() {
        val result: MutableList<Member> = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetch()

        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun paging2() {
        val result: QueryResults<Member> = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults()

        assertThat(result.total).isEqualTo(4)
        assertThat(result.limit).isEqualTo(2)
        assertThat(result.offset).isEqualTo(1)
        assertThat(result.results.size).isEqualTo(2)
    }

    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이 * from Member m
     */
    @Test
    fun aggregation() {
        val result: MutableList<Tuple> = queryFactory
            .select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min()
            )
            .from(member)
            .fetch();

        val tuple = result[0]
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25.0);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    fun group() {
        val result: MutableList<Tuple> = queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .fetch()

        val teamA: Tuple = result[0]
        val teamB: Tuple = result[1]

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15.0);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35.0);
    }

    /**
     * 팀A에 소속된 모든 회원
     */
    @Test
    fun join() {
        val result: MutableList<Member> = queryFactory
            .selectFrom(member)
            .join(member.team, team)
//            .leftJoin(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch()

        assertThat(result)
            .extracting("username")
            .containsExactly("member1", "member2");
    }

    // error 발생중
    @Test
    fun theta_join() {
//        em.persist(Member(username = "teamA"))
//        em.persist(Member(username = "teatB"))
//        em.persist(Member(username = "teatC"))
//
//        val result: MutableList<Member> = queryFactory
//            .select(member)
//            .from(member, team)
//            .where(member.username.eq(team.name))
//            .fetch()
//
//        assertThat(result)
//            .extracting("username")
//            .containsExactly("teamA", "teamB");
    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'
     */
    @Test
    fun join_on_filtering() {
        val result: MutableList<Tuple> = queryFactory.select(member, team)
            .from(member)
            .leftJoin(member.team, team).on(team.name.eq("teamA"))
            .fetch()

        for (value in result) {
            println("tuple = $value")
        }
    }

    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    fun join_on_no_relation() {
        em.persist(Member(username = "teamA"))
        em.persist(Member(username = "teatB"))
        em.persist(Member(username = "teatC"))

        val result: MutableList<Tuple> = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team).on(member.username.eq(team.name))
            .fetch()

        for (value in result) {
            println("tuple = $value")
        }
    }

    @Test
    fun fetchJoinNo() {
        em.flush()
        em.clear()

        val findMember: Member = queryFactory
            .selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.username.eq("member1"))
            .fetchOne()!!

        val loaded = emf.persistenceUnitUtil.isLoaded(findMember.team)
        assertThat(loaded).`as`("패치 조인 적용").isTrue
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    fun subQuery() {
        val memberSub: QMember = QMember("memberSub")

        val result = queryFactory
            .selectFrom(member)
            .where(
                member.age.eq(
                    JPAExpressions
                        .select(memberSub.age.max())
                        .from(memberSub)
                )
            )
            .fetch()

        assertThat(result).extracting("age")
            .containsExactly(40);
    }

    /**
     * 나이가 평균 나이 이상인 회원
     */
    @Test
    fun subQueryGoe() {
        val memberSub: QMember = QMember("memberSub")

        val result = queryFactory
            .selectFrom(member)
            .where(
                member.age.goe(
                    JPAExpressions
                        .select(memberSub.age.avg())
                        .from(memberSub)
                )
            )
            .fetch()

        assertThat(result).extracting("age")
            .containsExactly(30, 40);
    }

    /**
     * 서브쿼리 여러 건 처리, in 사용
     */
    @Test
    fun subQueryIn() {
        val memberSub: QMember = QMember("memberSub")

        val result: MutableList<Member> = queryFactory
            .selectFrom(member)
            .where(
                member.age.`in`(
                    JPAExpressions
                        .select(memberSub.age)
                        .from(memberSub)
                        .where(memberSub.age.gt(10))
                )
            )
            .fetch()

        assertThat(result).extracting("age")
            .containsExactly(20, 30, 40);
    }

    /**
     * 서브쿼리 여러 건 처리, in 사용
     */
    @Test
    fun selectSubQuery() {
        val memberSub: QMember = QMember("memberSub")

        val result: MutableList<Tuple> = queryFactory
            .select(member.username, JPAExpressions.select(memberSub.age.avg()).from(memberSub))
            .from(member)
            .fetch()

        for (tuple in result) {
            println("username = " + tuple.get(member.username))
            println("age = " + tuple.get(JPAExpressions.select(memberSub.age.avg()).from(memberSub)))
        }
    }

    @Test
    fun basicCase() {
        val result: MutableList<String> = queryFactory
            .select(
                member.age
                    .`when`(10).then("열살")
                    .`when`(20).then("스무살")
                    .otherwise("기타")
            )
            .from(member)
            .fetch()

        for (value in result) {
            println("s = $value")
        }
    }

    @Test
    fun complexCase() {
        val result: MutableList<String> = queryFactory
            .select(
                CaseBuilder()
                    .`when`(member.age.between(0, 20)).then("0~20살")
                    .`when`(member.age.between(21, 30)).then("21~30살")
                    .otherwise("기타")
            )
            .from(member)
            .fetch()

        for (value in result) {
            println("s = $value")
        }
    }

    @Test
    fun constant() {
        val result: MutableList<Tuple> = queryFactory
            .select(member.username, Expressions.constant("A"))
            .from(member)
            .fetch();

        for (value in result) {
            println("s = $value")
        }
    }

    @Test
    fun concat() {
        val result: MutableList<String> = queryFactory
            .select(member.username.concat("_").concat(member.age.stringValue()))
            .from(member)
            .where(member.username.eq("member1"))
            .fetch();

        for (value in result) {
            println("s = $value")
        }
    }

    @Test
    fun simpleProjection() {
        val result: MutableList<String> = queryFactory
            .select(member.username)
            .from(member)
            .fetch()

        for (value in result) {
            println("s = $value")
        }
    }

    @Test
    fun tupleProjection() {
        val result: MutableList<Tuple> = queryFactory
            .select(member.username, member.age)
            .from(member)
            .fetch()

        for (tuple in result) {
            val username = tuple.get(member.username)
            val age = tuple.get(member.age)
            println("username = $username")
            println("age = $age")
        }
    }

    @Test
    fun findDtoByJPQL() {
        val result: MutableList<MemberDto> = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto::class.java).resultList

        for (value in result) {
            println("memberDto = $value")
        }
    }

    /**
     * Setter 를 이용하여 생성
     */
    @Test
    fun findDtoBySetter() {
        val result: MutableList<MemberDto> = queryFactory
            .select(Projections.bean(MemberDto::class.java, member.username, member.age))
            .from(member)
            .fetch()

        for (value in result) {
            println("memberDto = $value")
        }
    }

    /**
     * 이름으로 매칭하여 생성
     */
    @Test
    fun findDtoByField() {
        val result: MutableList<MemberDto> = queryFactory
            .select(Projections.fields(MemberDto::class.java, member.username, member.age))
            .from(member)
            .fetch()

        for (value in result) {
            println("memberDto = $value")
        }
    }

    /**
     * 생성자로 생성
     */
    @Test
    fun findDtoByConstructor() {
        val result: MutableList<MemberDto> = queryFactory
            .select(Projections.constructor(MemberDto::class.java, member.username, member.age))
            .from(member)
            .fetch()

        for (value in result) {
            println("memberDto = $value")
        }
    }

    /**
     * 별칭사용하여 이름으로 매칭 후 생성
     */
    @Test
    fun findUserDto() {
        val result: MutableList<UserDto> = queryFactory
            .select(Projections.fields(UserDto::class.java, member.username.`as`("name"), member.age))
            .from(member)
            .fetch()

        for (value in result) {
            println("UserDto = $value")
        }
    }

    /**
     * 별칭사용하여 이름으로 매칭 후 생성
     */
    @Test
    fun findUserDto2() {
        val memberSub: QMember = QMember("memberSub")

        val result: MutableList<UserDto> = queryFactory
            .select(Projections.fields(
                UserDto::class.java,
                member.username.`as`("name"),
                ExpressionUtils.`as`(JPAExpressions.select(memberSub.age.max()).from(memberSub), "age")
            ))
            .from(member)
            .fetch()

        for (value in result) {
            println("UserDto = $value")
        }
    }

    /**
     * @QueryProjection 활용
     */
    @Test
    fun findDtoByQueryProjection() {
        val result: MutableList<MemberDto> = queryFactory
            .select(QMemberDto(member.username, member.age))
            .from(member)
            .fetch()

        for (value in result) {
            println("MemberDto = $value")
        }
    }

    @Test
    fun dynamicQuery_BooleanBuilder() {
        val usernameParam = "member1"
        val ageParam = 10

        val result: MutableList<Member> = this.searchMember1(usernameParam, ageParam)

        assertThat(result.size).isEqualTo(1)
    }

    private fun searchMember1(usernameCond: String, ageCond: Int): MutableList<Member> {
        val builder: BooleanBuilder = BooleanBuilder()

        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond))
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond))
        }

        return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch()
    }
}

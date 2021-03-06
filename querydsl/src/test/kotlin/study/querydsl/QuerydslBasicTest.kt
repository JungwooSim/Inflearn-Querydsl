package study.querydsl

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.QueryResults
import com.querydsl.core.Tuple
import com.querydsl.core.types.ExpressionUtils
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
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
            member.username.isNotNull() //????????? is not null
            member.age.in(10, 20) // age in (10,20)
            member.age.notIn(10, 20) // age not in (10, 20)
            member.age.between(10,30) //between 10, 30
            member.age.goe(30) // age >= 30
            member.age.gt(30) // age > 30
            member.age.loe(30) // age <= 30
            member.age.lt(30) // age < 30
            member.username.like("member%") //like ??????
            member.username.contains("member") // like ???%member%??? ??????
            member.username.startsWith("member") //like ???member%??? ??????
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

        //??? ???
//        val fetchOne: Member? = queryFactory
//            .selectFrom(member)
//            .fetchOne()

        //?????? ??? ??? ??????
        val fetchFirst: Member = queryFactory
            .selectFrom(member)
            .fetchFirst()

        //??????????????? ??????
        val results: QueryResults<Member> = queryFactory
            .selectFrom(member)
            .fetchResults()

        results.total
        val content: MutableList<Member> = results.results

        //count ??????
        val total: Long = queryFactory
            .selectFrom(member)
            .fetchCount()
    }

    /**
     *?????? ?????? ??????
     * 1. ?????? ?????? ????????????(desc)
     * 2. ?????? ?????? ????????????(asc)
     * ??? 2?????? ?????? ????????? ????????? ???????????? ??????(nulls last)
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
     * COUNT(m), //?????????
     * SUM(m.age), //?????? ???
     * AVG(m.age), //?????? ??????
     * MAX(m.age), //?????? ??????
     * MIN(m.age) //?????? ?????? * from Member m
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
     * ?????? ????????? ??? ?????? ?????? ????????? ?????????.
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
     * ???A??? ????????? ?????? ??????
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

    // error ?????????
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
     * ???) ????????? ?????? ???????????????, ??? ????????? teamA??? ?????? ??????, ????????? ?????? ??????
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
     * 2. ???????????? ?????? ????????? ?????? ??????
     * ???) ????????? ????????? ?????? ????????? ?????? ?????? ?????? ??????
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
        assertThat(loaded).`as`("?????? ?????? ??????").isTrue
    }

    /**
     * ????????? ?????? ?????? ?????? ??????
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
     * ????????? ?????? ?????? ????????? ??????
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
     * ???????????? ?????? ??? ??????, in ??????
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
     * ???????????? ?????? ??? ??????, in ??????
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
                    .`when`(10).then("??????")
                    .`when`(20).then("?????????")
                    .otherwise("??????")
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
                    .`when`(member.age.between(0, 20)).then("0~20???")
                    .`when`(member.age.between(21, 30)).then("21~30???")
                    .otherwise("??????")
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
     * Setter ??? ???????????? ??????
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
     * ???????????? ???????????? ??????
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
     * ???????????? ??????
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
     * ?????????????????? ???????????? ?????? ??? ??????
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
     * ?????????????????? ???????????? ?????? ??? ??????
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
     * @QueryProjection ??????
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

    /**
     * ?????? ?????? - BooleanBuilder ??????
     */
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

    /**
     * ?????? ?????? - Where ?????? ???????????? ??????
     */
    @Test
    fun dynamicQuery_WhereParam() {
        val usernameParam = "member1"
        val ageParam = null

        val result: MutableList<Member> = this.searchMember2(usernameParam, ageParam)

        assertThat(result.size).isEqualTo(1)
    }

    private fun searchMember2(usernameParam: String?, ageParam: Int?): MutableList<Member> {
        return queryFactory
            .selectFrom(member)
//            .where(usernameEq(usernameParam), ageEQ(ageParam))
            .where(allEq(usernameParam, ageParam))
            .fetch()
    }

    private fun usernameEq(usernameParam: String?): BooleanExpression? {
        return if (usernameParam != null) member.username.eq(usernameParam) else null
    }

    private fun ageEQ(ageParam: Int?): BooleanExpression? {
        return if (ageParam != null) member.age.eq(ageParam) else null
    }

    private fun allEq(usernameParam: String?, ageParam: Int?): BooleanExpression? {
        return usernameEq(usernameParam)?.and(ageEQ(ageParam))
    }

    /**
     * ?????? ??????
     */
    @Test
    fun bulkUpdate() {
        val count: Long = queryFactory
            .update(member)
            .set(member.username, "?????????")
            .where(member.age.lt(28))
            .execute()

        em.flush()
        em.clear()

        val result = queryFactory
            .selectFrom(member)
            .fetch()

        for (value in result) {
            println("member = $value")
        }
    }

    /**
     * ?????? ?????????
     */
    @Test
    fun bulkAdd() {
        val count: Long = queryFactory
            .update(member)
            .set(member.age, member.age.add(1))
            .execute()
    }

    /**
     * ?????? ??????
     */
    @Test
    fun bulkDelete() {
        val count = queryFactory
            .delete(member)
            .where(member.age.gt(18))
            .execute()
    }

    /**
     * SQL function ??????
     */
    @Test
    fun sqlFunction() {
        val result: MutableList<String> = queryFactory
            .select(
                Expressions.stringTemplate(
                    "function('replace', {0}, {1}, {2})",
                    member.username, "member", "M"
                )
            )
            .from(member)
            .fetch()

        for (value in result) {
            println("s = $value")
        }
    }

    @Test
    fun sqlFunction2() {
        val result: MutableList<String> = queryFactory
            .select(member.username)
            .from(member)
            .where(member.username.eq(member.username.lower()))
            .fetch()

        for (value in result) {
            println("s = $value")
        }
    }
}

package study.querydsl.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import study.querydsl.entity.Member
import study.querydsl.entity.Team
import javax.annotation.PostConstruct
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Profile("local")
@Component
class InitMember(@Autowired val initMemberService: InitMemberService) {

    @PostConstruct
    fun init() {
        initMemberService.init()
    }
}

@Component
class InitMemberService(@PersistenceContext private final val em: EntityManager) {

    @Transactional
    fun init() {
        val teamA = Team(name = "teamA")
        val teamB = Team(name = "teamB")
        em.persist(teamA)
        em.persist(teamB)

        for (i in 1..100) {
            val selectedTeam: Team = if (i % 2 == 0) teamA else teamB
            em.persist(Member(username = "member$i", age = i, team = selectedTeam))
        }
    }
}

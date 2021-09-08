package study.querydsl.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import study.querydsl.entity.Member
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
}





package study.querydsl.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import study.querydsl.entity.Member
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
}

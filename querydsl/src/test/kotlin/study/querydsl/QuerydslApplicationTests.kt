package study.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Commit
import org.springframework.transaction.annotation.Transactional
import study.querydsl.entity.Hello
import study.querydsl.entity.QHello
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
@Commit
class QuerydslApplicationTests(@Autowired private val em: EntityManager) {

    @Test
    fun contextLoads() {
        val hello = Hello()
        em.persist(hello)

        val query = JPAQueryFactory(em)
        val qHello: QHello = QHello.hello

        val result = query.selectFrom(qHello).fetchOne()

        assertThat(result).isEqualTo(hello)
        assertThat(result?.id).isEqualTo(hello.id)
    }
}

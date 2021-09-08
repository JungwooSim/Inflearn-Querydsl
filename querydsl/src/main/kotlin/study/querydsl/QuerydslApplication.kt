package study.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import javax.persistence.EntityManager

@SpringBootApplication
class QuerydslApplication

fun main(args: Array<String>) {
    runApplication<QuerydslApplication>(*args)
}

@Bean
fun jpaQueryFactory(em: EntityManager): JPAQueryFactory {
    return JPAQueryFactory(em)
}

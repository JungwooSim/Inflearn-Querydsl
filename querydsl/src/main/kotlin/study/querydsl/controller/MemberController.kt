package study.querydsl.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import study.querydsl.dto.MemberSearchCondition
import study.querydsl.dto.MemberTeamDto
import study.querydsl.repository.MemberJpaRepository
import study.querydsl.repository.MemberRepository

@RestController
class MemberController(
    @Autowired private val memberJpaRepository: MemberJpaRepository,
    @Autowired private val memberRepository: MemberRepository
) {

    @GetMapping("/v1/members")
    fun searchMemberV1(condition: MemberSearchCondition): MutableList<MemberTeamDto> {
        return memberJpaRepository.search(condition)
    }

    @GetMapping("/v2/members")
    fun searchMemberV2(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
        return memberRepository.searchPageSimple(condition, pageable)
    }

    @GetMapping("/v3/members")
    fun searchMemberV3(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
        return memberRepository.searchPageComplex(condition, pageable)
    }
}

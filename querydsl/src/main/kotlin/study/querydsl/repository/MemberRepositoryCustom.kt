package study.querydsl.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import study.querydsl.dto.MemberSearchCondition
import study.querydsl.dto.MemberTeamDto

interface MemberRepositoryCustom {
    fun search(condition: MemberSearchCondition): MutableList<MemberTeamDto>
    fun searchPageSimple(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto>
    fun searchPageComplex(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto>
}

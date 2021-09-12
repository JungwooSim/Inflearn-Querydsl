package study.querydsl.repository

import study.querydsl.dto.MemberSearchCondition
import study.querydsl.dto.MemberTeamDto

interface MemberRepositoryCustom {
    fun search(condition: MemberSearchCondition): MutableList<MemberTeamDto>
}

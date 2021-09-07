package study.querydsl.dto

import com.querydsl.core.annotations.QueryProjection

class MemberDto() {
    var username: String? = null
    var age: Int? = null

    @QueryProjection
    constructor(username: String, age: Int) : this() {
        this.username = username
        this.age = age
    }

    override fun toString(): String {
        return "MemberDto(username=$username, age=$age)"
    }
}

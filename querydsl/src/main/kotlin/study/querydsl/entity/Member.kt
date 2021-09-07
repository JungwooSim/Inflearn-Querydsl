package study.querydsl.entity

import javax.persistence.*

@Entity
class Member (
    @Id @GeneratedValue
    @Column(name = "member_id")
    val id: Long? = null,
    val username: String? = null,
    val age: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team? = null
) {
    fun changeTeam(team: Team) {
        this.team = team
        team.members.add(this)
    }

    override fun toString(): String {
        return "Member(id=$id, username=$username, age=$age, team=$team)"
    }
}


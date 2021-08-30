package study.querydsl.entity

import javax.persistence.*

@Entity
class Member (
    @Id @GeneratedValue
    @Column(name = "member_id")
    val id: Long? = null,
    val username: String,
    val age: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team
) {
    fun changeTeam(team: Team) {
        this.team = team
        team.members.add(this)
    }
}


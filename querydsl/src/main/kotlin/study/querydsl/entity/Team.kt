package study.querydsl.entity

import javax.persistence.*

@Entity
class Team (
    @Id @GeneratedValue
    @Column(name = "team_id")
    val id: Long? = null,
    val name: String,

    @OneToMany(mappedBy = "team")
    val members: MutableList<Member> = mutableListOf()
)

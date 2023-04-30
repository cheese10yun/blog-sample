package com.spring.camp.api

import javax.persistence.CollectionTable
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.Table

@Entity
@Table(name = "post")
class Post(
    @ElementCollection
    @CollectionTable(
        name = "post_tag",
        joinColumns = [JoinColumn(name = "post_id")]
    )
    val asd: Set<String>,
) : EntityAuditing() {


}
package com.project.adminchat.model

import java.io.Serializable

data class UserEntity (
    var id : String,
    var nickname : String,
    val uid : String,
    var token : String,
    var currentGym: String,
    var todayWorkOut:String = "",
    var profile_image:Int = 0
        ): Serializable {
    // Add a default, no-argument constructor
    constructor() : this("","", "","","","",0)
    constructor(id: String,nickname: String,uid: String) : this(id,nickname, uid,"","","",0)
}

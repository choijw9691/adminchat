package com.project.adminchat.model

import java.io.Serializable

data class Gym(
    val name : String,
    val userList : List<UserEntity>,
    val documentId : String
):Serializable{
    constructor() : this("", listOf(),"")
}

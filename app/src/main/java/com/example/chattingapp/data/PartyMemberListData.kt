package com.example.chattingapp.data

data class PartyMemberListData(
    val cmd: String,
    val errInfo: ErrorInfo,
    val data: List<UserData>
)

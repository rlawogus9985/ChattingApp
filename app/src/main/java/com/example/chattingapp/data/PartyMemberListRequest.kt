package com.example.chattingapp.data

data class PartyMemberListRequest(
    val partyNo: Int,
    val ownerMemNo: Int,
    val timeStamp: Long,
    val CountPerPage: Int
)

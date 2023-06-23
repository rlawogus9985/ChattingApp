package com.example.chattingapp.data

data class PartyChatLogRequest(
    val partyNo : Int,
    val rqMemNo: Int,
    val lastMsgNo: Long,
    val countPerPage: Int,
    val sortType: Boolean
)

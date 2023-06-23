package com.example.chattingapp.data

data class PsnChatLogRequest(
    val fromMemNo: Int,
    val toMemNo: Int,
    val lastMsgNo: Long,
    val countPerPage: Int,
    val sortType: Boolean
)

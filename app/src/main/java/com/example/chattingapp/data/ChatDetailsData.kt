package com.example.chattingapp.data

data class ChatDataRequest(
    val chatDetailsData: ChatDetailsData?,
    val grpChatDetailsData: GrpChatDetailsData?
)

data class ChatDetailsData(
    val hostInfo: UserData,
    val others: UserData,
    val chat: String,
    val msgNo: Long,
    val readCount: Int,
    val isDeleted: Boolean = false
)

data class GrpChatDetailsData(
    val hostNo: Int,
    val fromNo: Int,
    val chat: String,
    val msgNo: Long,
    val isNtLog : Boolean = false,
    val readCount: Int?,
    val isDeleted: Boolean = false
)

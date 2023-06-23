package com.example.chattingapp.data

data class DeletePsnMsgData(
    val rqDelete1On1Chat: RqDelete1On1Chat,
    val result: Int
)

data class RqDelete1On1Chat(
    val delMsgNo: Long,
    val fromMemNo: Int,
    val toMemNo: Int
)

data class DeleteGrpMsgData(
    val rqDeletePartyChat: RqDeletePartyChat,
    val result: Int
)

data class RqDeletePartyChat(
    val delMsgNo: Long,
    val fromMemNo: Int,
    val partyNo: Int
)
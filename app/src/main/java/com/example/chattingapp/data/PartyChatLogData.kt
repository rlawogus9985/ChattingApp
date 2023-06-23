package com.example.chattingapp.data

import com.google.gson.annotations.SerializedName

data class PartyChatLogData(
    val cmd: String,
    val errInfo: ErrorInfo,
    val data: ArrayList<PartyChatData>
)

data class PartyChatData(
    val cmd: String,
    val errInfo: ErrorInfo,
    val data: InnerData
)

data class InnerData(
    val textChatInfo: TextChatInfo,
    val commonRePartyChatInfo: CommonRePartyChatInfo,
    @SerializedName("msgNo") val ntUserMsgNo: Long,
    @SerializedName("remainReadCount") val ntUserRemainReadCount: Int,
    @SerializedName("partyNo") val ntUserPartyNo: Int,
    @SerializedName("memNo") val ntUserMemNo: Int,
    @SerializedName("joinUserInfo") val ntUserInfo: UserData
)

data class CommonRePartyChatInfo(
    val msgNo: Long,
    val replyMsgNo: Int,
    val remainReadCount: Int,
    val isDeleted: Boolean,
    val fromMemNo: Int,
    val partyNo: Int
)

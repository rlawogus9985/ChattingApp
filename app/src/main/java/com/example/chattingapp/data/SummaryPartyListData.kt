package com.example.chattingapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class SummaryPartyListData(
    val cmd: String,
    val errInfo: ErrorInfo,
    val data: ArrayList<PartyItem>
)

data class ErrorInfo(
    val errNo: Int = 0,
    val errMsg: String = ""
)

@Parcelize
data class PartyItem(
    val partyNo: Int = 0,
    val memNo: Int = 0,
    val mainPhotoUrl: String = "",
    val title: String = "",
    val location: String = "",
    val curMemberCount: Int = 0,
    val maxMemberCount: Int = 0,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val isAutoJoin: Boolean = false,
    val createAt: Long = 0L
): Parcelable
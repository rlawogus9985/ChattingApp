package com.example.chattingapp.data

data class CreatePartyContextRequest(
    val summaryPartyInfo: SummaryPartyInfo,
    val subPhotoUrlList: List<String>,
    val questContent: String
)

data class SummaryPartyInfo(
    val memNo: Int,
    val mainPhotoUrl: String,
    val title: String,
    val location: String,
    val maxMemberCount: Int,
    val startTime: Long,
    val endTime: Long,
    val isAutoJoin: Boolean
)


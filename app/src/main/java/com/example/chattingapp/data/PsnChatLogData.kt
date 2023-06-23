package com.example.chattingapp.data

data class PsnChatLogData(
    val cmd: String,
    val errorInfo: ErrorInfo,
    val data: ArrayList<PsnTextChatData?>
)

data class PsnTextChatData(
    val cmd: String = "",
    val errorInfo: ErrorInfo = ErrorInfo(),
    val data: PsnChaItemData = PsnChaItemData()
)

data class PsnChaItemData(
    val textChatInfo: TextChatInfo = TextChatInfo(),
    val commonRe1On1ChatInfo: CommonRe1On1ChatInfo = CommonRe1On1ChatInfo(),
    val isAccept: Boolean = false,
    val denyReason: Int = 0,
    val rqJoinParty: RqJoinParty = RqJoinParty(),
    val summaryPartyInfo: SummaryPartyInfoData = SummaryPartyInfoData(),
    val rqUserInfo: UserData = UserData()
)

data class SummaryPartyInfoData(
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
)

data class TextChatInfo(
    val msg: String = ""
)

data class CommonRe1On1ChatInfo(
    val msgNo: Long = 0L,
    val replyMsgNo: Int = 0,
    val remainReadCount: Int = 0,
    val isDeleted: Boolean = false,
    val fromMemNo: Int = 0,
    val toMemNo: Int = 0
)

data class RqJoinParty(
    val partyNo: Int = 0,
    val ownerMemNo: Int = 0,
    val rqMemNo: Int = 0
)
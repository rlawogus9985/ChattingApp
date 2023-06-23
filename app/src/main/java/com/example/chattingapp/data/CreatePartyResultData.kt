package com.example.chattingapp.data

data class CreatePartyResultData(
    val cmd: String,
    val errInfo: ErrorInfo,
    val data: PartyNumberData
)

data class PartyNumberData(
    val partyNo: String = ""
)
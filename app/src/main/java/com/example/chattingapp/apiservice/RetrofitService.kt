package com.example.chattingapp.apiservice

import com.example.chattingapp.data.CreatePartyContextRequest
import com.example.chattingapp.data.CreatePartyResultData
import com.example.chattingapp.data.DestroyPartyData
import com.example.chattingapp.data.DestroyPartyRequest
import com.example.chattingapp.data.MemNoRequest
import com.example.chattingapp.data.PartyChatLogData
import com.example.chattingapp.data.PartyChatLogRequest
import com.example.chattingapp.data.PartyListRequest
import com.example.chattingapp.data.PartyMemberListData
import com.example.chattingapp.data.PartyMemberListRequest
import com.example.chattingapp.data.PsnChatLogData
import com.example.chattingapp.data.PsnChatLogRequest
import com.example.chattingapp.data.SummaryPartyListData
import com.example.chattingapp.data.UserInfoData
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitService {
    @POST("RqSummaryUserInfo")
    suspend fun getSummaryUserInfo(
        @Body request: MemNoRequest
    ): UserInfoData

    @POST("RqSummaryPartyList")
    suspend fun getSummaryPartyList(
        @Body request: PartyListRequest
    ): SummaryPartyListData

    @POST("RqCreatePartyContext")
    suspend fun rqCreatePartyContext(
        @Body request: CreatePartyContextRequest
    ): CreatePartyResultData

    @POST("RqPartyMemberList")
    suspend fun rqPartyMemberList(
        @Body request: PartyMemberListRequest
    ): PartyMemberListData

    @POST("RqDestroyPartyContext")
    suspend fun rqDestroyPartyContext(
        @Body request: DestroyPartyRequest
    ): DestroyPartyData

    @POST("Rq1On1ChatLog")
    suspend fun rq1On1ChatLog(
        @Body request: PsnChatLogRequest
    ): PsnChatLogData

    @POST("RqPartyChatLog")
    suspend fun rqPartyChatLog(
        @Body request: PartyChatLogRequest
    ): PartyChatLogData
}
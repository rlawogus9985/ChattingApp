package com.example.chattingapp.apiservice

import com.example.chattingapp.data.CreatePartyContextRequest
import com.example.chattingapp.data.DestroyPartyRequest
import com.example.chattingapp.data.PartyChatLogData
import com.example.chattingapp.data.PartyChatLogRequest
import com.example.chattingapp.data.PartyItem
import com.example.chattingapp.data.PartyListRequest
import com.example.chattingapp.data.PartyMemberListRequest
import com.example.chattingapp.data.PartyNumberData
import com.example.chattingapp.data.PsnChatLogData
import com.example.chattingapp.data.PsnChatLogRequest
import com.example.chattingapp.data.UserData
import com.example.chattingapp.data.UserInfoData
import kotlinx.coroutines.flow.Flow

interface ApiHelper {
    fun getUserInfo(): Flow<Result<ArrayList<UserInfoData>>>
    fun getSummaryPartyList(req: PartyListRequest): Flow<Result<ArrayList<PartyItem>>>
    fun getCreatePartyResult(req: CreatePartyContextRequest): Flow<Result<PartyNumberData>>
    fun getPartyMemberList(req: PartyMemberListRequest): Flow<Result<List<UserData>>>
    fun destroyParty(req: DestroyPartyRequest): Flow<Result<Boolean>>
    fun get1On1ChatLog(req: PsnChatLogRequest): Flow<Result<PsnChatLogData>>
    fun getPastPartyChatLog(req: PartyChatLogRequest): Flow<Result<PartyChatLogData>>
    fun getFuturePartyChatLog(req: PartyChatLogRequest): Flow<Result<PartyChatLogData>>
}
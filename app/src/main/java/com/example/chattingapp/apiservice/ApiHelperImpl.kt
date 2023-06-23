package com.example.chattingapp.apiservice

import android.util.Log
import com.example.chattingapp.data.CreatePartyContextRequest
import com.example.chattingapp.data.DestroyPartyRequest
import com.example.chattingapp.data.MemNoRequest
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
import kotlinx.coroutines.flow.flow

class ApiHelperImpl(private val retrofitService: RetrofitService): ApiHelper {

    override fun getUserInfo(): Flow<Result<ArrayList<UserInfoData>>> = flow {
        val memNoList = listOf(10001,10002,10003,10004,10005,10006,10007,10008,10009,10010,10011,10012)
        try{
            val userInfoList = ArrayList<UserInfoData>()
            memNoList.forEach{
                val response = retrofitService.getSummaryUserInfo(MemNoRequest(memNo = it))
                userInfoList.add(response)
            }

            emit(Result.success(userInfoList))
        } catch (e:Exception){
            Log.d("로그", "apihelper오류: ${e.message} ")
        }
    }

    override fun getSummaryPartyList(req: PartyListRequest): Flow<Result<ArrayList<PartyItem>>> = flow {
        try{
            val response = retrofitService.getSummaryPartyList(req)
            emit(Result.success(response.data))
        } catch(e: Exception){
            Log.d("로그", "apihelper 오류 getSummaryPartylist: ${e.message} ")
        }
    }

    override fun getCreatePartyResult(req: CreatePartyContextRequest): Flow<Result<PartyNumberData>> = flow {
        try{
            val response = retrofitService.rqCreatePartyContext(req)
            if (response.errInfo.errNo != 0){
                Log.d("로그", "방만드는데 오류: ${response.errInfo} ")
            }
            emit(Result.success(response.data))
        } catch(e: Exception){
            Log.d("로그", "apihelper오류 getcreatePartyResult: ${e.message} ")
        }
    }

    override fun getPartyMemberList(req: PartyMemberListRequest): Flow<Result<List<UserData>>> = flow{
        try{
            val response = retrofitService.rqPartyMemberList(req)
            emit(Result.success(response.data))
        } catch(e: Exception){
            Log.d("로그", "apihelper오류 getPartyMemberList ${e.message} ")
        }
    }

    override fun destroyParty(req: DestroyPartyRequest): Flow<Result<Boolean>> = flow {
        try{
            val response = retrofitService.rqDestroyPartyContext(req)
            emit(Result.success(true))
        } catch (e: Exception){
            Log.d("로그", "apihelper오류 DestroyParty ${e.message} ")
        }
    }

    override fun get1On1ChatLog(req: PsnChatLogRequest): Flow<Result<PsnChatLogData>> = flow {
        try{
            val response = retrofitService.rq1On1ChatLog(req)
            emit(Result.success(response))
        } catch(e: Exception){
            Log.d("로그", "로그: apihelper오류 1대1 채팅로그 ${e.message} ")
        }
    }

    override fun getPastPartyChatLog(req: PartyChatLogRequest): Flow<Result<PartyChatLogData>> = flow{
        try{
            val response = retrofitService.rqPartyChatLog(req)
            emit(Result.success(response))
        } catch (e: Exception){
            Log.d("로그", "apihelper오류 그룹챗팅로그 ${e.message} ")
        }
    }
    override fun getFuturePartyChatLog(req: PartyChatLogRequest): Flow<Result<PartyChatLogData>> = flow{
        try{
            val response = retrofitService.rqPartyChatLog(req)
            emit(Result.success(response))
        } catch (e: Exception){
            Log.d("로그", "apihelper오류 그룹챗팅로그 ${e.message} ")
        }
    }
}
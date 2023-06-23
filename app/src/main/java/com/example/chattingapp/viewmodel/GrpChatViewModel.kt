package com.example.chattingapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattingapp.SocketEventListener.mSocket
import com.example.chattingapp.apiservice.ApiHelperImpl
import com.example.chattingapp.apiservice.RetrofitManager
import com.example.chattingapp.apiservice.RetrofitService
import com.example.chattingapp.data.DestroyPartyRequest
import com.example.chattingapp.data.PartyChatLogData
import com.example.chattingapp.data.PartyChatLogRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class GrpChatViewModel: ViewModel() {
    private val service: RetrofitService = RetrofitManager.retrofit
    private val apiHelper = ApiHelperImpl(service)

    private var _destroyParty = MutableSharedFlow<Boolean>()
    val destroyParty: SharedFlow<Boolean> = _destroyParty

    private var _pastGrpChatLog = MutableSharedFlow<PartyChatLogData>()
    val pastGrpChatLog: SharedFlow<PartyChatLogData> = _pastGrpChatLog

    private var _futureGrpChatLog = MutableSharedFlow<PartyChatLogData>()
    val futureGrpChatLog = _futureGrpChatLog.asSharedFlow()

    fun chatting(chat: String, fromNo: Int, partyNo: Int){
        val jsonObject = JSONObject().apply{
            put("cmd","RqPartyTextChat")
            put("data", JSONObject().apply{
                put("textChatInfo",JSONObject().apply{
                    put("msg",chat)
                })
                put("commonRqPartyChatInfo", JSONObject().apply{
                    put("replyMsgNo",0)
                    put("fromMemNo",fromNo)
                    put("partyNo",partyNo)
                })
            })
        }
        mSocket?.emit("Party",jsonObject)
    }

    fun acceptOrDeclineParty(partyNo: Int, ownerNo: Int, rqUserNo: Int, permit: Boolean){
        val jsonObject = JSONObject().apply{
            put("cmd", "RqAcceptParty")
            put("data",JSONObject().apply{
                put("isAccept",permit)
                put("rqJoinParty",JSONObject().apply{
                    put("partyNo",partyNo)
                    put("ownerMemNo",ownerNo)
                    put("rqMemNo",rqUserNo)
                })
            })
        }
        mSocket?.emit("Lobby",jsonObject)
    }

    fun deleteRoom(partyNo: Int, ownerNo: Int){
        viewModelScope.launch {
            apiHelper.destroyParty(DestroyPartyRequest(partyNo,ownerNo))
                .collect{
                    if(it.isSuccess){
                        _destroyParty.emit(it.getOrNull()!!)
                    }else {
                        Log.d("로그", "grpChatVieWModel destroyParty: ${it.exceptionOrNull()} ")
                    }
                }
        }
    }

    fun exitRoom(partyNo: Int, hostNo: Int){
        val jsonObject = JSONObject().apply{
            put("cmd", "RqLeaveParty")
            put("data",JSONObject().apply{
                put("partyNo",partyNo)
                put("memNo",hostNo)
            })
        }
        mSocket?.emit("Party",jsonObject)
    }

    fun kickUser(partyNo: Int, ownerMemNo: Int, kickoutMemNo: Int){
        val jsonObject = JSONObject().apply{
            put("cmd", "RqKickoutUser")
            put("data",JSONObject().apply{
                put("partyNo",partyNo)
                put("ownerMemNo",ownerMemNo)
                put("kickoutMemNo",kickoutMemNo)
            })
        }
        mSocket?.emit("Party",jsonObject)
    }

    fun rqPastGrpChatLog(partyNo: Int, rqMemNo: Int, lastMsgNo: Long, countPerPage: Int, sortType: Boolean = false){
        viewModelScope.launch {
            apiHelper.getPastPartyChatLog(PartyChatLogRequest(partyNo,rqMemNo,lastMsgNo,countPerPage, sortType))
                .collect{
                    if(it.isSuccess){
                        _pastGrpChatLog.emit(it.getOrNull()!!)
                    } else {
                        Log.d("로그", "pastGrpchatviewModel 채팅 내역 불러오기 실패 ${it.exceptionOrNull()} ")
                    }
                }
        }
    }

    fun rqFutureGrpChatLog(partyNo: Int, rqMemNo: Int, lastMsgNo: Long, countPerPage: Int, sortType: Boolean = true){
        viewModelScope.launch {
            apiHelper.getFuturePartyChatLog(PartyChatLogRequest(partyNo,rqMemNo,lastMsgNo,countPerPage,sortType))
                .collect{
                    if(it.isSuccess){
                        _futureGrpChatLog.emit(it.getOrNull()!!)
                    } else {
                        Log.d("로그", "futureGrpChatViewModel 채팅 내역 불러오기 실패 ${it.exceptionOrNull()} ")
                    }
                }
        }
    }

    fun rqDeletePartyChat(msgNo: Long, partyNo: Int, fromMemNo: Int){
        val jsonObject = JSONObject().apply{
            put("cmd","RqDeletePartyChat")
            put("data",JSONObject().apply{
                put("delMsgNo", msgNo)
                put("partyNo", partyNo)
                put("fromMemNo", fromMemNo)
            })
        }
        mSocket?.emit("Party", jsonObject)
    }

    fun readPartyChat(msgNo: List<Long>, fromNo: Int, partyNo: Int){
        val jsonObject = JSONObject().apply{
            put("cmd","RqReadPartyChat")
            put("data",JSONObject().apply{
                put("readMsgNos", JSONArray(msgNo))
                put("fromMemNo", fromNo)
                put("partyNo",partyNo)
            })
        }
        mSocket?.emit("Party",jsonObject)
    }

}
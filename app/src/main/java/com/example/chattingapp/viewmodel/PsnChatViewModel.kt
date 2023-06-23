package com.example.chattingapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattingapp.SocketEventListener.mSocket
import com.example.chattingapp.apiservice.ApiHelperImpl
import com.example.chattingapp.apiservice.RetrofitManager
import com.example.chattingapp.apiservice.RetrofitService
import com.example.chattingapp.data.PsnChatLogData
import com.example.chattingapp.data.PsnChatLogRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class PsnChatViewModel: ViewModel() {
    private val service: RetrofitService = RetrofitManager.retrofit
    private val apiHelper = ApiHelperImpl(service)

    private var _psnChatLog = MutableSharedFlow<PsnChatLogData>()
    val psnChatLog: SharedFlow<PsnChatLogData> = _psnChatLog

    fun chatting(chat: String, hostInfoNum: Int, othersInfoNum: Int){
        val jsonObject = JSONObject().apply{
            put("cmd","Rq1On1TextChat")
            put("data",JSONObject().apply{
                put("textChatInfo",JSONObject().apply{ put("msg",chat)})
                put("commonRq1On1ChatInfo",JSONObject().apply{
                    put("replyMsgNo",0)
                    put("fromMemNo",hostInfoNum)
                    put("toMemNo",othersInfoNum)
                })
            })
        }
        mSocket?.emit("Lobby",jsonObject)
    }

    fun rq1On1ChatLog(fromNo: Int, toMemNo: Int, lastMsgNo: Long, countPerPage: Int, sortType: Boolean = false){
        viewModelScope.launch{
            apiHelper.get1On1ChatLog(PsnChatLogRequest(fromNo,toMemNo,lastMsgNo,countPerPage, sortType))
                .collect{
                    if(it.isSuccess){
                        _psnChatLog.emit(it.getOrNull()!!)
                    } else {
                        Log.d("로그", "psnChatViewModel 채팅 내역 불러오기 실패 ${it.exceptionOrNull()} ")
                    }
                }
        }
    }

    fun read1On1Chat(msgNo: List<Long>, fromNo: Int, toNo: Int){
        val jsonObject = JSONObject().apply{
            put("cmd","RqRead1On1Chat")
            put("data",JSONObject().apply{
                put("readMsgNos", JSONArray(msgNo))
                put("fromMemNo", fromNo)
                put("toMemNo", toNo)
            })
        }
        mSocket?.emit("Lobby",jsonObject)
    }

    fun rqDelete1On1Chat(msgNo: Long, fromMemNo: Int, toMemNo: Int){
        val jsonObject = JSONObject().apply{
            put("cmd","RqDelete1On1Chat")
            put("data",JSONObject().apply{
                put("delMsgNo", msgNo)
                put("fromMemNo", fromMemNo)
                put("toMemNo", toMemNo)
            })
        }
        mSocket?.emit("Lobby", jsonObject)
    }
}
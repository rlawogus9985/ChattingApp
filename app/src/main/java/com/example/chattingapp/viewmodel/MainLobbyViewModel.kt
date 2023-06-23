package com.example.chattingapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattingapp.SocketEventListener.mSocket
import com.example.chattingapp.apiservice.ApiHelperImpl
import com.example.chattingapp.apiservice.RetrofitManager
import com.example.chattingapp.apiservice.RetrofitService
import com.example.chattingapp.data.CreatePartyContextRequest
import com.example.chattingapp.data.PartyItem
import com.example.chattingapp.data.PartyListRequest
import com.example.chattingapp.data.PartyMemberListRequest
import com.example.chattingapp.data.PartyNumberData
import com.example.chattingapp.data.UserData
import com.example.chattingapp.data.UserInfoData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainLobbyViewModel : ViewModel() {
    private val service: RetrofitService = RetrofitManager.retrofit
    private val apiHelper = ApiHelperImpl(service)

    init{
        getUserInfoLists()
    }

    private var _userInfoLists = MutableSharedFlow<ArrayList<UserInfoData>>()
    val userInfoLists: SharedFlow<ArrayList<UserInfoData>> = _userInfoLists

    private var _summaryPartyLists = MutableStateFlow(arrayListOf(PartyItem()))
    val summaryPartyLists = _summaryPartyLists.asStateFlow()

    private var _hostInfo: UserData = UserData()

    private var _partyMemberLists = MutableSharedFlow<List<UserData>?>()
    val partyMemberLists: SharedFlow<List<UserData>?> = _partyMemberLists

    private var _createPartyResult = MutableStateFlow(PartyNumberData())
    val createPartyResult = _createPartyResult.asStateFlow()

    private fun getUserInfoLists() {
        viewModelScope.launch {
            apiHelper.getUserInfo()
                .collect{
                    if(it.isSuccess){
                        _userInfoLists.emit(it.getOrNull()!!)
                    }else {
                        Log.d("userInfoLists","${it.exceptionOrNull()}")
                    }
                }
        }
    }

    fun getSummaryPartyLists(timeStamp: Long = System.currentTimeMillis(), countPerPage: Int = 30){
        viewModelScope.launch {
            apiHelper.getSummaryPartyList(PartyListRequest("1",timeStamp,countPerPage))
                .collect{
                    if (it.isSuccess){
                        _summaryPartyLists.value = it.getOrNull()!!
                    } else {
                        Log.d("로그", "mainViewModel getSummaryPartyLists: ${it.exceptionOrNull()} ")
                    }
                }
        }
    }

    fun setHostInfo(value: UserData){
        _hostInfo = value
//        _hostInfo = value.copy()
    }
    fun getHostInfo(): UserData{
        return _hostInfo
    }

    fun makeGroupRoom(value: CreatePartyContextRequest){
        viewModelScope.launch{
            apiHelper.getCreatePartyResult(value)
                .collect{
                    if (it.isSuccess){
                        _createPartyResult.emit(it.getOrNull()!!)
                    } else {
                        Log.d("로그", "Mainlobby viewmodel 방만들기 실패: ${it.exceptionOrNull()} ")
                    }
                }
        }
    }

    fun rqPartyMemberList(value: PartyMemberListRequest){
        viewModelScope.launch {
            apiHelper.getPartyMemberList(value)
                .collect{
                    if(it.isSuccess){
                        _partyMemberLists.emit(it.getOrNull()!!)
                    }else {
                        Log.d("로그", "lobbyViewModel rqPartyMemberList 실패: ${it.exceptionOrNull()} ")
                    }
                }
        }
    }

    fun joinParty(partyNo: Int, ownerNo: Int, hostNo: Int){
        val jsonObject = JSONObject().apply{
            put("cmd","RqJoinParty")
            put("data",JSONObject().apply{
                put("partyNo", partyNo)
                put("ownerMemNo", ownerNo)
                put("rqMemNo",hostNo)
            })
        }
        mSocket?.emit("Lobby",jsonObject)
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
}
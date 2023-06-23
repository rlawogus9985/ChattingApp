package com.example.chattingapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chattingapp.data.CommonRePartyChatInfo
import com.example.chattingapp.data.DeleteGrpMsgData
import com.example.chattingapp.data.DeletePsnMsgData
import com.example.chattingapp.data.PartyChatData
import com.example.chattingapp.data.PsnTextChatData
import com.example.chattingapp.data.UserData
import com.example.chattingapp.eventbus.SocketConnectErrorEvent
import com.example.chattingapp.eventbus.SocketDisconnectedEvent
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject


object SocketEventListener {

    private val _reAuthUserData = MutableLiveData<UserData>()
    val reAuthUserData: LiveData<UserData> = _reAuthUserData

    private val _re1On1Chat = MutableLiveData<PsnTextChatData?>()
    val re1On1Chat: MutableLiveData<PsnTextChatData?> = _re1On1Chat

    private val _nt1On1Chat = MutableLiveData<PsnTextChatData?>()
    val nt1On1Chat: LiveData<PsnTextChatData?> = _nt1On1Chat

    private val _reJoinParty = MutableLiveData<Map<Int,String>>()
    val reJoinParty: LiveData<Map<Int,String>> = _reJoinParty

    private val _ntRequestJoinParty = MutableLiveData<JSONObject?>()
    val ntRequestJoinParty: LiveData<JSONObject?> = _ntRequestJoinParty

    private val _rePartyChat = MutableLiveData<PartyChatData?>()
    val rePartyChat: LiveData<PartyChatData?> = _rePartyChat

    private val _ntPartyChat = MutableLiveData<PartyChatData?>()
    val ntPartyChat: LiveData<PartyChatData?> = _ntPartyChat

    private val _ntUserJoinedParty = MutableLiveData<JSONObject?>()
    val ntUserJoinedParty: LiveData<JSONObject?> = _ntUserJoinedParty

    private val _reLeaveParty = MutableLiveData<JSONObject>()
    val reLeaveParty: LiveData<JSONObject> = _reLeaveParty

    private val _ntDestroyParty = MutableLiveData<JSONObject?>()
    val ntDestroyParty: LiveData<JSONObject?> = _ntDestroyParty

    private val _ntUserLeavedParty = MutableLiveData<JSONObject?>()
    val ntUserLeavedParty: LiveData<JSONObject?> = _ntUserLeavedParty

    private val _ntRead1On1Chat = MutableLiveData<JSONObject?>()
    val ntRead1On1Chat: LiveData<JSONObject?> = _ntRead1On1Chat

    private val _ntReadPartyChat = MutableLiveData<JSONObject?>()
    val ntReadPartyChat: LiveData<JSONObject?> = _ntReadPartyChat

    private val _ntKickoutUser = MutableLiveData<CommonRePartyChatInfo?>()
    val ntKickoutUser: LiveData<CommonRePartyChatInfo?> = _ntKickoutUser

    private val _reDelete1On1chat = MutableLiveData<DeletePsnMsgData?>()
    val reDelete1On1Chat : LiveData<DeletePsnMsgData?> = _reDelete1On1chat

    private val _ntDelete1On1Chat = MutableLiveData<DeletePsnMsgData?>()
    val ntDelete1On1Chat: LiveData<DeletePsnMsgData?> = _ntDelete1On1Chat

    private val _reDeletePartychat = MutableLiveData<DeleteGrpMsgData?>()
    val reDeletePartyChat : LiveData<DeleteGrpMsgData?> = _reDeletePartychat

    private val _ntDeletePartyChat = MutableLiveData<DeleteGrpMsgData?>()
    val ntDeletePartyChat: LiveData<DeleteGrpMsgData?> = _ntDeletePartyChat

    private val _socketConnected = MutableStateFlow("")
    val socketConnected = _socketConnected.asStateFlow()

    var mSocket : Socket? = null

    fun initializeSocket(){
        mSocket = SocketHandler.get()
        Log.d("로그", "SocketEventListener.kt 소켓 상황 : $mSocket, ${mSocket?.connected()}")
    }
    fun connectionSocket(){
        mSocket?.on(Socket.EVENT_CONNECT){
            Log.d("로그", "SocketEventListener.kt Event_connect일때 소켓 상황 : $mSocket, ${mSocket?.connected()}")
            if(_socketConnected.value == "connect") {
                // 들어오는 시점에 connect면 오류가 있는 것.
                Log.d("로그", "connect오류 났었음")

                // 방법 1. 오류가났으니 disconnect후 toast를 띄워서 다시 클릭입력하게 하는 방법
                SocketHandler.disconnect()
                mSocket = null
                _socketConnected.value = ""
                handleSocketConnectError()
                // 방법 2. 시간주고 emit을 다시 해주는 방법
            } else {
                _socketConnected.value = "connect"
            }
        }

        mSocket?.on(Socket.EVENT_ERROR) {
            Log.d("로그", "EVENT_ERROR 났었음")
        }

        mSocket?.on(Socket.EVENT_RECONNECT) {
            Log.d("로그", "EVENT_RECONNECT 났었음")
        }

    }

    fun disconnectSocket() {
        mSocket?.on(Socket.EVENT_DISCONNECT) {
            Log.d("로그", "socket disconnect 성공")
            mSocket = null
            _socketConnected.value = ""
            Log.d("로그", "socket disconnect 상황 : ${mSocket}, ${mSocket?.connected()}")
        }
    }

    fun registSocketCallback(){
        mSocket?.on("Lobby") { args ->
            Log.d("로그", "socketEventListener lobby 실행 ")
            val jsonString = args[0] as String
            val data = JSONObject(jsonString)
            processDataLobby(data)
        }
        mSocket?.on("Party"){args->
            Log.d("로그", "socketEventListener party 실행")
            val jsonString = args[0] as String
            val data = JSONObject(jsonString)
            processDataParty(data)
        }
        mSocket?.on(Socket.EVENT_DISCONNECT){
            handleSocketDisconnected()
        }
    }
    private val eventBus: EventBus = EventBus.getDefault()
    private fun handleSocketDisconnected(){
        eventBus.post(SocketDisconnectedEvent())
    }
    private fun handleSocketConnectError(){
        eventBus.post(SocketConnectErrorEvent())
    }

    private fun processDataLobby(data: JSONObject) {
        when (data.getString("cmd")) {
            "ReAuthUser" -> {
                val reResult = data.getJSONObject("data").getInt("result")
                val gsonAuthData = Gson().fromJson(data.getJSONObject("data").getJSONObject("summaryUserInfo").toString(), UserData::class.java)
                Log.d("로그", "reAuthUser: $gsonAuthData ")

                if (reResult == 1) _reAuthUserData.postValue(gsonAuthData)
            }

            "Re1On1TextChat" -> {
                Log.d("로그", "소켓 Re1On1TextChat: $data ")
                _re1On1Chat.postValue((Gson().fromJson(data.toString(), PsnTextChatData::class.java )))
            }

            "Nt1On1TextChat" -> {
                Log.d("로그", "소켓 Nt1On1Chat: $data ")
                _nt1On1Chat.postValue(Gson().fromJson(data.toString(), PsnTextChatData::class.java ))
            }

            "ReJoinPartyResult" -> {
                Log.d("로그", "ReJoinParty: $data")
                val partyNo = data.getJSONObject("data").getJSONObject("rqJoinParty").getInt("partyNo")
                when(data.getJSONObject("data").getInt("denyReason")){
                    0->{ // 방 요청 수락됨
                        _reJoinParty.postValue(mapOf(0 to "${partyNo}번 방 요청이 허락되었습니다."))
                    }
                    1->{ // 방장거절
                        _reJoinParty.postValue(mapOf(1 to "방장이 ${partyNo}번 방 요청을 거절하였습니다."))
                    }
                    2->{ // 방 없음
                        _reJoinParty.postValue(mapOf(2 to "신청한 방이 존재하지 않습니다."))
                    }
                    3->{ // 방 꽉참
                        _reJoinParty.postValue(mapOf(3 to "방이 꽉 찼습니다."))
                    }
                    4->{ // 이미 참석해 있음
                        _reJoinParty.postValue(mapOf(4 to "이미 참석해 있습니다."))
                    }
                    5->{ // 이미 참석 신청 해놓음
                        _reJoinParty.postValue(mapOf(5 to "이미 신청을 보내놓은 방입니다."))
                    }
                    6->{ // 강퇴 유저
                        _reJoinParty.postValue(mapOf(6 to "방장에 의해 강퇴된 방입니다."))
                    }
                    7->{ // 방장 승락 대기중
                        _reJoinParty.postValue(mapOf(7 to "방장 승락 대기중입니다."))
                    }
                    8->{ // 기타
                        _reJoinParty.postValue(mapOf(8 to "기타 이유로 ${partyNo}번 방 입장이 거절되었습니다."))
                    }
                }
            }
            "NtRequestJoinParty"->{
                Log.d("로그", "소켓 NtRequestJoinParty 가입 요청: $data ")
                _ntRequestJoinParty.postValue(data)
            }
            "NtRead1On1Chat"->{
                Log.d("로그", "소켓 NtRead1On1Chat 채팅읽음: $data ")
                _ntRead1On1Chat.postValue(data)
            }
            "ReDelete1On1Chat"->{
                Log.d("로그", "소켓 ReDelete1On1Chat 내가 채팅 지움: $data ")
                _reDelete1On1chat.postValue(Gson().fromJson(data.getJSONObject("data").toString(), DeletePsnMsgData::class.java))
            }
            "NtDelete1On1Chat"->{
                Log.d("로그", "소켓 NtDelete1On1Chat 딴사람이 채팅 지움: $data ")
                _ntDelete1On1Chat.postValue(Gson().fromJson(data.getJSONObject("data").toString(), DeletePsnMsgData::class.java))
            }

            else -> {
                Log.d("로그", "소켓 다른 요청 lobby: $data ")
            }
        }
    }

    private fun processDataParty(data: JSONObject){
        when(data.getString("cmd")){
            "RePartyTextChat" -> {
                Log.d("로그", "소켓 rePartyChat: $data ")
                _rePartyChat.postValue(Gson().fromJson(data.toString(), PartyChatData::class.java ))
            }
            "NtPartyTextChat" -> {
                Log.d("로그", "소켓 ntPartyChat: $data ")
                _ntPartyChat.postValue(Gson().fromJson(data.toString(), PartyChatData::class.java ))
            }
            "NtUserJoinedParty" ->{ // 1. 다른 멤버가 자신이 속한 어떠한 방이라도 허가된다면 호출됨.
                Log.d("로그", "소켓 ntUserJoinedParty: $data ")
                _ntUserJoinedParty.postValue(data)
            }
            "ReLeaveParty" -> { // 1. 자신이 방을 나갔을때 호출됨.
                Log.d("로그", "소켓 reLeaveParty: $data ")
                _reLeaveParty.postValue(data)
            }
            "NtDestroyParty" -> { // 1. API를 통해서 방이 삭제됐을때 호출됌. 2. 방장이 socket을 통해 방을 나갔을때 호출됨.
                Log.d("로그", "소켓 ntDestroyParty: $data ")
                _ntDestroyParty.postValue(data)
            }
            "NtUserLeavedParty"->{ // 1. 내가 속해 있는 방의 다른 멤버가 방을 나갔을때 호출됨.
                Log.d("로그", "소켓 ntUserLeavedParty: $data")
                _ntUserLeavedParty.postValue(data.getJSONObject("data"))
            }
            "NtReadPartyChat"->{
                Log.d("로그", "소켓 ntReadPartyChat: $data ")
                _ntReadPartyChat.postValue(data.getJSONObject("data"))
            }
            "NtKickoutUser"->{
                Log.d("로그", "소켓 ntKickoutUser: $data ")
                _ntKickoutUser.postValue(Gson().fromJson(data.getJSONObject("data").getJSONObject("commonRePartyChatInfo").toString(), CommonRePartyChatInfo::class.java))
            }
            "ReDeletePartyChat"->{
                Log.d("로그", "소켓 ReDeletePartyChat 내가 채팅 지움: $data ")
                _reDeletePartychat.postValue(Gson().fromJson(data.getJSONObject("data").toString(), DeleteGrpMsgData::class.java))
            }
            "NtDeletePartyChat"->{
                Log.d("로그", "소켓 NtDeletePartyChat 딴사람이 채팅 지움: $data ")
                _ntDeletePartyChat.postValue(Gson().fromJson(data.getJSONObject("data").toString(), DeleteGrpMsgData::class.java))
            }
            else -> {
                Log.d("로그", "소켓 Error 예상 외 로그 party: $data ")
            }
        }
    }

    fun resetRe1On1Chat(){
        _re1On1Chat.value = null
    }
    fun resetNt1On1Chat(){
        _nt1On1Chat.value = null
    }
    fun resetRePartyChat(){
        _rePartyChat.value = null
    }
    fun resetNtPartyChat(){
        _ntPartyChat.value = null
    }
    fun resetNtRequestJoinParty(){
        _ntRequestJoinParty.value = null
    }
    fun resetNtDestroyParty(){
        _ntDestroyParty.value = null
    }
    fun resetNtUserLeavedParty(){
        _ntUserLeavedParty.value = null
    }
    fun resetNtUserJoinParty(){
        _ntUserJoinedParty.value = null
    }
    fun resetNtRead1On1Chat(){
        _ntRead1On1Chat.value = null
    }
    fun resetNtReadPartyChat(){
        _ntReadPartyChat.value = null
    }
    fun resetNtKickoutUser(){
        _ntKickoutUser.value = null
    }
    fun resetReDelete1On1Chat(){
        _reDelete1On1chat.value = null
    }
    fun resetNtDelete1On1Chat(){
        _ntDelete1On1Chat.value = null
    }
    fun resetReDeletePartyChat(){
        _reDeletePartychat.value = null
    }
    fun resetNtDeletePartyChat(){
        _ntDeletePartyChat.value = null
    }

}
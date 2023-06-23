package com.example.chattingapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.chattingapp.adapter.DetailsGrpChatAdapter
import com.example.chattingapp.data.CommonRePartyChatInfo
import com.example.chattingapp.data.GrpChatDetailsData
import com.example.chattingapp.data.PartyChatData
import com.example.chattingapp.data.PartyItem
import com.example.chattingapp.data.UserData
import com.example.chattingapp.databinding.ActivityGroupChatScreenBinding
import com.example.chattingapp.dialog.ViewMemberListDialog
import com.example.chattingapp.eventbus.SocketDisconnectedEvent
import com.example.chattingapp.extensions.collect
import com.example.chattingapp.room.dao.GroupChatEnterDao
import com.example.chattingapp.room.database.AppDatabase
import com.example.chattingapp.room.entity.GroupChatEnterHistory
import com.example.chattingapp.viewmodel.GrpChatViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

class GroupChatScreen : AppCompatActivity() {

    private lateinit var binding: ActivityGroupChatScreenBinding
    private lateinit var hostInfo: UserData
    private lateinit var partyRoomInfo: PartyItem
    private lateinit var userInfoList: ArrayList<UserData>
    private lateinit var grpChatAdapter: DetailsGrpChatAdapter

    private val grpChatViewModel: GrpChatViewModel by viewModels()
    private var enterTime: Long = System.currentTimeMillis()
    private var lastMsgNo: Long = System.currentTimeMillis()
    private var futureLastMsgNo: Long = 0L
    private var countPerPage: Int = 15
    private var stopAddHistory: Boolean = false
    private var stopAddFutureLog: Boolean = false
    private var isFirstExecution = true
    private var primaryKey: Long = 0
    private var exitTime: Long? = null
    private var firstEnterRoom = false

    private val CMD_RE_PARTY_TEXT_CHAT = "RePartyTextChat"
    private val CMD_NT_PARTY_TEXT_CHAT = "NtPartyTextChat"
    private val CMD_NT_USER_JOINED_PARTY = "NtUserJoinedParty"
    private val CMD_NT_USER_LEAVED_PARTY = "NtUserLeavedParty"

    private val eventBus: EventBus = EventBus.getDefault()

    private lateinit var db: AppDatabase
    private lateinit var dao: GroupChatEnterDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatScreenBinding.inflate(layoutInflater)

        eventBus.register(this)

        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,AppDatabase::class.java,"grpChatExcessDB"
        ).fallbackToDestructiveMigration().build()
        dao = db.groupChatEnterDao()

        // 툴바 활성화
        setSupportActionBar(binding.toolbar)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            partyRoomInfo = intent.getParcelableExtra("partyRoomInfo",PartyItem::class.java)!!
            hostInfo = intent.getParcelableExtra("hostInfo", UserData::class.java)!!
            userInfoList = intent.getParcelableArrayListExtra("UserInfos", UserData::class.java) as ArrayList<UserData>
        } else {
            partyRoomInfo = intent.getParcelableExtra("partyRoomInfo")!!
            hostInfo = intent.getParcelableExtra("hostInfo")!!
            userInfoList = intent.getParcelableArrayListExtra<UserData>("UserInfos") as ArrayList<UserData>
        }

        lifecycleScope.launch{
            exitTime = dao.getExitTime(partyRoomInfo.partyNo, hostInfo.memNo)
            lastMsgNo = if (exitTime == null){
                Log.d("로그", "dao연결 됐고 enterTime가져왔는데 : null ")
                primaryKey = dao.insertHistory(GroupChatEnterHistory(null, partyRoomInfo.partyNo,hostInfo.memNo,enterTime,null))
                Log.d("로그", "insert생성 primarykey : $primaryKey ")
                System.currentTimeMillis()
            } else {
                Log.d("로그", "dao연결 됐고 enterTime가져옴: $exitTime ")
                primaryKey = dao.getPrimaryKey(partyRoomInfo.partyNo,hostInfo.memNo)
                dao.updateHistory(GroupChatEnterHistory(primaryKey,partyRoomInfo.partyNo,hostInfo.memNo,enterTime,exitTime))
                futureLastMsgNo = exitTime!!
                Log.d("로그", "futureLastMsgNo을 exitTime으로 처음 갱신함 : $futureLastMsgNo ");
                exitTime!!
            }


        }

        supportActionBar?.apply{
            title = partyRoomInfo.title
            setDisplayHomeAsUpEnabled(true)
        }

        resetSocket()

        grpChatAdapter = DetailsGrpChatAdapter()

        grpChatAdapter.initUserDataList(userInfoList)

        binding.chatEnterButton.setOnClickListener{
            val chat = binding.chatContext.text.toString()
            grpChatViewModel.chatting(chat, hostInfo.memNo, partyRoomInfo.partyNo)
            binding.chatContext.setText("")
        }

        observeSocketEvent()

        binding.DetailsGrpChatRecyclerView.apply{
            adapter = grpChatAdapter
            layoutManager = LinearLayoutManager(this@GroupChatScreen, LinearLayoutManager.VERTICAL, false)

            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(!binding.DetailsGrpChatRecyclerView.canScrollVertically(-1)){
                        if(!stopAddHistory && !firstEnterRoom){
                            Log.d("로그", "상단 닿음. 과거채팅 불러옴: ");
                            grpChatViewModel.rqPastGrpChatLog(partyRoomInfo.partyNo,hostInfo.memNo,lastMsgNo,countPerPage)
                        }
                    }
                    if(!binding.DetailsGrpChatRecyclerView.canScrollVertically(1)){
                        if(!stopAddFutureLog){
                            Log.d("로그", "하단 닿음. 미래 채팅 불러옴: ");
                            grpChatViewModel.rqFutureGrpChatLog(partyRoomInfo.partyNo,hostInfo.memNo,futureLastMsgNo,countPerPage)
                        }
                    }
                }
            })
        }

        // 아이템 삭제
        grpChatAdapter.setOnDeleteItemClickListener {
            grpChatViewModel.rqDeletePartyChat(it.msgNo,partyRoomInfo.partyNo,it.hostNo)
        }

        observeViewModel()

        if(exitTime != null){
            Log.d("로그", "exitTime null아닐때 목록 불러오기: ")
            grpChatViewModel.rqPastGrpChatLog(partyRoomInfo.partyNo,hostInfo.memNo, exitTime!!, countPerPage)
            grpChatViewModel.rqFutureGrpChatLog(partyRoomInfo.partyNo,hostInfo.memNo,exitTime!!,countPerPage)
        } else {
            // exitTime이 null이다. -> 처음 들어오는 경우
            Log.d("로그", "exitTime null일때 목록 불러오기 ")
            grpChatViewModel.rqFutureGrpChatLog(partyRoomInfo.partyNo,hostInfo.memNo, futureLastMsgNo, countPerPage)
            firstEnterRoom = true
        }

    } // onCreate

    private fun observeSocketEvent() = with(SocketEventListener){

        // 누군가 파티 들어오면 userInfoList 업데이트
        ntUserJoinedParty.observe(this@GroupChatScreen){
            if(it!=null){
                val data = it.getJSONObject("data")
                val commonRePartyChatInfo = Gson().fromJson(data.getJSONObject("commonRePartyChatInfo").toString(), CommonRePartyChatInfo::class.java)
                if(commonRePartyChatInfo.partyNo == partyRoomInfo.partyNo){
                    val joinUserInfo = Gson().fromJson(data.getJSONObject("joinUserInfo").toString(), UserData::class.java)
                    if (joinUserInfo !in userInfoList){
                        userInfoList.add(joinUserInfo)
                        grpChatAdapter.initUserDataList(userInfoList)
                        grpChatAdapter.sendMessage(GrpChatDetailsData(hostInfo.memNo, joinUserInfo.memNo,
                            "가입하셨습니다.",commonRePartyChatInfo.msgNo, true, null))
                        grpChatAdapter.scrollToBottom(binding.DetailsGrpChatRecyclerView)
                    }
                }
            }
        }

        // 내가 파티채팅 침
        rePartyChat.observe(this@GroupChatScreen){
            if(it != null){
                grpChatAdapter.sendMessage(GrpChatDetailsData(hostInfo.memNo,
                    it.data.commonRePartyChatInfo.fromMemNo,
                    it.data.textChatInfo.msg, it.data.commonRePartyChatInfo.msgNo,
                    readCount = it.data.commonRePartyChatInfo.remainReadCount))
                grpChatAdapter.scrollToBottom(binding.DetailsGrpChatRecyclerView)
            }
        }

        // 상대가 채팅 침
        ntPartyChat.observe(this@GroupChatScreen){
            if(it != null){
                val data = it.data
                val commonInfo = data.commonRePartyChatInfo
                if(partyRoomInfo.partyNo == commonInfo.partyNo){
                    grpChatViewModel.readPartyChat(listOf(commonInfo.msgNo),hostInfo.memNo,partyRoomInfo.partyNo)
                    // Todo remainReadCount의 수 1줄이기.(내가 읽었으니까)
                    grpChatAdapter.sendMessage(GrpChatDetailsData(hostInfo.memNo,
                        commonInfo.fromMemNo, data.textChatInfo.msg,commonInfo.msgNo,
                        readCount = commonInfo.remainReadCount - 1))
                    grpChatAdapter.scrollToBottom(binding.DetailsGrpChatRecyclerView)
                }
            }
        }

        // 다른 사람들이 어떤 메시지를 읽음.
        ntReadPartyChat.observe(this@GroupChatScreen){
            if(it!=null && it.getInt("partyNo") == partyRoomInfo.partyNo && it.optJSONArray("readMsgNos") != null){
                val parsingList = it.getJSONArray("readMsgNos")
                val readMsgNosList = jsonArrayToArrayList(parsingList)
                Log.d("로그", "adpater로 수정들어가기전 무슨 메시지를 읽음처리 해야하는지: $readMsgNosList ");
                runOnUiThread { // 원자성 확보할 수단 필요
                    grpChatAdapter.modifyReadCount(readMsgNosList)
                }
            }
        }

        ntDestroyParty.observe(this@GroupChatScreen){
            if(it!=null){
                val destroyPartyNo = it.getJSONObject("data").getInt("partyNo")
                Toast.makeText(this@GroupChatScreen,"${destroyPartyNo}번 방이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                if(destroyPartyNo == partyRoomInfo.partyNo){
                    finish()
                }
            }
        }

        // 누가 방에서 탈퇴하면 userInfoList 업데이트
        ntUserLeavedParty.observe(this@GroupChatScreen){ leavedUser->
            if(leavedUser != null){
                val commonRePartyChatInfo = Gson().fromJson(leavedUser.getJSONObject("commonRePartyChatInfo").toString(), CommonRePartyChatInfo::class.java)
                if(commonRePartyChatInfo.partyNo == partyRoomInfo.partyNo){
                    userInfoList.removeAll { it.memNo == leavedUser.getInt("memNo") }
                    grpChatAdapter.initUserDataList(userInfoList)
                    grpChatAdapter.sendExitMessage(GrpChatDetailsData(hostInfo.memNo,
                        leavedUser.getInt("memNo"),
                        "탈퇴하셨습니다.", commonRePartyChatInfo.msgNo, true, null))
                    grpChatAdapter.scrollToBottom(binding.DetailsGrpChatRecyclerView)
                }
            }
        }

        ntKickoutUser.observe(this@GroupChatScreen){
            if(it!=null && it.partyNo == partyRoomInfo.partyNo){
                Toast.makeText(this@GroupChatScreen, getString(R.string.kicked_message),Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        ntRequestJoinParty.observe(this@GroupChatScreen){
            if(it!=null){
                val ntMemNo = it.getJSONObject("data").getJSONObject("rqUserInfo").getInt("memNo")
                val ntPartyNo = it.getJSONObject("data").getJSONObject("summaryPartyInfo").getInt("partyNo")
                val alertDialog = AlertDialog.Builder(this@GroupChatScreen)
                alertDialog.apply{
                    setTitle("${ntMemNo}번 님께서 ${ntPartyNo}번 방에 요청을 보냈습니다.")
                    setCancelable(false)
                    setPositiveButton("승인하기"){ dialog, _ ->
                        grpChatViewModel.acceptOrDeclineParty(ntPartyNo, hostInfo.memNo, ntMemNo, true)
                        dialog.dismiss()
                    }
                    setNegativeButton("거절하기"){ dialog, _ ->
                        grpChatViewModel.acceptOrDeclineParty(ntPartyNo, hostInfo.memNo, ntMemNo, false)
                        dialog.dismiss()
                    }
                    show()
                }
            }
        }

        reDeletePartyChat.observe(this@GroupChatScreen){
            if(it!=null){
                if(it.result == 0){
                    val msgNo = it.rqDeletePartyChat.delMsgNo
                    val fromNo = it.rqDeletePartyChat.fromMemNo
                    grpChatAdapter.changeToDeletedMessage(msgNo, fromNo, getString(R.string.deleted_message))
                } else if(it.result == 2){
                    Toast.makeText(this@GroupChatScreen, getString(R.string.delete_fail_timeout), Toast.LENGTH_SHORT).show()
                }
            }
        }

        ntDeletePartyChat.observe(this@GroupChatScreen){
            if(it!=null && it.result == 0){
                val msgNo = it.rqDeletePartyChat.delMsgNo
                val fromNo = it.rqDeletePartyChat.fromMemNo
                val partyNo = it.rqDeletePartyChat.partyNo
                if(partyRoomInfo.partyNo == partyNo){
                    grpChatAdapter.changeToDeletedMessage(msgNo, fromNo, getString(R.string.deleted_message))
                }
            }
        }
    } // observeSocketEvent()

    private fun observeViewModel() = with(grpChatViewModel){

        destroyParty.collect(lifecycleScope){
            if(it) Log.d("로그", "성공적으로 방 삭제됌")
        }

        // 과거 채팅 로그 불러오기
        pastGrpChatLog.collect(lifecycleScope){ partyChatLogData->
            if(partyChatLogData.data.isNotEmpty()){
                for (data in partyChatLogData.data){
                    Log.d("로그", "과거 채팅 로그 넣을 내용: ${data} ");
                    lastMsgNo = when(data.cmd){
                        CMD_RE_PARTY_TEXT_CHAT -> processPartyTextChat(data)
                        CMD_NT_PARTY_TEXT_CHAT -> processPartyTextChat(data)
                        CMD_NT_USER_JOINED_PARTY -> processNtUserJoinedParty(data)
                        CMD_NT_USER_LEAVED_PARTY -> processNtUserLeavedParty(data)
                        else -> {
                            data.data.commonRePartyChatInfo.msgNo
                        }
                    }
                }
            } else {
                stopAddHistory = true
            }
//            scrollToBottomIfFirstExecution()
        }

        // 미래 채팅 로그 불러오기
        futureGrpChatLog.collect(lifecycleScope){ partyChatLogData ->
            if(partyChatLogData.data.isNotEmpty()){
                val notReadMessage = arrayListOf<Long>()
                for(data in partyChatLogData.data){
                    Log.d("로그", "미래 채팅 로그 넣을 내용: ${data} ");
                    futureLastMsgNo = when(data.cmd){
                        CMD_RE_PARTY_TEXT_CHAT -> processFuturePartyTextChat(data)
                        CMD_NT_PARTY_TEXT_CHAT -> {
                            notReadMessage.add(data.data.commonRePartyChatInfo.msgNo)
                            processFuturePartyTextChat(data)
                        }
                        CMD_NT_USER_JOINED_PARTY -> processFutureNtUserJoinedParty(data)
                        CMD_NT_USER_LEAVED_PARTY -> processFutureNtUserLeavedParty(data)
                        else -> {data.data.commonRePartyChatInfo.msgNo}
                    }
                }
                Log.d("로그", "미래 채팅로그 불러온 후 갱신된 futureLastMsgNo: $futureLastMsgNo ");
                grpChatViewModel.readPartyChat(notReadMessage,hostInfo.memNo,partyRoomInfo.partyNo)
                grpChatAdapter.modifyReadCount(notReadMessage)
            } else {
                stopAddFutureLog = true
            }
        }
    }

    private fun processPartyTextChat(data: PartyChatData): Long{
        val  msgNo = data.data.commonRePartyChatInfo.msgNo

        grpChatAdapter.addHistory(GrpChatDetailsData(hostInfo.memNo,
            data.data.commonRePartyChatInfo.fromMemNo,
            data.data.textChatInfo.msg, msgNo,
            readCount = data.data.commonRePartyChatInfo.remainReadCount,
            isDeleted = data.data.commonRePartyChatInfo.isDeleted))
        return msgNo
    }
    private fun processFuturePartyTextChat(data: PartyChatData): Long{
        val msgNo = data.data.commonRePartyChatInfo.msgNo

        grpChatAdapter.sendMessage(GrpChatDetailsData(hostInfo.memNo,
        data.data.commonRePartyChatInfo.fromMemNo,
        data.data.textChatInfo.msg, msgNo,
        readCount = data.data.commonRePartyChatInfo.remainReadCount,
        isDeleted = data.data.commonRePartyChatInfo.isDeleted))
        return msgNo
    }
    private fun processNtUserJoinedParty(data: PartyChatData): Long{
        val msgNo = data.data.commonRePartyChatInfo.msgNo

        grpChatAdapter.addHistory(GrpChatDetailsData(hostInfo.memNo,
            data.data.ntUserInfo.memNo, "가입하셨습니다.", msgNo, true, null))

        return msgNo
    }
    private fun processNtUserLeavedParty(data: PartyChatData): Long{
        val msgNo = data.data.commonRePartyChatInfo.msgNo

        grpChatAdapter.addHistory(GrpChatDetailsData(hostInfo.memNo,
            data.data.ntUserMemNo, "탈퇴하셨습니다.", msgNo, true, null))

        return msgNo
    }
    private fun processFutureNtUserJoinedParty(data: PartyChatData): Long{
        val msgNo = data.data.commonRePartyChatInfo.msgNo

        grpChatAdapter.sendMessage(GrpChatDetailsData(hostInfo.memNo,
            data.data.ntUserInfo.memNo, "가입하셨습니다.", msgNo, true, null))

        return msgNo
    }
    private fun processFutureNtUserLeavedParty(data: PartyChatData): Long{
        val msgNo = data.data.commonRePartyChatInfo.msgNo

        grpChatAdapter.sendMessage(GrpChatDetailsData(hostInfo.memNo,
            data.data.ntUserMemNo, "탈퇴하셨습니다.", msgNo, true, null))

        return msgNo
    }
    private fun scrollToBottomIfFirstExecution(){
        if(isFirstExecution){
            grpChatAdapter.scrollToBottom(binding.DetailsGrpChatRecyclerView)
            isFirstExecution = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> { // R.id.home은 안드로이드 시스템의 리소스 식별자로, 액션바의 툴바의 홈/업 버튼을 의미
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            R.id.viewMemberList -> {
                val customDialog = ViewMemberListDialog(this)
                customDialog.setOnMemberClickListener {
                    // 추방할 유저정보가 넘겨져온다.
                    grpChatViewModel.kickUser(partyRoomInfo.partyNo,partyRoomInfo.memNo,it.memNo)
                    if(userInfoList.contains(it)){
                        userInfoList.remove(it)
                    }
                }
                customDialog.show(userInfoList, partyRoomInfo.memNo==hostInfo.memNo, hostInfo)
            }
            R.id.exitRoom -> {
                makeExitRoomDialog()
            }
            R.id.deleteRoom -> {
                makeDeleteRoomDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 클릭하면 옵션메뉴(햄버거)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_grp_chat_hamburger, menu)

        if(partyRoomInfo.memNo != hostInfo.memNo){
            val menuItem = menu?.findItem(R.id.deleteRoom)
            menuItem?.isVisible = false
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun makeDeleteRoomDialog(){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("${partyRoomInfo.partyNo}번 방을 삭제하시겠습니까?")
        alertDialog.setPositiveButton("삭제하기"){dialog,_ ->
            grpChatViewModel.deleteRoom(partyRoomInfo.partyNo,hostInfo.memNo)
            dialog.dismiss()
            finish()
        }
        alertDialog.setNegativeButton("취소하기"){dialog,_->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun makeExitRoomDialog(){
        val alertDialog = AlertDialog.Builder(this)
        if(partyRoomInfo.memNo == hostInfo.memNo){
            alertDialog.setTitle("방 주인이 나가면 방이 삭제됩니다. 방을 나가시겠습니까?")
        } else {
            alertDialog.setTitle("방을 나가시겠습니까?")
        }
        alertDialog.setPositiveButton("방 나가기"){dialog, _ ->
            grpChatViewModel.exitRoom(partyRoomInfo.partyNo,hostInfo.memNo)
            dialog.dismiss()
            finish()
        }
        alertDialog.setNegativeButton("취소하기"){dialog,_->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun jsonArrayToArrayList(jsonArray: JSONArray): ArrayList<Long>{
        val readMsgNosList = arrayListOf<Long>()
        for(i in 0 until jsonArray.length() ){
            readMsgNosList.add(jsonArray.getLong(i))
        }
        return readMsgNosList
    }

    override fun onPause() {
        super.onPause()
        SocketEventListener.resetNtRequestJoinParty()
        lifecycleScope.launch{
            dao.updateHistory(GroupChatEnterHistory(primaryKey,partyRoomInfo.partyNo,hostInfo.memNo,enterTime,System.currentTimeMillis()))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSocketDisconnected(event: SocketDisconnectedEvent){
        Toast.makeText(this, "연결 끊킴",Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetSocket(){
        SocketEventListener.apply{
            resetRePartyChat()
            resetNtPartyChat()
            resetNtRequestJoinParty()
            resetNtDestroyParty()
            resetNtUserJoinParty()
            resetNtReadPartyChat()
            resetNtKickoutUser()
            resetReDeletePartyChat()
            resetNtDeletePartyChat()
        }
    }

}
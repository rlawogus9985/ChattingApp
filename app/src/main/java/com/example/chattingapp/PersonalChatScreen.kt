package com.example.chattingapp

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.adapter.DetailsPsnChatAdapter
import com.example.chattingapp.data.ChatDetailsData
import com.example.chattingapp.data.PsnChaItemData
import com.example.chattingapp.data.UserData
import com.example.chattingapp.databinding.ActivityPersonalChatScreenBinding
import com.example.chattingapp.eventbus.SocketDisconnectedEvent
import com.example.chattingapp.extensions.collect
import com.example.chattingapp.viewmodel.PsnChatViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

class PersonalChatScreen : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalChatScreenBinding
    private lateinit var chatDetailsAdapter: DetailsPsnChatAdapter
    private val psnChatViewModel: PsnChatViewModel by viewModels()
    private var lastMsgNo: Long = System.currentTimeMillis()
    private var countPerPage: Int = 15
    private var stopAddHistory: Boolean = false

    private lateinit var others: UserData
    private lateinit var hostInfo: UserData
    private var isFirstExecution = true

    private val CMD_RE_1ON1_TEXT_CHAT = "Re1On1TextChat"
    private val CMD_NT_1ON1_TEXT_CHAT = "Nt1On1TextChat"

    private val eventBus: EventBus = EventBus.getDefault()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalChatScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventBus.register(this)

        // 툴바 활성화
        setSupportActionBar(binding.toolbar)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            others = intent.getParcelableExtra("others",UserData::class.java)!!
            hostInfo = intent.getParcelableExtra("hostInfo",UserData::class.java)!!
        } else {
            others = intent.getParcelableExtra("others")!!
            hostInfo = intent.getParcelableExtra("hostInfo")!!
        }

        supportActionBar?.apply{
            title = others.nickName // 제목 등록
            setDisplayHomeAsUpEnabled(true) // 업버튼 활성화
        }

        resetSocket()

        chatDetailsAdapter = DetailsPsnChatAdapter(hostInfo.memNo)

        // 채팅 로그 API 호출
        psnChatViewModel.rq1On1ChatLog(hostInfo.memNo, others.memNo, lastMsgNo, countPerPage)

        binding.chatDetailsRecyclerView.apply{
            adapter = chatDetailsAdapter
            layoutManager = LinearLayoutManager(this@PersonalChatScreen, LinearLayoutManager.VERTICAL, false)

            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(!binding.chatDetailsRecyclerView.canScrollVertically(-1)){
                        Log.d("로그", "상단 닿음")
                        if(!stopAddHistory){
                            Log.d("로그", "채팅로그 업데이트")
                            psnChatViewModel.rq1On1ChatLog(hostInfo.memNo, others.memNo, lastMsgNo, countPerPage)
                        }
                    }
                }
            })
        }

        // 채팅 입력
        binding.chatEnterButton.setOnClickListener{
            val chat = binding.chatContext.text.toString()
            psnChatViewModel.chatting(chat,hostInfo.memNo, others.memNo)
            binding.chatContext.setText("")
        }

        binding.chatContext.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val isTextEmpty = s.isNullOrEmpty()
                binding.chatEnterButton.isEnabled = !isTextEmpty
            }
        })

        // 아이템 삭제
        chatDetailsAdapter.setOnDeleteItemClickListener {
            psnChatViewModel.rqDelete1On1Chat(it.msgNo,it.hostInfo.memNo,it.others.memNo)
        }

        observeViewModel()
        observeSocket()
    } // onCreate

    private fun observeSocket() = with(SocketEventListener) {

        // 내가 친 채팅
        re1On1Chat.observe(this@PersonalChatScreen){
            if(it != null){
                chatDetailsAdapter.sendMessage(ChatDetailsData(hostInfo, others,
                    it.data.textChatInfo.msg, it.data.commonRe1On1ChatInfo.msgNo,
                    it.data.commonRe1On1ChatInfo.remainReadCount))
                chatDetailsAdapter.scrollToBottom(binding.chatDetailsRecyclerView)
            }
        }

        // 상대가 친 채팅
        nt1On1Chat.observe(this@PersonalChatScreen){
            if (it != null) {
                if (it.data.commonRe1On1ChatInfo.fromMemNo == others.memNo) {
                    val info = it.data.commonRe1On1ChatInfo
                    psnChatViewModel.read1On1Chat(listOf(info.msgNo), info.toMemNo, info.fromMemNo)
                    chatDetailsAdapter.sendMessage(
                        ChatDetailsData(
                            others, hostInfo,
                            it.data.textChatInfo.msg, info.msgNo, 0
                        )
                    ) // nt1On1Chat을 받을때 read1On1Chat을 보내니 readCount는 0으로..수동..
                    chatDetailsAdapter.scrollToBottom(binding.chatDetailsRecyclerView)
                } else {
                    Toast.makeText(this@PersonalChatScreen,"${it.data.commonRe1On1ChatInfo.fromMemNo} : ${it.data.textChatInfo.msg}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 상대가 메시지 읽음
        ntRead1On1Chat.observe(this@PersonalChatScreen){
            if(it!=null && it.getJSONObject("data").getInt("fromMemNo") == others.memNo
                && it.getJSONObject("data").optJSONArray("readMsgNos") != null){
                val parsingList = it.getJSONObject("data").getJSONArray("readMsgNos")
                val readMsgNosList = jsonArrayToArrayList(parsingList)

                chatDetailsAdapter.modifyReadCount(readMsgNosList)
            }
        }

        // 내가 메시지 지움
        reDelete1On1Chat.observe(this@PersonalChatScreen){
            if(it!=null){
                if(it.result == 0){
                    val msgNo = it.rqDelete1On1Chat.delMsgNo
                    val fromNo = it.rqDelete1On1Chat.fromMemNo
                    val toNo = it.rqDelete1On1Chat.toMemNo
                    chatDetailsAdapter.changeToDeletedMessage(msgNo,fromNo,toNo, getString(R.string.deleted_message))
                } else if(it.result == 2){
                    Toast.makeText(this@PersonalChatScreen, getString(R.string.delete_fail_timeout), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 상대가 메시지 지움
        ntDelete1On1Chat.observe(this@PersonalChatScreen){
            if(it!=null && it.result == 0){
                val msgNo = it.rqDelete1On1Chat.delMsgNo
                val fromNo = it.rqDelete1On1Chat.fromMemNo
                val toNo = it.rqDelete1On1Chat.toMemNo
                chatDetailsAdapter.changeToDeletedMessage(msgNo,fromNo,toNo, getString(R.string.deleted_message))
            }
        }

    } // observeSocket()

    private fun observeViewModel() = with(psnChatViewModel){

        // 채팅로그 조회
        psnChatLog.collect(lifecycleScope){ psnChatLogData ->
            if(psnChatLogData.data.isNotEmpty()){
                val data = psnChatLogData.data
                val notReadMessage = arrayListOf<Long>()
                for (psnTextChatData in data){
                    if (psnTextChatData != null) {
                        lastMsgNo = when (psnTextChatData.cmd){
                            CMD_RE_1ON1_TEXT_CHAT -> processRe1On1TextChatData(psnTextChatData.data)
                            CMD_NT_1ON1_TEXT_CHAT -> {
                                if(psnTextChatData.data.commonRe1On1ChatInfo.remainReadCount == 1){
                                    notReadMessage.add(psnTextChatData.data.commonRe1On1ChatInfo.msgNo)
                                }
                                processNt1On1TextChatData(psnTextChatData.data)
                            }
                            else -> processDefaultData(psnTextChatData.data)
                        }
                    }
                }
                psnChatViewModel.read1On1Chat(notReadMessage,hostInfo.memNo,others.memNo)
                chatDetailsAdapter.modifyReadCount(notReadMessage)
            } else {
                stopAddHistory = true
            }
            scrollToBottomIfFirstExecution()
        }
    } // observeViewModel()

    private fun processRe1On1TextChatData(data: PsnChaItemData): Long{
        chatDetailsAdapter.addHistory(ChatDetailsData(hostInfo, others,
            data.textChatInfo.msg, data.commonRe1On1ChatInfo.msgNo,
            data.commonRe1On1ChatInfo.remainReadCount, data.commonRe1On1ChatInfo.isDeleted))
        return data.commonRe1On1ChatInfo.msgNo
    }
    private fun processNt1On1TextChatData(data: PsnChaItemData): Long{
        chatDetailsAdapter.addHistory(ChatDetailsData(others, hostInfo,
            data.textChatInfo.msg, data.commonRe1On1ChatInfo.msgNo,
            data.commonRe1On1ChatInfo.remainReadCount,data.commonRe1On1ChatInfo.isDeleted))
        return data.commonRe1On1ChatInfo.msgNo
    }
    private fun processDefaultData(data: PsnChaItemData): Long{
        return data.commonRe1On1ChatInfo.msgNo
    }
    private fun scrollToBottomIfFirstExecution(){
        if(isFirstExecution){
            isFirstExecution = false
            chatDetailsAdapter.scrollToBottom(binding.chatDetailsRecyclerView)
        }
    }
     private fun jsonArrayToArrayList(jsonArray: JSONArray): ArrayList<Long>{
        val readMsgNosList = arrayListOf<Long>()
        for(i in 0 until jsonArray.length() ){
            readMsgNosList.add(jsonArray.getLong(i))
        }
        return readMsgNosList
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> { // R.id.home은 안드로이드 시스템의 리소스 식별자로, 액션바의 툴바의 홈/업 버튼을 의미
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetSocket(){
        SocketEventListener.apply{
            resetRe1On1Chat()
            resetNt1On1Chat()
            resetNtRead1On1Chat()
            resetReDelete1On1Chat()
            resetNtDelete1On1Chat()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSocketDisconnected(event: SocketDisconnectedEvent){
        Toast.makeText(this, "연결 끊킴",Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventBus.unregister(this)
    }

}
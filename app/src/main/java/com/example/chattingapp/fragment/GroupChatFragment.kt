package com.example.chattingapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.GroupChatScreen
import com.example.chattingapp.R
import com.example.chattingapp.SocketEventListener
import com.example.chattingapp.adapter.GroupAdapter
import com.example.chattingapp.data.PartyItem
import com.example.chattingapp.data.PartyMemberListRequest
import com.example.chattingapp.data.UserData
import com.example.chattingapp.databinding.FragmentGroupChatBinding
import com.example.chattingapp.dialog.MakeRoomDialog
import com.example.chattingapp.extensions.clicks
import com.example.chattingapp.extensions.collect
import com.example.chattingapp.extensions.throttleFirst
import com.example.chattingapp.viewmodel.MainLobbyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GroupChatFragment : Fragment() , View.OnClickListener{

    private val viewModel: MainLobbyViewModel by activityViewModels()
    private lateinit var binding: FragmentGroupChatBinding
    private lateinit var groupAdapter: GroupAdapter

    private val hostInfo by lazy{
        viewModel.getHostInfo()
    }
    private var lastTimeStamp: Long = System.currentTimeMillis()
    private var stopAddList = false
    private var isAddList = false
    private var isClickRefresh = false
    private var clickedPartyItem: PartyItem? = null
    private var partyRoomList = ArrayList<PartyItem?>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // GroupAdapter안에 클릭 이벤트 넣을 예정
        groupAdapter = GroupAdapter()

        binding.groupChatRecyclerView.apply{
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(!binding.groupChatRecyclerView.canScrollVertically(1)){
                        if(!stopAddList){
                            isAddList = true
                            viewModel.getSummaryPartyLists(lastTimeStamp)
                        }
                    }
                }
            })
        }

//        binding.makeGroupRoomButton.setOnClickListener(this)
        val makeRoom = binding.makeGroupRoomButton.clicks().throttleFirst(700)
        makeRoom.collect(viewLifecycleOwner.lifecycleScope){
            val customDialog = MakeRoomDialog(requireContext())
            customDialog.setOnMakeRoomClickListener {
                // 방만들어진 데이터
                viewModel.makeGroupRoom(it)
            }
            customDialog.show(hostInfo)
        }

        binding.refreshList.setOnClickListener(this)

        groupAdapter.setItemClickListener(object: GroupAdapter.OnItemClickListener{
            override fun onClick(v: View, realItem: PartyItem) {

                // API요청
                val req = PartyMemberListRequest(realItem.partyNo,realItem.memNo,20230517163022,30)
                viewModel.rqPartyMemberList(req)

                clickedPartyItem = realItem
            }
        })

        setObserve()

        observeViewModel()

    } // onViewCreated


    private fun navigateGroupChatScreen(partyRoomInfo: PartyItem, hostInfo: UserData, UserInfo: List<UserData>){
        val intent = Intent(requireContext(), GroupChatScreen::class.java)
        val bundle = Bundle().apply{
            putParcelable("partyRoomInfo",partyRoomInfo)
            putParcelable("hostInfo",hostInfo)
            putParcelableArrayList("UserInfos", ArrayList(UserInfo))
        }
        intent.putExtras(bundle)
        requireContext().startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setObserve(){
        SocketEventListener.reJoinParty.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(),"${it.values}",Toast.LENGTH_SHORT).show()
            if(it.keys.contains(0)){
                viewModel.getSummaryPartyLists()
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.refreshList -> {
                isClickRefresh = true
                viewModel.getSummaryPartyLists()
            }
        }
    }

    private fun observeViewModel() {

        // 방을 만들면 새로고침
        viewModel.createPartyResult.collect(viewLifecycleOwner.lifecycleScope) {
            viewModel.getSummaryPartyLists(System.currentTimeMillis()+500)
        }

        lifecycleScope.launch{
            viewModel.summaryPartyLists.collect{
                if(it.isNotEmpty()){
                    lastTimeStamp = it.last().createAt
                    stopAddList = false
                } else {
                    stopAddList = true
                }
                if(isAddList){ // 안에서 stopAddList가 true인지 봐서 더 안붙일수도있는데 굳이?
                    isAddList = false
                    partyRoomList = ArrayList(groupAdapter.currentList)
                    partyRoomList.addAll(it)
                    groupAdapter.submitList(partyRoomList)
                } else {
                    groupAdapter.submitList(it)
                    delay(300)
                    binding.groupChatRecyclerView.smoothScrollToPosition(0)
                    if(isClickRefresh){
                        isClickRefresh = false
                        Toast.makeText(requireContext(),"새로고침 완료", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.partyMemberLists.collect(viewLifecycleOwner.lifecycleScope) {
            if (it != null) {
                val memNoLists = ArrayList<Int>()
                for (i in it) {
                    memNoLists.add(i.memNo)
                }
                val reallyEnter = AlertDialog.Builder(requireContext())
                if (hostInfo.memNo in memNoLists) {
                    reallyEnter.setTitle("${clickedPartyItem!!.partyNo}번 방에 이미 가입되었습니다. 입장하시겠습니까?")
                    reallyEnter.setPositiveButton("입장하기") { dialog, _ ->
                        dialog?.dismiss()
                        navigateGroupChatScreen(clickedPartyItem!!,hostInfo,it)
                    }
                } else if (clickedPartyItem!!.isAutoJoin) {
                    reallyEnter.setTitle("${clickedPartyItem!!.partyNo}번 방은 공개방입니다. 가입하시겠습니까?")
                    reallyEnter.setPositiveButton("가입하기") { dialog, _ ->
                        viewModel.joinParty(clickedPartyItem!!.partyNo, clickedPartyItem!!.memNo, hostInfo.memNo)
                        dialog?.dismiss()
                    }
                } else {
                    reallyEnter.setTitle("${clickedPartyItem!!.partyNo}번 방은 비밀방입니다. 신청을 보내시겠습니까?")
                    reallyEnter.setPositiveButton("신청보내기") { dialog, _ ->
                        viewModel.joinParty(clickedPartyItem!!.partyNo, clickedPartyItem!!.memNo, hostInfo.memNo)
                        dialog?.dismiss()
                    }
                }
                reallyEnter.show()
            }
        }
    }
}
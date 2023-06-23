package com.example.chattingapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.data.ChatDataRequest
import com.example.chattingapp.data.GrpChatDetailsData
import com.example.chattingapp.data.UserData
import com.example.chattingapp.databinding.ItemChatDetailRightBinding
import com.example.chattingapp.databinding.ItemChatDetailsLeftBinding
import com.example.chattingapp.databinding.ItemExitEnterLogBinding

class DetailsGrpChatAdapter : ListAdapter<UserData, RecyclerView.ViewHolder>(UserDataDiffCallback()) {

//    private var userDataList: ArrayList<UserData> = ArrayList()
    private var item: ArrayList<GrpChatDetailsData> = ArrayList()

    private val VIEW_TYPE_RIGHT = 0
    private val VIEW_TYPE_LEFT = 1
    private val VIEW_EXIT_ENTER_LOG = 2

    inner class RightViewHolder(private val binding:ItemChatDetailRightBinding): RecyclerView.ViewHolder(binding.root){
        private val msgTextView = binding.chatTextView
        init{
            msgTextView.setOnLongClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    val clickedItem = item[position]
                    if(!clickedItem.isDeleted){
                        showDeleteConfirmationDialog(itemView.context, clickedItem)
                    }
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
        }
        fun bind(data: GrpChatDetailsData){
            val userData = currentList.first {
                it.memNo == data.hostNo
            }
            binding.rightUserData = userData
            binding.chatDetailRequestData = ChatDataRequest(null,data)
        }

    }

    inner class LeftViewHolder(private val binding:ItemChatDetailsLeftBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(data: GrpChatDetailsData){
            val userData = currentList.firstOrNull {
                it.memNo == data.fromNo
            }
            binding.leftUserData = userData
            binding.chatDetailRequestData = ChatDataRequest(null,data)
        }
    }
    inner class ExitEnterLogHolder(private val binding: ItemExitEnterLogBinding): RecyclerView.ViewHolder(binding.root){
        private val logText = binding.exitEnterLog
        fun bind(data: GrpChatDetailsData){
            logText.text = itemView.context.getString(R.string.exit_enter_text,
            data.fromNo,data.chat)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_RIGHT -> {
                val binding = ItemChatDetailRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                RightViewHolder(binding)
            }
            VIEW_TYPE_LEFT -> {
                val binding = ItemChatDetailsLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                LeftViewHolder(binding)
            }
            else -> {
                val binding = ItemExitEnterLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ExitEnterLogHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = item[position]

        when(getItemViewType(position)){
            VIEW_TYPE_RIGHT -> {
                val rightHolder = holder as RightViewHolder
                rightHolder.bind(currentItem)
            }
            VIEW_TYPE_LEFT -> {
                val leftHolder = holder as LeftViewHolder
                leftHolder.bind(currentItem)
            }
            VIEW_EXIT_ENTER_LOG -> {
                val exitEnterHolder = holder as ExitEnterLogHolder
                exitEnterHolder.bind(currentItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(item[position].isNtLog){
            VIEW_EXIT_ENTER_LOG
        } else if(item[position].hostNo == item[position].fromNo){
            VIEW_TYPE_RIGHT
        } else {
            VIEW_TYPE_LEFT
        }
    }

    fun initUserDataList(data: ArrayList<UserData>){
        // DiffUtil을 사용하지 않고 notifyDataSetChanged를 사용하면 화면상 바로 바뀌긴하는데
        // 성능상 DiffUtil을 사용. 대신 바로 바뀌지 않고 RecyclerView가 다시 그려질때 바뀐 화면으로 나온다.
        submitList(data)
    }

    fun sendMessage(message: GrpChatDetailsData){
        item.add(message)
        notifyItemInserted(item.size - 1)
    }
    fun addHistory(message: GrpChatDetailsData){
        item.add(0,message)
        notifyItemInserted(0)
    }
    fun sendExitMessage(message: GrpChatDetailsData){
        item.add(message)
        notifyItemInserted(item.size - 1)
    }

    fun scrollToBottom(recyclerView: RecyclerView){
        recyclerView.scrollToPosition(item.size - 1)
    }

    fun changeToDeletedMessage(msgNo: Long, fromNo:Int, deletedMessage: String){
        for (index in item.indices){
            val newItem = item[index]
            if(newItem.fromNo == fromNo && newItem.msgNo == msgNo
            ){
                item[index] = newItem.copy(chat = deletedMessage, isDeleted = true)
                notifyItemChanged(index)
                break
            }
        }
    }

    fun modifyReadCount(msgList: ArrayList<Long>){
        if(item.isNotEmpty()) {

            item.forEachIndexed { index, grpChatDetailsData ->
                if (msgList.contains(grpChatDetailsData.msgNo)) {
                    item[index] = grpChatDetailsData.copy(readCount = grpChatDetailsData.readCount?.minus(1))
                }
            }

            val startPosition = item.indexOfFirst { msgList.contains(it.msgNo) }
            val endPosition = item.indexOfLast { msgList.contains(it.msgNo) }
            Log.d("로그", "그룹챗 StratPosition: $startPosition, endPosition : $endPosition ")

            if (startPosition != -1 && endPosition != -1) {
                if (startPosition == endPosition) {
                    notifyItemChanged(startPosition)
                } else {
                    notifyItemRangeChanged(startPosition, endPosition - startPosition + 1)
                }
            }
        }
    }

    fun showDeleteConfirmationDialog(context: Context, clickedItem: GrpChatDetailsData){
        val alertdialog = AlertDialog.Builder(context)
            .setTitle("삭제 확인")
            .setMessage("해당 메시지를 삭제하시겠습니까?")
            .setPositiveButton("삭제"){ dialog, _ ->
                deleteItem.onClick(clickedItem)
                dialog.dismiss()
            }
            .setNegativeButton("취소"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertdialog.show()
    }

    fun setOnDeleteItemClickListener(deleteItem: (GrpChatDetailsData) -> Unit){
        this.deleteItem = object: OnDeleteItemClickListener{
            override fun onClick(clickedItem: GrpChatDetailsData) {
                deleteItem(clickedItem)
            }
        }
    }
    interface OnDeleteItemClickListener{
        fun onClick(clickedItem: GrpChatDetailsData)
    }
    private lateinit var deleteItem: OnDeleteItemClickListener

}
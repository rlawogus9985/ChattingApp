package com.example.chattingapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.data.ChatDataRequest
import com.example.chattingapp.data.ChatDetailsData
import com.example.chattingapp.databinding.ItemChatDetailRightBinding
import com.example.chattingapp.databinding.ItemChatDetailsLeftBinding

class DetailsPsnChatAdapter(private val host: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var item: ArrayList<ChatDetailsData> = ArrayList()

    private val VIEW_TYPE_RIGHT = 0
    private val VIEW_TYPE_LEFT = 1

    inner class RightViewHolder(private val binding: ItemChatDetailRightBinding): RecyclerView.ViewHolder(binding.root){

        private val messageTextView = binding.chatTextView
        init{
            messageTextView.setOnLongClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    val clickedItem = item[position]
                    if(!clickedItem.isDeleted){
                        showDeleteConfirmationDialog(itemView.context,clickedItem)
                    }
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
        }
        fun bind(data: ChatDetailsData){
            binding.chatDetailRequestData = ChatDataRequest(data,null)
        }
    }

    inner class LeftViewHolder(private val binding: ItemChatDetailsLeftBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(data: ChatDetailsData){
            binding.chatDetailRequestData = ChatDataRequest(data,null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_RIGHT -> {
                val binding = ItemChatDetailRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                RightViewHolder(binding)
            }
            else -> {
                val binding = ItemChatDetailsLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                LeftViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = item[position]

        if(getItemViewType(position) == VIEW_TYPE_RIGHT){
            val rightHolder = holder as RightViewHolder
            rightHolder.bind(currentItem)
        } else if (getItemViewType(position) == VIEW_TYPE_LEFT){
            val leftHolder = holder as LeftViewHolder
            leftHolder.bind(currentItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (host == item[position].hostInfo.memNo){
            VIEW_TYPE_RIGHT
        } else {
            VIEW_TYPE_LEFT
        }
    }

    fun sendMessage(message: ChatDetailsData){
        item.add(message)
        notifyItemInserted(item.size - 1)
    }
    fun addHistory(message: ChatDetailsData){
        item.add(0,message)
        notifyItemInserted(0)
    }
    fun modifyReadCount(msgList: ArrayList<Long>){
        if(item.isNotEmpty()) {

            item.forEachIndexed { index, chatDetailsData ->
                if (msgList.contains(chatDetailsData.msgNo)) {
                    item[index] = chatDetailsData.copy(readCount = 0)
                }
            }
            val startPosition = item.indexOfFirst { msgList.contains(it.msgNo) }
            val endPosition = item.indexOfLast { msgList.contains(it.msgNo) }

            if (startPosition != -1 && endPosition != -1) {
                if (startPosition == endPosition) {
                    notifyItemChanged(startPosition)
                } else {
                    notifyItemRangeChanged(startPosition, endPosition - startPosition + 1)
                }
            }
        }
    }

    fun scrollToBottom(recyclerView: RecyclerView){
        recyclerView.scrollToPosition(item.size - 1 )
    }

    fun changeToDeletedMessage(msgNo: Long, fromNo:Int, toNo:Int, deletedMessage: String){

        for (index in item.indices){
            val newItem = item[index]
            if(newItem.hostInfo.memNo == fromNo &&
                    newItem.others.memNo == toNo && newItem.msgNo == msgNo
            ){
                val updatedItem = newItem.copy(chat = deletedMessage, isDeleted = true)
                item[index] = updatedItem
                notifyItemChanged(index)
                break
            }
        }
    }

    fun showDeleteConfirmationDialog(context: Context, clickedItem: ChatDetailsData){
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

    fun setOnDeleteItemClickListener(deleteItem: (ChatDetailsData) -> Unit){
        this.deleteItem = object: OnDeleteItemClickListener {
            override fun onClick(clickedItem: ChatDetailsData) {
                deleteItem(clickedItem)
            }
        }
    }
    interface OnDeleteItemClickListener{
        fun onClick(clickedItem: ChatDetailsData)
    }
    private lateinit var deleteItem: OnDeleteItemClickListener

}
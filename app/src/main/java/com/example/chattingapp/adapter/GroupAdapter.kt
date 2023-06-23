package com.example.chattingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.data.PartyItem
import com.example.chattingapp.databinding.ItemGroupChatBinding

class GroupAdapter : ListAdapter<PartyItem, GroupAdapter.CustomViewHolder>(PartyListDiffCallback()) {

//    private var item: ArrayList<PartyItem> = ArrayList()

    inner class CustomViewHolder(val binding: ItemGroupChatBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(data: PartyItem){
            binding.itemGroupChatData = data
        }

        init{
            itemView.setOnClickListener {
                itemClickListener.onClick(it, getItem(adapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemGroupChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    // 2 리스너 인터페이스
    interface OnItemClickListener {
        fun onClick(v: View, realItem: PartyItem)
    }
    // 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener){
        this.itemClickListener = onItemClickListener
    }
    // setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener: OnItemClickListener

    /*fun setItemList(data: ArrayList<PartyItem>, recyclerView: RecyclerView) {
        if(item.isEmpty()){
            item = data
//            notifyItemRangeInserted(0,item.size-1)
            submitList(item)
        } else {
            val matchingItem = item.find{existingItem ->
                data.any { newItem -> newItem.createAt == existingItem.createAt}
            }
            if(matchingItem != null){
                item = data
//                notifyDataSetChanged()
                submitList(item)
                recyclerView.smoothScrollToPosition(0)
            } else {
                item.addAll(data)
                submitList(item)
//                notifyItemRangeInserted(originalSize, data.size)
            }
        }
    }*/
}

package com.example.chattingapp.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.chattingapp.R
import com.example.chattingapp.data.ChatDataRequest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object BindingAdapters {

    @BindingAdapter("ImageWithUrl")
    @JvmStatic
    fun setImage(view: ImageView, imageUrl: String?){
        if(imageUrl == null){
            Glide.with(view.context)
                .load(R.drawable.baseline_person_outline_24)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(view)
        } else {
            Glide.with(view.context)
                .load(imageUrl)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(35)))
                .error(R.drawable.cat_cloud)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(view)
        }
    }

    @BindingAdapter("FormatMillis")
    @JvmStatic
    fun setLocalTime(view: TextView, time: Long){
        val instant = Instant.ofEpochMilli(time)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatTime = localDateTime.format(DateTimeFormatter.ofPattern("a h시 m분"))
        view.text = formatTime
    }

    @BindingAdapter("ReadCount")
    @JvmStatic
    fun setReadCount(view: TextView, count: Int){
        if(count == 0){
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            view.text = count.toString()
        }
    }

    @BindingAdapter("IsLocked")
    @JvmStatic
    fun setLockedImaged(view: ImageView, isAutoJoin: Boolean){
        view.visibility = if (isAutoJoin) View.GONE else View.VISIBLE
    }

    @BindingAdapter("allChatText")
    @JvmStatic
    fun setPsnChatText(view: TextView, chatData: ChatDataRequest?){
        val chatDetailsData = chatData?.chatDetailsData
        val grpChatDetailsData = chatData?.grpChatDetailsData
        val deletedMessage = view.context.getString(R.string.deleted_message)

        if(chatDetailsData != null) {
            if (chatDetailsData.isDeleted) {
                view.text = deletedMessage
            } else {
                view.text = chatDetailsData.chat
            }
        } else if(grpChatDetailsData != null) {
            if (grpChatDetailsData.isDeleted) {
                view.text = deletedMessage
            } else {
                view.text = grpChatDetailsData.chat
            }
        }
    }

}
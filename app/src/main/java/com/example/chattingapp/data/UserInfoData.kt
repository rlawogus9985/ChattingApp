package com.example.chattingapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfoData(
    val data: UserData?
) : Parcelable

@Parcelize
data class UserData(
    val memNo: Int = 0,
    val nickName: String = "",
    val mainProfileUrl: String = ""
): Parcelable


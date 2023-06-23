package com.example.chattingapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReAuthUserData(
    val memNo: Int,
    val nickName: String,
    val mainProfile: String,
    val result: Int
): Parcelable

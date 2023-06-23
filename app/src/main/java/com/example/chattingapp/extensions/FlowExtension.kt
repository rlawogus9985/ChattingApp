package com.example.chattingapp.extensions

import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.chattingapp.databinding.ActivityMainBinding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

inline fun <T> Flow<T>.collect(
    externalScope: LifecycleCoroutineScope,
    crossinline action: (T) -> Unit
) = onEach { action.invoke(it) }.launchIn(externalScope)

fun <T> Flow<T>.throttleFirst(periodMillis: Long): Flow<T>{
    require(periodMillis > 0) {"period should be positive"}
    return flow{
        var lastTime = 0L
        collect{ value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime >= periodMillis){
                lastTime = currentTime
                emit(value)
            }
        }
    }
}

fun clickToFlow(binding: ActivityMainBinding): Flow<Unit>{
    return callbackFlow {
        val listener = View.OnClickListener {
            trySend(Unit).isSuccess
        }
        binding.loginButton.setOnClickListener(listener)
        awaitClose{
            binding.loginButton.setOnClickListener(null)
        }
    }
}

fun View.clicks(): Flow<Unit>{
    return callbackFlow {
        val listener = View.OnClickListener {
            trySend(Unit).isSuccess
        }
        setOnClickListener(listener)
        awaitClose{
            setOnClickListener(null)
        }
    }
}
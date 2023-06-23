package com.example.chattingapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chattingapp.databinding.ActivityMainBinding
import com.example.chattingapp.eventbus.SocketConnectErrorEvent
import com.example.chattingapp.extensions.clicks
import com.example.chattingapp.extensions.collect
import com.example.chattingapp.extensions.hideKeyboard
import com.example.chattingapp.extensions.throttleFirst
import com.example.chattingapp.viewmodel.MainViewModel
import kotlinx.coroutines.flow.Flow
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private var memberId: Int? = null

    private val eventBus: EventBus = EventBus.getDefault()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            binding.highLevelConstraint.setOnApplyWindowInsetsListener { _, insets ->
                binding.highLevelConstraint.setPadding(0,insets.getInsets(WindowInsets.Type.statusBars()).top,0,0)
                insets
            }
        } else {
            binding.highLevelConstraint.translationY = getStatusBarHeight(this).toFloat()
        }

        binding.highLevelConstraint.setOnClickListener(this)

        observeSocket()

        val throttledClicks: Flow<Unit> = binding.loginButton.clicks().throttleFirst(700)

        throttledClicks.collect(lifecycleScope){
            memberId = binding.memberId.text.toString().toIntOrNull()
            if(memberId !in 10001..10012 || memberId == null){
                Toast.makeText(this, getString(R.string.invalid_login_memberId), Toast.LENGTH_SHORT).show()
                return@collect
            }

            SocketEventListener.initializeSocket()
            SocketEventListener.connectionSocket()
        }

        SocketEventListener.socketConnected.collect(lifecycleScope){
            Log.d("로그", "socketConnected가 바뀐걸 인식함")
            if(it=="connect"){
                SocketEventListener.registSocketCallback()
                mainViewModel.loginUser(memberId!!)
                binding.memberId.setText("")
            }
        }

    } // onCreate()

    override fun onStart() {
        super.onStart()
        eventBus.register(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.high_level_constraint -> {
                this@MainActivity.hideKeyboard()
            }
        }
    }

    private fun observeSocket() = with(SocketEventListener){
        reAuthUserData.observe(this@MainActivity){
            val intent = Intent(this@MainActivity, MainLobby::class.java)
            val bundle = Bundle()
            bundle.putParcelable("hostInfo",it)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) { context.resources.getDimensionPixelSize(resourceId) } else { 0 }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSocketConnectError(event: SocketConnectErrorEvent){
        Toast.makeText(this,"연결 오류. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketHandler.disconnect()
    }
}


package com.example.chattingapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.adapter.PersonalAdapter
import com.example.chattingapp.databinding.FragmentPersonalChatBinding
import com.example.chattingapp.extensions.collect
import com.example.chattingapp.viewmodel.MainLobbyViewModel

class PersonalChatFragment : Fragment() {

    private val viewModel: MainLobbyViewModel by activityViewModels()
    private lateinit var binding: FragmentPersonalChatBinding
    private lateinit var personalAdapter: PersonalAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        personalAdapter = PersonalAdapter(null)

        observeViewModel()

        binding.personalRecyclerView.apply{
            adapter = personalAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            addItemDecoration(DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL))
        }
        personalAdapter.hostInfo = viewModel.getHostInfo()
    }

    private fun observeViewModel() = with(viewModel) {
        userInfoLists.collect(viewLifecycleOwner.lifecycleScope){
            personalAdapter.setItemList(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPersonalChatBinding.inflate(inflater, container, false)
        return binding.root
    }

}
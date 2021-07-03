package com.example.kanetaka.problem.contriviewer.page.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.databinding.FragmentDetailBinding
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DetailFragment : Fragment() {

    // Navigation Editor で自動生成された遷移パラメータ
    val args: DetailFragmentArgs by navArgs()

    private var _binding: FragmentDetailBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_Detail_to_Overview)
        }

        val detailArguments = args.detailArguments
        debugLog("DetailFragment#onViewCreated  detailArguments[id:${detailArguments.id}, login:${detailArguments.login}, url:${detailArguments.contributorUrl}]")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
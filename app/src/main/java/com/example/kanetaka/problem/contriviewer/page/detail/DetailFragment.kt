package com.example.kanetaka.problem.contriviewer.page.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.kanetaka.problem.contriviewer.databinding.FragmentDetailBinding
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

class DetailFragment : Fragment() {

    // Navigation Editor で自動生成された遷移パラメータ
    private val args: DetailFragmentArgs by navArgs()

    private val viewModel: DetailViewModel by lazy {
        ViewModelProvider(this).get(DetailViewModel::class.java)
    }

    private lateinit var _viewBinding: DetailViewBinding
    private val viewBinding get() = _viewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding =
            DetailViewBinding(FragmentDetailBinding.inflate(inflater, container, false))
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailArguments = args.detailArguments
        debugLog("DetailFragment#onViewCreated  detailArguments[id:${detailArguments.id}, login:${detailArguments.login}, url:${detailArguments.contributorUrl}]")

        viewModel.setup(this, viewBinding, detailArguments.login)
        viewBinding.setup(viewModel)

        viewModel.refreshContributor()
    }
}
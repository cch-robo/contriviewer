package com.example.kanetaka.problem.contriviewer.page.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.kanetaka.problem.contriviewer.application.ContriViewerApplication
import com.example.kanetaka.problem.contriviewer.databinding.FragmentDetailBinding
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
import com.example.kanetaka.problem.contriviewer.util.SimpleFactory
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

class DetailFragment : Fragment() {

    // Navigation Editor で自動生成された遷移パラメータ
    private val args: DetailFragmentArgs by navArgs()

    private lateinit var _viewModel: DetailViewModel
    val viewModel get() = _viewModel

    // viewBinding は、_viewBinding 初期化後にしか参照されない。
    private var _viewBinding: DetailViewBinding? = null
    val viewBinding get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewModel = ViewModelProvider(this).get(DetailViewModel::class.java)
        _viewBinding =
            DetailViewBinding(FragmentDetailBinding.inflate(inflater, container, false))
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailArguments = args.detailArguments
        debugLog("DetailFragment#onViewCreated  detailArguments[id:${detailArguments.id}, login:${detailArguments.login}, url:${detailArguments.contributorUrl}]")

        viewModel.setup(
            this.viewLifecycleOwner,
            viewBinding,
            SimpleFactory.create<ContriViewerRepository>((this.activity?.application as ContriViewerApplication).repo, this),
            detailArguments.login
        )
        viewBinding.setup(viewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }
}
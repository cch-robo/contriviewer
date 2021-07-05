package com.example.kanetaka.problem.contriviewer.page.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.kanetaka.problem.contriviewer.application.ContriViewerApplication
import com.example.kanetaka.problem.contriviewer.databinding.FragmentOverviewBinding

class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by lazy {
        ViewModelProvider(this).get(OverviewViewModel::class.java)
    }

    private lateinit var _viewBinding: OverviewViewBinding
    private val viewBinding get() = _viewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding =
            OverviewViewBinding(FragmentOverviewBinding.inflate(inflater, container, false))

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setup(
            this,
            viewBinding,
            (this.activity?.application as ContriViewerApplication).repo
        )
        viewBinding.setup(this, viewModel)

        if (viewModel.contributors == null || viewModel.contributors!!.isEmpty()) {
            // コントリビュータ一覧を更新する
            viewModel.refreshContributors()
        }
    }

}
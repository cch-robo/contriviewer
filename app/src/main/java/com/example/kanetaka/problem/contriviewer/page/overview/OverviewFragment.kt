package com.example.kanetaka.problem.contriviewer.page.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.kanetaka.problem.contriviewer.application.ContriViewerApplication
import com.example.kanetaka.problem.contriviewer.databinding.FragmentOverviewBinding
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
import com.example.kanetaka.problem.contriviewer.util.SimpleInjector

class OverviewFragment : Fragment() {

    private lateinit var _viewModel: OverviewViewModel
    val viewModel get() = _viewModel

    // viewBinding は、_viewBinding 初期化後にしか参照されない。
    private var _viewBinding: OverviewViewBinding? = null
    val viewBinding get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewModel = ViewModelProvider(this).get(OverviewViewModel::class.java)
        _viewBinding =
            OverviewViewBinding(FragmentOverviewBinding.inflate(inflater, container, false))
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setup(
            this.viewLifecycleOwner,
            viewBinding,
            SimpleInjector.inject<ContriViewerRepository>(
                (this.activity?.application as ContriViewerApplication).repo,
                this
            )
        )
        viewBinding.setup(this, viewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }
}
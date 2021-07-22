package com.example.kanetaka.problem.contriviewer.page.overview

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.databinding.FragmentOverviewBinding
import com.example.kanetaka.problem.contriviewer.page.DestinationUnspecifiedStateChangeNotifier
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

/**
 * ViewBinding用の通知インターフェース。（ViewModelには公開しない）
 * ViewModel から View への更新通知に対応するインターフェースを提供します。
 */
interface OverviewViewBindingNotifier {
    // ページ更新開始通知
    fun updatePage(viewModel: OverviewViewModel)

    /*
    // リフレッシュ終了通知
    fun refreshStopped()

    // リフレッシュエラー通知
    fun refreshErrored()
    */

    // ユーザへのメッセージ依頼通知
    fun showNotice(@StringRes messageId: Int)
}

/**
 * View の ViewModel との同期表示を管理する
 */
class OverviewViewBinding(
    private val binding: FragmentOverviewBinding
) : OverviewViewBindingNotifier, DestinationUnspecifiedStateChangeNotifier {

    val root: View
        get() = binding.root

    private lateinit var _viewModel: OverviewViewModel
    private val viewModel: OverviewViewModel
        get() = _viewModel

    private lateinit var _notify: OverviewViewModelNotifier
    private val notify: OverviewViewModelNotifier
        get() = _notify

    private lateinit var contributorListAdapter: OverviewContributorsAdapter

    fun setup(fragment: OverviewFragment, viewModel: OverviewViewModel) {
        debugLog("OverviewViewBinding  setup start")
        _viewModel = viewModel
        _notify = viewModel

        binding.overviewList.apply {
            layoutManager = LinearLayoutManager(fragment.context)
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
            adapter = OverviewContributorsAdapter(fragment).also {
                contributorListAdapter = it
            }
        }

        // スワイプによるコントリビュータ一覧の更新
        binding.overviewSwipe.setOnRefreshListener {
            notify.swipeRefreshContributors()
        }
        debugLog("OverviewViewBinding  setup end")
    }

    /**
     * 不特定先からの状態更新通知(状態遷移先通知)への対応。
     */
    override fun updateState() {
        when (viewModel.status) {
            OverviewViewModelStatus.INIT_REFRESH -> {
                // コントリビュータ一覧更新開始（標準プログレス）
                startProgress(viewModel.status)
                showNotice(R.string.contributors_overview_refresh_request)
            }
            OverviewViewModelStatus.SWIPE_REFRESH -> {
                // コントリビュータ一覧更新開始（スワイププログレス）
                startProgress(viewModel.status)
            }
            OverviewViewModelStatus.REFRESH_CONTRIBUTORS -> {
                // コントリビュータ一覧更新成功
                stopProgress()
                updatePage(viewModel)
            }
            OverviewViewModelStatus.REFRESH_FAILED -> {
                // コントリビュータ一覧更新失敗
                stopProgress()
                showNotice(R.string.contributors_overview_refresh_error)
                updatePage(viewModel)
            }
        }
    }

    /**
     * コントリビュータ一覧更新プログレス開始通知（ViewModelには公開しない）
     */
    private fun startProgress(status: OverviewViewModelStatus) {
        debugLog("OverviewViewBinding  refreshStart")
        when (status) {
            OverviewViewModelStatus.INIT_REFRESH -> {
                // プログレスを表示する
                binding.overviewProgress.visibility = View.VISIBLE
                binding.overviewList.visibility = View.GONE
                binding.overviewConnectionError.visibility = View.GONE
            }
            OverviewViewModelStatus.SWIPE_REFRESH -> {
                // SwipeRefreshLayout のプログレスを利用する
                binding.overviewProgress.visibility = View.GONE
                binding.overviewList.visibility = View.GONE
                binding.overviewConnectionError.visibility = View.GONE
            }
            else -> return
        }

        // コントリビュータ一覧の状態を更新する。
        notify.refreshContributors()
    }

    /**
     * コントリビュータ一覧更新プログレス完了通知（ViewModelには公開しない）
     */
    private fun stopProgress() {
        debugLog("OverviewViewBinding  refreshStopped")
        // プログレス表示を終了する
        binding.overviewProgress.visibility = View.GONE

        // Swipe プログレスの回転を止める。
        binding.overviewSwipe.isRefreshing = false
    }

    /**
     * コントリビュータ一覧更新エラー通知（ViewModelには公開しない）
     */
    /*
    override fun refreshErrored() {
        // コントリビュータ一覧を表示不可にする
        binding.overviewList.visibility = View.GONE

        // コネクションエラー表示を表示可能にする
        binding.overviewConnectionError.visibility = View.VISIBLE
        debugLog("OverviewViewBinding  refreshErrored")
    }
    */

    /**
     * コントリビュータ一覧更新通知（ViewModelには公開しない）
     */
    override fun updatePage(viewModel: OverviewViewModel) {
        debugLog("OverviewViewBinding  updatePage(${viewModel.contributors.size})")

        /*
        // コントリビュータ一覧を表示可能にする
        binding.overviewList.visibility = View.VISIBLE
        */

        // リストを更新
        contributorListAdapter.submitList(viewModel.contributors)

        if (viewModel.contributors.isEmpty()) {
            when (viewModel.status) {
                OverviewViewModelStatus.INIT_REFRESH -> {
                    binding.overviewList.visibility = View.GONE
                    binding.overviewConnectionError.visibility = View.GONE
                }
                else -> {
                    // REFRESH_FAILED もしくは、REFRESH_CONTRIBUTORS かつコントリビュータ一覧無し
                    binding.overviewList.visibility = View.GONE
                    binding.overviewConnectionError.visibility = View.VISIBLE
                    debugLog("OverviewViewBinding  refresh Error")
                }
            }
        } else {
            if (viewModel.status == OverviewViewModelStatus.REFRESH_CONTRIBUTORS) {
                // コントリビュータ一覧を表示可能にする
                binding.overviewList.visibility = View.VISIBLE
                binding.overviewConnectionError.visibility = View.GONE
            }
        }
    }

    /**
     * ユーザへメッセージを通知（ViewModelには公開しない）
     */
    override fun showNotice(@StringRes messageId: Int) {
        // ユーザへメッセージを通知
        val message: String = binding.root.context.getString(messageId)
        Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
    }
}

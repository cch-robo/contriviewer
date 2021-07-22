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
    fun updatePage(contributors: List<OverviewContributor>)

    // リフレッシュ終了通知
    fun refreshStopped()

    // リフレッシュエラー通知
    fun refreshErrored()

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

    private lateinit var _notify: DestinationUnspecifiedStateChangeNotifier
    private val notify: DestinationUnspecifiedStateChangeNotifier
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
            refreshStart()
        }
        debugLog("OverviewViewBinding  setup end")
    }

    /**
     * 不特定先からの状態更新通知(状態遷移先通知)への対応。
     */
    override fun updateState() {
        when (viewModel.status) {
            OverviewViewModelStatus.INIT_EMPTY -> {
                // コントリビュータ一覧更新要求を通知
                showNotice(R.string.contributors_overview_refresh_request)
            }
            OverviewViewModelStatus.REFRESH_CONTRIBUTORS -> {
                // コントリビュータ一覧更新成功
                refreshStopped() // ViewBinding にリフレッシュが終了したことを通知
                updatePage(viewModel.contributors)
            }
            OverviewViewModelStatus.REFRESH_FAILED -> {
                // コントリビュータ一覧更新失敗
                refreshStopped() // ViewBinding にリフレッシュが終了したことを通知
                showNotice(R.string.contributors_overview_refresh_error)
                refreshErrored()
            }
        }
    }

    /**
     * コントリビュータ一覧更新開始通知（ViewModelには公開しない）
     */
    private fun refreshStart() {
        debugLog("OverviewViewBinding  refreshStart")
        // コネクションエラー表示を消去
        binding.overviewConnectionError.visibility = View.GONE

        // Swipe によりプログレスが回りだしたので、コントリビュータ一覧の状態を更新する。
        notify.updateState()
    }

    /**
     * コントリビュータ一覧更新完了通知（ViewModelには公開しない）
     */
    override fun refreshStopped() {
        // Swipe によりプログレスが回っているので、リフレッシュプログレスの回転を止める。
        binding.overviewSwipe.isRefreshing = false
        debugLog("OverviewViewBinding  refreshStopped")
    }

    /**
     * コントリビュータ一覧更新エラー通知（ViewModelには公開しない）
     */
    override fun refreshErrored() {
        // コントリビュータ一覧を表示不可にする
        binding.overviewList.visibility = View.GONE

        // コネクションエラー表示を表示可能にする
        binding.overviewConnectionError.visibility = View.VISIBLE
        debugLog("OverviewViewBinding  refreshErrored")
    }

    /**
     * コントリビュータ一覧更新通知（ViewModelには公開しない）
     */
    override fun updatePage(contributors: List<OverviewContributor>) {
        debugLog("OverviewViewBinding  updatePage(${contributors.size})")

        // コントリビュータ一覧を表示可能にする
        binding.overviewList.visibility = View.VISIBLE

        // リストを更新
        contributorListAdapter.submitList(contributors)
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

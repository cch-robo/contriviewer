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
 * View への通知インターフェースを提供します。
 */
interface OverviewViewBindingNotifier {
    // ページ更新（プログレス）開始通知
    fun startProgress(status: OverviewViewModelStatus)

    // ページ更新（プログレス）終了通知
    fun stopProgress()

    // ページ更新開始通知
    fun updatePage(viewModel: OverviewViewModel)

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
            viewModel.swipeRefreshContributors()
        }
        debugLog("OverviewViewBinding  setup end")
    }

    /**
     * 不特定先からの状態更新通知(状態遷移先通知)への対応。
     */
    override fun updateState() {
        debugLog("OverviewViewBinding  updateState, status=${viewModel.status}")
        when (viewModel.status) {
            OverviewViewModelStatus.INIT_REFRESH -> {
                // コントリビュータ一覧更新開始（標準プログレス）
                // Activity#onResume のタイミングで実行されるよう遅延を設定しています。
                // INIT_REFRESH が呼ばれるタイミングは、Activity#onCreate() 直後のため
                // プログレス表示設定をしてもタイミングが早すぎて動作しません。
                // このため onResume() タイミングでプログレス表示されるよう調整します。
                root.postDelayed({
                    startProgress(viewModel.status)
                    showNotice(R.string.contributors_overview_refresh_request)
                },500)
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
    override fun startProgress(status: OverviewViewModelStatus) {
        debugLog("OverviewViewBinding  refreshStart")
        when (status) {
            OverviewViewModelStatus.INIT_REFRESH -> {
                // プログレスを表示する
                updatePageMode(OverviewViewModelStatus.INIT_REFRESH)
            }
            OverviewViewModelStatus.SWIPE_REFRESH -> {
                // SwipeRefreshLayout のプログレスを利用する
                updatePageMode(OverviewViewModelStatus.SWIPE_REFRESH)
            }
            else -> return
        }

        // コントリビュータ一覧の状態を更新する。
        viewModel.refreshContributors()
    }

    /**
     * コントリビュータ一覧更新プログレス完了通知（ViewModelには公開しない）
     */
    override fun stopProgress() {
        debugLog("OverviewViewBinding  refreshStopped")
        // プログレス表示を終了する （プログレスのないスワイプ表示にしてから、回転を止める）
        updatePageMode(OverviewViewModelStatus.SWIPE_REFRESH)

        // Swipe プログレスの回転を止める。
        binding.overviewSwipe.isRefreshing = false
    }

    /**
     * コントリビュータ一覧更新通知（ViewModelには公開しない）
     */
    override fun updatePage(viewModel: OverviewViewModel) {
        debugLog("OverviewViewBinding  updatePage(${viewModel.contributors.size}), status=${viewModel.status}")

        if (viewModel.contributors.isEmpty()) {
            when (viewModel.status) {
                OverviewViewModelStatus.REFRESH_FAILED -> {
                    updatePageMode(OverviewViewModelStatus.REFRESH_FAILED)
                    debugLog("OverviewViewBinding  refresh Error")
                }
                else -> {
                    // INIT_REFRESH か SWIPE_REFRESH もしくは、REFRESH_CONTRIBUTORS かつコントリビュータ一覧無し（想定外）
                    stopProgress()
                    updatePageMode(OverviewViewModelStatus.REFRESH_FAILED)
                    debugLog("OverviewViewBinding  refresh Unexpected")
                }
            }
        } else {
            if (viewModel.status == OverviewViewModelStatus.REFRESH_CONTRIBUTORS) {
                // コントリビュータ一覧を表示可能にする
                updatePageMode(OverviewViewModelStatus.REFRESH_CONTRIBUTORS)
            }
        }

        // コントリビュータ一覧画面表示コンテンツ更新
        updatePageContents(viewModel.contributors)
    }

    /**
     * ユーザへメッセージを通知（ViewModelには公開しない）
     */
    override fun showNotice(@StringRes messageId: Int) {
        // ユーザへメッセージを通知
        val message: String = binding.root.context.getString(messageId)
        Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * コントリビュータ一覧画面表示コンテンツ更新
     */
    private fun updatePageContents(contributors: List<OverviewContributor>) {
        // コントリビュータ一覧画面表示コンテンツ（リスト）更新
        contributorListAdapter.submitList(contributors)
    }

    /**
     * コントリビュータ一覧画面表示モード更新
     */
    private fun updatePageMode(status: OverviewViewModelStatus) {
        when(status) {
            OverviewViewModelStatus.INIT_REFRESH -> {
                // 初期表示（空白画面）
                updatePageMode(true, false, false)
            }
            OverviewViewModelStatus.SWIPE_REFRESH -> {
                // スワイプによる更新中表示（空白画面＋スワイププログレス）
                /*
                updatePageMode(false, false, false)
                */
                // プログレスのみ非表示（他は現状表示を継続）
                binding.overviewProgress.visibility = View.GONE
            }
            OverviewViewModelStatus.REFRESH_CONTRIBUTORS -> {
                // コントリビュータ一覧表示
                updatePageMode(false, true, false)
            }
            OverviewViewModelStatus.REFRESH_FAILED -> {
                // エラー表示（雲アイコンにスラッシュ）
                updatePageMode(false, false, true)
            }
        }
    }

    private fun updatePageMode(
        isShowProgress: Boolean,
        isShowContents: Boolean,
        isShowError: Boolean
    ) {
        binding.overviewProgress.visibility = getVisibility(isShowProgress)
        binding.overviewList.visibility = getVisibility(isShowContents)
        binding.overviewConnectionError.visibility = getVisibility(isShowError)
    }

    private fun getVisibility(isShow: Boolean): Int {
        if (isShow) return View.VISIBLE
        return View.GONE
    }
}

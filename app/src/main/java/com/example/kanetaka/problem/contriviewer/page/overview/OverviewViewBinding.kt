package com.example.kanetaka.problem.contriviewer.page.overview

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.databinding.FragmentOverviewBinding
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

/**
 * ViewBinding用の通知インターフェース。
 * ViewModel から View への通知インターフェースを提供します。
 */
interface OverviewViewBindingNotifier {
    // ページ更新開始通知
    fun updatePage(contributors : List<OverviewContributor>)

    // リフレッシュ終了通知
    fun refreshStopped()

    // ユーザへのメッセージ依頼通知
    fun showNotice(@StringRes messageId: Int)
}

/**
 * View の ViewModel との同期表示を管理する
 */
class OverviewViewBinding(
    private val binding: FragmentOverviewBinding) : OverviewViewBindingNotifier {

    val root: View
        get() = binding.root

    private lateinit var _notify: OverviewViewModelNotifier
    private val notify:OverviewViewModelNotifier
        get() = _notify

    private lateinit var contributorListAdapter : OverviewContributorsAdapter

    fun setup(fragment: OverviewFragment, ViewModelNotifier: OverviewViewModelNotifier) {
        debugLog("OverviewViewBinding  setup start")
        _notify = ViewModelNotifier

        binding.overviewList.apply {
            layoutManager = LinearLayoutManager(fragment.context)
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
            adapter = OverviewContributorsAdapter().also {
                contributorListAdapter = it
            }
        }

        // スワイプによるコントリビュータ一覧の更新
        binding.overviewSwipe.setOnRefreshListener {
            refreshStart()
        }
        debugLog("OverviewViewBinding  setup end")
    }

    private fun refreshStart() {
        debugLog("OverviewViewBinding  refreshStart")
        // Swipe によりプログレスが回りだしたので、コントリビュータ一覧を更新する。
        notify.refreshContributors()
    }

    override fun refreshStopped() {
        // Swipe によりプログレスが回っているので、リフレッシュプログレスの回転を止める。
        binding.overviewSwipe.isRefreshing = false
        debugLog("OverviewViewBinding  refreshStopped")
    }

    override fun updatePage(contributors : List<OverviewContributor>) {
        debugLog("OverviewViewBinding  updatePage(${contributors.size})")

        // リストを更新
        contributorListAdapter.submitList(contributors)
    }

    override fun showNotice(@StringRes messageId: Int) {
        // ユーザへメッセージを通知
        val message : String = binding.root.context.getString(messageId)
        Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
    }
}

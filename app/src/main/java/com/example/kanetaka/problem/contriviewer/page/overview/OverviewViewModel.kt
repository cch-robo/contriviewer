package com.example.kanetaka.problem.contriviewer.page.overview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * ViewModel用の通知インターフェース。
 * ViewBinding から ViewModel への通知インターフェースを提供します。
 */
interface OverviewViewModelNotifier {
    // コントリビュータ一覧リフレッシュ開始通知
    fun refreshContributors()
}

/**
 * ViewModel として View の状態値を管理する
 */
class OverviewViewModel : ViewModel(), OverviewViewModelNotifier {
    private lateinit var _repo: ContriViewerRepository
    private val repo: ContriViewerRepository
        get() = _repo

    private lateinit var _notify: OverviewViewBindingNotifier
    private val notify: OverviewViewBindingNotifier
        get() = _notify

    // コントリビュータ一覧
    private val _contributors = mutableListOf<OverviewContributor>()
    private var _contributorsObserver = MutableLiveData<MutableList<OverviewContributor>>()
    val contributors: MutableList<OverviewContributor>?
        get() = _contributorsObserver.value

    fun setup(
        fragment: OverviewFragment,
        viewBindingNotifier: OverviewViewBindingNotifier,
        repo: ContriViewerRepository
    ) {
        _notify = viewBindingNotifier
        _repo = repo

        // コントリビュータ一覧更新通知
        _contributorsObserver.observe(fragment, {
            _notify.updatePage(it)
        })

        // コントリビュータ一覧更新要求を通知
        if (_contributors.isEmpty()) {
            notify.showNotice(R.string.contributors_overview_refresh_request)
        }
    }

    /**
     * コントリビュータ情報をリフレシュする。
     */
    override fun refreshContributors() {
        // IOスレッドでサーバからコントリビュータ情報を取得する
        viewModelScope.launch(Dispatchers.IO) {
            debugLog("refreshContributors  refresh start!!")

            val results = mutableListOf<OverviewContributor>()
            val result = repo.fetchContributors()

            // 上記処理が完了してから、メインスレッドで実行されます。
            withContext(Dispatchers.Main) {
                // ViewBinding にリフレッシュが終了したことを通知
                notify.refreshStopped()

                if (result.isSuccess) {
                    result.getOrNull()!!.forEach { model: OverviewModel ->
                        debugLog("login=${model.login}, contributions=${model.contributions}, url=${model.url}")
                        results.add(
                            OverviewContributor(
                                model.id,
                                model.login,
                                model.avatar_url,
                                model.contributions,
                                model.url
                            )
                        )
                    }

                    // コントリビュータ一覧更新
                    _contributors.clear()
                    _contributors.addAll(results)
                    _contributorsObserver.value = _contributors

                } else {
                    notify.showNotice(R.string.contributors_overview_refresh_error)
                    notify.refreshErrored()
                    debugLog("refreshContributors failed")
                }
                debugLog("refreshContributors  refresh END!!")
            }
        }
    }
}
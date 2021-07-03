package com.example.kanetaka.problem.contriviewer.page.overview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewService
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog
import kotlinx.coroutines.*


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
    private lateinit var _notify: OverviewViewBindingNotifier
    private val notify: OverviewViewBindingNotifier
        get() = _notify

    // コントリビュータ一覧
    private val _contributors = mutableListOf<OverviewContributor>()
    private var _contributorsObserver = MutableLiveData<MutableList<OverviewContributor>>()

    fun setup(fragment: OverviewFragment, viewBindingNotifier: OverviewViewBindingNotifier) {
        _notify = viewBindingNotifier

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
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            debugLog("refreshContributors  refresh start!!")
            val results = mutableListOf<OverviewContributor>()

            val result = try {
                val response = OverviewService.retrofitService.getContributors()
                response.forEach { prop: OverviewModel ->
                    debugLog("login=${prop.login}, contributions=${prop.contributions}, url=${prop.url}")
                    results.add(
                        OverviewContributor(
                            prop.id,
                            prop.login,
                            prop.avatar_url,
                            prop.contributions
                        )
                    )
                }
                Result.success("success")
            } catch (e: Exception) {
                debugLog("refreshContributors exception ${e.message}")
                Result.failure<Exception>(e)
            } finally {
            }

            // 上記処理が完了してから、メインスレッドで実行されます。
            withContext(Dispatchers.Main) {
                // ViewBinding にリフレッシュが終了したことを通知
                notify.refreshStopped()

                if (result.isSuccess) {
                    // コントリビュータ一覧更新
                    _contributors.clear()
                    _contributors.addAll(results)
                    _contributorsObserver.value = _contributors
                } else {
                    notify.showNotice(R.string.contributors_overview_refresh_error)
                    debugLog("refreshContributors failed")
                }
                debugLog("refreshContributors  refresh END!!")
            }
        }
    }
}
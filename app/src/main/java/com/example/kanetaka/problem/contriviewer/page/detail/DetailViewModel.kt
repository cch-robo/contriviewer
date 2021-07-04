package com.example.kanetaka.problem.contriviewer.page.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailService
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * ViewModel用の通知インターフェース。
 * ViewBinding から ViewModel への通知インターフェースを提供します。
 */
interface DetailViewModelNotifier {
    // コントリビュータ・リフレッシュ開始通知
    fun refreshContributor()
}

/**
 * ViewModel として View の状態値を管理する
 */
class DetailViewModel : ViewModel(), DetailViewModelNotifier {
    private lateinit var _notify: DetailViewBindingNotifier
    private val notify: DetailViewBindingNotifier
        get() = _notify

    private lateinit var _login: String
    private val login: String
        get() = _login

    // コントリビューター
    private val contributor = MutableLiveData<DetailContributor>()

    fun setup(
        fragment: DetailFragment,
        viewBindingNotifier: DetailViewBindingNotifier,
        login: String
    ) {
        _notify = viewBindingNotifier

        _login = login

        // コントリビュータ一覧更新通知
        contributor.observe(fragment, {
            _notify.updatePage(it)
        })
    }

    /**
     * コントリビュータ情報をリフレシュする。
     */
    override fun refreshContributor() {
        // ViewBinding にリフレッシュが開始したことを通知
        notify.refreshStart()

        // IOスレッドでサーバからコントリビュータ情報を取得する
        viewModelScope.launch {
            debugLog("refreshContributors  refresh start!!")
            var property: DetailContributor?

            val result = try {
                val response = DetailService.retrofitService.getContributor(login)

                debugLog("login=${response.login}, name=${response.name}")
                property = DetailContributor(
                    response.avatar_url,
                    response.login,
                    response.name,
                    response.bio,
                    response.company,
                    response.location,
                    response.email,
                    response.blog,
                    response.twitter_username,
                    response.followers,
                    response.following,
                    response.public_repos,
                    response.public_gists
                )
                Result.success("success")
            } catch (e: Exception) {
                debugLog("refreshContributors exception ${e.message}")
                property = null
                Result.failure<Exception>(e)
            } finally {
            }

            // 上記処理が完了してから、メインスレッドで実行されます。
            withContext(Dispatchers.Main) {
                // ViewBinding にリフレッシュが終了したことを通知
                notify.refreshStopped()

                if (result.isSuccess) {
                    // コントリビュータ更新
                    contributor.value = property
                } else {
                    // コントリビュータ更新
                    contributor.value = null
                    notify.showNotice(R.string.contributor_detail_refresh_error)
                    debugLog("refreshContributors failed")
                }
                debugLog("refreshContributors  refresh END!!")
            }
        }
    }
}

package com.example.kanetaka.problem.contriviewer.page.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
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
    private lateinit var _repo: ContriViewerRepository
    private val repo: ContriViewerRepository
        get() = _repo

    private lateinit var _notify: DetailViewBindingNotifier
    private val notify: DetailViewBindingNotifier
        get() = _notify

    private lateinit var _login: String
    private val login: String
        get() = _login

    // コントリビューター
    private var _contributorObserver = MutableLiveData<DetailContributor>()

    // FIXME Android Unit test での Observer リアクション設定用プロパティ（本来不要）
    var contributorObserver = MutableLiveData<DetailContributor>()

    fun setup(
        fragment: DetailFragment,
        viewBindingNotifier: DetailViewBindingNotifier,
        repo: ContriViewerRepository,
        login: String
    ) {
        _notify = viewBindingNotifier
        _repo = repo

        _login = login

        // コントリビュータ一覧更新通知
        _contributorObserver.observe(fragment, {
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
        viewModelScope.launch(Dispatchers.IO) {
            debugLog("refreshContributor  refresh start!!")

            var contributor: DetailContributor?
            val result = repo.fetchContributor(login)

            // 上記処理が完了してから、メインスレッドで実行されます。
            withContext(Dispatchers.Main) {
                // ViewBinding にリフレッシュが終了したことを通知
                notify.refreshStopped()

                if (result.isSuccess) {
                    val model = result.getOrNull()!!
                    debugLog("login=${model.login}, name=${model.name}")

                    contributor = DetailContributor(
                        model.avatar_url,
                        model.login,
                        model.name,
                        model.bio,
                        model.html_url,
                        model.company,
                        model.location,
                        model.email,
                        model.blog,
                        model.twitter_username,
                        model.followers,
                        model.following,
                        model.public_repos,
                        model.public_gists
                    )

                    // コントリビュータ更新
                    _contributorObserver.value = contributor
                    if (Thread.currentThread().name != "main") {
                        // FIXME Android Unit Test では、LiveData#observer() が反応しないためのパッチ （対応次第削除すること）
                        // Android Unit test では、Mainスレッドが Dispatchers.setMain() によりテスト用スレッドになるため、
                        // スレッド名が main とならないことを利用しています。
                        contributorObserver.value = contributor
                    }

                } else {
                    // コントリビュータ更新
                    _contributorObserver.value = null
                    notify.showNotice(R.string.contributor_detail_refresh_error)
                    debugLog("refreshContributor  failed")
                }
                debugLog("refreshContributor  refresh END!!")
            }
        }
    }
}

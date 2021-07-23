package com.example.kanetaka.problem.contriviewer.page.detail

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanetaka.problem.contriviewer.page.DestinationUnspecifiedStateChangeNotifier
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * ViewModel用の内部通知インターフェース。（外部公開しない）
 * ViewBinding から ViewModel への通知インターフェースを提供します。
 */
private interface DetailViewModelNotifier {
    // コントリビュータ・リフレッシュ開始通知
    fun refreshContributor()
}

/**
 * ViewModel用のステータス。
 * ViewModel用のコントリビュータ詳細画面ステータス
 */
enum class DetailViewModelStatus {
    INIT_REFRESH,
    REFRESH_CONTRIBUTOR,
    REFRESH_FAILED;
}

/**
 * ViewModel として View の状態値を管理する
 */
class DetailViewModel : ViewModel(), DetailViewModelNotifier,
    DestinationUnspecifiedStateChangeNotifier {
    private lateinit var _repo: ContriViewerRepository
    private val repo: ContriViewerRepository
        get() = _repo

    private lateinit var _notify: DestinationUnspecifiedStateChangeNotifier
    private val notify: DestinationUnspecifiedStateChangeNotifier
        get() = _notify

    private lateinit var _login: String
    private val login: String
        get() = _login

    // コントリビューター
    private var _contributorObserver: DetailContributor? = null
    val contributor: DetailContributor?
        get() = _contributorObserver

    // カレント・コントリビュータ一覧画面ステータス
    private var _status = MutableLiveData(DetailViewModelStatus.INIT_REFRESH)
    lateinit var status: DetailViewModelStatus

    fun setup(
        viewLifecycleOwner: LifecycleOwner,
        viewBindingNotifier: DestinationUnspecifiedStateChangeNotifier,
        repo: ContriViewerRepository,
        login: String
    ) {
        _notify = viewBindingNotifier
        _repo = repo

        _login = login

        // コントリビュータ一覧画面ステータス・オブサーバー
        _status.observe(viewLifecycleOwner, Observer {
            status = it
            notify.updateState()
        })
    }

    /**
     * 不特定先からの状態更新通知への対応。（現状では、使われていない）
     */
    override fun updateState() {
        debugLog("DetailViewModel  updateState, status=${status}")
        refreshContributor()
    }

    /**
     * コントリビュータ情報をリフレシュする。（ViewBindingに公開する)
     */
    override fun refreshContributor() {
        debugLog("DetailViewModel  refreshContributor")
        // IOスレッドでサーバからコントリビュータ情報を取得する
        viewModelScope.launch(Dispatchers.IO) {
            debugLog("refreshContributor  refresh start!!")

            var contributor: DetailContributor?
            val result = repo.fetchContributor(login)

            // 上記処理が完了してから、メインスレッドで実行されます。
            withContext(Dispatchers.Main) {
                if (result.isSuccess && result.getOrNull() != null) {
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
                    _contributorObserver = contributor
                    _status.value = DetailViewModelStatus.REFRESH_CONTRIBUTOR

                } else {
                    // コントリビュータ更新
                    _contributorObserver = null
                    _status.value = DetailViewModelStatus.REFRESH_FAILED
                    debugLog("refreshContributor  failed")
                }
                debugLog("refreshContributor  refresh END!!")
            }
        }
    }
}

package com.example.kanetaka.problem.contriviewer.page.overview

/*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanetaka.problem.contriviewer.R
*/
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.page.DestinationUnspecifiedStateChangeNotifier
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * ViewModel用の内部通知インターフェース。（ViewBindingに公開する）
 * ViewBinding から ViewModel への更新通知に対応するインターフェースを提供します。
 */
interface OverviewViewModelNotifier {
    // スワイプによるコントリビュータ一覧リフレッシュ開始通知
    fun swipeRefreshContributors()

    // コントリビュータ一覧リフレッシュ開始通知
    fun refreshContributors()
}

/**
 * ViewModel用のステータス。
 * ViewModel用のコントリビュータ一覧画面ステータス
 */
enum class OverviewViewModelStatus {
    INIT_REFRESH,
    SWIPE_REFRESH,
    REFRESH_CONTRIBUTORS,
    REFRESH_FAILED;
}

/**
 * ViewModel として View の状態値を管理する
 */
class OverviewViewModel : ViewModel(), OverviewViewModelNotifier,
    DestinationUnspecifiedStateChangeNotifier {
    private lateinit var _repo: ContriViewerRepository
    private val repo: ContriViewerRepository
        get() = _repo

    private lateinit var _notify: DestinationUnspecifiedStateChangeNotifier
    private val notify: DestinationUnspecifiedStateChangeNotifier
        get() = _notify

    /*
    // コントリビュータ一覧
    private val _contributors = mutableListOf<OverviewContributor>()
    private var _contributorsObserver = MutableLiveData<MutableList<OverviewContributor>>()
    val contributors: MutableList<OverviewContributor>?
        get() = _contributorsObserver.value
    */
    // コントリビュータ一覧
    private val _contributors = mutableListOf<OverviewContributor>()
    val contributors: MutableList<OverviewContributor>
        get() = _contributors

    // カレント・コントリビュータ一覧画面ステータス
    private var _status =
        MutableLiveData<OverviewViewModelStatus>(OverviewViewModelStatus.INIT_REFRESH)

    /*
    private val status : LiveData<ViewModelStatus>
        get() = _status
    */
    lateinit var status: OverviewViewModelStatus

    fun setup(
        viewLifecycleOwner: LifecycleOwner,
        viewBindingNotifier: DestinationUnspecifiedStateChangeNotifier,
        repo: ContriViewerRepository
    ) {
        _notify = viewBindingNotifier
        _repo = repo

        /*
        // コントリビュータ一覧更新通知
        _contributorsObserver.observe(viewLifecycleOwner, Observer {
            _notify.updatePage(it)
        })

        // コントリビュータ一覧更新要求を通知
        if (_contributors.isEmpty()) {
            notify.showNotice(R.string.contributors_overview_refresh_request)
        }
        */
        // コントリビュータ一覧画面ステータス・オブサーバー
        _status.observe(viewLifecycleOwner, Observer {
            status = it
            notify.updateState()
        })
    }

    /**
     * 不特定先からの状態更新通知への対応。
     */
    override fun updateState() {
        refreshContributors()
    }

    /**
     * スワイプによりコントリビュータ情報をリフレシュする。（ViewBindingに公開する)
     */
    override fun swipeRefreshContributors() {
        _status.value = OverviewViewModelStatus.SWIPE_REFRESH
        refreshContributors()
    }

    /**
     * コントリビュータ情報をリフレシュする。（ViewBindingに公開する)
     */
    override fun refreshContributors() {
        // IOスレッドでサーバからコントリビュータ情報を取得する
        viewModelScope.launch(Dispatchers.IO) {
            debugLog("refreshContributors  refresh start!!")

            val results = mutableListOf<OverviewContributor>()
            val result = repo.fetchContributors()

            // 上記処理が完了してから、メインスレッドで実行されます。
            withContext(Dispatchers.Main) {
                /*
                // ViewBinding にリフレッシュが終了したことを通知
                notify.refreshStopped()
                */

                if (result.isSuccess && result.getOrNull() != null) {
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
                    /*
                    _contributorsObserver.value = _contributors
                    */
                    _status.value = OverviewViewModelStatus.REFRESH_CONTRIBUTORS

                } else {
                    /*
                    notify.showNotice(R.string.contributors_overview_refresh_error)
                    notify.refreshErrored()
                    */
                    // コントリビュータ一覧更新
                    _contributors.clear()
                    _status.value = OverviewViewModelStatus.REFRESH_FAILED
                    debugLog("refreshContributors failed")
                }
                debugLog("refreshContributors  refresh END!!")
            }
        }
    }
}
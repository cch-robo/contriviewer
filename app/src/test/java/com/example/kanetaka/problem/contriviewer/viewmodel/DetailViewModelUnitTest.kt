package com.example.kanetaka.problem.contriviewer.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.page.detail.DetailContributor
import com.example.kanetaka.problem.contriviewer.page.detail.DetailViewBindingNotifier
import com.example.kanetaka.problem.contriviewer.page.detail.DetailViewModel
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugTestLog
import com.example.kanetaka.problem.contriviewer.viewmodel.FakeDetailViewBindingNotifier.Notify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * DetailViewModel がコントリビュータを更新するかチェック
 */
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class DetailViewModelUnitTest {
    private lateinit var viewModel: DetailViewModel
    private val fakeViewBindingNotifier = FakeDetailViewBindingNotifier()
    private val fakeSuccessRepository = FakeSuccessContriViewerRepository()
    private val fakeFailedRepository = FakeFailedContriViewerRepository()

    // JUnit テスト環境用の CoroutineDispatcher
    private val testDispatcher = TestCoroutineDispatcher()

    /**
     * Executes each task synchronously using Architecture Components.
     * make can Kotlin Coroutine invoke Suspend
     *
     * LiveDataは、Main スレッドでしか利用できない。
     * このためこのルールがないと、
     * MutableLiveData#observe() のリアクション設定や、
     * MutableLiveData#value = XXX で値を設定できない。
     */
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    /**
     * コントリビュータ情報取得成功時の処理とフローを確認する。
     */
    @Test
    fun success_refreshContributors() {
        debugTestLog("before  success - refreshContributor()")
        viewModel = DetailViewModel()

        // LifecycleOwner のフェイクを生成
        val fragment = FakeDetailFragment()
        fragment.setLifecycleState(Lifecycle.State.RESUMED)

        // ViewModel 初期設定
        viewModel.setup(
            fragment.viewLifecycleOwner,
            fakeViewBindingNotifier,
            fakeSuccessRepository,
            "dlam"
        )

        // viewBindingNotify.refreshStart ⇒ コントリビュータ取得開始通知
        // viewBindingNotify.refreshStopped ⇒ プログレス終了通知
        // viewBindingNotify.updatePage ⇒ 取得したコントリビュータでの画面更新通知
        viewModel.refreshContributor()

        // コントリビュータ取得完了(updatePage完了)まで待機
        fakeViewBindingNotifier.await()
        debugTestLog("after  success - refreshContributor()")

        // コントリビュータ情報確認
        val contributor: DetailContributor? = fakeViewBindingNotifier.contributor
        assertEquals("https://avatars.githubusercontent.com/u/831038?v=4", contributor?.iconUrl)
        assertEquals("dlam", contributor?.login)
        assertEquals("Dustin Lam", contributor?.name)
        assertEquals("https://github.com/dlam", contributor?.account)
        debugTestLog("contributor {login:${contributor?.login}, name:${contributor?.name}, account:${contributor?.account}}")

        // 各種通知への呼出順確認
        assertEquals(3, fakeViewBindingNotifier.notifies.size)
        assertEquals(Notify.REFRESH_START, fakeViewBindingNotifier.notifies[0].first)
        assertEquals(Notify.REFRESH_STOPPED, fakeViewBindingNotifier.notifies[1].first)
        assertEquals(Notify.UPDATE_PAGE, fakeViewBindingNotifier.notifies[2].first)
        fakeViewBindingNotifier.notifies
    }

    /**
     * コントリビュータ情報取得失敗時の処理とフローを確認する。
     */
    @Test
    fun failed_refreshContributors() {
        debugTestLog("before  failed - refreshContributor()")
        viewModel = DetailViewModel()

        // LifecycleOwner のフェイクを生成
        val fragment = FakeDetailFragment()
        fragment.setLifecycleState(Lifecycle.State.RESUMED)

        // ViewModel 初期設定
        viewModel.setup(
            fragment.viewLifecycleOwner,
            fakeViewBindingNotifier,
            fakeFailedRepository,
            "dlam"
        )

        // viewBindingNotify.refreshStart ⇒ コントリビュータ取得開始通知
        // viewBindingNotify.refreshStopped ⇒ プログレス終了通知
        // viewBindingNotify.updatePage ⇒ コントリビュータ未取得での画面更新通知
        // viewBindingNotify.showNotice ⇒ コントリビュータ取得失敗表示通知
        viewModel.refreshContributor()

        // コントリビュータ取得完了(updatePage完了)まで待機
        fakeViewBindingNotifier.await()
        debugTestLog("after  failed - refreshContributor()")

        // コントリビュータ情報確認
        val contributor: DetailContributor? = fakeViewBindingNotifier.contributor
        assertEquals(null, contributor)
        debugTestLog("contributor fetch failed")

        // 各種通知への呼出順確認
        assertEquals(4, fakeViewBindingNotifier.notifies.size)
        assertEquals(Notify.REFRESH_START, fakeViewBindingNotifier.notifies[0].first)
        assertEquals(Notify.REFRESH_STOPPED, fakeViewBindingNotifier.notifies[1].first)
        assertEquals(Notify.UPDATE_PAGE, fakeViewBindingNotifier.notifies[2].first)
        assertEquals(Notify.SHOW_NOTICE, fakeViewBindingNotifier.notifies[3].first)

        // メッセージ確認
        assertEquals(
            R.string.contributor_detail_refresh_error,
            fakeViewBindingNotifier.notifies[3].second
        )
    }
}

/**
 * テスト用 DetailFragment.
 */
private class FakeDetailFragment : LifecycleOwner {
    private val _lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    val viewLifecycleOwner: LifecycleOwner
        get() = this

    override fun getLifecycle(): Lifecycle {
        return _lifecycleRegistry
    }

    fun setLifecycleState(state: Lifecycle.State) {
        _lifecycleRegistry.currentState = state
    }
}

/**
 * テスト用 DetailViewBindingNotifier.
 */
private class FakeDetailViewBindingNotifier : DetailViewBindingNotifier {
    // コントリビュータ取得完了待機用ラッチ
    private val _latch: CountDownLatch = CountDownLatch(1)
    fun await() {
        _latch.await(10000, TimeUnit.MILLISECONDS)
    }

    // 各種通知への呼出順
    private var _notifies: MutableList<Pair<Notify, Int>> = mutableListOf()
    val notifies: List<Pair<Notify, Int>>
        get() = _notifies

    // コントリビュータ
    private var _contributor: DetailContributor? = null
    val contributor: DetailContributor?
        get() = _contributor

    override fun updatePage(contributor: DetailContributor?) {
        debugTestLog("Called updatePage()")
        _notifies.add(Pair(Notify.UPDATE_PAGE, 0))
        _contributor = contributor

        // 成功時/エラー時の完了待機解除
        _latch.countDown()
    }

    override fun refreshStart() {
        debugTestLog("Called refreshStart()")
        _notifies.add(Pair(Notify.REFRESH_START, 0))
    }

    override fun refreshStopped() {
        debugTestLog("Called refreshStopped()")
        _notifies.add(Pair(Notify.REFRESH_STOPPED, 0))
    }

    override fun showNotice(messageId: Int) {
        debugTestLog("Called showNotice()")
        _notifies.add(Pair(Notify.SHOW_NOTICE, messageId))
    }

    // 通知種別
    enum class Notify {
        UPDATE_PAGE,
        REFRESH_START,
        REFRESH_STOPPED,
        SHOW_NOTICE
    }
}

/**
 * テスト用フェッチ失敗時 ContriViewerRepository.
 * OverViewViewModel と DetailViewModel から利用できるよう public アクセスとする。
 */
class FakeFailedContriViewerRepository : ContriViewerRepository {

    override suspend fun fetchContributors(): Result<List<OverviewModel>> {
        return Result.failure(
            Throwable(
                "Unable to resolve host \"api.github.com\": No address associated with hostname",
                RuntimeException("Network error!")
            )
        )
    }

    override suspend fun fetchContributor(login: String): Result<DetailModel> {
        return Result.failure(
            Throwable(
                "Unable to resolve host \"api.github.com\": No address associated with hostname",
                RuntimeException("Network error!")
            )
        )
    }
}

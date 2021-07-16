package com.example.kanetaka.problem.contriviewer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.kanetaka.problem.contriviewer.page.detail.DetailContributor
import com.example.kanetaka.problem.contriviewer.page.detail.DetailFragment
import com.example.kanetaka.problem.contriviewer.page.detail.DetailViewBindingNotifier
import com.example.kanetaka.problem.contriviewer.page.detail.DetailViewModel
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugTestLog
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


/**
 * DetailViewModel がコントリビュータを更新するかチェック
 */
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class DetailViewModelUnitTest {
    private lateinit var viewModel: DetailViewModel
    private val fakeViewBindingNotifier = FakeDetailViewBindingNotifier()
    private val fakeRepository = FakeContriViewerRepository()

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
     * だがルールを設定しても、リアクションは実行されないことに留意。
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

    @Test
    fun refreshContributors() {
        debugTestLog("before  refreshContributor()")
        viewModel = DetailViewModel()

        // ViewModel 初期設定
        viewModel.setup(
            DetailFragment(),
            fakeViewBindingNotifier,
            fakeRepository,
            "dlam"
        )

        // FIXME Android Unit Test では、本来実装の LiveData#observer() が反応しないためのテスト用パッチ
        viewModel.contributorObserver.observeForever { fakeViewBindingNotifier.updatePage(it) }

        // viewModelNotify.refreshContributors ⇒ コントリビュータ一覧取得開始
        // viewBindingNotify.refreshStopped ⇒ プログレス終了通知
        // viewBindingNotify.updatePage ⇒ 取得したコントリビュータ一覧で画面更新通知
        viewModel.refreshContributor()

        // コントリビュータ取得完了(updatePage完了)まで待機
        fakeViewBindingNotifier.await()
        debugTestLog("after  refreshContributor()")

        // コントリビュータ情報確認
        val contributor: DetailContributor? = fakeViewBindingNotifier.contributor
        assertEquals("https://avatars.githubusercontent.com/u/831038?v=4", contributor?.iconUrl)
        assertEquals("dlam", contributor?.login)
        assertEquals("Dustin Lam", contributor?.name)
        assertEquals("https://github.com/dlam", contributor?.account)
        debugTestLog("contributor {login:${contributor?.login}, name:${contributor?.name}, account:${contributor?.account}}")

        // 各種通知への呼出順確認
        assertEquals(1, fakeViewBindingNotifier.refreshStart)
        assertEquals(2, fakeViewBindingNotifier.refreshStopped)
        assertEquals(3, fakeViewBindingNotifier.updatePage)
        assertEquals(-1, fakeViewBindingNotifier.showNotice)
    }
}

/**
 * テスト用 DetailViewBindingNotifier.
 */
private class FakeDetailViewBindingNotifier : DetailViewBindingNotifier {
    // コントリビュータ取得完了待機用ラッチ
    private val _latch: CountDownLatch = CountDownLatch(1)
    fun await() {
        _latch.await()
    }

    private var _operationIndex = 0
    private var _updatePage: Int = -1
    private var _refreshStart: Int = -1
    private var _refreshStopped: Int = -1
    private var _showNotice: Int = -1

    // 各種通知への呼出順
    val updatePage get() = _updatePage
    val refreshStart get() = _refreshStart
    val refreshStopped get() = _refreshStopped
    val showNotice get() = _showNotice

    // コントリビュータ
    private var _contributor: DetailContributor? = null
    val contributor: DetailContributor?
        get() = _contributor

    override fun updatePage(contributor: DetailContributor?) {
        debugTestLog("Called updatePage()")
        _updatePage = (++_operationIndex)

        _contributor = contributor
        _latch.countDown()
    }

    override fun refreshStart() {
        debugTestLog("Called refreshStart()")
        _refreshStart = (++_operationIndex)
    }

    override fun refreshStopped() {
        debugTestLog("Called refreshStopped()")
        _refreshStopped = (++_operationIndex)
    }

    override fun showNotice(messageId: Int) {
        debugTestLog("Called showNotice()")
        _showNotice = (++_operationIndex)
    }
}

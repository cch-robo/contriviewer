package com.example.kanetaka.problem.contriviewer.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewContributor
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewFragment
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewViewBindingNotifier
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewViewModel
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
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
 * OverviewViewModel がコントリビュータ一覧を更新するかチェック
 */
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class OverviewViewModelUnitTest {
    private lateinit var viewModel: OverviewViewModel
    private val fakeViewBindingNotifier = FakeOverviewViewBindingNotifier()
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
        debugTestLog("before  refreshContributors()")
        viewModel = OverviewViewModel()

        // ViewModel 初期設定
        // viewBindingNotify.showNotice ⇒ スワイプダウンで表示更新を通知
        viewModel.setup(
            OverviewFragment(),
            fakeViewBindingNotifier,
            fakeRepository
        )

        // FIXME Android Unit Test では、本来実装の LiveData#observer() が反応しないためのテスト用パッチ
        viewModel.contributorsObserver.observeForever { fakeViewBindingNotifier.updatePage(it) }

        // viewModelNotify.refreshContributors ⇒ コントリビュータ一覧取得開始
        // viewBindingNotify.refreshStopped ⇒ プログレス終了通知
        // viewBindingNotify.updatePage ⇒ 取得したコントリビュータ一覧で画面更新通知
        viewModel.refreshContributors()

        // コントリビュータ一覧取得完了(updatePage完了)まで待機
        fakeViewBindingNotifier.await()
        debugTestLog("after  refreshContributors()")

        // コントリビュータ一覧取得確認
        assertEquals(1, fakeViewBindingNotifier.contributors.size)
        val contributor: OverviewContributor = fakeViewBindingNotifier.contributors[0]

        // コントリビュータ情報確認
        assertEquals(831038, contributor.id)
        assertEquals("dlam", contributor.name)
        assertEquals("https://api.github.com/users/dlam", contributor.contributorUrl)
        debugTestLog("contributor {id:${contributor.id}, name:${contributor.name}, account:${contributor.contributorUrl}}")

        // 各種通知への呼出順確認
        assertEquals(1, fakeViewBindingNotifier.showNotice)
        assertEquals(2, fakeViewBindingNotifier.refreshStopped)
        assertEquals(3, fakeViewBindingNotifier.updatePage)
        assertEquals(-1, fakeViewBindingNotifier.refreshErrored)
    }
}

/**
 * テスト用 OverviewBindingNotifier.
 */
private class FakeOverviewViewBindingNotifier : OverviewViewBindingNotifier {
    // コントリビュータ一覧取得完了待機用ラッチ
    private val _latch: CountDownLatch = CountDownLatch(1)
    fun await() {
        _latch.await()
    }

    private var _operationIndex = 0
    private var _updatePage: Int = -1
    private var _refreshStopped: Int = -1
    private var _refreshErrored: Int = -1
    private var _showNotice: Int = -1

    // 各種通知への呼出順
    val updatePage get() = _updatePage
    val refreshStopped get() = _refreshStopped
    val refreshErrored get() = _refreshErrored
    val showNotice get() = _showNotice

    // コントリビュータ一覧
    private var _contributors: MutableList<OverviewContributor> = mutableListOf()
    val contributors: List<OverviewContributor>
        get() = _contributors

    override fun updatePage(contributors: List<OverviewContributor>) {
        debugTestLog("Called updatePage()")
        _updatePage = (++_operationIndex)

        _contributors.addAll(contributors)
        _latch.countDown()
    }

    override fun refreshStopped() {
        debugTestLog("Called refreshStopped()")
        _refreshStopped = (++_operationIndex)
    }

    override fun refreshErrored() {
        debugTestLog("Called refreshErrored()")
        _refreshErrored = (++_operationIndex)
    }

    override fun showNotice(messageId: Int) {
        debugTestLog("Called showNotice()")
        _showNotice = (++_operationIndex)
    }
}

/**
 * テスト用 ContriViewerRepository.
 * OverViewViewModel と DetailViewModel から利用できるよう public アクセスとする。
 */
class FakeContriViewerRepository : ContriViewerRepository {

    override suspend fun fetchContributors(): Result<List<OverviewModel>> {
        // 実際のコントリビュータ一覧(下記 URL)データから 1名分のみを利用します。
        // https://api.github.com/repos/googlesamples/android-architecture-components/contributors
        val model: List<OverviewModel> = listOf(
            OverviewModel(
                "dlam",
                831038,
                "MDQ6VXNlcjgzMTAzOA==",
                "https://avatars.githubusercontent.com/u/831038?v=4",
                "",
                "https://api.github.com/users/dlam",
                "https://github.com/dlam",
                "https://api.github.com/users/dlam/followers",
                "https://api.github.com/users/dlam/following{/other_user}",
                "https://api.github.com/users/dlam/gists{/gist_id}",
                "https://api.github.com/users/dlam/starred{/owner}{/repo}",
                "https://api.github.com/users/dlam/subscriptions",
                "https://api.github.com/users/dlam/orgs",
                "https://api.github.com/users/dlam/repos",
                "https://api.github.com/users/dlam/events{/privacy}",
                "https://api.github.com/users/dlam/received_events",
                "User",
                false,
                113
            )
        )
        return Result.success(model)
    }

    override suspend fun fetchContributor(login: String): Result<DetailModel> {
        // 実際のコントリビュータ(下記 URL)データを利用します。
        // https://api.github.com/users/dlam
        val model = DetailModel(
            "dlam",
            831038,
            "MDQ6VXNlcjgzMTAzOA==",
            "https://avatars.githubusercontent.com/u/831038?v=4",
            "",
            "https://api.github.com/users/dlam",
            "https://github.com/dlam",
            "https://api.github.com/users/dlam/followers",
            "https://api.github.com/users/dlam/following{/other_user}",
            "https://api.github.com/users/dlam/gists{/gist_id}",
            "https://api.github.com/users/dlam/starred{/owner}{/repo}",
            "https://api.github.com/users/dlam/subscriptions",
            "https://api.github.com/users/dlam/orgs",
            "https://api.github.com/users/dlam/repos",
            "https://api.github.com/users/dlam/events{/privacy}",
            "https://api.github.com/users/dlam/received_events",
            "User",
            false,
            "Dustin Lam",
            "@Google",
            "https://www.dustinlam.com/",
            "San Francisco, CA",
            null,
            null,
            null,
            "itsdustinlam",
            12,
            2,
            67,
            18,
            "2011-06-05T16:33:01Z",
            "2021-07-15T20:36:55Z"
        )
        return Result.success(model)
    }
}

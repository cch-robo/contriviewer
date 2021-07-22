package com.example.kanetaka.problem.contriviewer.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.page.DestinationUnspecifiedStateChangeNotifier
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewContributor
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewViewModel
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewViewModelStatus
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
import java.util.concurrent.TimeUnit


/**
 * OverviewViewModel がコントリビュータ一覧を更新するかチェック
 */
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class OverviewViewModelUnitTest {
    private lateinit var viewModel: OverviewViewModel
    private val fakeViewBindingNotifier = FakeOverviewViewBindingNotifier()
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
     * コントリビュータ一覧取得成功時の処理を確認する。
     */
    @Test
    fun success_refreshContributors() {
        debugTestLog("before  success - refreshContributors()")
        viewModel = OverviewViewModel()

        // LifecycleOwner のフェイクを生成
        val fragment = FakeOverviewFragment()
        fragment.setLifecycleState(Lifecycle.State.RESUMED)

        // fakeViewBindingNotifier 初期設定
        fakeViewBindingNotifier.setup(fragment, viewModel)

        // ViewModel 初期設定
        // viewBindingNotify.showNotice ⇒ スワイプダウンで表示更新を通知
        viewModel.setup(
            fragment.viewLifecycleOwner,
            fakeViewBindingNotifier,
            fakeSuccessRepository
        )

        // ViewModel 状態更新
        viewModel.updateState()

        // コントリビュータ一覧取得完了(updatePage完了)まで待機
        fakeViewBindingNotifier.await()
        debugTestLog("after  success - refreshContributors()")

        // コントリビュータ一覧取得確認
        assertEquals(1, fakeViewBindingNotifier.contributors.size)
        val contributor: OverviewContributor = fakeViewBindingNotifier.contributors[0]
        debugTestLog("contributors fetch success")

        // コントリビュータ情報確認
        assertEquals(831038, contributor.id)
        assertEquals("dlam", contributor.name)
        assertEquals("https://api.github.com/users/dlam", contributor.contributorUrl)
        debugTestLog("contributor {id:${contributor.id}, name:${contributor.name}, account:${contributor.contributorUrl}}")

        // コントリビュータ一覧状態確認
        assertEquals(
            OverviewViewModelStatus.REFRESH_CONTRIBUTORS,
            fakeViewBindingNotifier.viewModelStatus
        )
    }

    /**
     * コントリビュータ一覧取得失敗時の処理を確認する。
     */
    @Test
    fun failed_refreshContributors() {
        debugTestLog("before  failed - refreshContributors()")
        viewModel = OverviewViewModel()

        // LifecycleOwner のフェイクを生成
        val fragment = FakeOverviewFragment()
        fragment.setLifecycleState(Lifecycle.State.RESUMED)

        // fakeViewBindingNotifier 初期設定
        fakeViewBindingNotifier.setup(fragment, viewModel)

        // ViewModel 初期設定
        // viewBindingNotify.showNotice ⇒ スワイプダウンで表示更新を通知
        viewModel.setup(
            fragment.viewLifecycleOwner,
            fakeViewBindingNotifier,
            fakeFailedRepository
        )

        // ViewModel 状態更新
        viewModel.updateState()

        // コントリビュータ一覧取得完了(updatePage完了)まで待機
        fakeViewBindingNotifier.await()
        debugTestLog("after  failed - refreshContributors()")

        // コントリビュータ一覧取得確認
        assertEquals(0, fakeViewBindingNotifier.contributors.size)
        debugTestLog("contributors fetch failed")

        // コントリビュータ一覧状態確認
        assertEquals(
            OverviewViewModelStatus.REFRESH_FAILED,
            fakeViewBindingNotifier.viewModelStatus
        )
    }
}

/**
 * テスト用 OverviewFragment.
 */
private class FakeOverviewFragment : LifecycleOwner {
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
 * テスト用 OverviewBindingNotifier.
 */
private class FakeOverviewViewBindingNotifier : DestinationUnspecifiedStateChangeNotifier {
    // コントリビュータ一覧画面 ViewModel
    private lateinit var _viewModel: OverviewViewModel
    val viewModel: OverviewViewModel
        get() = _viewModel

    // コントリビュータ一覧取得完了待機用ラッチ
    private val _latch: CountDownLatch = CountDownLatch(1)
    fun await() {
        _latch.await(10000, TimeUnit.MILLISECONDS)
    }

    // コントリビュータ一覧画面ステータス
    private lateinit var _viewModelStatus: OverviewViewModelStatus
    val viewModelStatus: OverviewViewModelStatus
        get() = _viewModelStatus

    // コントリビュータ一覧
    private var _contributors: MutableList<OverviewContributor> = mutableListOf()
    val contributors: List<OverviewContributor>
        get() = _contributors

    /**
     * FIXME テスト用のメソッドのため、実態とシグニチャが異なります。
     */
    fun setup(fragment: LifecycleOwner, viewModel: OverviewViewModel) {
        _viewModel = viewModel
    }

    override fun updateState() {
        // ViewModel状態遷移先ステータス
        _viewModelStatus = viewModel.status
        _contributors = viewModel.contributors
        debugTestLog("ViewBinding  updateState, status=${viewModelStatus}, contributors=${viewModel.contributors.size}")

        when (viewModelStatus) {
            OverviewViewModelStatus.INIT_REFRESH -> {
                // コントリビュータ一覧更新要求を通知
            }
            OverviewViewModelStatus.REFRESH_CONTRIBUTORS -> {
                // コントリビュータ一覧更新成功
                _latch.countDown()
            }
            OverviewViewModelStatus.REFRESH_FAILED -> {
                // コントリビュータ一覧更新失敗
                _latch.countDown()
            }
            else -> OverviewViewModelStatus.INIT_REFRESH
        }
    }
}

/**
 * テスト用フェッチ成功時 ContriViewerRepository.
 * OverViewViewModel と DetailViewModel から利用できるよう public アクセスとする。
 */
class FakeSuccessContriViewerRepository : ContriViewerRepository {

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

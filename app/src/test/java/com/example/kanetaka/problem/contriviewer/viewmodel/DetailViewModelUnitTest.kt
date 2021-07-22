package com.example.kanetaka.problem.contriviewer.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.page.DestinationUnspecifiedStateChangeNotifier
import com.example.kanetaka.problem.contriviewer.page.detail.DetailContributor
import com.example.kanetaka.problem.contriviewer.page.detail.DetailViewModel
import com.example.kanetaka.problem.contriviewer.page.detail.DetailViewModelStatus
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

        // fakeViewBindingNotifier 初期設定
        fakeViewBindingNotifier.setup(viewModel)

        // ViewModel 初期設定
        viewModel.setup(
            fragment.viewLifecycleOwner,
            fakeViewBindingNotifier,
            fakeSuccessRepository,
            "dlam"
        )

        // ViewModel 状態更新
        viewModel.updateState()

        // コントリビュータ詳細取得完了(updatePage完了)まで待機
        fakeViewBindingNotifier.await()
        debugTestLog("after  success - refreshContributor()")

        // コントリビュータ詳細情報確認
        val contributor: DetailContributor? = fakeViewBindingNotifier.contributor
        assertEquals("https://avatars.githubusercontent.com/u/831038?v=4", contributor?.iconUrl)
        assertEquals("dlam", contributor?.login)
        assertEquals("Dustin Lam", contributor?.name)
        assertEquals("https://github.com/dlam", contributor?.account)
        debugTestLog("contributor {login:${contributor?.login}, name:${contributor?.name}, account:${contributor?.account}}")

        // コントリビュータ詳細状態確認
        assertEquals(
            DetailViewModelStatus.REFRESH_CONTRIBUTOR,
            fakeViewBindingNotifier.viewModelStatus
        )
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

        // fakeViewBindingNotifier 初期設定
        fakeViewBindingNotifier.setup(viewModel)

        // ViewModel 初期設定
        viewModel.setup(
            fragment.viewLifecycleOwner,
            fakeViewBindingNotifier,
            fakeFailedRepository,
            "dlam"
        )

        // ViewModel 状態更新
        viewModel.updateState()

        // コントリビュータ詳細取得完了(updatePage完了)まで待機
        fakeViewBindingNotifier.await()
        debugTestLog("after  failed - refreshContributor()")

        // コントリビュータ詳細情報確認
        val contributor: DetailContributor? = fakeViewBindingNotifier.contributor
        assertEquals(null, contributor)
        debugTestLog("contributor fetch failed")

        // コントリビュータ詳細状態確認
        assertEquals(DetailViewModelStatus.REFRESH_FAILED, fakeViewBindingNotifier.viewModelStatus)
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
private class FakeDetailViewBindingNotifier : DestinationUnspecifiedStateChangeNotifier {
    // コントリビュータ詳細画面 ViewModel
    private lateinit var _viewModel: DetailViewModel
    val viewModel: DetailViewModel
        get() = _viewModel

    // コントリビュータ取得完了待機用ラッチ
    private val _latch: CountDownLatch = CountDownLatch(1)
    fun await() {
        _latch.await(10000, TimeUnit.MILLISECONDS)
    }

    // コントリビュータ詳細画面ステータス
    private lateinit var _viewModelStatus: DetailViewModelStatus
    val viewModelStatus: DetailViewModelStatus
        get() = _viewModelStatus

    // コントリビュータ
    private var _contributor: DetailContributor? = null
    val contributor: DetailContributor?
        get() = _contributor

    fun setup(viewModel: DetailViewModel) {
        _viewModel = viewModel
    }

    override fun updateState() {
        // ViewModel状態遷移先ステータス
        _viewModelStatus = viewModel.status
        _contributor = viewModel.contributor
        debugTestLog("ViewBinding  updateState, status=${viewModelStatus}, contributors=${viewModel.contributor}")

        when (viewModelStatus) {
            DetailViewModelStatus.INIT_REFRESH -> {
                // コントリビュータ詳細更新要求を通知
            }
            DetailViewModelStatus.REFRESH_CONTRIBUTOR -> {
                // コントリビュータ詳細更新成功
                _latch.countDown()
            }
            DetailViewModelStatus.REFRESH_FAILED -> {
                // コントリビュータ詳細更新失敗
                _latch.countDown()
            }
            else -> OverviewViewModelStatus.INIT_REFRESH
        }
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

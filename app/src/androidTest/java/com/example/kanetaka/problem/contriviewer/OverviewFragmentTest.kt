package com.example.kanetaka.problem.contriviewer

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewFragment
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewViewBinding
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewViewModel
import com.example.kanetaka.problem.contriviewer.page.overview.OverviewViewModelStatus
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
import com.example.kanetaka.problem.contriviewer.util.SimpleFactory
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog
import com.example.kanetaka.problem.contriviewer.util.VariableHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * OverviewFragment チェック
 */
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class OverviewViewFragmentTest {

    private val fakeSuccessRepository = FakeSuccessContriViewerRepository()
    private val fakeFailedRepository = FakeFailedContriViewerRepository()

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    /**
     * FIXME 【作成途中】 Espresso で表示状態を確認すること。
     *
     * フェッチ成功発生時に、OverviewFragment が一覧表示状態に遷移しているかチェックします。
     *
     * - Instrumented test では、アプリ実行中のコンテキストを持つオブジェクトが取得できます。
     * - FragmentScenario を利用すると、指定状態遷移後から処理を開始したフラグメント・オブジェクトが取得できます。
     * - FragmentScenario の moveToState(Lifecycle.State)は、RESUMEDなどの指定ステートへの状態遷移後の実装処理フローが実行されます。
     * - Instrumented test では、処理フローに介在できないため、
     *   処理フロー中の必須前提物オブジェクトを
     *   テスト時に外部から可換化できるような実装にしておかないと意味の有るテストができません。
     *
     * 【注意事項】
     * - Fragmentは、テストで利用する参照専用のプロパティを公開する必要が有る。
     * - Fragmentオブジェクトは取得できるが、処理フローに介入できないため、参照プロパティは、結果しか確認できない。
     * - 処理フローに介入できないため、実装コード上で「任意条件で 必須物 を差替可能」にしないと意味のある動作確認ができない。
     * - テスト中にエミュレータを回転させても、エミュレータ画面が回転しても、内部的には onDestroy ⇒ onCreate が発生しない。
     */
    fun fragment_success_view_transition_test() {
        // 処理終了判定用ラッチ
        val latch = CountDownLatch(1)

        // 簡易依存性注入条件設定
        SimpleFactory.setCondition<ContriViewerRepository>(
            fakeSuccessRepository,
            "com.example.kanetaka.problem.contriviewer.page.overview.OverviewFragment")

        // OverviewFragment の ViewBinding と ViewModel の可変ホルダー生成
        val viewBinding = VariableHolder<OverviewViewBinding>()
        val viewModel = VariableHolder<OverviewViewModel>()

        // ステータスとコントリビュータ一覧数の初期値設定用
        var initStatus = OverviewViewModelStatus.REFRESH_FAILED
        var initContributorCounts = -1

        // OverviewFragment にテストする際の LifeCycle 状態を指定できるようにする。
        val fragmentScenario = launchFragmentInContainer<OverviewFragment>()

        debugLog("before  fragment_success_view_transition_test - RESUMED")
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // OverviewFragment オブジェクトを取得する。
        fragmentScenario.onFragment { fragment ->
            // Fragment のテスト用オブジェクトから ViewBinding と ViewModel を取得
            viewBinding.nullableValue = fragment.viewBinding
            viewModel.nullableValue = fragment.viewModel

            // Repository オブジェクトは、
            // 簡易依存性注入条件設定により fakeSuccessRepository に差し替えられています。

            // ViewModel 初期状態値取得
            initStatus = viewModel.value.status
            initContributorCounts = viewModel.value.contributors.size

            /*
            // ViewModel#_status : MutableLiveData<OverviewViewModelStatus> が、公開されている場合なら利用可能
            // テスト終了条件を設定（コントリビュータ一覧画面ステータスが REFRESH_CONTRIBUTORS に遷移するまで待機）
            viewModel.value._status.observeForever {
                debugLog("status=${it}")
                if (it == OverviewViewModelStatus.REFRESH_CONTRIBUTORS) latch.countDown()
            }
            */
        }

        latch.await(5000, TimeUnit.MILLISECONDS)
        debugLog("after  fragment_success_view_transition_test - RESUMED")

        // ViewModel 初期状態のチェック
        Assert.assertEquals(OverviewViewModelStatus.INIT_REFRESH, initStatus)
        Assert.assertEquals(0, initContributorCounts)

        // ViewModel 一覧取得後状態のチェック
        Assert.assertEquals(OverviewViewModelStatus.REFRESH_CONTRIBUTORS, viewModel.value.status)
        Assert.assertEquals(1, viewModel.value.contributors.size)
        debugLog("contributors.size=${viewModel.value.contributors.size}, status=${viewModel.value.status}")

        /*
        // 取得した ViewBinding オブジェクトを使ってトーストを表示させる。
        // ・下記のエラーが出ないように、Mainスレッドの Looper を作成します。
        // 　java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            viewBinding.value.showNotice(R.string.contributor_detail_twitter_template)
        }
        Thread.sleep(5000)
        */

        // 後始末
        viewModel.destroy()
        viewBinding.destroy()

        /*
        // 状態遷移監視対象とリアクションの設定
        val status = VariableHolder<MutableLiveData<OverviewViewModelStatus>>()
        val observer = VariableHolder<Observer<OverviewViewModelStatus>>()

        // ステータスのオブザーバにリアクションを追加
        observer.value = Observer {
            debugLog("status=${it}")
            if (it == OverviewViewModelStatus.REFRESH_CONTRIBUTORS) latch.countDown()
        }
        status.value.observe(observer)

        // 後始末
        status.value.removeObserver(observer.value)
        observer.destroy()
        status.destroy()
        */
    }

    @Test
    /**
     * FIXME 【作成途中】 Espresso で表示状態を確認すること。
     * フェッチ失敗発生時に、OverviewFragment がエラー状態に遷移しているかチェックします。
     */
    fun fragment_failed_view_transition_test() {
        // 処理終了判定用ラッチ
        val latch = CountDownLatch(1)

        // 簡易依存性注入条件設定
        SimpleFactory.setCondition<ContriViewerRepository>(
            fakeFailedRepository,
            "com.example.kanetaka.problem.contriviewer.page.overview.OverviewFragment")

        // OverviewFragment の ViewBinding と ViewModel の可変ホルダー生成
        val viewBinding = VariableHolder<OverviewViewBinding>()
        val viewModel = VariableHolder<OverviewViewModel>()

        // ステータスとコントリビュータ一覧数の初期値設定用
        var initStatus = OverviewViewModelStatus.REFRESH_FAILED
        var initContributorCounts = -1

        // OverviewFragment にテストする際の LifeCycle 状態を指定できるようにする。
        val fragmentScenario = launchFragmentInContainer<OverviewFragment>()

        debugLog("before  fragment_failed_view_transition_test - RESUMED")
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // OverviewFragment オブジェクトを取得する。
        fragmentScenario.onFragment { fragment ->
            // Fragment のテスト用オブジェクトから ViewBinding と ViewModel を取得
            viewBinding.nullableValue = fragment.viewBinding
            viewModel.nullableValue = fragment.viewModel

            // Repository オブジェクトは、
            // 簡易依存性注入条件設定により fakeFailedRepository に差し替えられています。

            // ViewModel 初期状態値取得
            initStatus = viewModel.value.status
            initContributorCounts = viewModel.value.contributors.size

            /*
            // ViewModel#_status : MutableLiveData<OverviewViewModelStatus> が、公開されている場合なら利用可能
            // テスト終了条件を設定（コントリビュータ一覧画面ステータスが REFRESH_CONTRIBUTORS に遷移するまで待機）
            viewModel.value._status.observeForever {
                debugLog("status=${it}")
                if (it == OverviewViewModelStatus.REFRESH_CONTRIBUTORS) latch.countDown()
            }
            */
        }

        latch.await(5000, TimeUnit.MILLISECONDS)
        debugLog("after  fragment_failed_view_transition_test - RESUMED")

        // ViewModel 初期状態のチェック
        Assert.assertEquals(OverviewViewModelStatus.INIT_REFRESH, initStatus)
        Assert.assertEquals(0, initContributorCounts)

        // ViewModel 一覧取得後状態のチェック
        Assert.assertEquals(OverviewViewModelStatus.REFRESH_FAILED, viewModel.value.status)
        Assert.assertEquals(0, viewModel.value.contributors.size)
        debugLog("contributors.size=${viewModel.value.contributors.size}, status=${viewModel.value.status}")

        // 後始末
        viewModel.destroy()
        viewBinding.destroy()
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
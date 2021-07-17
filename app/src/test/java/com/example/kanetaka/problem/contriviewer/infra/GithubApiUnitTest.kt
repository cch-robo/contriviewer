package com.example.kanetaka.problem.contriviewer.infra

import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailService
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewService
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugTestLog
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * OverviewService ＆ DetailService 動作確認（ネットワークが機能するときしかテストできません）
 */
@ExperimentalCoroutinesApi
class GithubApiUnitTest {

    private val testDispatcher = TestCoroutineDispatcher()

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
     * OverviewService がコントリビュータ一覧を返すかチェック
     */
    @Test
    fun can_fetch_github_api_contributors() {
        val latch = CountDownLatch(1)
        var overviews = listOf<OverviewModel>()

        val scope = CoroutineScope(Job() + testDispatcher)
        scope.launch(Dispatchers.IO) {
            val response = OverviewService.retrofitService.getContributors(30, 1, false)
            overviews = response
            latch.countDown()
        }
        latch.await(10000, TimeUnit.MILLISECONDS)

        assertTrue(overviews.isNotEmpty())
        assertEquals(30, overviews.size)
        debugTestLog("fetched contributor counts are ${overviews.size}.")
    }

    /**
     * DetailService からコントリビュータを取得する。
     * （dlam さんは、実在するコントリビュータ）
     */
    @Test
    fun can_fetch_github_api_contributor() {
        val latch = CountDownLatch(1)
        var contributor: DetailModel? = null

        val scope = CoroutineScope(Job() + testDispatcher)
        scope.launch(Dispatchers.IO) {
            val response = DetailService.retrofitService.getContributor("dlam")
            contributor = response
            latch.countDown()
        }
        latch.await(10000, TimeUnit.MILLISECONDS)

        Assert.assertNotNull(contributor)
        assertEquals("dlam", contributor?.login)
        assertEquals("@Google", contributor?.company)
        debugTestLog("fetched contributor name is ${contributor?.name}.")
    }
}

package com.example.kanetaka.problem.contriviewer.infra

import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailService
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewService
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * OverviewService ＆ DetailService 動作確認（ネットワークが機能するときしかテストできません）
 */
class GithubApiUnitTest {
    /**
     * OverviewService がコントリビュータ一覧を返すかチェック
     */
    @ExperimentalCoroutinesApi
    @Test
    fun can_fetch_github_api_contributors() {
        val testDispatcher = TestCoroutineDispatcher()
        val scope = CoroutineScope(Job() + testDispatcher)
        scope.launch(Dispatchers.IO) {
            val response = OverviewService.retrofitService.getContributors(30, 1, false)
            val overviews: List<OverviewModel> = response
            assertTrue(overviews.isNotEmpty())
            assertEquals(30, overviews.size)
        }
        testDispatcher.cleanupTestCoroutines()
    }

    /**
     * DetailService からコントリビュータを取得する。
     * （dlam さんは、実在するコントリビュータ）
     */
    @ExperimentalCoroutinesApi
    @Test
    fun can_fetch_github_api_contributor() {
        val testDispatcher = TestCoroutineDispatcher()
        val scope = CoroutineScope(Job() + testDispatcher)
        scope.launch(Dispatchers.IO) {
            val response = DetailService.retrofitService.getContributor("dlam")
            val contributor: DetailModel = response

            Assert.assertNotNull(contributor)
            assertEquals("dlam", contributor.login)
            assertEquals("@Google", contributor.company)
        }
    }
}

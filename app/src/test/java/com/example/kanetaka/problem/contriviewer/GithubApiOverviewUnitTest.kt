package com.example.kanetaka.problem.contriviewer

import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * OverviewService 動作確認（ネットワークが機能するときしかテストできません）
 */
class GithubApiOverviewUnitTest {
    /**
     * OverviewService がコントリビュータ一覧を返すかチェック
     */
    @Test
    fun can_fetch_github_api_contributors() {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            val response = OverviewService.retrofitService.getContributors()
            assertTrue(response.isNotEmpty())
        }
    }

    /**
     * GitHub API のコントリビュータ一覧のデフォルト仕様、30件分を取得するかチェック
     */
    @Test
    fun fetch_github_api_contributors_isComplete() {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            val response = OverviewService.retrofitService.getContributors()
            assertEquals(response.size, 30)
        }
    }
}

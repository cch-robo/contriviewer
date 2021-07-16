package com.example.kanetaka.problem.contriviewer.infra

import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * OverviewService 動作確認（ネットワークが機能するときしかテストできません）
 */
class GithubApiOverviewUnitTest {
    private lateinit var contributors: List<OverviewModel>

    @Before
    fun setUp() {
        // テスト時には、コルーチンから
        // プラットフォーム Mainディスパッチャーが使用できないので単純な解決策で対応
        //
        // Kotlin の Unit Test 用ライブラリを試したが、複雑になるため今回は利用していません。
        // kotlinx.coroutines/kotlinx-coroutines-test/
        // https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test

        setupContributors()
        Thread.sleep(5000)
    }

    @After
    fun tearDown() {
    }


    /**
     * OverviewService がコントリビュータ一覧を返すかチェック
     */
    @Test
    fun can_fetch_github_api_contributors() {
        assertTrue(contributors.isNotEmpty())
    }

    /**
     * GitHub API のコントリビュータ一覧のデフォルト仕様、30件分を取得するかチェック
     */
    @Test
    fun fetch_github_api_contributors_isComplete() {
        assertEquals(30, contributors.size)
    }

    /**
     * OverviewService からコントリビュータ一覧を取得する。
     */
    private fun setupContributors() {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            val response = OverviewService.retrofitService.getContributors(30, 1, false)
            contributors = response
        }
    }
}

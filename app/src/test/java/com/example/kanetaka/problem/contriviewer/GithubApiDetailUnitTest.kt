package com.example.kanetaka.problem.contriviewer

import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.ContributorModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * DetailService 動作確認（ネットワークが機能するときしかテストできません）
 */
class GithubApiDetailUnitTest {
    private lateinit var contributor: ContributorModel

    @Before
    fun setUp() {
        // テスト時には、コルーチンから
        // プラットフォーム Mainディスパッチャーが使用できないので単純な解決策で対応
        //
        // Kotlin の Unit Test 用ライブラリを試したが、複雑になるため今回は利用していません。
        // kotlinx.coroutines/kotlinx-coroutines-test/
        // https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test

        setupContributor()
        Thread.sleep(5000)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun can_fetch_github_api_contributor() {
        assertNotNull(contributor)
        assertEquals("dlam", contributor.login)
        assertEquals("@Google", contributor.company)
    }

    /**
     * DetailService からコントリビュータを取得する。
     * （dlam さんは、実在するコントリビュータ）
     */
    private fun setupContributor() {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            val response = DetailService.retrofitService.getContributor("dlam")
            contributor = response
        }
    }
}


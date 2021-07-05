package com.example.kanetaka.problem.contriviewer.repository

import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

class ContributorCaches {
    // コントリビュータ一覧キャッシュ取得時間
    private var overviewCachedTime = 0L

    // コントリビュータ一覧キャッシュ
    private val overviewCache = mutableSetOf<Result<List<OverviewModel>>>()

    // コントリビュータ詳細キャッシュ
    private val detailCache = mutableMapOf<String, Result<DetailModel>>()

    /**
     * コントリビュータ一覧キャッシュ・チェック
     * （前回取得より5分以上経過していた場合は、再取得する）
     */
    fun isCachedOverview(): Boolean {
        return overviewCache.isNotEmpty() && (overviewCachedTime > (System.currentTimeMillis() - 5 * 60 * 1000))
    }

    /**
     * コントリビュータ一覧キャッシュ取得
     */
    fun getCachedOverview(): Result<List<OverviewModel>> {
        debugLog("ContributorCaches  getCachedOverview")
        return overviewCache.first()
    }

    /**
     * コントリビュータ一覧キャッシュ更新（詳細クリアを伴う）
     */
    fun setCacheOverview(result: Result<List<OverviewModel>>) {
        if (result.isSuccess) {
            overviewCachedTime = System.currentTimeMillis()
            overviewCache.clear()
            detailCache.clear()
            overviewCache.add(result)
            debugLog("ContributorCaches  setCacheOverview")
        } else {
            debugLog("ContributorCaches  setCacheOverview canceled by error")
        }
    }

    /**
     * コントリビュータ詳細キャッシュ・チェック
     */
    fun isCachedDetail(login: String): Boolean {
        return detailCache.containsKey(login)
    }

    /**
     * コントリビュータ詳細キャッシュ取得
     */
    fun getCachedDetail(login: String): Result<DetailModel> {
        debugLog("ContributorCaches  getCachedDetail")
        return detailCache[login]!!
    }

    /**
     * コントリビュータ詳細キャッシュ追加
     */
    fun addCacheDetail(login: String, result: Result<DetailModel>) {
        if (result.isSuccess) {
            debugLog("ContributorCaches  addCacheDetail")
            detailCache[login] = result
        } else {
            debugLog("ContributorCaches  addCacheDetail canceled by error")
        }
    }
}
package com.example.kanetaka.problem.contriviewer.repository

import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailService
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewService
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

class ContributorRepository : ContriViewerRepository {

    /**
     * コントリビュータ一覧をフェッチする。
     */
    override suspend fun fetchContributors(): Result<List<OverviewModel>> {
        val result: Result<List<OverviewModel>> = try {
            val response = OverviewService.retrofitService.getContributors(100, 1, false)
            Result.success(response)
        } catch (e: Exception) {
            debugLog("fetchContributors exception ${e.message}")
            Result.failure(e)
        } finally {
        }
        return result
    }

    /**
     * コントリビュータ情報をフェッチする。
     */
    override suspend fun fetchContributor(login: String): Result<DetailModel> {
        val result: Result<DetailModel> = try {
            val response = DetailService.retrofitService.getContributor(login)

            debugLog("login=${response.login}, name=${response.name}")
            Result.success(response)
        } catch (e: Exception) {
            debugLog("refreshContributors exception ${e.message}")
            Result.failure(e)
        } finally {
        }
        return result
    }
}
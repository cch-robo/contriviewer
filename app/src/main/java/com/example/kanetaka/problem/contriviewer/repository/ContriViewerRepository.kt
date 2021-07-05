package com.example.kanetaka.problem.contriviewer.repository

import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailModel
import com.example.kanetaka.problem.contriviewer.infra.githubapi.overview.OverviewModel

interface ContriViewerRepository {
    // コントリビュータ一覧取得
    suspend fun fetchContributors(): Result<List<OverviewModel>>

    // コントリビュータ情報取得
    suspend fun fetchContributor(login: String): Result<DetailModel>
}
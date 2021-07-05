package com.example.kanetaka.problem.contriviewer.application

import android.app.Application
import com.example.kanetaka.problem.contriviewer.repository.ContriViewerRepository
import com.example.kanetaka.problem.contriviewer.repository.ContributorRepository

class ContriViewerApplication : Application() {
    // リポジトリをアプリケーションスコープで管理します
    private val _repo: ContriViewerRepository = ContributorRepository()
    val repo: ContriViewerRepository
        get() = _repo
}



package com.example.kanetaka.problem.contriviewer.page.overview

data class OverviewContributor(
    val id: Long,
    val name: String,
    val iconUrl: String,
    val contributions: Long,
    val contributorUrl: String
)
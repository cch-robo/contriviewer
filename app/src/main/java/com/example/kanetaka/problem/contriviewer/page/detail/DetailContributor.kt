package com.example.kanetaka.problem.contriviewer.page.detail

data class DetailContributor(
    val iconUrl: String,
    val login: String,
    val name: String?,
    val bio: String?,
    val account: String,
    val company: String?,
    val location: String?,
    val email: String?,
    val blog: String,
    val twitter_username: String?,
    val followers: Long,
    val following: Long,
    val public_repos: Long,
    val public_gists: Long
)

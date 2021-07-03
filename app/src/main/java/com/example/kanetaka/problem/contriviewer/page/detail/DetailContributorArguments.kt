package com.example.kanetaka.problem.contriviewer.page.detail

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class DetailContributorArguments(
    val id: Long,
    val login: String,
    val contributorUrl: String
) : Parcelable

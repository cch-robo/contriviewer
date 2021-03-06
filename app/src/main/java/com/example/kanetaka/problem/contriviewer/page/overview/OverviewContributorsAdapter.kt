package com.example.kanetaka.problem.contriviewer.page.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.databinding.OverviewItemBinding
import com.example.kanetaka.problem.contriviewer.page.detail.DetailContributorArguments
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

class OverviewContributorsAdapter(private val fragment: OverviewFragment) :
    ListAdapter<OverviewContributor, RecyclerView.ViewHolder>(OverviewContributorDiffCallback()) {

    init {
        debugLog("OverviewContributorsAdapter#init")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributorViewHolder {
        debugLog("OverviewContributorsAdapter#onCreateViewHolder [${viewType}]")
        val layoutInflater = LayoutInflater.from(parent.context)
        return ContributorViewHolder(
            OverviewItemBinding.inflate(layoutInflater, parent, false),
            fragment
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as ContributorViewHolder).bind(item)
    }

    class ContributorViewHolder(
        private val binding: OverviewItemBinding,
        private val fragment: OverviewFragment
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OverviewContributor) {
            debugLog("ContributorViewHolder#bind [${item.name}]")
            binding.contributorName.text = item.name
            binding.contributorContribution.text = item.contributions.toString()

            Glide.with(binding.root.context)
                .load(item.iconUrl)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_connection_error)
                .into(binding.contributorIcon)

            binding.contributorItem.setOnClickListener {
                val action = OverviewFragmentDirections.actionOverviewToDetail(
                    DetailContributorArguments(item.id, item.name, item.contributorUrl)
                )
                fragment.findNavController().navigate(action)
            }
        }
    }

    class OverviewContributorDiffCallback : DiffUtil.ItemCallback<OverviewContributor>() {
        override fun areItemsTheSame(
            oldItem: OverviewContributor,
            newItem: OverviewContributor
        ): Boolean {
            debugLog("OverviewContributorDiffCallback#areItemsTheSame")
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: OverviewContributor,
            newItem: OverviewContributor
        ): Boolean {
            debugLog("OverviewContributorDiffCallback#areContentsTheSame")
            return oldItem == newItem
        }
    }
}
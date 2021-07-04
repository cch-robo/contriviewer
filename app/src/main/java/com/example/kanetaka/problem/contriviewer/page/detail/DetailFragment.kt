package com.example.kanetaka.problem.contriviewer.page.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.databinding.FragmentDetailBinding
import com.example.kanetaka.problem.contriviewer.infra.githubapi.detail.DetailService
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog
import kotlinx.coroutines.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DetailFragment : Fragment() {

    // Navigation Editor で自動生成された遷移パラメータ
    val args: DetailFragmentArgs by navArgs()

    private var _binding: FragmentDetailBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailArguments = args.detailArguments
        debugLog("DetailFragment#onViewCreated  detailArguments[id:${detailArguments.id}, login:${detailArguments.login}, url:${detailArguments.contributorUrl}]")

        setupContributor(detailArguments.login)
    }

    private fun setupContributor(login:String) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            debugLog("setupContributor  setup start!!")
            val response = DetailService.retrofitService.getContributor(login)

            // 上記処理が完了してから、メインスレッドで実行されます。
            withContext(Dispatchers.Main) {
                Glide.with(binding.root.context)
                    .load(response.avatar_url)
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_connection_error)
                    .into(binding.contributorIcon)

                binding.textviewLogin.text = response.login
                binding.textviewName.text = response.name ?: "none"
                binding.textviewBio.text = response.bio ?: "none"

                binding.textviewCompany.text = response.company ?: "none"
                binding.textviewLocation.text = response.location ?: "none"
                binding.textviewEmail.text = response.email ?: "none"
                binding.textviewBlog.text = response.blog ?: "none"
                if (response.blog != null && response.blog.isNotEmpty()) {
                    binding.textviewBlog.text = response.blog
                } else {
                    binding.textviewBlog.text = "none"
                }
                if (response.twitter_username != null) {
                    binding.textviewTwitter.text = "https://twitter.com/${response.twitter_username}"
                } else {
                    binding.textviewTwitter.text = "none"
                }

                binding.textviewFollowers.text = "followers  ${response.followers.toString()}"
                binding.textviewFollowing.text = "following  ${response.following.toString()}"

                binding.textviewPublicRepos.text = "public repos  ${response.public_repos.toString()}"
                binding.textviewPublicGists.text = "public gists  ${response.public_gists.toString()}"

                debugLog("setupContributor  setup end!!")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
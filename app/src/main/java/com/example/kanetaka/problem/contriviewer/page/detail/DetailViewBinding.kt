package com.example.kanetaka.problem.contriviewer.page.detail

import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.bumptech.glide.Glide
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.databinding.FragmentDetailBinding
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

/**
 * ViewBinding用の通知インターフェース。
 * ViewModel から View への通知インターフェースを提供します。
 */
interface DetailViewBindingNotifier {
    // ページ更新開始通知
    fun updatePage(contributor: DetailContributor?)

    // リフレッシュ終了通知
    fun refreshStopped()

    // ユーザへのメッセージ依頼通知
    fun showNotice(@StringRes messageId: Int)
}

/**
 * View の ViewModel との同期表示を管理する
 */
class DetailViewBinding(
    private val binding: FragmentDetailBinding
) : DetailViewBindingNotifier {

    val root: View
        get() = binding.root

    private lateinit var _notify: DetailViewModelNotifier
    private val notify: DetailViewModelNotifier
        get() = _notify

    fun setup(ViewModelNotifier: DetailViewModelNotifier) {
        debugLog("DetailViewBinding  setup start")
        _notify = ViewModelNotifier

        // スワイプによるコントリビュータ一覧の更新
        binding.overviewSwipe.setOnRefreshListener {
            refreshStart()
        }
        debugLog("DetailViewBinding  setup end")
    }

    private fun refreshStart() {
        debugLog("DetailViewBinding  refreshStart")
        // Swipe によりプログレスが回りだしたので、コントリビュータ一覧を更新する。
        notify.refreshContributor()
    }

    override fun refreshStopped() {
        // Swipe によりプログレスが回っているので、リフレッシュプログレスの回転を止める。
        binding.overviewSwipe.isRefreshing = false
        debugLog("DetailViewBinding  refreshStopped")
    }

    override fun showNotice(@StringRes messageId: Int) {
        // ユーザへメッセージを通知
        val message: String = binding.root.context.getString(messageId)
        Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
    }

    override fun updatePage(contributor: DetailContributor?) {
        debugLog("DetailViewBinding  updatePage(${contributor?.login})")
        if (contributor == null) {
            binding.contributorMainItem.visibility = View.GONE
            binding.contributorSubItem.visibility = View.GONE
            return
        }

        binding.contributorMainItem.visibility = View.VISIBLE
        binding.contributorSubItem.visibility = View.VISIBLE

        Glide.with(binding.root.context)
            .load(contributor.iconUrl)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_connection_error)
            .into(binding.contributorIcon)

        binding.textviewLogin.text = contributor.login
        setupTextView(binding.textviewName, contributor.name)
        setupTextView(binding.textviewBio, contributor.bio)

        setupTextView(binding.textviewCompany, contributor.company)
        setupTextView(binding.textviewLocation, contributor.location)
        setupTextView(binding.textviewEmail, contributor.email)

        setupBlogTextView(binding.textviewBlog, contributor.blog)
        setupTwitterTextView(binding.textviewTwitter, contributor.twitter_username)

        setupTextView(
            R.string.contributor_detail_followers_template,
            binding.textviewFollowers,
            contributor.followers
        )
        setupTextView(
            R.string.contributor_detail_following_template,
            binding.textviewFollowing,
            contributor.following
        )

        setupTextView(
            R.string.contributor_detail_public_repos_template,
            binding.textviewPublicRepos,
            contributor.public_repos
        )
        setupTextView(
            R.string.contributor_detail_public_gists_template,
            binding.textviewPublicGists,
            contributor.public_gists
        )
        debugLog("setupContributor  setup end!!")
    }

    private fun setupTextView(textView: TextView, text: String?) {
        if (text != null) {
            textView.text = text
            textView.visibility = View.VISIBLE
        } else {
            textView.text = ""
            textView.visibility = View.GONE
        }
    }

    private fun setupBlogTextView(textView: TextView, blogUrl: String) {
        if (blogUrl.isNotEmpty()) {
            textView.text = blogUrl
            textView.visibility = View.VISIBLE
        } else {
            textView.text = ""
            textView.visibility = View.GONE
        }
    }

    private fun setupTwitterTextView(textView: TextView, userName: String?) {
        if (userName != null) {
            val twitterUrl =
                binding.root.context.getString(R.string.contributor_detail_twitter_template) + userName
            textView.text = twitterUrl
            textView.visibility = View.VISIBLE
        } else {
            textView.text = ""
            textView.visibility = View.GONE
        }
    }

    private fun setupTextView(@StringRes stringId: Int, textView: TextView, number: Long) {
        val numeric = number.toShort()
        val text = binding.root.context.getString(stringId) + "  $numeric"
        textView.text = text
        textView.visibility = View.VISIBLE
    }
}
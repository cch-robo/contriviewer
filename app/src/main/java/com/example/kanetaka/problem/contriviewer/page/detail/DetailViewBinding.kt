package com.example.kanetaka.problem.contriviewer.page.detail

import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.bumptech.glide.Glide
import com.example.kanetaka.problem.contriviewer.R
import com.example.kanetaka.problem.contriviewer.databinding.FragmentDetailBinding
import com.example.kanetaka.problem.contriviewer.page.DestinationUnspecifiedStateChangeNotifier
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog

/**
 * ViewBinding用の通知インターフェース。（ViewModelには公開しない）
 * View への通知インターフェースを提供します。
 */
interface DetailViewBindingNotifier {
    // ページ更新開始通知
    fun updatePage(viewModel: DetailViewModel)

    // ユーザへのメッセージ依頼通知
    fun showNotice(@StringRes messageId: Int)
}

/**
 * View の ViewModel との同期表示を管理する
 */
class DetailViewBinding(
    private val binding: FragmentDetailBinding
) : DetailViewBindingNotifier, DestinationUnspecifiedStateChangeNotifier {

    val root: View
        get() = binding.root

    private lateinit var _viewModel: DetailViewModel
    private val viewModel: DetailViewModel
        get() = _viewModel

    private lateinit var _notify: DestinationUnspecifiedStateChangeNotifier
    private val notify: DestinationUnspecifiedStateChangeNotifier
        get() = _notify

    fun setup(viewModel: DetailViewModel) {
        debugLog("DetailViewBinding  setup start")
        _viewModel = viewModel
        _notify = viewModel
        debugLog("DetailViewBinding  setup end")
    }

    /**
     * 不特定先からの状態更新通知(状態遷移先通知)への対応。（ViewModelにも公開される）
     */
    override fun updateState() {
        debugLog("DetailViewBinding  updateState, state=${viewModel.status}")
        when (viewModel.status) {
            DetailViewModelStatus.INIT_REFRESH -> {
                // コントリビュータ詳細初期表示
                updatePage(viewModel)

                // コントリビュータ詳細を更新する
                viewModel.refreshContributor()
            }
            DetailViewModelStatus.REFRESH_CONTRIBUTOR -> {
                // コントリビュータ詳細更新成功
                updatePage(viewModel)
            }
            DetailViewModelStatus.REFRESH_FAILED -> {
                // コントリビュータ詳細更新失敗
                updatePage(viewModel)
                showNotice(R.string.contributor_detail_refresh_error)
            }
        }
    }

    /**
     * ユーザへメッセージを通知（ViewModelには公開しない）
     */
    override fun showNotice(@StringRes messageId: Int) {
        // ユーザへメッセージを通知
        val message: String = binding.root.context.getString(messageId)
        Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * コントリビュータ詳細更新通知（ViewModelには公開しない）
     */
    override fun updatePage(viewModel: DetailViewModel) {
        debugLog("DetailViewBinding  updatePage(${viewModel.contributor?.login}), state=${viewModel.status}")
        if (viewModel.contributor == null) {
            when (viewModel.status) {
                DetailViewModelStatus.INIT_REFRESH -> {
                    updatePageMode(DetailViewModelStatus.INIT_REFRESH)
                }
                DetailViewModelStatus.REFRESH_FAILED -> {
                    updatePageMode(DetailViewModelStatus.REFRESH_FAILED)
                    debugLog("DetailViewBinding  refresh Error")
                }
                else -> {
                    // REFRESH_CONTRIBUTOR かつコントリビュータ無し
                    updatePageMode(DetailViewModelStatus.REFRESH_FAILED)
                    debugLog("DetailViewBinding  refresh Unexpected")
                }
            }
        } else {
            if (viewModel.status == DetailViewModelStatus.REFRESH_CONTRIBUTOR) {
                updatePageMode(DetailViewModelStatus.REFRESH_CONTRIBUTOR)

                // contributor コンディションは、not null が保証されている。
                updatePageContents(viewModel.contributor!!)
            }
        }
    }

    /**
     * コントリビュータ詳細画面表示コンテンツ更新
     */
    private fun updatePageContents(contributor: DetailContributor) {
        Glide.with(binding.root.context)
            .load(contributor.iconUrl)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_connection_error)
            .into(binding.contributorIcon)

        binding.textviewLogin.text = contributor.login
        setupTextView(binding.textviewName, contributor.name)
        setupTextView(binding.textviewBio, contributor.bio)

        setupTextView(binding.textviewAccount, contributor.account)
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

    /**
     * コントリビュータ詳細画面表示モード更新
     */
    private fun updatePageMode(status : DetailViewModelStatus) {
        when(status) {
            DetailViewModelStatus.INIT_REFRESH -> {
                // 初期表示（空白画面）
                updatePageMode(true, false, false)
            }
            DetailViewModelStatus.REFRESH_CONTRIBUTOR -> {
                // コントリビュータ詳細表示
                updatePageMode(false, true, false)
            }
            DetailViewModelStatus.REFRESH_FAILED -> {
                // エラー表示（雲アイコンにスラッシュ）
                updatePageMode(false, false, true)
            }
        }
    }

    private fun updatePageMode(
        isShowProgress: Boolean,
        isShowContents: Boolean,
        isShowError: Boolean
    ) {
        binding.detailProgress.visibility = getVisibility(isShowProgress)
        binding.contributorContents.visibility = getVisibility(isShowContents)
        binding.detailConnectionError.visibility = getVisibility(isShowError)
    }

    private fun getVisibility(isShow: Boolean): Int {
        if (isShow) return View.VISIBLE
        return View.GONE
    }
}

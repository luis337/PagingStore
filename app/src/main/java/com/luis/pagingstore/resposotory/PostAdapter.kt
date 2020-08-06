package com.luis.pagingstore.resposotory

import android.util.Log
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dropbox.android.external.fs3.FileSystemPersister
import com.dropbox.android.external.fs3.PathResolver
import com.dropbox.android.external.fs3.filesystem.FileSystemFactory
import com.dropbox.android.external.store4.StoreResponse
import com.luis.pagingstore.GlideRequests
import com.luis.pagingstore.R
import com.luis.pagingstore.iu.NetworkStateItemViewHolder
import com.luis.pagingstore.iu.RedditPostViewHolder
import com.luis.pagingstore.vo.RedditPost

class PostAdapter(private val glide: GlideRequests,
                  private val retryCallback: () -> Unit
): PagedListAdapter<RedditPost, RecyclerView.ViewHolder>(POST_COMPARATOR) {
    var response: StoreResponse<Any>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.reddit_post_item -> {
                RedditPostViewHolder.create(parent, glide)
            }
            R.layout.network_state_item -> {

                NetworkStateItemViewHolder.create(parent, retryCallback)
            }
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.reddit_post_item -> (holder as RedditPostViewHolder).bind(getItem(position))
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bindTo(response)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val item = getItem(position)
            (holder as RedditPostViewHolder).updateScore(item)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    private fun hasExtraRow(): Boolean {
        return  response != null && response !is StoreResponse.Loading
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {

            R.layout.network_state_item
        } else {

            R.layout.reddit_post_item
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setStoreResponse(newResponse: StoreResponse<Any>) {
        val previousState = this.response
        val hadExtraRow = hasExtraRow()
        this.response = newResponse
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newResponse) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        private val PAYLOAD_SCORE = Any()
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<RedditPost>() {
            override fun areContentsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean =
                oldItem.name == newItem.name

            override fun getChangePayload(oldItem: RedditPost, newItem: RedditPost): Any? {
                return if (sameExceptScore(oldItem, newItem)) {
                    PAYLOAD_SCORE
                } else {
                    null
                }
            }
        }

        private fun sameExceptScore(oldItem: RedditPost, newItem: RedditPost): Boolean {
            return oldItem.copy(score = newItem.score) == newItem
        }

    }
}



package com.luis.pagingstore.iu

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luis.pagingstore.GlideRequests
import com.luis.pagingstore.R
import com.luis.pagingstore.vo.RedditPost

class RedditPostViewHolder(view: View, private val glide: GlideRequests): RecyclerView.ViewHolder(view) {

    private val title: TextView = view.findViewById(R.id.title)
    private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val score: TextView = view.findViewById(R.id.score)
    private val thumbnail : ImageView = view.findViewById(R.id.thumbnail)
    private var post : RedditPost? = null

    fun bind(post: RedditPost?) {
        this.post = post
        title.text = post?.title ?: "loading"
        subtitle.text = itemView.context.resources.getString(R.string.post_subtitle,
            post?.author ?: "unknown")
        score.text = "${post?.score ?: 0}"
        if (post?.thumbnail?.startsWith("http") == true) {
            thumbnail.visibility = View.VISIBLE
            glide.load(post.thumbnail)
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(thumbnail)
        } else {
            thumbnail.visibility = View.GONE
            glide.clear(thumbnail)
        }
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): RedditPostViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.reddit_post_item, parent, false)
            return RedditPostViewHolder(view, glide)
        }
    }

    fun updateScore(item: RedditPost?) {
        post = item
        score.text = "${item?.score ?: 0}"
    }
}
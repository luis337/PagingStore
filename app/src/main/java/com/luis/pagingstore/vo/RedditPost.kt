package com.luis.pagingstore.vo

import androidx.room.*

@Entity(tableName = "search")
data class Search (
    @PrimaryKey(autoGenerate = true) var searchId: Long? = null,
    var name: String
)

@Entity(tableName = "reddit",
    foreignKeys = [ForeignKey(
        entity = Search::class,
        parentColumns = ["searchId"],
        childColumns = ["search_reddit_id"]
    )])
data class RedditPost(
    @PrimaryKey(autoGenerate = true) var redditId: Long? = null,
    val id: String,

    val name: String,

    val title: String,

    val score: Int,

    val author: String,

    val subreddit: String,

    val num_comments: Int,

    val created: Long,
    val thumbnail: String?,
    val url: String?,
    @ColumnInfo(name = "search_reddit_id", index = true) var searchRedditId: Long? =null) {
    var indexInResponse: Int = -1

}

data class SearchReddit(
    @Embedded val search: Search,
    @Relation(
        entity = RedditPost::class,
        parentColumn = "searchId",
        entityColumn = "search_reddit_id"

    )
    val post: List<RedditPost>
)

data class ListingResponse(val data: ListingData)

data class ListingData(
    val children: List<RedditChildrenResponse>,
    val after: String?,
    val before: String?
)

data class RedditChildrenResponse(val data: RedditPost)

package com.luis.pagingstore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.luis.pagingstore.vo.RedditPost
import com.luis.pagingstore.vo.Search

@Database(
    entities = [RedditPost::class, Search::class],
    version = 1,
    exportSchema = false
)
abstract class RedditDb: RoomDatabase() {
    abstract fun redditDao(): RedditDao
}
package com.luis.pagingstore.data.local

import android.util.Log
import androidx.paging.DataSource
import androidx.room.*
import com.luis.pagingstore.vo.RedditPost
import com.luis.pagingstore.vo.Search
import com.luis.pagingstore.vo.SearchReddit
import kotlinx.coroutines.flow.Flow

@Dao
interface RedditDao {

    @Query("SELECT * FROM search WHERE search.name = :name")
    fun readRedditFlow(name: String): Flow<SearchReddit>

    @Query("SELECT * FROM search WHERE search.name = :name")
    fun readReddit(name: String): SearchReddit

    @Query("SELECT searchId FROM search WHERE name = :name")
    fun readRedditId(name: String): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @Transaction
    suspend fun insert(list: List<RedditPost>)

    @Transaction
    suspend fun insert(name: String, redditPost: List<RedditPost>){
        var id = readId(name)
        if (id <= 0) id = insert(Search( name= name))
        Log.d("Check Id: ", id.toString() + " - $name")
        redditPost.map {
            it.searchRedditId = id
            insert(it)
            //Log.d("Id inserción :::", insert(it).toString())
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(it: RedditPost): Long

    @Query("SELECT searchId FROM search WHERE name = :name")
    fun readId(name: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reddit: Search): Long
}
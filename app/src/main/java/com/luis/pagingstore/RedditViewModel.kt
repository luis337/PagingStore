package com.luis.pagingstore

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.map
import androidx.paging.*
import androidx.room.Room
import com.dropbox.android.external.store4.*
import com.dropbox.mobile.store.Load
import com.dropbox.mobile.store.Param
import com.dropbox.mobile.store.streamPaging
import com.luis.pagingstore.data.RedditApi
import com.luis.pagingstore.data.local.RedditDb
import com.luis.pagingstore.vo.ListingData
import com.luis.pagingstore.vo.ListingResponse
import com.luis.pagingstore.vo.RedditChildrenResponse
import com.luis.pagingstore.vo.RedditPost
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
@FlowPreview
class RedditViewModel(application: Application) : AndroidViewModel(application) {

    private val pageMutable = MediatorLiveData<PagedList<RedditPost>>()
    private val responseMutable = MediatorLiveData<StoreResponse<Any>>()

    val pageList = map(pageMutable){it}!!
    val response = map(responseMutable){it}!!
    private var retry: (() -> Unit)? = null


    @ExperimentalCoroutinesApi
    @FlowPreview
    fun execute(key: String) {

        viewModelScope.launch {
            getStoreRepo(this).streamPaging(
                type = RedditPost::class.java,

                request =  { param ->
                    StoreRequest.fresh(key to param)
                },

                result = { load ->
                    when (load) {
                        is Load.LoadInitial -> load.data.data.also { redditApi ->
                            val items = redditApi.children.map { it.data }
                            load.callback.onResult(items, redditApi.before, redditApi.after)
                        }
                        is Load.LoadAfter-> load.data.data.also { redditApi ->
                            val items = redditApi.children.map { it.data }
                            load.callback.onResult(items, redditApi.after)
                        }
                    }
                }
            ).also { listing ->
                pageMutable.addSource(
                    listing.pagedList.toLiveData(Config(pageSize = 30,prefetchDistance = 5))
                ) {
                        value -> pageMutable.value = value
                }

                retry = {
                    listing.retry.invoke()}


                listing.response.collect {
                    responseMutable.value = it
                }
            }
        }

    }

    fun retry () {
        retry?.invoke()
    }


    private fun provideRoom(context: Context): RedditDb {
        return Room.inMemoryDatabaseBuilder(context, RedditDb::class.java)
            .build()
    }
    val db = provideRoom(context = this.getApplication())
    fun getStoreRepo(scope: CoroutineScope): Store<Pair<String, Param>, ListingResponse> {

        return StoreBuilder.fromNonFlow<Pair<String, Param>, ListingResponse>{
                (string, params) ->
            when (params) {
                is Param.Initial -> RedditApi.create().getTop(
                    subreddit = string,  limit = params.param.requestedLoadSize).execute().body()!!
                is Param.After -> RedditApi.create().getTopAfter(
                    subreddit = string, after = params.param.key, limit = params.param.requestedLoadSize).execute().body()!!
            }
        }.persister(
            reader = {(value, param) ->
                flow{
                    db.redditDao().readReddit(value).let { reddit ->
                        val list = reddit.post.map {
                            RedditChildrenResponse(it)
                        }
                        emit(
                            ListingResponse(
                                ListingData(
                                    list,
                                    runCatching { list[list.size -1].data.name }.getOrNull(),
                                    null
                                )
                            )
                        )

                    }


                }
            },
            writer = { (value, param), data ->
                val list = data.data.children.map { it.data }
                db.redditDao().insert(value, list)
            }
        )
        .build()
    }
}
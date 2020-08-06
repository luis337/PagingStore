package com.dropbox.mobile.store

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.dropbox.android.external.store4.StoreResponse
import kotlinx.coroutines.flow.Flow


data class Listing<P, T>(
    val pagedList: DataSource.Factory<String, P>,
    val response: Flow<StoreResponse<T>>,
    var retry: () -> Unit?
)

sealed class Load <P, D>{
    data class LoadInitial<P, D>(
        val param: PageKeyedDataSource.LoadInitialParams<String>,
        val callback: PageKeyedDataSource.LoadInitialCallback<String, P>,
        val data: D
    ): Load<P, D>()
    data class LoadAfter<P, D>(
        val param: PageKeyedDataSource.LoadParams<String>,
        val callback: PageKeyedDataSource.LoadCallback<String, P>,
        val data: D
    ): Load<P, D>()
}

sealed class Param {
    data class Initial(val param: PageKeyedDataSource.LoadInitialParams<String>): Param()
    data class After(val param: PageKeyedDataSource.LoadParams<String>): Param()
}
package com.dropbox.mobile.store

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource

class PaginatedRepository<Paging: Any>(
    private val f: Pair<((params: PageKeyedDataSource.LoadInitialParams<String>, callback: PageKeyedDataSource.LoadInitialCallback<String, Paging>
    ) -> Unit),(params: PageKeyedDataSource.LoadParams<String>, callback: PageKeyedDataSource.LoadCallback<String, Paging>) -> Unit >
): DataSource.Factory<String, Paging>() {
    override fun create(): DataSource<String, Paging> = PageKeyed(f)
}

class PageKeyed<Paging: Any>(
    private val f: Pair<((params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Paging>
    ) -> Unit),(params: LoadParams<String>, callback: LoadCallback<String, Paging>) -> Unit >
): PageKeyedDataSource<String, Paging>() {
    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Paging>) {
        Log.d("From Repository", "-------------Initial invocation--------------------------------------")
        f.first.invoke( params, callback)
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Paging>) {
        Log.d("From Repository", "-------------After invocation--------------------------------------")
        f.second.invoke( params, callback )
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Paging>) {
        Log.d("From Repository", "-------------before invocation--------------------------------------")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

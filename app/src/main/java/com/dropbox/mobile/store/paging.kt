package com.dropbox.mobile.store

import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.dropbox.android.external.store4.ResponseOrigin
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

typealias newTry = () -> Unit
@FlowPreview
suspend fun <Paging: Any, Key: Any, Output: Any> Store<Key, Output>.streamPaging(
    type: Class<Paging>,
    scope: CoroutineScope = GlobalScope,
    request: ((Param) -> StoreRequest<Key>),
    result: ((Load<Paging, Output>) -> Unit)): Listing<Paging, Output> {

    val response = Channel<StoreResponse<Output>>(capacity = Channel.UNLIMITED)
    var retry: newTry? = null
    val retryAllFails = {
        retry?.invoke()
    }

    suspend fun getStream(request: StoreRequest<Key>, data : (Output) -> Unit) = withContext(Dispatchers.IO)  {
        try {
            stream(request).onEach {  respons ->
                response.send(respons)
            }.filterNot { it is StoreResponse.Loading }.first().requireData().let {
                data(it)
            }
        } catch (e: Exception) {
            response.send(StoreResponse.Error(e, ResponseOrigin.Fetcher))
        }
    }


    fun execute (resquestt: StoreRequest<Key>, data : (Output) -> Unit) {
        scope.launch  {
            getStream(resquestt, data)
        }
        retry = {
            execute(resquestt, data)
        }
    }

    val a = Pair<(
        ( params: PageKeyedDataSource.LoadInitialParams<String>,
        callback: PageKeyedDataSource.LoadInitialCallback<String, Paging>
    ) -> Unit), (
        params: PageKeyedDataSource.LoadParams<String>,
        callback: PageKeyedDataSource.LoadCallback<String, Paging>
    ) -> Unit> (
        { p, c ->
            execute(request.invoke(Param.Initial(p))) {
                    result(Load.LoadInitial(p, c, it))
            }
        }, { p, c ->
            execute(request.invoke(Param.After(p))) {
                    result(Load.LoadAfter(p, c, it))
            }
        }
    )

    return Listing(
        pagedList = PaginatedRepository(a),
        response = response.consumeAsFlow(),
        retry = retryAllFails
    )
}
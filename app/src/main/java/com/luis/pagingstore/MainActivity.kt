package com.luis.pagingstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dropbox.android.external.fs3.FileSystemPersister
import com.dropbox.android.external.fs3.PathResolver
import com.dropbox.android.external.fs3.filesystem.FileSystemFactory
import com.dropbox.android.external.store4.ExperimentalStoreApi
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import com.dropbox.store.rx2.observe
import com.luis.pagingstore.resposotory.PostAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

class MainActivity : AppCompatActivity() {

    val modelView by viewModels<RedditViewModel>()

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val glide = GlideApp.with(this)
        val adaptera = PostAdapter(glide) {
            modelView.retry()
        }
        val aaa = LinearLayoutManager(this)
        recyclerview.apply {
            setHasFixedSize(true)
            adapter = adaptera
            layoutManager = aaa
        }
        modelView.pageList.observe(this, Observer {
            adaptera.submitList(it)
        })

        modelView.response.observe(this, Observer {
            adaptera.setStoreResponse(it)
        })

        modelView.execute(editText.text.toString())


        button.setOnClickListener { modelView.execute(editText.text.toString()) }
    }


}

package com.lgtm.i_am_home.data.source.local

import com.lgtm.i_am_home.data.source.FooDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class FooLocalDataSource(
    private val fooDao: FooDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FooDataSource {
}
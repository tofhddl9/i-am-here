package com.lgtm.i_am_home.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lgtm.i_am_home.data.FooData

@Database(entities = [FooData::class], version = 1)
abstract class FooDatabase : RoomDatabase() {

    abstract fun fooDao(): FooDao

}
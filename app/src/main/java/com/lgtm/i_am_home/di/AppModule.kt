package com.lgtm.i_am_home.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.room.Room
import com.lgtm.i_am_home.BluetoothRepository
import com.lgtm.i_am_home.data.source.FooDataSource
import com.lgtm.i_am_home.data.source.FooRepository
import com.lgtm.i_am_home.data.source.FooRepositoryImpl
import com.lgtm.i_am_home.data.source.local.FooDatabase
import com.lgtm.i_am_home.data.source.local.FooLocalDataSource
import com.lgtm.i_am_home.usecase.ScanDeviceUsecase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideFooLocalDataSource(
        database: FooDatabase,
        ioDispatcher: CoroutineDispatcher
    ): FooDataSource {
        return FooLocalDataSource(database.fooDao(), ioDispatcher)
    }

    @Singleton
    @Provides
    fun provideFooRepository(
        localTasksDataSource: FooDataSource,
        ioDispatcher: CoroutineDispatcher
    ): FooRepository {
        return FooRepositoryImpl(localTasksDataSource, ioDispatcher)
    }

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): FooDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FooDatabase::class.java,
            "Foo.db"
        ).build()
    }

    @Singleton
    @Provides
    fun provideBluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter {
        return (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    @Singleton
    @Provides
    fun provideBluetoothRepository(bluetoothAdapter: BluetoothAdapter): BluetoothRepository {
        return BluetoothRepository(bluetoothAdapter)
    }

    @Singleton
    @Provides
    fun provideScanDeviceUseCase(bluetoothRepository: BluetoothRepository): ScanDeviceUsecase {
        return ScanDeviceUsecase(bluetoothRepository)
    }

}
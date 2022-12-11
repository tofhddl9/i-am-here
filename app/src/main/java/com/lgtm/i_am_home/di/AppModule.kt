package com.lgtm.i_am_home.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.lgtm.i_am_home.data.BluetoothRepository
import com.lgtm.i_am_home.data.DeviceDataSource
import com.lgtm.i_am_home.data.DeviceLocalDataSource
import com.lgtm.i_am_home.domain.usecase.ForgetDeviceUsecase
import com.lgtm.i_am_home.domain.usecase.ScanDeviceUsecase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// TODO : adjust module lifecycle
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideBluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter {
        return (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    @Singleton
    @Provides
    fun provideDeviceDataSource(@ApplicationContext context: Context): DeviceDataSource {
        return DeviceLocalDataSource(context.getSharedPreferences("RememberedDevice", MODE_PRIVATE))
    }

    @Singleton
    @Provides
    fun provideBluetoothRepository(
        @ApplicationContext context: Context,
        deviceDataSource: DeviceDataSource,
        bluetoothAdapter: BluetoothAdapter
    ): BluetoothRepository {
        return BluetoothRepository(context, deviceDataSource, bluetoothAdapter)
    }

    @Singleton
    @Provides
    fun provideScanDeviceUseCase(bluetoothRepository: BluetoothRepository): ScanDeviceUsecase {
        return ScanDeviceUsecase(bluetoothRepository)
    }

    @Singleton
    @Provides
    fun provideForgetDeviceUseCase(bluetoothRepository: BluetoothRepository): ForgetDeviceUsecase {
        return ForgetDeviceUsecase(bluetoothRepository)
    }

}
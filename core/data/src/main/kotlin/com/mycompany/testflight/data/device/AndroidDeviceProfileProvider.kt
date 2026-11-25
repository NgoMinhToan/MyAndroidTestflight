package com.mycompany.testflight.data.device

import android.os.Build
import com.mycompany.testflight.domain.device.DeviceProfile
import com.mycompany.testflight.domain.device.DeviceProfileProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDeviceProfileProvider @Inject constructor() : DeviceProfileProvider {
    override fun current(): DeviceProfile = DeviceProfile(
        supportedAbis = Build.SUPPORTED_ABIS.toList(),
        sdkInt = Build.VERSION.SDK_INT,
    )
}

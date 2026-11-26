package dev.minhtoan.mytestflight.data.device

import android.os.Build
import dev.minhtoan.mytestflight.domain.device.DeviceProfile
import dev.minhtoan.mytestflight.domain.device.DeviceProfileProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDeviceProfileProvider @Inject constructor() : DeviceProfileProvider {
    override fun current(): DeviceProfile = DeviceProfile(
        supportedAbis = Build.SUPPORTED_ABIS.toList(),
        sdkInt = Build.VERSION.SDK_INT,
    )
}

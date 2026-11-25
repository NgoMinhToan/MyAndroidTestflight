package com.mycompany.testflight.domain.device

/**
 * The parts of the device that decide which APK artifact is usable. Kept as plain data so the
 * selection logic stays in the JVM-only domain module and can be unit tested.
 */
data class DeviceProfile(
    /** [android.os.Build.SUPPORTED_ABIS], most preferred first. */
    val supportedAbis: List<String>,
    val sdkInt: Int,
)

interface DeviceProfileProvider {
    fun current(): DeviceProfile
}

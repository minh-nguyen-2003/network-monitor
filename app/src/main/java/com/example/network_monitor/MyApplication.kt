package com.example.network_monitor

import android.app.Activity
import android.app.Application
import android.widget.Toast
import dagger.hilt.android.HiltAndroidApp
import vn.minh_nguyen.vkey.network_monitor.NetworkMonitor

@HiltAndroidApp
class MyApplication : Application() {
    private lateinit var networkMonitor: NetworkMonitor

    override fun onCreate() {
        super.onCreate()

        networkMonitor = NetworkMonitor(
            app = this,
            dialogProvider = object : NetworkMonitor.DialogProvider {
                override fun show(activity: Activity) {
                    // TODO: Ở đây show dialog mất mạng tùy chỉnh, có thể call data khi có mạng lại ở đây
                    Toast.makeText(
                        activity,
                        "Mất kết nối mạng!, Hiện dialog mấy kết nối mạng ở đây",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun dismiss(activity: Activity) {
                    // TODO: Ẩn dialog
                    Toast.makeText(
                        activity,
                        "Đã kết nối lại!, ẩn dialog mất kết nối mạng",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        networkMonitor.start()

    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor.stop()
    }

    fun addNetworkListener(listener: NetworkMonitor.Listener) {
        networkMonitor.addListener(listener)
    }

    fun removeNetworkListener(listener: NetworkMonitor.Listener) {
        networkMonitor.removeListener(listener)
    }
}
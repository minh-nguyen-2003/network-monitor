package vn.minh_nguyen.vkey.network_monitor

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission

/**
 * Theo dõi trạng thái mạng toàn app và hiển thị dialog khi mất mạng.
 *
 * Hỗ trợ minSdk 21+.
 */
class NetworkMonitor(
    private val app: Application,
    private val dialogProvider: DialogProvider
) : Application.ActivityLifecycleCallbacks {

    /** Cách hiển thị và ẩn dialog */
    interface DialogProvider {
        fun show(activity: Activity)
        fun dismiss(activity: Activity)
    }

    /** Listener để các Activity/Fragment nhận sự kiện */
    interface Listener {
        fun onLost()
        fun onRestored()
    }

    private var currentActivity: Activity? = null
    private val listeners = mutableSetOf<Listener>()
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    /** Đăng ký lắng nghe */
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    /** Bỏ lắng nghe */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    /** Bắt đầu theo dõi mạng */
    fun start() {
        app.registerActivityLifecycleCallbacks(this)
        registerNetworkCallback()

        if (!isNetworkAvailable()) {
            currentActivity?.let { safeShow(it) }
        }
    }

    /** Ngừng theo dõi mạng */
    fun stop() {
        app.unregisterActivityLifecycleCallbacks(this)
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            networkCallback?.let { cm.unregisterNetworkCallback(it) }
        } catch (_: Exception) {
            // ignore nếu đã unregister
        }
    }

    /**
     * Kiểm tra có internet hay không
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isNetworkAvailable(): Boolean {
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(n) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val info: NetworkInfo? = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            info?.isConnected == true
        }
    }

    /** Đăng ký callback theo dõi mạng */
    private fun registerNetworkCallback() {
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder().build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                Handler(Looper.getMainLooper()).post {
                    currentActivity?.let { safeShow(it) }
                    listeners.forEach { it.onLost() }
                }
            }

            override fun onAvailable(network: Network) {
                Handler(Looper.getMainLooper()).post {
                    currentActivity?.let { safeDismiss(it) }
                    listeners.forEach { it.onRestored() }
                }
            }
        }
        cm.registerNetworkCallback(request, networkCallback!!)
    }

    private fun safeShow(activity: Activity) {
        if (!activity.isFinishing && !activity.isDestroyed) {
            dialogProvider.show(activity)
        }
    }

    private fun safeDismiss(activity: Activity) {
        if (!activity.isFinishing && !activity.isDestroyed) {
            dialogProvider.dismiss(activity)
        }
    }

    // ----------------- Application.ActivityLifecycleCallbacks -----------------
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
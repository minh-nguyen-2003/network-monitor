package com.example.network_monitor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import vn.minh_nguyen.vkey.network_monitor.NetworkMonitor

class MainActivity : AppCompatActivity() {
    private val networkListener: NetworkMonitor.Listener = object : NetworkMonitor.Listener{
        override fun onLost() {
            Toast.makeText(this@MainActivity, "MainActivity bắt tắt mạng", Toast.LENGTH_SHORT).show()
        }

        override fun onRestored() {
            Toast.makeText(this@MainActivity, "MainActivity bắt kết nối lại mạng", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStart() {
        super.onStart()
        (application as MyApplication).addNetworkListener(networkListener)
    }

    override fun onPause() {
        (application as MyApplication).removeNetworkListener(networkListener)
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
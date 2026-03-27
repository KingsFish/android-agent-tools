package com.androidagent.mcp

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidagent.mcp.service.McpService

/**
 * Main activity for MCP Server app.
 * Provides UI to start/stop the server and displays connection information.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var addressText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var instructionText: TextView

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        addressText = findViewById(R.id.addressText)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        instructionText = findViewById(R.id.instructionText)

        updateUI()

        startButton.setOnClickListener {
            requestNotificationPermissionAndStart()
        }

        stopButton.setOnClickListener {
            McpService.stop(this)
            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun requestNotificationPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST
                )
            } else {
                startServer()
            }
        } else {
            startServer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startServer()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                // Still start the server, just without notifications
                startServer()
            }
        }
    }

    private fun startServer() {
        McpService.start(this)
        Toast.makeText(this, "MCP Server starting...", Toast.LENGTH_SHORT).show()
        // Wait a moment for service to start
        statusText.postDelayed({ updateUI() }, 500)
    }

    private fun updateUI() {
        if (McpService.isRunning) {
            statusText.text = getString(R.string.status_running)
            statusText.setTextColor(ContextCompat.getColor(this, R.color.status_running))
            startButton.isEnabled = false
            stopButton.isEnabled = true

            val ipAddress = getWifiIpAddress()
            if (ipAddress.isNotEmpty()) {
                addressText.text = getString(R.string.address_format, ipAddress, McpService.DEFAULT_PORT)
            } else {
                addressText.text = getString(R.string.address_format_no_wifi, McpService.DEFAULT_PORT)
            }

            instructionText.text = getString(R.string.instructions_running)
        } else {
            statusText.text = getString(R.string.status_stopped)
            statusText.setTextColor(ContextCompat.getColor(this, R.color.status_stopped))
            startButton.isEnabled = true
            stopButton.isEnabled = false
            addressText.text = ""
            instructionText.text = getString(R.string.instructions_stopped)
        }
    }

    private fun getWifiIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress

        if (ipAddress == 0) return ""

        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    }
}
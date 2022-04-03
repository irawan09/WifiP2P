package irawan.electroshock.wifip2p

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import irawan.electroshock.wifip2p.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    val TAG = "STATUS"


    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        binding.onOff.setOnClickListener {
            if (wifiManager.isWifiEnabled) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(Intent(Settings.Panel.ACTION_WIFI))
                    binding.onOff.text = getString(R.string.off)
                } else {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    wifiManager.setWifiEnabled(true)
                    binding.onOff.text = getString(R.string.off)
                }
            } else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(Intent(Settings.Panel.ACTION_WIFI))
                    binding.onOff.text = getString(R.string.on)
                } else {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    wifiManager.setWifiEnabled(true)
                    binding.onOff.text = getString(R.string.on)
                }
            }
        }

        binding.discover.setOnClickListener {

        }

        binding.sendButton.setOnClickListener {

        }
    }
}
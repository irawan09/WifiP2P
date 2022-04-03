package irawan.electroshock.wifip2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import irawan.electroshock.wifip2p.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var mManager: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var mReceiver: BroadcastReceiver
    private lateinit var mIntentFilter: IntentFilter
    val TAG = "STATUS"


    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager.initialize(this, Looper.getMainLooper(), null)

        mReceiver = WifiDirectBroadcastReceiver(mManager, mChannel, this)
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)


        binding.onOff.setOnClickListener {
            if (wifiManager.isWifiEnabled) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(Intent(Settings.Panel.ACTION_WIFI))
                    binding.onOff.text = getString(R.string.off)
                } else {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    binding.onOff.text = getString(R.string.off)
                }
            } else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(Intent(Settings.Panel.ACTION_WIFI))
                    binding.onOff.text = getString(R.string.on)
                } else {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    binding.onOff.text = getString(R.string.on)
                }
            }
        }

        binding.discover.setOnClickListener {

        }

        binding.sendButton.setOnClickListener {

        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, mIntentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mReceiver)
    }
}
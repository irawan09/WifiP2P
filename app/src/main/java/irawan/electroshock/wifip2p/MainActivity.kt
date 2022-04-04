package irawan.electroshock.wifip2p

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import irawan.electroshock.wifip2p.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private val mManager: WifiP2pManager by lazy (LazyThreadSafetyMode.NONE){
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var mReceiver: BroadcastReceiver
    private lateinit var mIntentFilter: IntentFilter
    var peers : MutableList<WifiP2pDevice> = mutableListOf<WifiP2pDevice>()
    lateinit var deviceNameArray : Array<String?>
    lateinit var deviceArray : Array<WifiP2pDevice?>
    val TAG = "STATUS"


    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mChannel = mManager.initialize(this, mainLooper, null)!!
        mChannel.also { channel ->
            mReceiver = WifiDirectBroadcastReceiver(mManager, channel, this)
        }
        mIntentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

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
            val hasWritePermission:Int = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)){
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1)
                } else{
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1)
                }

            }
            mManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener{
                override fun onSuccess() {
                    binding.connectionStatus.text = getString(R.string.discovery)
                }

                override fun onFailure(p0: Int) {
                    binding.connectionStatus.text = getString(R.string.discovery_failed)
                }

            })
        }

        binding.sendButton.setOnClickListener {

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(
                            this,
                            "Permission Granted",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Permission Denied",
                        Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    val peerListListener : WifiP2pManager.PeerListListener = object : WifiP2pManager.PeerListListener {
        override fun onPeersAvailable(peerList: WifiP2pDeviceList?) {
            if(!peerList?.deviceList?.equals(peers)!!){
                peers.clear()
                peers.addAll(peerList.deviceList)

                deviceNameArray = arrayOfNulls<String>(peerList.deviceList.size)
                deviceArray = arrayOfNulls<WifiP2pDevice>(peerList.deviceList.size)
                var index:Int = 0
                peerList.deviceList.forEach { device ->
                    deviceNameArray[index] = device.deviceName
                    deviceArray[index] = device
                    index++
                }

                val deviceNameAdapter: ArrayAdapter<String> = ArrayAdapter(
                    applicationContext,
                android.R.layout.simple_list_item_1,
                deviceNameArray)
                binding.peerListView.adapter = deviceNameAdapter
            }

            if (peers.size == 0){
                Toast.makeText(applicationContext,"No Device Found", Toast.LENGTH_LONG).show()
            }
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
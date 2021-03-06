package irawan.electroshock.wifip2p

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import irawan.electroshock.wifip2p.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var mManager: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var mReceiver: BroadcastReceiver
    private lateinit var mIntentFilter: IntentFilter
    private var peers : MutableList<WifiP2pDevice> = mutableListOf()
    private lateinit var deviceNameArray : Array<String?>
    private lateinit var deviceArray : Array<WifiP2pDevice?>
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager.initialize(this, mainLooper, null)!!
        mReceiver = WifiDirectBroadcastReceiver(mManager, mChannel, this)
        mIntentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        binding.onOff.setOnClickListener {
            wifiButtonStateChecker()
            if (wifiManager.isWifiEnabled) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(Intent(Settings.Panel.ACTION_WIFI))
                } else {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
            } else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(Intent(Settings.Panel.ACTION_WIFI))
                } else {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
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
            binding.peerListView.setOnItemClickListener{ parent, view, position, id ->
                val device:WifiP2pDevice? = deviceArray[position]
                val config = WifiP2pConfig()
                config.deviceAddress = device?.deviceAddress

                mManager.connect(mChannel, config, object : WifiP2pManager.ActionListener{
                    override fun onSuccess() {
                        makeText(
                            applicationContext,
                            "Connected to"+device?.deviceName,
                            Toast.LENGTH_LONG).show()
                    }

                    override fun onFailure(p0: Int) {
                        makeText(
                            applicationContext,
                            "Not Connected",
                            Toast.LENGTH_LONG).show()
                    }

                })
            }
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
                        makeText(
                            this,
                            "Permission Granted",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    makeText(
                        this,
                        "Permission Denied",
                        Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    var peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList?.deviceList
        if (!refreshedPeers?.equals(peers)!!) {
            peers.clear()
            peers.addAll(peerList.deviceList)

            deviceNameArray = arrayOfNulls(peerList.deviceList.size)
            deviceArray = arrayOfNulls(peerList.deviceList.size)
            var index = 0
            peerList.deviceList.forEach { device ->
                deviceNameArray[index] = device.deviceName
                deviceArray[index] = device
                index++
            }

            val deviceNameAdapter: ArrayAdapter<String> = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_list_item_1,
                deviceNameArray
            )
            binding.peerListView.adapter = deviceNameAdapter
        }

        if (peers.isEmpty()) {
            makeText(applicationContext, "No Device Found", Toast.LENGTH_LONG).show()
            return@PeerListListener
        }
    }

    var connectionInfoListener = WifiP2pManager.ConnectionInfoListener{ info ->
        val ownerGroupAddress = info.groupOwnerAddress
        if (info.groupFormed && info.isGroupOwner){
            binding.connectionStatus.text = getString(R.string.host)
        } else if(info.groupFormed){
            binding.connectionStatus.text = getString(R.string.client)
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

    private fun wifiButtonStateChecker(){
        if (wifiManager.wifiState == 0 || wifiManager.wifiState == 1){
//            binding.onOff.text = getString(R.string.off)
        Log.i("WiFi State","${wifiManager.wifiState}")
        } else {
//            binding.onOff.text = getString(R.string.on)
            Log.i("WiFi State","${wifiManager.wifiState}")
            /*
            It will return a number from 0-4 which indicates if it is on or off.
            0 = WiFi is being disabled
            1 = WiFi Disabled
            2 = WiFi is being enabled
            3 = WiFi is Enabled
            4 = Error
            */
        }
    }
}
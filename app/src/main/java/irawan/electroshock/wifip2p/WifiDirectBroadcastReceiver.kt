package irawan.electroshock.wifip2p

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

class WifiDirectBroadcastReceiver(
    private val mManager: WifiP2pManager,
    private val mChannel: WifiP2pManager.Channel,
    private val mActivity: MainActivity
): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action : String? = intent?.action
        Log.i("Action : ", "$action")
        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state:Int = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                Log.i("WiFi STATUS : ", state.toString())
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                    Log.i("WiFi STATUS : ", state.toString())
                    Toast.makeText(context, "WiFi is ON", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "WiFi is OFF", Toast.LENGTH_LONG).show()
                    Log.i("WiFi STATUS : ", state.toString())
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                val hasWritePermission:Int = ActivityCompat.checkSelfPermission(
                mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        mActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)){
                    ActivityCompat.requestPermissions(
                        mActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1)
                } else{
                    ActivityCompat.requestPermissions(
                        mActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1)
                }

            }
                Log.i("Peer Listener", "${mActivity.peerListListener}")
                mManager.requestPeers(mChannel, mActivity.peerListListener)
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(
                    WifiP2pManager.EXTRA_NETWORK_INFO
                )
                if (networkInfo?.isConnected == true) {
                    mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener)
                } else {
                    mActivity.binding.connectionStatus.text = mActivity.getString(R.string.device_disconnected)
                }
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
            }
        }
    }

}
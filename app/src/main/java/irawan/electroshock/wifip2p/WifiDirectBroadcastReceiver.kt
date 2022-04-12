package irawan.electroshock.wifip2p

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class WifiDirectBroadcastReceiver(
    private val mManager: WifiP2pManager,
    private val mChannel: WifiP2pManager.Channel,
    private val mActivity: MainActivity
): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action : String? = intent?.action

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            val state:Int = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
//                Toast.makeText(context, "WiFi is ON", Toast.LENGTH_LONG).show()
            } else {
//                Toast.makeText(context, "WiFi is OFF", Toast.LENGTH_LONG).show()
            }
        } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
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
            mManager.requestPeers(mChannel, mActivity.peerListListener)
        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //do something
        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            // do something
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(mActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(
                            mActivity,
                            "Permission Granted",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        mActivity,
                        "Permission Denied",
                        Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

}
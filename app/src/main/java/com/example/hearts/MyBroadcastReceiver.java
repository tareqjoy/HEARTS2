package com.example.hearts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Network mActivity;
    private boolean IsPeersAvailable = false;
    private boolean IsConnectionAvailable = false;


    private WifiPeersInAdhoc wifipeersinadhoc = new WifiPeersInAdhoc();


    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            mActivity.peers.clear();
            mActivity.peers.addAll(peerList.getDeviceList());

            IsPeersAvailable = true;

            for(int i=0;i<mActivity.peers.size();i++) {
                wifipeersinadhoc.addWifiP2pDevice(mActivity.peers.get(i));
                if(mActivity.peers.get(i).isGroupOwner()) {
                    wifipeersinadhoc.setGroupOwnerIndex(i);
                }
            }



            mActivity.availListAdapter.clear();
            for (WifiP2pDevice device : peerList.getDeviceList()) {
                mActivity.availListAdapter.add(device.deviceName);
            }
            mActivity.availListAdapter.notifyDataSetChanged();

            // If an AdapterView is backed by this data, notify it
            // of the change. For instance, if you have a ListView of
            // available peers, trigger an update.
            //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.





        }
    };

    public WifiPeersInAdhoc getWifiPeersInAdhoc() {

        return wifipeersinadhoc;
    }


    private WifiP2pManager.ConnectionInfoListener myConnectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {

            //Set IsConnectionAvailable
          //  IsConnectionAvailable = true;
            IsConnectionAvailable = true;
            // InetAddress from WifiP2pInfo struct.
            String  groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

            // After the group negotiation, we can determine the group owner.
            if (info.groupFormed && info.isGroupOwner) {
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a server thread and accepting
                // incoming connections.
                //Start the data receiving in background thread
                wifipeersinadhoc.setIsServer(true);
                mActivity.ServerThreadStart();
            } else if (info.groupFormed) {
                // The other device acts as the client. In this case,
                // you'll want to create a client thread that connects to the group
                // owner.
                wifipeersinadhoc.setIsServer(false);
                mActivity.ClientThreadStart(groupOwnerAddress);
            }
        }
    };



    public MyBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                               Network activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();


        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // mActivity.setIsWifiP2pEnabled(true);
            } else {
                //    mActivity.setIsWifiP2pEnabled(false);
            }
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }
            //   Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {


            // Respond to new connection or disconnections
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // We are connected with the other device, request connection
                // info to find group owner IP
                Toast.makeText(mActivity.getApplicationContext(), "Connect succeed.",
                        Toast.LENGTH_SHORT).show();

                mManager.requestConnectionInfo(mChannel, myConnectionListener);
            }




/*
            if (mManager == null)
                return;

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
           //     WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            //    String thisDeviceName = device.deviceName;

                mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);*/

            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            // mActivity.myDeviceName = device.deviceName;
            mActivity.setMyDeviceName(device.deviceName + " " + device.deviceAddress);
            // Respond to this device's wifi state changing
        }
    }


}

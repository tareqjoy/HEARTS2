package com.example.hearts;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class Network extends Activity implements WifiP2pManager.ConnectionInfoListener {
    private final IntentFilter mIntentFilter = new IntentFilter();
    private String mode;
    public String myDeviceName;
    private TextView info, hostId;
    private Button startGameButton;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    MyBroadcastReceiver mReceiver;
    private int ClientSum;
    private int ClientNum;

    private String MsgReceived;
    private int MsgSource;


    private PipedOutputStream pout_transmit_client = null;
    private PipedInputStream pin_transmit_client = null;
    private PipedOutputStream pout_rcv_client = null;
    private PipedInputStream pin_rcv_client = null;
    private ClientThread clientthread;
    //Thread and pipes for the server's data exchange
    private PipedOutputStream pout_rcv_server = null;
    private PipedInputStream pin_rcv_server = null;
    private PipedOutputStream pout_transmit_server = null;
    private PipedInputStream pin_transmit_server = null;
    private ServerThread serverthread;


    private ListView pairedDevices, availDevices;
    private TextView log;
    public ArrayAdapter<String> availListAdapter;
    private ArrayAdapter<String> pairedListAdapter;
    //private List<WifiP2pDevice> scanList;
    public List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private boolean scanning = false;

    private Timer timer = new Timer("Timer_Display", true);
    // WifiPeersInAdhoc wifiPeersInAdhoc;
    private final int REQ_BLUETOOTH = 1, REQ_BLUETOOTH_ADMIN = 2, REQ_LOCATION = 3;


    private AdapterView.OnItemClickListener onScanListItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            final WifiP2pDevice device = peers.get(i);



            int index;
            //No group has been formed yet, get a peer and connect
            if(mReceiver.getWifiPeersInAdhoc().getGroupOwnerIndex()==-2) {
                index = 0;
            }
            //A group exists, find the group owner and connect
            else {
                index = mReceiver.getWifiPeersInAdhoc().getGroupOwnerIndex();
            }

            if (!mReceiver.getWifiPeersInAdhoc().getIsConnected()){
                final WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = mReceiver.getWifiPeersInAdhoc().getWifiP2pDevice(index).deviceAddress;
                config.groupOwnerIntent = 0;
                config.wps.setup = WpsInfo.PBC;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        mReceiver.getWifiPeersInAdhoc().setIsConnected(true);
                        Toast.makeText(Network.this, "Connected to " + config.deviceAddress, Toast.LENGTH_SHORT).show();
                        // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(Network.this, "Connect failed. Retry.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }



        }
    };


    private Handler UIupdate = new Handler() {
        public void handleMessage(Message msg) {
            //Update the UI
            if (msg.what == 1) {        //Show the message on UI
                //display(MsgReceived);
                log.append("Received: " + MsgReceived+ "\n");
            } else if (msg.what == 2) {//Update check box availability on UI
                //Update check box availability
                if (!mReceiver.getWifiPeersInAdhoc().getIsServer()) {

                    //Display the assigned client node ID
                    log.append("I am client with number " + ClientNum + "\n");
                    // ((TextView) findViewById(R.id.head)).setText("Message History--CT" + Integer.toString(ClientNum));
                } else {
                    //Display GO for group owner
                    log.append("I am server\n");

                }
                log.append("Number of clients: " + ClientSum + "\n");
                //Toast.makeText(Network.this,"Number of clients: "+ClientSum,Toast.LENGTH_SHORT).show();
                ;

/*
                switch (ClientNum) {
                    case 1:
                        ((CheckBox) findViewById(R.id.checkbox_ct1)).setEnabled(false);
                        break;
                    case 2:
                        ((CheckBox) findViewById(R.id.checkbox_ct2)).setEnabled(false);
                        break;
                    case 3:
                        ((CheckBox) findViewById(R.id.checkbox_ct3)).setEnabled(false);
                        break;
                    case 4:
                        ((CheckBox) findViewById(R.id.checkbox_ct4)).setEnabled(false);
                        break;
                    case 5:
                        ((CheckBox) findViewById(R.id.checkbox_ct5)).setEnabled(false);
                        break;
                    default:
                        ;
                }*/
            } else if (msg.what == 3) {
                //Try to connect to Internet

            } else {
                //Send the obtained Internet message to the requesting node

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();


        //Close the opened threads
        if (clientthread != null) {
            clientthread.interrupt();
            clientthread = null;
        }
        if (serverthread != null) {
            serverthread.interrupt();
            serverthread = null;
        }

        //Close the opened streams
        if (pout_transmit_client != null) {
            try {
                pout_transmit_client.close();
            } catch (IOException e) {
                //Catch Logic
            }
        }
        if (pin_transmit_client != null) {
            try {
                pin_transmit_client.close();
            } catch (IOException e) {
                //Catch Logic
            }
        }
        if (pout_rcv_client != null) {
            try {
                pout_rcv_client.close();
            } catch (IOException e) {
                //Catch Logic
            }
        }
        if (pin_rcv_client != null) {
            try {
                pin_rcv_client.close();
            } catch (IOException e) {
                //Catch Logic
            }
        }
        if (pout_transmit_server != null) {
            try {
                pout_transmit_server.close();
            } catch (IOException e) {
                //Catch Logic
            }
        }
        if (pin_transmit_server != null) {
            try {
                pin_transmit_server.close();
            } catch (IOException e) {
                //Catch Logic
            }
        }
        if (pout_rcv_server != null) {
            try {
                pout_rcv_server.close();
            } catch (IOException e) {
                //Catch Logic
            }
        }
        if (pin_rcv_server != null) {
            try {
                pin_rcv_server.close();
            } catch (IOException e) {
                //Catch Logic
            }
        }

        //Cancel all on-going P2P connections
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Connection cancelled!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(getApplicationContext(), "Connection Cancellation Failed! Reason Code:" + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });

        unregisterReceiver(mReceiver);

        timer.cancel();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network);
        // Log.d("somee","Invoked");
        mode = getIntent().getStringExtra("mode");
        pairedDevices = findViewById(R.id.pairedDevices);
        availDevices = findViewById(R.id.availDevices);
        info = findViewById(R.id.info);
        hostId = findViewById(R.id.hostId);
        log = findViewById(R.id.log);
        startGameButton = findViewById(R.id.startGameButton);
        pairedListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        pairedDevices.setAdapter(pairedListAdapter);

        availListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        availDevices.setAdapter(availListAdapter);
        availDevices.setOnItemClickListener(onScanListItemClick);


        if (mode.equals("host")) {
            availDevices.setVisibility(View.GONE);
            info.setText("Hosting, Name:");
            hostId.setVisibility(View.VISIBLE);
        }

        if (isPermissionGranted(Manifest.permission.ACCESS_WIFI_STATE, REQ_BLUETOOTH)) {
            //already granted, do stuff
        }
        if (isPermissionGranted(Manifest.permission.CHANGE_WIFI_STATE, REQ_BLUETOOTH_ADMIN)) {
            //already granted, do stuff
        }
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, REQ_LOCATION)) {
            //already granted, do stuff
        }
        if (isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION, REQ_LOCATION)) {
            //already granted, do stuff
        }
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, REQ_LOCATION)) {
            //already granted, do stuff
        }

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new MyBroadcastReceiver(mManager, mChannel, this);


        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reasonCode) {

            }
        });

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("Msg from server, how are you?");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case REQ_BLUETOOTH:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //yay! granted

                } else {
                    //     not granted, oops
                }
                break;
            case REQ_BLUETOOTH_ADMIN:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //yay! granted

                } else {
                    //     not granted, oops
                }
                break;
            case REQ_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //yay! granted

                } else {
                    //     not granted, oops
                }
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        Toast.makeText(this, info.toString(), Toast.LENGTH_SHORT).show();
    }


    public void setMyDeviceName(String name) {
        myDeviceName = name;
        hostId.setText(name);
    }


    public void ClientThreadStart(String host) {

        try {
            pout_transmit_client = new PipedOutputStream();
            pin_transmit_client = new PipedInputStream(pout_transmit_client);

            pout_rcv_client = new PipedOutputStream();
            pin_rcv_client = new PipedInputStream(pout_rcv_client);

            clientthread = new ClientThread(host, pout_rcv_client, pin_transmit_client);
            clientthread.setDaemon(true);
            clientthread.start();
        } catch (IOException e) {
            //Catch Logic
        }

        //Schedule the timer
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int tmp = MsgRcv();
                //If a new message received, notify the handler
                Message m = new Message();
                m.what = tmp;
                UIupdate.sendMessage(m);
            }
        }, 1000, 500);
    }


    //Receive a new message(the returned value 0 for failure, 1 for data message, 2 for protocol message and 3 for Internet message)
    public int MsgRcv() {

        //Get the received message
        byte buf[] = new byte[1024];
        String message = null;
        int msg_len;
        int msg_type;
        int msg_src;
        int msg_dst;
        int len;
        PipedInputStream pin_rcv = null;

        try {
            if (mReceiver.getWifiPeersInAdhoc().getIsServer()) {
                pin_rcv = pin_rcv_server;
            } else {
                pin_rcv = pin_rcv_client;
            }

            if (pin_rcv.available() <= 0) {
                return 0;
            }
            msg_len = pin_rcv.read();    //Get the Message Head (message length,head and type not included)
            msg_type = pin_rcv.read();    //Get the Message Type (0 for data,1 for protocol)
            msg_src = pin_rcv.read();    //Get the Message Source (Binary,a "1" in the ith(0~7) bit stands for the ith client, 0x01 for server)
            msg_dst = pin_rcv.read();    //Get the Message Destination (Binary,a "1" in the ith(0~7) bit stands for the ith client, 0x01 for server)
            len = 0;
            while (len < msg_len) {
                len += pin_rcv.read(buf, len, msg_len - len);    //Get the Message Body (message content)
            }
            if (msg_type == 0) {            //Judge the message type; store it into message if it is a data frame
                message = new String(buf, 0, msg_len, "UTF-8");
            } else if (msg_type == 1) {    //Set the availability of check boxes
                ClientSum = buf[0];
                ClientNum = buf[1];
                return 2;
            } else {    //Internet Connection Request
                //Obtain the url
                message = new String(buf, 0, msg_len, "UTF-8");
                MsgReceived = message;

                //Remember who is requesting Internet access
                MsgSource = (int) java.lang.Math.round((java.lang.Math.log(msg_src) / java.lang.Math.log(2)));

                return 3;
            }
        } catch (IOException e) {
            //Catch logic
            return 0;
        }

        //Update the received message and source node number
        MsgReceived = message;
        MsgSource = (int) java.lang.Math.round((java.lang.Math.log(msg_src) / java.lang.Math.log(2)));

        //Accumulate the count of received messages
        mReceiver.getWifiPeersInAdhoc().setRcvMsgCnt(mReceiver.getWifiPeersInAdhoc().getRcvMsgCnt() + 1);

        return 1;
    }


    public boolean isPermissionGranted(String permission, final int REQ_CODE) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(permission)
                    == PackageManager.PERMISSION_GRANTED) {
                // Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                //  Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{permission}, REQ_CODE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            // Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    public void ServerThreadStart() {

        try {
            pout_rcv_server = new PipedOutputStream();
            pin_rcv_server = new PipedInputStream(pout_rcv_server);

            pout_transmit_server = new PipedOutputStream();
            pin_transmit_server = new PipedInputStream(pout_transmit_server);

            serverthread = new ServerThread(pout_rcv_server, pin_transmit_server);
            serverthread.setDaemon(true);
            serverthread.start();
        } catch (IOException e) {
            //Catch Logic
        }

        //Schedule the timer
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int tmp = MsgRcv();
                //If a new message received, notify the handler
                Message m = new Message();
                m.what = tmp;
                UIupdate.sendMessage(m);
            }
        }, 1000, 500);
    }


    //Called when the user clicks the Send button
    public void sendMessage(String message) {

        //Peers not available, please wait!
    	/*if(!mReceiver.getIsPeersAvailable()){
    		Toast.makeText(getApplicationContext(), "Peers not available, please wait!",
                    Toast.LENGTH_SHORT).show();
    		return;
    	}

    	//Connection not available, please wait!
    	if(!mReceiver.getIsConnectionAvailable()){
    		Toast.makeText(getApplicationContext(), "Connection not available, please wait!",
    				Toast.LENGTH_SHORT).show();
    		return;
    	}*/

        //Get the content in the text box
        int dst_addr = 0;
        byte buf[] = new byte[1024];


        int len = message.length();
        if (len <= 0) {
            Toast.makeText(getApplicationContext(), "Empty message!",
                    Toast.LENGTH_SHORT).show();
            return;
        }




        //Get the destination address from the check boxes
        dst_addr = 0;

        dst_addr += 1;

        if(ClientSum==1)
            dst_addr += 2;
        else if(ClientSum==2)
          dst_addr += 4;
        else
            dst_addr += 8;


        if (dst_addr <= 0) {
            Toast.makeText(getApplicationContext(), "No Receiver Selected!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //Send the message
        try {
            buf = message.getBytes("UTF-8");
            if (mReceiver.getWifiPeersInAdhoc().getIsServer()) {
                pout_transmit_server.write(len);        //Message Head (message length,head and type not included)
                pout_transmit_server.write(0);            //Message Type (0 for data,1 for protocol)
                pout_transmit_server.write(0x01);        //Message Source (Binary,a "1" in the ith(0~7) bit stands for the ith client, 0x01 for server)
                pout_transmit_server.write(dst_addr);    //Message Destination (Binary,a "1" in the ith(0~7) bit stands for the ith client, 0x01 for server)
                pout_transmit_server.write(buf, 0, len);    //Message Body (message content)
            } else {
                pout_transmit_client.write(len);        //Message Head (message length,head and type not included)
                pout_transmit_client.write(0);            //Message Type (0 for data,1 for protocol)
                pout_transmit_client.write((int) (java.lang.Math.pow(2, ClientNum)));    //Message Source (Binary,a "1" in the ith(0~7) bit stands for the ith client, 0x01 for server)
                pout_transmit_client.write(dst_addr);    //Message Destination (Binary,a "1" in the ith(0~7) bit stands for the ith client, 0x01 for server)
                pout_transmit_client.write(buf, 0, len);    //Message Body (message content)
            }
        } catch (IOException e) {
            //Catch logic
        }


        //Accumulate the count of transmitted messages
        mReceiver.getWifiPeersInAdhoc().setTransmitMsgCnt(mReceiver.getWifiPeersInAdhoc().getTransmitMsgCnt() + 1);

        //Get the handle of the list view


        //Get the current time
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        //Fill the data into the Hash map
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (mReceiver.getWifiPeersInAdhoc().getIsServer()) {
            map.put("ItemNumber", "GO " + dateFormat.format(cal.getTime()));
        } else {
            map.put("ItemNumber", "CT" + Integer.toString(ClientNum) + " " + dateFormat.format(cal.getTime()));
        }
        map.put("ItemMessage", message);


        //Display the items


        //Scroll to the bottom

    }



}

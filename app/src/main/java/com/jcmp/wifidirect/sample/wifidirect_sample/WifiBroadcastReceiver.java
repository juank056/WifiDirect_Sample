package com.jcmp.wifidirect.sample.wifidirect_sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by JuanCamilo on 06/10/2015.
 */
public class WifiBroadcastReceiver extends BroadcastReceiver {

    /**
     * Instancia del singleton
     */
    private static WifiBroadcastReceiver instance;

    /**
     * Manager de Wifi P2P
     */
    private WifiP2pManager mManager;

    /**
     * Canal
     */
    private WifiP2pManager.Channel mChannel;

    /**
     * Intent filter
     */
    private IntentFilter mIntentFilter;

    /**
     * Actividad Wifi (Definida por programador)
     */
    private WifiDirectActivityInterface mActivity;

    /**
     * Peer listener
     */
    private WifiP2pManager.PeerListListener myPeerListListener;

    /**
     * Constructor del que recibe los broadcast
     */
    public WifiBroadcastReceiver() {
        super();
        //Inicia myPeerListener
        myPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                //Los peers están disponibles (Se debe actualizar la lista de peers en pantalla)
                mActivity.setNewDevices(peers.getDeviceList());
            }
        };
    }

    public static WifiBroadcastReceiver getInstance() {
        if (instance == null)
            instance = new WifiBroadcastReceiver();
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {/*Cambia el estado*/
            //Obtiene datos extra para saber si esta activo el wifi
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {/*Wifi Activo*/
                // Wifi P2P is enabled
                mActivity.showMessageOnScreen("Wifi activo!");
            } else { /*Wifi Desactivado*/
                // Wi-Fi P2P is not enabled
                mActivity.showMessageOnScreen("Wifi NO activo!");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, myPeerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //Obtiene informacion de Red
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            //Revisa si esta conectado
            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP

                mManager.requestConnectionInfo(mChannel, mActivity);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }


    /********************
     * GETTERS Y SETTERS
     *******************/

    public WifiP2pManager getmManager() {
        return mManager;
    }

    public void setmManager(WifiP2pManager mManager) {
        this.mManager = mManager;
    }

    public WifiP2pManager.Channel getmChannel() {
        return mChannel;
    }

    public void setmChannel(WifiP2pManager.Channel mChannel) {
        this.mChannel = mChannel;
    }

    public WifiDirectActivityInterface getmActivity() {
        return mActivity;
    }

    public void setmActivity(WifiDirectActivityInterface mActivity) {
        this.mActivity = mActivity;
    }

    public IntentFilter getmIntentFilter() {
        return mIntentFilter;
    }

    public void setmIntentFilter(IntentFilter mIntentFilter) {
        this.mIntentFilter = mIntentFilter;
    }
}

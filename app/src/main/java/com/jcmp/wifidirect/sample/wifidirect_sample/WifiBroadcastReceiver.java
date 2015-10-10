package com.jcmp.wifidirect.sample.wifidirect_sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by JuanCamilo on 06/10/2015.
 */
public class WifiBroadcastReceiver extends BroadcastReceiver {

    /**
     * Manager de Wifi P2P
     */
    private WifiP2pManager mManager;

    /**
     * Canal
     */
    private WifiP2pManager.Channel mChannel;

    /**
     * Actividad Wifi (Definida por programador)
     */
    private MyWiFiActivity mActivity;

    /**
     * Peer listener
     */
    private WifiP2pManager.PeerListListener myPeerListListener;
    ;

    /**
     * Constructor del que recibe los broadcast
     *
     * @param manager  manager wifi
     * @param channel  Canal
     * @param activity La actividad que lo invoca
     */
    public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                 MyWiFiActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
        //Inicia myPeerListener
        myPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                //Los peers están disponibles (Se debe actualizar la lista de peers en pantalla)
            }
        };
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {/*Cambia el estado*/
            //Obtiene datos extra para saber si esta activo el wifi
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {/*Wifi Activo*/
                // Wifi P2P is enabled
            } else { /*Wifi Desactivado*/
                // Wi-Fi P2P is not enabled
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, myPeerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}

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
     * Actividad Wifi
     */
    private WifiDirectActivityInterface mActivity;

    /**
     * Peer listener
     */
    private WifiP2pManager.PeerListListener myPeerListListener;

    /**
     * Constructor del que recibe los broadcast
     */
    private WifiBroadcastReceiver() {
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
        //Obtiene la acción del intent
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {/*Cambio de estado*/
            //Obtiene datos extra para saber si esta activo el wifi
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {/*Wifi Activo*/
                // Wifi P2P activado
                mActivity.showMessageOnScreen("Wifi activo!");
            } else { /*Wifi Desactivado*/
                // Wi-Fi P2P no activado (Podría activarse)
                mActivity.showMessageOnScreen("Wifi NO activo!");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) { /*Cambio de Peers*/
            // Hubo un cambio con los peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, myPeerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) { /*Conectado o no*/
            //Obtiene informacion de Red
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            //Revisa si esta conectado
            if (networkInfo.isConnected()) {/*Conectado a otro dispositivo*/
                //Obtiene información de la conexión
                mManager.requestConnectionInfo(mChannel, mActivity);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) { /*Cambio detalle Movil*/
            /*Hacer algo si corresponde*/
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

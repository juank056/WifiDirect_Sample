package com.jcmp.wifidirect.sample.wifidirect_sample;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Collection;

/**
 * Created by JuanCamilo on 11/10/2015.
 */
public interface WifiDirectActivityInterface extends WifiP2pManager.ConnectionInfoListener {

    /**
     * Incluir nuevos dispositivos en la lista
     *
     * @param deviceList lista de dispositivos
     */
    void setNewDevices(Collection<WifiP2pDevice> deviceList);

    /**
     * Mostrar mensaje en pantalla
     *
     * @param message Mensaje a mostrar
     */
    void showMessageOnScreen(String message);

    /**
     * Ingresa nuevo mensaje en el panel de mensajes
     *
     * @param message Mensaje a mostrar
     */
    void setNewMessageOnPanel(String message);


}

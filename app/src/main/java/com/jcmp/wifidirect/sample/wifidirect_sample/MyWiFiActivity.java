package com.jcmp.wifidirect.sample.wifidirect_sample;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MyWiFiActivity extends AppCompatActivity implements WifiDirectActivityInterface,
        View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * Lista de peers
     */
    ListView peers;

    /**
     * Lista de devices
     */
    private List<WifiP2pDevice> devices;


    /**
     * @param savedInstanceState Instancia que llega
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wi_fi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Obtiene boton de descubrimiento
        Button b = (Button) findViewById(R.id.b1);
        //Registra boton para listener
        b.setOnClickListener(this);
        //Inicia parte de Wifi direct
        WifiP2pManager mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel mChannel = mManager.initialize(this, getMainLooper(), null);
        //Inicia Broadcast receiver
        WifiBroadcastReceiver.getInstance();
        //Le asigna los parametros requeridos
        //Manager
        WifiBroadcastReceiver.getInstance().setmManager(mManager);
        //Canal
        WifiBroadcastReceiver.getInstance().setmChannel(mChannel);
        //Actividad
        WifiBroadcastReceiver.getInstance().setmActivity(this);
        //Crea intent filter
        IntentFilter mIntentFilter = new IntentFilter();
        //Adiciona las mismas acciones que tendra el broadcast receiver
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        //Asocia intent filter al broadcast receiver
        WifiBroadcastReceiver.getInstance().setmIntentFilter(mIntentFilter);
        //Obtiene lista de Peers
        peers = (ListView) findViewById(R.id.list_peers);
        peers.setOnItemClickListener(this);
        //Inicia lista de devices
        devices = new ArrayList<WifiP2pDevice>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Registra el broadcast receiver con el intent filter
        registerReceiver(WifiBroadcastReceiver.getInstance(),
                WifiBroadcastReceiver.getInstance().getmIntentFilter());
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        //Desregistra el broadcast receiver
        unregisterReceiver(WifiBroadcastReceiver.getInstance());
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.b1:/*Descubir peers*/
                Toast.makeText(getApplicationContext(), R.string.init_peers_discovery, Toast
                        .LENGTH_SHORT).show();
                WifiBroadcastReceiver.getInstance().getmManager().
                        discoverPeers(WifiBroadcastReceiver.getInstance().getmChannel(),
                                new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(getApplicationContext(), R.string.peers_discovered, Toast
                                                .LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(int reasonCode) {
                                        Toast.makeText(getApplicationContext(), R.string.peers_discovered_failed, Toast
                                                .LENGTH_SHORT).show();
                                    }
                                });
                break;
        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Obtiene posicion del device
        WifiP2pDevice device = devices.get(position);
        if (device != null) {/*Device encontrado*/
            //Crea intent para ir al chat
            Intent intent = new Intent(MyWiFiActivity.this, ChatActivity.class);
            //Adiciona device en los datos
            intent.putExtra(Constants.DEVICE_NAME, device.deviceName);
            intent.putExtra(Constants.DEVICE_ADDR, device.deviceAddress);
            intent.putExtra(Constants.IS_SERVER, true);
            //Inicia la actividad
            startActivity(intent);
        } else {/*No existe el device*/
            Toast.makeText(getApplicationContext(), R.string.no_device, Toast
                    .LENGTH_SHORT).show();
        }
    }

    /**
     * Asigna devices a la actividad
     *
     * @param deviceList
     */
    public void setNewDevices(Collection<WifiP2pDevice> deviceList) {
        //Limpia lista de devices
        devices.clear();
        //Limpia lista de peers que se tenia enteriormente
        peers.clearChoices();
        //Lista de devices
        List<String> listDevices = new ArrayList<String>();
        //Recorre lista de devices
        Iterator<WifiP2pDevice> iterator = deviceList.iterator();
        while (iterator.hasNext()) {
            //Obtiene device
            WifiP2pDevice device = iterator.next();
            //Nombre a mostrar en la pantalla
            String toShow = getResources().getString(R.string.name) + device.deviceName + " " +
                    getResources().getString(R.string.address) + device.deviceAddress;
            listDevices.add(toShow);
            //Adiciona device a la lista
            devices.add(device);
        }
        //Para incluir elementos de la lista
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listDevices);
        //Incluye nuevos peers
        peers.setAdapter(adapter);
    }

    /**
     * Mostrar mensaje en pantalla
     *
     * @param message Mensaje a mostrar
     */
    @Override
    public void showMessageOnScreen(String message) {
        Toast.makeText(getApplicationContext(), message, Toast
                .LENGTH_SHORT).show();
    }

    /**
     * Ingresa nuevo mensaje en el panel de mensajes
     *
     * @param message Mensaje a mostrar
     */
    @Override
    public void setNewMessageOnPanel(String message) {
        Toast.makeText(getApplicationContext(), message, Toast
                .LENGTH_SHORT).show();
    }

    /**
     * The requested connection info is available
     *
     * @param info Wi-Fi p2p connection info
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        //Mensaje de conexion disponible
        this.showMessageOnScreen("Conectado con: " + info.groupOwnerAddress);
        //Crea intent para ir al chat
        Intent intent = new Intent(MyWiFiActivity.this, ChatActivity.class);
        //Adiciona device en los datos
        intent.putExtra(Constants.DEVICE_NAME, "Peer");
        intent.putExtra(Constants.DEVICE_ADDR, info.groupOwnerAddress.getHostAddress());
        intent.putExtra(Constants.IS_SERVER, false);
        //Inicia la actividad
        startActivity(intent);
    }
}

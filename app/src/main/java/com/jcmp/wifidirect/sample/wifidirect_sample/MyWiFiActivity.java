package com.jcmp.wifidirect.sample.wifidirect_sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MyWiFiActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * Manager wifi
     */
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    /**
     * Intent filter
     */
    IntentFilter mIntentFilter;

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
        //Inicia parte de Wifi direct
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, this);
        //Crea intent filter
        mIntentFilter = new IntentFilter();
        //Adiciona las mismas acciones que tendra el broadcast receiver
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        //Obtiene lista de Peers
        peers = (ListView) findViewById(R.id.list_peers);
        //Inicia lista de devices
        devices = new ArrayList<WifiP2pDevice>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Registra el broadcast receiver con el intent filter
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        //Desregistra el broadcast receiver
        unregisterReceiver(mReceiver);
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
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
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
        switch (view.getId()) {
            case R.id.list_peers:/*Se le hizo click a un*/
                //Obtiene posicion del device
                WifiP2pDevice device = devices.get(position);
                if (device != null) {/*Device encontrado*/

                } else {/*No existe el device*/
                    Toast.makeText(getApplicationContext(), R.string.no_device, Toast
                            .LENGTH_SHORT).show();
                }
                break;
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


}

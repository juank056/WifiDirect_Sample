package com.jcmp.wifidirect.sample.wifidirect_sample;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.Collection;

public class ChatActivity extends AppCompatActivity implements WifiDirectActivityInterface, View.OnClickListener {

    /**
     * Nombre del dispositivo
     */
    private String device_name;

    /**
     * Titulo chat
     */
    private TextView title;

    /**
     * Direccion del dispositivo
     */
    private String device_address;

    /**
     * Manager de Wifi P2P
     */
    private WifiP2pManager mManager;

    /**
     * Canal
     */
    private WifiP2pManager.Channel mChannel;

    /**
     * Indicador de cliente o servidor
     */
    private boolean isServer;

    /**
     * Communications manager
     */
    private CommunicationsManager communicator;

    /**
     * Panel de mensajes
     */
    private TextView messagePanel;

    /**
     * Mensaje a enviar
     */
    private EditText messageSend;

    /**
     * Creacion de la actividad
     *
     * @param savedInstanceState instancia guardada
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Obtiene parametros de la actividad llamante
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        //Revisa que vengan parametros
        if (extras != null) {
            //Nombre del dispositivo
            device_name = extras.getString(Constants.DEVICE_NAME);
            //Direccion del dispositivo
            device_address = extras.getString(Constants.DEVICE_ADDR);
            //Indicador de isServer
            isServer = extras.getBoolean(Constants.IS_SERVER);
        }
        //Obtiene boton de envio
        Button b = (Button) findViewById(R.id.send_button);
        //Registra boton para listener
        b.setOnClickListener(this);
        //Titulo del chat
        title = (TextView) findViewById(R.id.title);
        //Asigna titulo
        title.setText("Friend: " + device_name + " Address: " + device_address);
        //Obtiene texto de envio
        messageSend = (EditText) findViewById(R.id.send_text);
        //Panel de mensajes
        messagePanel = (TextView) findViewById(R.id.chat_text);
        //Asigna actividad al broadcast receiver
        WifiBroadcastReceiver.getInstance().setmActivity(this);
        //Obtiene manager y channel
        mManager = WifiBroadcastReceiver.getInstance().getmManager();
        mChannel = WifiBroadcastReceiver.getInstance().getmChannel();
        //Se inicia la parte de conexion solo si es server
        if (isServer) {
            //Se trata de conectar
            WifiP2pConfig config = new WifiP2pConfig();
            //Asigna la direccion para conectar
            config.deviceAddress = device_address;
            //Se trata de conectar a dispositivo
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), R.string.connected, Toast
                            .LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), R.string.connected_failed, Toast
                            .LENGTH_SHORT).show();
                }
            });
        } else {/*Es cliente*/
            //Inicia conexion como cliente
            //Crea el communications manager
            communicator = new CommunicationsManager(this, isServer, device_address);
            //Inicia la comunicacion entre ambos
            communicator.start();
        }
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
        //Cierra conexion en caso de estar establecida
        communicator.setIsShutdown(true);
    }

    /**
     * Metodo para asignar dispositivos a la actividad
     *
     * @param deviceList Lista de dispositivos a asignar
     */
    @Override
    public void setNewDevices(Collection<WifiP2pDevice> deviceList) {
        /*Aqui no interesa la lista de dispositivos*/
    }

    /**
     * The requested connection info is available
     *
     * @param info Wi-Fi p2p connection info
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo struct.
        InetAddress groupOwnerAddress = info.groupOwnerAddress;
        // Se determina si esta formado el grupo y el dueño
        if (info.groupFormed && info.isGroupOwner) {
            showMessageOnScreen("Inicia conexion como group owner!");
            //Crea el communications manager
            communicator = new CommunicationsManager(this, isServer, info.groupOwnerAddress.getHostAddress());
            //Inicia la comunicacion entre ambos
            communicator.start();
        } else if (info.groupFormed) { /*Cliente*/
            //No se aceptan comunicaciones como cliente en este punto
            showMessageOnScreen("No se aceptan conexiones como cliente aqui!");
        }
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
        //Pone mensaje en el panel de mensajes
        String panel = messagePanel.getText().toString();
        panel += getResources().getString(R.string.partner) + message + "\n";
        messagePanel.setText(panel);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_button:/*Enviar mensaje*/
                //No se aceptan comunicaciones como cliente en este punto
                showMessageOnScreen("Se va a enviar un mensaje!");
                //Mensaje a enviar
                String text = messageSend.getText().toString();
                //Trim al mensaje
                text = text.trim();
                //Lo envia si no es blancos
                if (!Constants.BLANKS.equals(text)) {
                    //Pone mensaje en la cola de envio
                    communicator.getOutputMessages().offer(text.getBytes());
                    //Pone mensaje en el panel de mensajes
                    String panel = messagePanel.getText().toString();
                    panel += getResources().getString(R.string.me) + text + "\n";
                    messagePanel.setText(panel);
                }

                break;
        }
    }
}

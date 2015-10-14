package com.jcmp.wifidirect.sample.wifidirect_sample;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by JuanCamilo on 11/10/2015.
 */
public class CommunicationsManager extends Thread {

    /**
     * Socket de TCP
     */
    private Socket socketTCP;


    /**
     * Servidor TCP
     */
    private ServerSocket serverTCP;

    /**
     * Actividad
     */
    private WifiDirectActivityInterface activity;

    /**
     * Direccion del servidor
     */
    private String serverAddress;

    /**
     * Indicador de servidor
     */
    private boolean isServer;

    /**
     * Indicador de shutdown
     */
    private boolean isShutdown;

    /**
     * Cola de mensajes de salida
     */
    private BlockingQueue<byte[]> outputMessages;

    /**
     * Enrutador de mensajes de salida
     */
    private OutputRouter outputRouter;

    /**
     * Indicador de iniciado
     */
    private boolean started;


    /**
     * Constructor del communications manager
     *
     * @param activity Actividad
     * @param isServer Indicador de si es un servidor
     */
    public CommunicationsManager(WifiDirectActivityInterface activity, boolean isServer, String serverAddress) {
        //Asigna parametros recibidos
        this.activity = activity;
        this.isServer = isServer;
        this.isShutdown = false;
        this.serverAddress = serverAddress;
        //Inicia cola de mensajes de salida
        outputMessages = new LinkedBlockingQueue<byte[]>();
        //Crea el output router
        outputRouter = new OutputRouter();
        //Started false
        this.started = false;
    }

    /**
     * Run del thread de comunicaciones
     */
    @Override
    public void run() {
        try {
            //Started true
            this.started = true;
            //Inicia el output router
            outputRouter.start();
            /*En este punto la conexion esta establecida*/
            //Coloca mensaje en pantalla
            //activity.showMessageOnScreen("LA CONEXIÓN HA SIDO INICIADA!");
            //Lee mensajes mientras no haya shutdown
            while (!isShutdown) {
                //Revisa conexion
                checkConnection();
                // Lee los bytes para obtener el tamano
                byte[] mLength = new byte[4];
                socketTCP.getInputStream().read(mLength);
                // Ahora obtiene el numero de bytes a leer
                int realTam = this.messageLength(mLength);
                // Creamos mensaje con lo realmente leido
                byte[] message = new byte[realTam];
                int readed = socketTCP.getInputStream().read(message);
                //Coloca mensaje si es mayor a cero
                if (readed > 0) {
                    //Crea string con el mensaje
                    String messageText = new String(message);
                    //Coloca mensaje en pantalla
                    activity.setNewMessageOnPanel(messageText);
                }
            }
            //Hubo shutdown
            //Cierra conexion
            if (isServer)
                serverTCP.close();
            //Cierra socket
            socketTCP.close();
        } catch (IOException e) {/*Ocurrio error*/
            e.printStackTrace();
        } finally {/*Finalmente cerrar conexion*/
            //Cierra  conexion
            closeConnection();
        }
    }

    /**
     * Revisa conexion
     *
     * @throws IOException Error en conexion
     */
    private void checkConnection() throws IOException {
        //Inicia conexion
        if (isServer) {
            if (serverTCP == null || serverTCP.isClosed()) {
                // Crea server socket
                serverTCP = new ServerSocket(Constants.CONNECTION_PORT);
                //Inicia accept (para una unica conexion)
                socketTCP = serverTCP.accept();
            }
        } else {/*Es cliente*/
            if (socketTCP == null || socketTCP.isClosed()) {
                //Inicia socket con el servidor
                socketTCP = new Socket(serverAddress, Constants.CONNECTION_PORT);
            }
        }
    }

    private void closeConnection() {
        try {
            //Cierra conexion
            if (socketTCP != null)
                socketTCP.close();
            //Intenta cerrar socket
            if (serverTCP != null) {
                serverTCP.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Retorna indicador de iniciado
     *
     * @return Indicador de thread iniciado
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Metodo para obtener la longitud de un mensaje. Se utiliza para saber
     * cuantos bytes se deben leer de la linea.
     *
     * @param lenbuf arreglo de bytes que indican la longitud total del mensaje.
     * @return La longitud del mensaje como numero entero
     */
    private int messageLength(byte[] lenbuf) {
        int size = 0;
        size = ((lenbuf[0] & 0xff) << 8) | (lenbuf[1] & 0xff)
                | (lenbuf[2] & 0xff) | (lenbuf[3] & 0xff);
        return size;
    }

    /**
     * Obtiene la longitud del mensaje en bytes
     *
     * @param data los datos del mensaje
     * @return longitud en bytes del mensaje
     */
    private byte[] messageLengthBytes(byte[] data) {
        byte[] buf = null;
        int l = data.length;
        buf = new byte[4];
        buf[0] = (byte) ((l & 0xff000000) >> 24);
        buf[1] = (byte) ((l & 0xff0000) >> 16);
        buf[2] = (byte) ((l & 0xff00) >> 8);
        buf[3] = (byte) (l & 0xff);
        return buf;
    }

    /**
     * Contatena dos arreglos de bytes
     *
     * @param A el primer arreglo
     * @param B el segundo arreglo
     * @return A+B en un solo arreglo
     */
    private byte[] concat(byte[] A, byte[] B) {
        byte[] C = new byte[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public void setIsShutdown(boolean isShutdown) {
        this.isShutdown = isShutdown;
    }

    /**
     * Obtiene cola de mensajes de salida
     *
     * @return Cola de mensajes de salida
     */
    public BlockingQueue<byte[]> getOutputMessages() {
        return outputMessages;
    }


    /**
     * Constructor de clase para el envio de los mensajes
     */
    private class OutputRouter extends Thread {

        /**
         * Constructor del output router
         */
        public OutputRouter() {
            super();
        }

        /**
         * Ejecuta tareas para el envio de los mensajes
         */

        public void run() {
            try {
                //Lee de la cola mientras no tenga shutdown
                while (!isShutdown) {
                    //Revisa conexion
                    checkConnection();
                    //Obtiene mensaje
                    byte[] message = outputMessages.take();
                    //Obtiene longitud del mensaje
                    byte[] length = messageLengthBytes(message);
                    //Crea mensaje final a enviar
                    byte[] messageToSend = concat(length, message);
                    //Envia mensaje por la linea
                    // Crea data output stream
                    DataOutputStream messageStream = new DataOutputStream(
                            socketTCP.getOutputStream());
                    // Envia mensaje
                    messageStream.write(messageToSend);
                    // Realiza flush para forzar que se vaya el mensaje completo
                    messageStream.flush();
                }
            } catch (Exception e) {/*Error en envio de mensaje*/
                e.printStackTrace();
            } finally {
                //Cierra conexion
                closeConnection();
            }
        }
    }


}
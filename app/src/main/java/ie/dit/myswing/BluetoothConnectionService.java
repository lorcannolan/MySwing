package ie.dit.myswing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "MySwing";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter bluetoothAdapter;
    Context context;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private BluetoothDevice device;
    private UUID deviceUUID;
    ProgressBar progressBar;

    public BluetoothConnectionService(Context context) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        start();
    }

    /*
        Thread behaves similarly to server-side client - Listens for incoming connections.
        Runs until connection is accepted or cancelled.
    */
    private class AcceptThread extends Thread {
        // local server socket
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // create listening server socket
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID);
                Log.d(TAG, "************\nAcceptThread: Setting up server using: " + MY_UUID);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            serverSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "*************\nrun: Accept thread running");

            BluetoothSocket socket = null;

            try {
                Log.d(TAG, "*************\nrun: Server socket start....");
                // Blocking call, waits until connection received or exception
                socket = serverSocket.accept();
                Log.d(TAG, "*************\nrun: Server socket accepted connection");
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if (socket != null) {
                connected(socket, device);
            }

            Log.d(TAG, "*************\nEND acceptThread");
        }

        public void cancel() {
            Log.d(TAG, "*************\nCancel: Cancelling acceptThread");
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                Log.d(TAG, "*************\nCancel: Closing acceptThread failed. " + e.getMessage());
            }
        }
    }

    /*
        Thread runs while attempting to make an outgoing connection.
        Connection can succeed or fail.
    */
    private class ConnectThread extends Thread {
        private  BluetoothSocket socket;

        public ConnectThread(BluetoothDevice bluetoothDevice, UUID uuid) {
            Log.d(TAG, "*************\nConnectThread: Started.");
            device = bluetoothDevice;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.d(TAG, "*************\nRun ConnectThread.");

            // Get a bluetoothSocket for a connection with the given BluetoothDevice
            try {
                Log.d(TAG, "*************\nConnectThread: Creating InsecureRfcommSocket using UUID: " + MY_UUID);
                tmp = device.createRfcommSocketToServiceRecord(deviceUUID);
            }
            catch (IOException e) {
                Log.d(TAG, "*************\nConnectThread: Could not create InsecureRfcommSocket. " + e.getMessage());
            }

            socket = tmp;

            // Cancel discovery after connection made
            bluetoothAdapter.cancelDiscovery();

            try {
                // Blocking call. Will only return on successful connection or exception
                socket.connect();
                Log.d(TAG, "*************\nConnectThread: connected.");
            }
            catch (IOException e) {
                // Close the socket
                try {
                    socket.close();
                    Log.d(TAG, "*************\nrun: Closed Socket.");
                }
                catch (IOException e1) {
                    Log.d(TAG, "*************\nConnectThread: Unable to close socket connection. " + e1.getMessage());
                }
                Log.d(TAG, "*************\nConnectThread: Could not connect to UUID: " + MY_UUID);
            }

            connected(socket, device);
        }

        public void cancel() {
            try {
                Log.d(TAG, "*************\nCancel: Closing Client Socket.");
                socket.close();
            }
            catch (IOException e) {
                Log.d(TAG, "*************\nCancel: close() of socket in ConnectThread failed. " + e.getMessage());
            }
        }
    }

    /*
        Start AcceptThread to begin a session in listening (server) mode.
        Initiates acceptThread object.
    */
    public synchronized void start() {
        Log.d(TAG, "start");

        if(connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    /*
        acceptThread starts and waits for connection.
        connectThread then starts and attempts to make a connection with other device's acceptThread.
    */
    public void startClient(BluetoothDevice bluetoothDevice, UUID uuid) {
        Log.d(TAG, "startClient: Started\n--------Progress Dialog Start--------");

        connectThread = new ConnectThread(bluetoothDevice, uuid);
        connectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream in;
        private final OutputStream out;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            Log.d(TAG, "ConnectedThread: Starting\n--------Progress Dialog Finish--------");

            socket = bluetoothSocket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            in = tmpIn;
            out = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[10240]; //buffer store for the stream
            int bytes; //bytes returned from read()

            // Always listen to inputStream
            while (true) {
                // read from input stream
                try {
                    bytes = in.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "***********\nInputStream: " + incomingMessage);
                }
                catch (IOException e) {
                    Log.d(TAG, "***********\nInputStream: Error reading inputStream. " + e.getMessage());
                    break;
                }
            }
        }

        public void cancel() {
            try {
                socket.close();
            }
            catch (IOException e) {}
        }
    }

    private void connected(BluetoothSocket bluetoothSocket, BluetoothDevice bluetoothDevice) {
        Log.d(TAG, "***********\nconnected: starting");

        // Start thread to manage connection and perform transmissions
        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }
}

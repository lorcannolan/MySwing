package ie.dit.myswing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "MySwing";

    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    Context context;

    private AcceptThread acceptThread;

    public BluetoothConnectionService(Context context) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
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
}

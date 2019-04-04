package ie.dit.myswing.play;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

import ie.dit.myswing.R;

public class PlayFragment extends Fragment {

    private Button playButton, discoverButton;
    private TextView bluetoothStatus;
    private ImageView bluetoothButton;

    BluetoothAdapter bluetoothAdapter;

    BluetoothConnectionService bluetoothConnectionService;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothDevice bluetoothDevice;

    private ArrayList<BluetoothDevice> allDevices = new ArrayList<>();
    private DeviceListAdapter deviceListAdapter;
    private ListView deviceListView;

    private static final String TAG = "Play";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // Create a BroadcastReceiver for ACTION_STATE_CHANGED
    private final BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        bluetoothButton.setImageResource(R.drawable.ic_bluetooth_off_120dp);
                        discoverButton.setVisibility(View.INVISIBLE);
                        allDevices.clear();
                        deviceListView.setAdapter(null);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        bluetoothButton.setImageResource(R.drawable.ic_bluetooth_on_120dp);
                        discoverButton.setVisibility(View.VISIBLE);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                allDevices.add(newDevice);

                Log.d(TAG, "***************\n" + newDevice.getName() + ": " + newDevice.getAddress());
                deviceListAdapter = new DeviceListAdapter(context, R.layout.device_list_row, allDevices);
                deviceListView.setAdapter(deviceListAdapter);
                deviceListView.setDivider(null);
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver broadcastReceiver3 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (newDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "***************\nBOND_BONDED" );
                    bluetoothStatus.setText("Connected to " + newDevice.getName());
                    playButton.setVisibility(View.VISIBLE);
                    bluetoothDevice = newDevice;

                    editor.putString("BT Device Address", bluetoothDevice.getAddress());
                    editor.apply();
                }
                else if (newDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "***************\nBOND_BONDING");
                    bluetoothStatus.setText("Connecting to " + newDevice.getName() + "...");
                }
                else if (newDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "***************\nBOND_NONE");
                }
            }
        }
    };

//    @Override
//    public void onDestroy() {
//        Log.d(TAG, "***************\nonDestroy: called");
////        super.onDestroy();
//        getActivity().unregisterReceiver(broadcastReceiver1);
//        getActivity().unregisterReceiver(broadcastReceiver2);
//        getActivity().unregisterReceiver(broadcastReceiver3);
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_play, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothStatus = (TextView) view.findViewById(R.id.bluetooth_status);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = prefs.edit();
        editor.putString("BT Device Address", "");
        editor.apply();
        Log.d(TAG, "***************\nBT Device Address: " + prefs.getString("BT Device Address", ""));

        deviceListView = (ListView) view.findViewById(R.id.device_list);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();

                Log.d(TAG, "***************\n" + allDevices.get(position).getName() + ": " + allDevices.get(position).getAddress() + " clicked!");

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    Log.d(TAG, "***************\nPairing with " + allDevices.get(position).getName());
                    allDevices.get(position).createBond();

                    bluetoothDevice = allDevices.get(position);
                    bluetoothConnectionService = new BluetoothConnectionService(getContext());
                }
            }
        });

        playButton = (Button) view.findViewById(R.id.button);
        playButton.setVisibility(View.VISIBLE);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectRoundTypeIntent = new Intent(getActivity(), SelectRoundType.class);
                startActivity(selectRoundTypeIntent);
            }
        });

        discoverButton = (Button) view.findViewById(R.id.discover_devices_button);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "***************\nLooking For Devices");
                allDevices.clear();
                deviceListView.setAdapter(null);

                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();

                    checkPermissions();

                    bluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    getActivity().registerReceiver(broadcastReceiver2, discoverDevicesIntent);
                }
                if (!bluetoothAdapter.isDiscovering()) {
                    checkPermissions();

                    bluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    getActivity().registerReceiver(broadcastReceiver2, discoverDevicesIntent);
                }
            }
        });

        bluetoothButton = (ImageView) view.findViewById(R.id.bluetooth_image);
        // Set Bluetooth image
        if (bluetoothAdapter.isEnabled()) {
            bluetoothButton.setImageResource(R.drawable.ic_bluetooth_on_120dp);
            discoverButton.setVisibility(View.VISIBLE);
        }
        else {
            bluetoothButton.setImageResource(R.drawable.ic_bluetooth_off_120dp);
            discoverButton.setVisibility(View.INVISIBLE);
        }
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter == null) {
                    Log.d(TAG, "***************\nDoes not have bluetooth capabilities");
                }
                if (!bluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "***************\nEnabling Bluetooth");
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);

                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    getActivity().registerReceiver(broadcastReceiver1, BTIntent);
                }
                if (bluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "***************\nDisabling Bluetooth");
                    bluetoothAdapter.disable();

                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    getActivity().registerReceiver(broadcastReceiver1, BTIntent);
                }
            }
        });

        // Broadcasts when bond state changes (i.e. pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getActivity().registerReceiver(broadcastReceiver3, filter);

        return view;
    }

    // Start connection. App will crash if pairing not complete first
    public void startConnection() {
        startBTConnection(bluetoothDevice, MY_UUID);
    }

    /*
        Start receiving data input from Arduino.
    */
    public void startBTConnection(BluetoothDevice bluetoothDevice1, UUID uuid) {
        Log.d(TAG, "***************\nstartBTConnection: Initializing RFCOM Bluetooth Connection.");

        bluetoothConnectionService.startClient(bluetoothDevice1, uuid);
    }

    public void checkPermissions() {
        /*
        Permission check is required for devices running API 23+.
        Must programmatically check for Bluetooth permissions
        */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = getActivity().checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += getActivity().checkSelfPermission("Manifest.permission.ACCESS_COURSE_LOCATION");
            if (permissionCheck != 0) {
                getActivity().requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }
    }
}

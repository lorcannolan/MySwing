package ie.dit.myswing;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class PlayFragment extends Fragment {

    private Button button;
    private ImageView bluetoothButton;
    BluetoothAdapter bluetoothAdapter;

    private static final String TAG = "Play";

    // Create a BroadcastReceiver for ACTION_FOUND
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
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        bluetoothButton.setImageResource(R.drawable.ic_bluetooth_on_120dp);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "***************\nonDestroy: called");
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_play, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectRoundTypeIntent = new Intent(getActivity(), SelectRoundType.class);
                startActivity(selectRoundTypeIntent);
            }
        });

        bluetoothButton = (ImageView) view.findViewById(R.id.bluetooth_image);
        // Set Bluetooth image
        if (bluetoothAdapter.isEnabled()) {
            bluetoothButton.setImageResource(R.drawable.ic_bluetooth_on_120dp);
        }
        else {
            bluetoothButton.setImageResource(R.drawable.ic_bluetooth_off_120dp);
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

        return view;
    }
}

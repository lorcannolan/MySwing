package ie.dit.myswing;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private ArrayList<BluetoothDevice> devices;
    private Context mContext;
    private int mResource;


    public DeviceListAdapter(Context context, int resource, ArrayList<BluetoothDevice> objects) {
        super(context, resource, objects);
        mContext = context;
        this.devices = objects;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        BluetoothDevice device = devices.get(position);

        if (device != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);
            TextView deviceAddress = (TextView) convertView.findViewById(R.id.device_address);

            if (deviceName != null) {
                deviceName.setText(device.getName());
            }
            if (deviceAddress != null) {
                deviceAddress.setText(device.getAddress());
            }
        }

        return convertView;
    }
}

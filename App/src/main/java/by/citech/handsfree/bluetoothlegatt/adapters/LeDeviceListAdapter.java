package by.citech.handsfree.bluetoothlegatt.adapters;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import by.citech.handsfree.R;
import by.citech.handsfree.param.Settings;

/**
 * Created by tretyak on 26.10.2017.
 */
// При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
// останавливаем сканирование и запускаем новое Activity
public class LeDeviceListAdapter extends BaseAdapter {

    private final static String TAG = "WSD_LeListAdapter";
    // View, которые будут содержаться в списке
    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<String> mLeRssi;
    private LayoutInflater mInflator;

    public LeDeviceListAdapter(LayoutInflater mInflator) {
        super();
        mLeDevices = new ArrayList<>();
        mLeRssi = new ArrayList<>();
        this.mInflator = mInflator;
    }

    static class ViewHolder {
        ImageView deviceIcon;
        ImageView deviceHeadSet;
        TextView  deviceName;
        TextView  deviceAddress;
//      TextView  bluetoothClass;
//      TextView  deviceRssi;
        ImageView checkIcon;
    }

    public void addDevice(BluetoothDevice device, int rssi) {
        if (!mLeDevices.contains(device)) {
            if (Settings.debug) Log.i(TAG, "addDevice device added: " + device.getAddress());
            mLeDevices.add(device);
            mLeRssi.add(String.valueOf(rssi));
            notifyDataSetChanged();
        }
    }

    public String getRssi(int position) {
        return mLeRssi.get(position);
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
        mLeRssi.clear();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LeDeviceListAdapter.ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.lineitem_device, null);
            viewHolder = new LeDeviceListAdapter.ViewHolder();
            viewHolder.deviceIcon = view.findViewById(R.id.icon);
            viewHolder.deviceIcon.setVisibility(View.VISIBLE);
            viewHolder.deviceHeadSet = view.findViewById(R.id.iconhead);
            viewHolder.deviceHeadSet.setVisibility(View.GONE);
            viewHolder.deviceName = view.findViewById(R.id.device_name);
            viewHolder.deviceAddress = view.findViewById(R.id.device_address);
//          viewHolder.bluetoothClass = view.findViewById(R.id.bluetooth_class);
//          viewHolder.deviceRssi = view.findViewById(R.id.device_rssi);
            viewHolder.checkIcon = view.findViewById(R.id.iconcheck);
            view.setTag(viewHolder);
        } else
            viewHolder = (LeDeviceListAdapter.ViewHolder) view.getTag();

        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();

        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName);
            if (deviceName.length() > 13)
                if (deviceName.substring(0,13).equals("CIT HandsFree")) {
                    viewHolder.deviceIcon.setVisibility(View.GONE);
                    viewHolder.deviceHeadSet.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.deviceIcon.setVisibility(View.VISIBLE);
                    viewHolder.deviceHeadSet.setVisibility(View.GONE);
                }
        } else {
            viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceIcon.setVisibility(View.VISIBLE);
            viewHolder.deviceHeadSet.setVisibility(View.GONE);
        }

        if (mLeRssi.get(i).equals("200")) {
            //if (Settings.debug) Log.i(TAG, "Set Icon to List");
            viewHolder.checkIcon.setVisibility(View.VISIBLE);
        }
        else
            viewHolder.checkIcon.setVisibility(View.GONE);

        viewHolder.deviceAddress.setText(device.getAddress());
//      viewHolder.bluetoothClass.setText("Device Class: " + device.getBluetoothClass().toString());
//      viewHolder.deviceRssi.setText("RSSI: " + mLeRssi.get(i) + " dbm");

        return view;
    }

}

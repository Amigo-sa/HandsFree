package by.citech.handsfree.bluetoothlegatt.ui;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import by.citech.handsfree.R;
import by.citech.handsfree.bluetoothlegatt.IBtList;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

/**
 * Created by tretyak on 26.10.2017.
 */
// При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
// останавливаем сканирование и запускаем новое Activity
public class LeDeviceListAdapter extends BaseAdapter implements IBtList {

    private final static String STAG = "WSD_LeListAdapter";
    private static int objCount;
    private final String TAG;

    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    // View, которые будут содержаться в списке
    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<Boolean> connecting;
    private ArrayList<Boolean> connected;
    private LayoutInflater mInflator;

    public LeDeviceListAdapter(LayoutInflater mInflator) {
        super();
        mLeDevices = new ArrayList<>();
        connecting = new ArrayList<>();
        connected = new ArrayList<>();
        this.mInflator = mInflator;
    }

    static class ViewHolder {
        ImageView deviceIcon;
        ImageView deviceHeadSet;
        TextView  deviceName;
        TextView  deviceAddress;
        ImageView checkIcon;
        ProgressBar progressBar;
    }

    public void addDevice(BluetoothDevice device, boolean connecting, boolean connected) {
        if (!mLeDevices.contains(device)) {
            Timber.i("addDevice device added: %s", device.getAddress());
            mLeDevices.add(device);
            this.connecting.add(connecting);
            this.connected.add(connected);
            notifyDataSetChanged();
        }
    }

    public void removeDevice(BluetoothDevice device) {
        if (mLeDevices.contains(device)) {
            int num = mLeDevices.indexOf(device);
            this.connecting.remove(num);
            this.connected.remove(num);
            mLeDevices.remove(num);
            notifyDataSetChanged();
        }
    }

    public BluetoothDevice getDevice(int position) {
        if (mLeDevices != null && !mLeDevices.isEmpty()) {
            if (position >= 0 && position <= mLeDevices.size())
                return mLeDevices.get(position);
        } else
            Timber.e("Index out of bounds list %s", position);
        return null;
    }

    public void clear() {
        mLeDevices.clear();
        connected.clear();
        connecting.clear();
        notifyDataSetChanged();
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
            viewHolder.checkIcon = view.findViewById(R.id.iconcheck);
            viewHolder.progressBar = view.findViewById(R.id.connecting);
            view.setTag(viewHolder);
        } else
            viewHolder = (LeDeviceListAdapter.ViewHolder) view.getTag();

        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        final String deviceAddr = device.getAddress();

        if (deviceAddr != null && deviceName != null) {
            viewHolder.deviceName.setText(deviceName);
            if (deviceAddr.substring(0, 8).equals(Settings.Bluetooth.deviceAddressPrefix)) {
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

        if (connecting.get(i).equals(true)) {
            //if (Settings.debug) Log.i(TAG, "Set Icon to List");
            viewHolder.progressBar.setVisibility(View.VISIBLE);
        } else
            viewHolder.progressBar.setVisibility(View.GONE);

        if (connected.get(i).equals(true)) {
            //if (Settings.debug) Log.i(TAG, "Set Icon to List");
            viewHolder.checkIcon.setVisibility(View.VISIBLE);
        } else
            viewHolder.checkIcon.setVisibility(View.GONE);

        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }

}

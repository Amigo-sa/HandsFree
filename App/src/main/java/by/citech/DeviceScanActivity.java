/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package by.citech;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import by.citech.bluetoothlegatt.Resource;
/**
 *  В отличие от классического Bluetooth, BLE призван обеспечить существенно меньшее
 *  энергопотребление. Это позволяет приложениям для Android общаться с BLE-устройствами,
 *  которые имеют низкие требования к питанию, таких как датчики, мониторы сердечного ритма,
 *  фитнес-устройства и так далее.
 *
 * Ключевые термины и понятия
 *
 * Generic Attribute Profile (GATT) – профиль GATT является общей спецификацией для отправки и
 * получения коротких фрагментов данных, известных как "атрибуты" через BLE-соединение. Все текущие
 * LE-профили приложений основаны на GATT. Создатели BLE определили множество профилей для
 * низкоэнергетических устройств. Профиль представляет собой определение того, как устройство
 * работает в конкретном приложении. Устройство может реализовывать более одного профиля.
 * Например, устройство может содержать профили пульсометра и датчика уровня заряда батареи.
 *
 * Attribute Protocol (ATT) – GATT строится на основе протокола атрибутов АТТ. АТТ оптимизирован
 * для работы на BLE-устройствах. Для этого он использует настолько мало байтов, насколько возможно.
 * Каждый атрибут идентифицируется уникальным универсальным идентификатором (UUID), который
 * представляет собой стандартизированный 128-битный строковый идентификатор используемый для
 * однозначной идентификации информации. Атрибуты переносятся с помощью АТТ в виде
 * характеристик и услуг(сервисов).
 *
 * Характеристика (Characteristic) – содержит одно значение, и от 0 до N дескрипторов, описывающих
 * свойства характеристики. Характеристика может рассматриваться как тип, аналог класса.
 *
 * Дескриптор (Descriptor) может содержать удобочитаемое описание, приемлемый диапазон значений или
 * единицу измерения, конкретные значения характеристики.
 * Дескриптор - это свойство для характеристики.
 *
 * Сервис (Service) – это набор характеристик. Например, вы можете иметь услугу под названием
 * "пульсометр", что включает в себя такую характеристику, как "Измерение пульса". Список
 * существующих на основе GATT профилей и услуг можно найти на bluetooth.org.
 *
 *
 * Роли и обязанности при взаимодействии Android с BLE-устройством
 *
 * Центральная/периферийная роль. Это относится к самому BLE-соединению. Центральное устройство(смартфон)
 * сканирует, ищет объявления(доступные данные), а устройства в периферийной роли( например
 * какой-нибудь датчик) создаёт объявления(список имеющихся сервисов и характеристик).
 *
 * GATT-сервер/GATT-клиент. Это определяет, каким образом два устройства общаются друг с другом,
 * когда они установили связь.
 *
 * Таким образом периферийное BLE - устройство имеет список того, что с ним делать. Периферийное
 * оно потому, что само ничего не сканирует, а только подтверждает запрос соединения. Обычно
 * периферийное устройство является сервером, поскольку предоставляет клиенту(смартфону) данные(сервисы и
 * характеристики).
 *
 * Например мы(при помощи смартфона) соединились с периферийным устройством(датчиком температуры)
 * и просматриваем его данные:
 * сервисы и характеристики(то, что мы можем получить от этого устройства), они будут иметь следующий вид:
 *
 * - Информация об устройстве(Сервис, имеет свой UUID)
 *      -- Получить информацию об устройстве (Характеристика, имеет свой UUID, свойство(дескриптор) - чтение)
 *      -- Получить информацию о производителе (Характеристика, имеет свой UUID, свойство(дескриптор) - чтение)
 *      -- Получить информацию о софте (Характеристика, имеет свой UUID, свойство(дескриптор) - чтение)
 *
 * - Информация о Температуре(Сервис, имеет свой UUID)
 *      -- Получить текущую температуру (Характеристика, имеет свой UUID, свойство - чтение, нотификация)
 *      -- Изменить характер данных  (Характеристика, имеет свой UUID, свойство - чтение, запись)
 *
 *
 * Все эти данные считываются центральным устройством(смартфоном) как клиентом. У характеристик есть
 * свойства(Дескрипторы): 1) чтение
 *                        2) запись
 *                        3) нотификация
 *                        4) индикация
 *
 * свойства - это то, что мы можем делать с характеристикой. Записывать данные, считывать данные, получать
 * данные как регулярное уведомление(нотификация и индикация, разница в том, что нотификация происходит
 * без подтверждения, а индикация с подтверждением от центрального устройства).
 *
 * Некоторые свойства необходимо запускають с сервера(центрального устройства). Это делается при
 * помощи изменения атрибутов характеристики.
 *
 * Характеристики обычно имеют следующие атрибуты:
 * 1) Значение Характеристики (конкретное значение данных)
 * 2) Декларация Характеристики (свойства, тип и положение характеристики)
 * 3) Client Characteristic Configuration (Конфигурация, которая позволяет центральному устройству
 * (Серверу GATT) настраивать характеристику для нотификации (асинхронное сообщение без подтверждения)
 * и индикации(асинхронное сообщение с подтверждением))
 * 4) Characteristic User Description (ASCII - строка с описанием характеристики)
 *
 * Обращение к аттрибутам характеристики происходит посредством дескрипторов.
 *
 * Последовательность работы 2х BLE устройств Центрального(клиента) - смартфона и
 * периферийного(сервера) - датчика:
 *
 * 1) Датчик включил видимость и всем себя обьявляет
 * 2) Смартфон включает сканирование и ищет все BLE устройства производя(запрос обнаружения)
 * 3) Датчик предоставляет свои характеристики
 * 4) Смартфон получает данные о имеющихся дескрипторах характеристики и устанавливает нужные свойства
 * 5) Далее смарфон работает по этим свойствам: запись, чтение данных от датчика, запускает нотификацию и/или индикацию
 * 6) Работа происходит до тех пор пока смартфон не отключит соединение или не оборвётся соединение.
 *
 * */

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
    // Коллекция для найденных устройств BLE
    private LeDeviceListAdapter mLeDeviceListAdapter;
    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    // Сборщик информации с различных потоков
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    // Создаём пользовательское меню с кнопкой включения/отключения сканирования
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    // включаем/выключаем сканирование в зависимости от того нажата клавиша SCAN или нет
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    // выводим запрос на включение Bluetooth если оне не включен
    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }
    // Завершение работы данного Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    // при свёртывании окна отключаем сканирование и очищаем список найденных устройств
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    // При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
    // останавливаем сканирование и запускаем новое Activity
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        //DeviceControlActivity.class
        final Intent intent = new Intent(this, DeviceControlActivity.class );
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }
    // процедура сканирования устройства
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<String> mLeRssi;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mLeRssi = new ArrayList<String>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device, int rssi) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                mLeRssi.add(String.valueOf(rssi));
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
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.bluetoothClass = (TextView) view.findViewById(R.id.bluetooth_class);
                viewHolder.deviceRssi = (TextView) view.findViewById(R.id.device_rssi);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }


            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
                viewHolder.deviceAddress.setText(device.getAddress());
                viewHolder.bluetoothClass.setText("Device Class: " + device.getBluetoothClass().toString());
                viewHolder.deviceRssi.setText("RSSI: " + mLeRssi.get(i) + " dbm");

            return view;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device, rssi);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView bluetoothClass;
        TextView deviceRssi;
    }
}
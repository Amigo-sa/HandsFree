package by.citech.handsfree.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;


import static android.content.Context.MODE_PRIVATE;

public final class AppData {

    private static String bluetoothDeviceId;
    private static String remoteIP;
    private static int remotePort;
    private static SharedPreferences preferences;

    public static void init(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        preferences = context.getSharedPreferences(context.getString(stringId), MODE_PRIVATE);
        restore();
    }

    public static void save() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("bluetoothDeviceId", bluetoothDeviceId).
                putString("remoteIP", remoteIP).
                putInt("remotePort", remotePort);
        editor.apply();
    }

    private static void restore() {
        bluetoothDeviceId = preferences.getString("bluetoothDeviceId", "");
        remoteIP = preferences.getString("remoteIP", "");
        remotePort = preferences.getInt("remotePort", 80);
    }

    public static String getBluetoothDeviceId() {
        return bluetoothDeviceId;
    }

    public static void setBluetoothDeviceId(String bluetoothDeviceId) {
        AppData.bluetoothDeviceId = bluetoothDeviceId;
        save();
    }

    public static String getRemoteIP() {
        return remoteIP;
    }

    public static void setRemoteIP(String remoteIP) {
        AppData.remoteIP = remoteIP;
        save();
    }

    public static int getRemotePort() {
        return remotePort;
    }

    public static void setRemotePort(int remotePort) {
        AppData.remotePort = remotePort;
        save();
    }

    public static void reset() {
        bluetoothDeviceId = "";
        remoteIP = "";
        remotePort = 0;
        save();
    }

    //--------------------------------- TODO LIST
    /*

    Добавить в Activity и прочих номер объекта к тэгу.

    Сделать Settings синглтоном.
    Повсюду при инициализации объектов вызывать его интерфейс для обновления настроек.
    Добавлять вызвавшие объекты в подписку на обновление настроек.
    Подумать о порядке обновления настроек.
    Разные уровни срочности обновления настроек?

    IBase вызывать не в виде дерева, а перенести в синглтон ResourceManager.
    В нём хранить список вызвавших baseStart.
    В нём тоже реализовать IBase?

    Подумать об интерфейсе ICheck.
    Использование: для предстартовой проверки состояния ключевых объектов.
    Реализовать через postDelayed?

    Подумать о повторном использовании объектов без создания новых, где это возможно.

    Подумать о переносе работы в фон при звонке, использовании датчика приближения.

    Убрать из Activity остатки того, что можно убрать.

    Разобрать пример FactoryMethod, Factory и AbstractFactory.

    Заменить в классах с рассылкой сообщений (callui, connectornet) for-ы на один метод (с раннаблом?).
    Там же при работе со state добавить действие при невозможности перехода.

    Добавить возможность автоматического перебора портов для поиска: свободный порт серверу и активный порт клиенту.

    Подумать над тем, что плата не перестаёт присылать пакеты после отключения передачи данных.

*/

}

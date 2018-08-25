package com.hzecool.printer.rn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.hzecool.printer.BitmapToHexService;
import com.hzecool.printer.bean.MyDeviceBean;
import com.hzecool.printer.bean.PrintImageSettingOptionsType;
import com.hzecool.printer.bluetoothtea.TeaUtil;
import com.hzecool.printer.btlibrary.BluetoothSdkManager;
import com.hzecool.printer.btlibrary.constant.ConstantDefine;
import com.hzecool.printer.btlibrary.listener.BluetoothConnectListener;
import com.hzecool.printer.btlibrary.listener.DiscoveryDevicesListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PrinterModule extends ReactContextBaseJavaModule {

    private Context mContext;
    private BluetoothSdkManager manager;

    public PrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
    }

    @Override
    public String getName() {
        return "DLPrintUtilManager";
    }


    @ReactMethod
    public void printImages(ReadableArray imageSource, int printerType, ReadableMap options, Promise promise) {

        try {
            List<PrintImageSettingOptionsType.PrintImageSourceType> list = new ArrayList<>();
            for (int i = 0; i < imageSource.size(); i++) {
                String url = "";
                String desc = "";
                String logoUrl = "";
                String qrcodeContent = "";
                ReadableMap map = imageSource.getMap(i);
                if (map.hasKey("url")) {
                    url = map.getString("url");
                }
                if (map.hasKey("desc")) {
                    desc = map.getString("desc");
                }
                if (map.hasKey("logoUrl")) {
                    logoUrl = map.getString("logoUrl");
                }
                if (map.hasKey("qrcodeContent")) {
                    qrcodeContent = map.getString("qrcodeContent");
                }

                PrintImageSettingOptionsType.PrintImageSourceType imageSourceType = new PrintImageSettingOptionsType.PrintImageSourceType(url, desc, logoUrl, qrcodeContent);
                list.add(imageSourceType);
            }

            // 考虑只有行或列的情况
            int column = options.getInt("column");
            int row = options.getInt("row");
            if (row == 0 && column != 0) {
                row = (list.size() - 1) / column + 1;
            } else if (column == 0 && row != 0) {
                column = (list.size() - 1) / row + 1;
            }

            PrintImageSettingOptionsType optionsType = new PrintImageSettingOptionsType();
            optionsType.column = new BigDecimal(column);
            optionsType.imageHeight = new BigDecimal(options.getDouble("imageHeight"));
            optionsType.imageWidth = new BigDecimal(options.getDouble("imageWidth"));
            optionsType.margin = new BigDecimal(options.getDouble("margin"));
            optionsType.row = new BigDecimal(row);
            optionsType.scale = new BigDecimal(options.getDouble("scale"));
            optionsType.startX = new BigDecimal(options.getDouble("startX"));
            optionsType.startY = new BigDecimal(options.getDouble("startY"));

            BitmapToHexService.getInstance().setPrinterType(printerType).executeWork(this.mContext, list, optionsType, promise);
        } catch (Throwable e) {
            promise.reject(e);
        }

    }

    @ReactMethod
    public void getPrinterEncryCodeWithBase64Code(String base64Code, Callback callback) {
        String resultBase64Code = TeaUtil.getPrinterEncryCodeWithBase64Code(base64Code);
        if (callback != null) {
            callback.invoke(resultBase64Code);
        }
    }

    /**==================== 蓝牙2.0相关方法 ===================== start **/

    /**
     * Spp蓝牙服务初始化
     */
    @ReactMethod
    public void initialize() {
        // 在主线程中初始化
        getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                manager = BluetoothSdkManager.INSTANCE;
                manager.init(mContext);
                if (!manager.isServiceRunning()) {
                    manager.setupService();
                }
            }
        });
    }

    /**
     * 开始扫描设备
     *
     */
    @ReactMethod
    public void startScan() {
        final ArrayList<MyDeviceBean> myDeviceBeans = new ArrayList<>();
        // 检查蓝牙是否可用
        manager.checkBluetooth();
        // 取消当前的扫描
        if (manager.isDiscoverying()) {
            manager.cancelDiscovery();
        }

        manager.setDiscoveryDeviceListener(new DiscoveryDevicesListener() {
            @Override
            public void startDiscovery() {
                myDeviceBeans.addAll(getPairedData());
                sendEventToJs("sppDiscoveryDevice", convertDevices(myDeviceBeans));
            }

            @Override
            public void discoveryNew(BluetoothDevice device) {
                if (device != null) {
                    myDeviceBeans.add(new MyDeviceBean(device.getName(), device.getAddress()));
                    sendEventToJs("sppDiscoveryDevice", convertDevices(myDeviceBeans));
                }
            }

            @Override
            public void discoveryFinish(List<BluetoothDevice> list) {

            }
        });
    }

    /**
     * 停止扫描设备并释放资源
     * 在退出扫描页面前需要调用
     */
    @ReactMethod
    public void stopScan() {
        manager.stopDiscovery();
    }

    /**
     * 连接设备
     *
     * @param address
     */
    @ReactMethod
    public void connect(String address) {
        try {
            if (!manager.isServiceRunning()) {
                manager.setupService();
            }
            if (manager.isDiscoverying()) {
                manager.cancelDiscovery();
            }
            //如果切换连接设备,先断开之前的.
            if (manager.getConnectDeviceAddress() != null) {
                //不同设备切换
                if (!manager.getConnectDeviceAddress().equals(address)) {
                    manager.disconnect();
                    manager.connect(address);
                }
            } else {
                //正在进行连接操作的设备不可操作
                if (manager.getServiceState() != ConstantDefine.CONNECT_STATE_CONNECTING) {
                    //当前无连接
                    manager.connect(address);
                }
            }
            manager.setBluetoothConnectListener(new BluetoothConnectListener() {
                @Override
                public void onBTDeviceConnected(String address, String name) {
                    sendEventToJs("sppConnectStateChange", convertConnectStatus("0", name+"已连接"));
                }

                @Override
                public void onBTDeviceDisconnected() {
                    sendEventToJs("sppConnectStateChange", convertConnectStatus("-1", "设备已断开连接"));
                }

                @Override
                public void onBTDeviceConnectFailed() {
                    sendEventToJs("sppConnectStateChange", convertConnectStatus("-1", "蓝牙连接失败"));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            sendEventToJs("sppConnectStateChange", convertConnectStatus("-1", "蓝牙连接失败"));
        }
    }

    /**
     * 断开连接
     */
    @ReactMethod
    public void disconnect() {
        manager.disconnect();
    }

    /**
     * 发送数据到打印机
     */
    @ReactMethod
    public void write(String base64code) {
        manager.print(Base64.decode(base64code, Base64.DEFAULT));
    }

    /**
     * 停止连接服务进程
     */
    @ReactMethod
    public void stopService() {
        manager.stopService();
    }

    //取得已经配对的蓝牙信息
    private List<MyDeviceBean> getPairedData() {
        List<MyDeviceBean> data = new ArrayList<MyDeviceBean>();
        //默认的蓝牙适配器
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        // 得到当前的一个已经配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = defaultAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                data.add(new MyDeviceBean(device.getName(), device.getAddress()));
            }
        }
        return data;
    }

    // 将devices转换成 rn 可接收的array
    private WritableArray convertDevices(List<MyDeviceBean> originalDevices) {
        WritableArray devicesArray = Arguments.createArray();
        for (MyDeviceBean device : originalDevices) {
            WritableMap map = Arguments.createMap();
            map.putString("name", device.getName());
            map.putString("address", device.getAddress());
            devicesArray.pushMap(map);
        }
        return devicesArray;
    }

    private WritableMap convertConnectStatus(String code, String msg) {
        WritableMap result = Arguments.createMap();
        result.putString("code", code);
        result.putString("message", msg);
        return result;
    }

    private void sendEventToJs(String eventName, Object obj) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, obj);
    }

    /**==================== 蓝牙2.0相关方法 ===================== end **/
}

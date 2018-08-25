package com.hzecool.printer.bluetoothUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.hzecool.printer.bean.MyDeviceBean;
import com.hzecool.printer.btlibrary.BluetoothSdkManager;
import com.hzecool.printer.btlibrary.constant.ConstantDefine;
import com.hzecool.printer.btlibrary.listener.BluetoothConnectListener;
import com.hzecool.printer.btlibrary.listener.DiscoveryDevicesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothUtils {
    private BluetoothSdkManager manager;

    /**
     * Spp蓝牙服务初始化
     * @param context
     */
    public void initialize(Context context){
        manager = BluetoothSdkManager.INSTANCE;
        manager.init(context);
        if (!manager.isServiceRunning()) {
            manager.setupService();
        }
    }

    /**
     * 开始扫描设备
     * @param callback
     */
    public void scan(final Callback callback){
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
            }

            @Override
            public void discoveryNew(BluetoothDevice device) {
                if (callback != null) {
                    if (device != null) {
                        myDeviceBeans.add(new MyDeviceBean(device.getName(), device.getAddress()));
                        //callback.invoke(null, convertDevices(myDeviceBeans));
                        callback.invoke(null, myDeviceBeans);
                    }
                }
            }

            @Override
            public void discoveryFinish(List<BluetoothDevice> list) {
                if (callback != null) {
                    callback.invoke("扫描完成");
                }
            }
        });
    }

    /**
     * 停止扫描设备
     */
    public void stopScan(){
        manager.stopDiscovery();
    }

    /**
     * 连接设备
     * @param address
     */
    public void connect(String address, final Callback callback) {
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
                    callback.invoke(null, name+"已连接");
                }

                @Override
                public void onBTDeviceDisconnected() {
                    callback.invoke("蓝牙已断开");
                }

                @Override
                public void onBTDeviceConnectFailed() {
                    callback.invoke("蓝牙连接失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.invoke("蓝牙连接失败");
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        manager.disconnect();
    }

    /**
     * 发送数据到打印机
     */
    public void write(String base64code){
        manager.print(Base64.decode(base64code, Base64.DEFAULT));
    }

    /**
     * 停止连接服务进程
     */
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
    private WritableArray convertDevices(List<MyDeviceBean> originalDevices){
        WritableArray devicesArray = Arguments.createArray();
        for (MyDeviceBean device : originalDevices) {
            WritableMap map = Arguments.createMap();
            map.putString("name", device.getName());
            map.putString("address", device.getAddress());
            devicesArray.pushMap(map);
        }
        return devicesArray;
    }
}

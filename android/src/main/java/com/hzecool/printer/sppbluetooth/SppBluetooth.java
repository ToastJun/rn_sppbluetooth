package com.hzecool.printer.sppbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.feasycom.bean.BluetoothDeviceWrapper;
import com.feasycom.controler.FscSppApi;
import com.feasycom.controler.FscSppApiImp;
import com.feasycom.controler.FscSppCallbacksImp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 二代蓝牙工具类
 */
public class SppBluetooth {
    private final int ENABLE_BT_REQUEST_ID = 2;

    private static SppBluetooth sInstance;

    private static FscSppApi fscSppApi;

    private Activity activity;

    private ArrayList<BluetoothDeviceWrapper> mDevicesList;

    Queue<BluetoothDeviceWrapper> deviceQueue = new LinkedList<BluetoothDeviceWrapper>();

    public static SppBluetooth getInstance(Activity activity) {
        if (sInstance == null || fscSppApi == null) {
            sInstance = new SppBluetooth();
            sInstance.activity = activity;

            fscSppApi = FscSppApiImp.getInstance(activity);
            fscSppApi.initialize();
        }
        return sInstance;
    }

    /**
     * 扫描附近的蓝牙设备
     * @return
     */
    public void scan(final Promise promise){
        if (!fscSppApi.isBtEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
        }
        mDevicesList = new ArrayList<>();
        fscSppApi.setCallbacks(new FscSppCallbacksImp(){
            @Override
            public void sppDeviceFound(BluetoothDeviceWrapper device, int rssi) {
                deviceQueue.offer(device);
                // 将蓝牙设备列表返回
                if (promise != null) {
                    addDevicesToList(mDevicesList, device);
                    // 返回所需结构的列表
                    promise.resolve(convertToWritableArray(mDevicesList));
                }
            }

            @Override
            public void stopScan() {
                if (promise != null) {
                    promise.reject("-1", "停止扫描");
                }
            }
        });
        fscSppApi.startScan(10000);
    }

    /**
     * 连接蓝牙设备
     * @param mac
     */
    public void connectDevice(String mac, final Promise promise) {
        fscSppApi.disconnect();
        fscSppApi.setCallbacks(new FscSppCallbacksImp(){
            @Override
            public void sppConnected(BluetoothDevice device) {
                if (promise != null) {
                    promise.resolve(device.getName()+"蓝牙连接成功");
                }
            }

            @Override
            public void sppDisconnected(BluetoothDevice device) {
                if (promise != null) {
                    promise.reject("-1", device.getName()+"断开连接");
                }
            }
        });
        fscSppApi.connect(mac);
    }

    /**
     * 断开蓝牙设备
     */
    public void disconnectDevice(final Promise promise){
        fscSppApi.setCallbacks(new FscSppCallbacksImp(){
            @Override
            public void sppDisconnected(BluetoothDevice device) {
                if (promise != null) {
                    promise.resolve(true);
                }
            }
        });
        fscSppApi.disconnect();
    }

    /**
     * 发送到打印机
     * @param
     * @param
     */
    public void write(String base64code, final Promise promise) {
        fscSppApi.setCallbacks(new FscSppCallbacksImp(){
            @Override
            public void sendPacketProgress(BluetoothDevice device, int percentage, byte[] sendByte) {
                if (promise != null) {
                    if (percentage == FscSppApi.PACKGE_SEND_FINISH) {
                        promise.resolve(true);
                    }
                }
            }

            @Override
            public void sppDisconnected(BluetoothDevice device) {
                if (promise != null) {
                    promise.reject("-1", device.getName()+"断开连接");
                }
            }
        });
        // 将base64code转换成bytes
        byte[] decode = Base64.decode(base64code, Base64.DEFAULT);
        if (fscSppApi.isConnected()) {
            fscSppApi.send(decode);
        }
    }

    /**
     * 将搜索到的蓝牙设备信息添加到list中
     * @param mDevices
     * @param deviceDetail
     */
    private void addDevicesToList(List<BluetoothDeviceWrapper> mDevices, BluetoothDeviceWrapper deviceDetail) {
        int i = 0;
        for (; i < mDevices.size(); i++) {
            if (deviceDetail.getAddress().equals(mDevices.get(i).getAddress())) {
                mDevices.get(i).setName(deviceDetail.getName());
                mDevices.get(i).setRssi(deviceDetail.getRssi());
                mDevices.get(i).setAdvData(deviceDetail.getAdvData());
                break;
            }
        }
        if (i >= mDevices.size()) {
            mDevices.add(deviceDetail);
        }
    }

    /**
     * 转换成rn可接收的array
     * @param original
     * @return
     */
    private ReadableArray convertToWritableArray(List<BluetoothDeviceWrapper> original) {
        WritableArray array = Arguments.createArray();
        for (BluetoothDeviceWrapper device : original) {
            WritableMap map = Arguments.createMap();
            map.putString("name", device.getName());
            map.putString("mac", device.getAddress());
            array.pushMap(map);
        }
        return array;
    }
}

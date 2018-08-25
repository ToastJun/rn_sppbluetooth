package com.printer.ecool.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.react.bridge.Callback;
import com.hzecool.printer.bean.MyDeviceBean;
import com.hzecool.printer.bluetoothUtil.BluetoothUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private MyAdapter mAdapter;
    private EditText editText;
    private BluetoothUtils bluetoothUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothUtils = new BluetoothUtils();
        bluetoothUtils.initialize(getApplicationContext());

        View scanBtn = findViewById(R.id.scan_bluetooth);
        View sendBtn = findViewById(R.id.send);
        View disconnectBtn = findViewById(R.id.disconnect_btn);
        ListView listview = findViewById(R.id.devices_listview);
        findViewById(R.id.stop_scan).setOnClickListener(this);
        editText = findViewById(R.id.data_edittext);

        mAdapter = new MyAdapter();
        listview.setAdapter(mAdapter);
        scanBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        disconnectBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("base64", Base64.encodeToString("123".getBytes(), Base64.NO_WRAP));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scan_bluetooth) {
            bluetoothUtils.scan(new Callback() {
                @Override
                public void invoke(Object... args) {
                    if (args[0] == null) {
                        mAdapter.addData((List<MyDeviceBean>) args[1]);
                    }
                }
            });
        } else if (v.getId() == R.id.send) {
            String dataString = editText.getText().toString().trim();
            String content = Base64.encodeToString(dataString.getBytes(), Base64.DEFAULT);
            bluetoothUtils.write(content);
        } else if (v.getId() == R.id.disconnect_btn) {
            bluetoothUtils.disconnect();
        } else if (v.getId() == R.id.stop_scan) {
            bluetoothUtils.stopScan();
            bluetoothUtils.stopService();
        }
    }


    class MyAdapter extends BaseAdapter{
        private List<MyDeviceBean> datas;

        public void addData(List<MyDeviceBean> data) {
            this.datas = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return datas == null ? 0 : datas.size();
        }

        @Override
        public Object getItem(int i) {
            return datas == null ? null : datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view= View.inflate(MainActivity.this, R.layout.item_listview, null);
                holder = new ViewHolder();
                holder.tvName = view.findViewById(R.id.item_name);
                holder.tvAddr = view.findViewById(R.id.item_addr);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.tvName.setText(datas.get(i).getName());
            holder.tvAddr.setText(datas.get(i).getAddress());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bluetoothUtils.connect(datas.get(i).getAddress(), new Callback() {
                        @Override
                        public void invoke(Object... args) {

                        }
                    });
                }
            });
            return view;
        }
    }

    static class ViewHolder{
        TextView tvName;
        TextView tvAddr;
    }
}

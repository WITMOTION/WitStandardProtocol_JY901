package com.wit.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wit.witsdk.modular.sensor.device.exceptions.OpenDeviceException;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.BluetoothBLE;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.BluetoothSPP;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.WitBluetoothManager;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.exceptions.BluetoothBLEException;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.interfaces.IBluetoothFoundObserver;
import com.wit.witsdk.modular.sensor.modular.processor.constant.WitSensorKey;
import com.wit.witsdk.modular.witsensorapi.modular.spp.Bwt901cl;
import com.wit.witsdk.modular.witsensorapi.modular.spp.interfaces.IBwt901clRecordObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 功能：主界面
 * 说明：
 * 1. 本程序是维特智能开发的蓝牙2.0sdk使用示例
 * 2. 本程序适用于维特智能以下产品
 * BWT901CL
 * BWT61CL
 *
 * 3. 本程序只有一个页面，没有其它页面
 *
 * @author huangyajun
 * @date 2022/6/29 11:35
 */
public class MainActivity extends AppCompatActivity implements IBluetoothFoundObserver, IBwt901clRecordObserver {

    /**
     * 日志标签
     */
    private static final String TAG = "MainActivity";

    /**
     * 设备列表
     */
    private List<Bwt901cl> bwt901clList = new ArrayList<>();

    /**
     * 控制自动刷新线程是否工作
     */
    private boolean destroyed = true;

    /**
     * activity 创建时
     *
     * @author huangyajun
     * @date 2022/6/29 8:43
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            WitBluetoothManager.requestPermissions(this);
            // 初始化蓝牙管理器，这里会申请蓝牙权限
            WitBluetoothManager.initInstance(this);
        }catch (Exception e){
            Log.e("",e.getMessage());
            e.printStackTrace();
        }

        // 开始搜索按钮
        Button startSearchButton = findViewById(R.id.startSearchButton);
        startSearchButton.setOnClickListener((v) -> {
            startDiscovery();
        });

        // 停止搜索按钮
        Button stopSearchButton = findViewById(R.id.stopSearchButton);
        stopSearchButton.setOnClickListener((v) -> {
            stopDiscovery();
        });

        // 加计校准按钮
        Button appliedCalibrationButton = findViewById(R.id.appliedCalibrationButton);
        appliedCalibrationButton.setOnClickListener((v) -> {
            handleAppliedCalibration();
        });

        // 开始磁场校准按钮
        Button startFieldCalibrationButton = findViewById(R.id.startFieldCalibrationButton);
        startFieldCalibrationButton.setOnClickListener((v) -> {
            handleStartFieldCalibration();
        });

        // 结束磁场校准按钮
        Button endFieldCalibrationButton = findViewById(R.id.endFieldCalibrationButton);
        endFieldCalibrationButton.setOnClickListener((v) -> {
            handleEndFieldCalibration();
        });

        // 读取03寄存器按钮
        Button readReg03Button = findViewById(R.id.readReg03Button);
        readReg03Button.setOnClickListener((v) -> {
            handleReadReg03();
        });

        // 自动刷新数据线程
        Thread thread = new Thread(this::refreshDataTh);
        destroyed = false;
        thread.start();
    }

    /**
     * activity 销毁时
     *
     * @author huangyajun
     * @date 2022/6/29 13:59
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /**
     * 开始搜索设备
     *
     * @author huangyajun
     * @date 2022/6/29 10:04
     */
    public void startDiscovery() {

        // 关闭所有设备
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            bwt901cl.removeRecordObserver(this);
            bwt901cl.close();
        }

        // 清除所有设备
        bwt901clList.clear();

        // 开始搜索蓝牙
        try {
            // 获得蓝牙管理器
            WitBluetoothManager bluetoothManager = WitBluetoothManager.getInstance();
            // 注册监听蓝牙
            bluetoothManager.registerObserver(this);
            // 开始搜索
            bluetoothManager.startDiscovery();
        } catch (BluetoothBLEException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止搜索设备
     *
     * @author huangyajun
     * @date 2022/6/29 10:04
     */
    public void stopDiscovery() {
        // 停止搜索蓝牙
        try {
            // 获得蓝牙管理器
            WitBluetoothManager bluetoothManager = WitBluetoothManager.getInstance();
            // 取消注册监听蓝牙
            bluetoothManager.removeObserver(this);
            // 停止搜索
            bluetoothManager.stopDiscovery();
        } catch (BluetoothBLEException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当搜到蓝牙5.0设备时会回调这个方法
     *
     * @author huangyajun
     * @date 2022/6/29 8:46
     */
    @Override
    public void onFoundBle(BluetoothBLE bluetoothBLE) {
        // 不做任何处理，这个示例程序只演示如何连接蓝牙2.0设备
    }

    /**
     * 当搜索到蓝牙2.0设备时会回调这个方法
     *
     * @author huangyajun
     * @date 2022/6/29 10:01
     */
    @Override
    public void onFoundSPP(BluetoothSPP bluetoothSPP) {
        // 创建蓝牙2.0传感器连接对象
        Bwt901cl bwt901cl = new Bwt901cl(bluetoothSPP);
        // 避免重复连接
        for(int i = 0; i < bwt901clList.size(); i++){
            if(Objects.equals(bwt901clList.get(i).getDeviceName(), bwt901cl.getDeviceName())){
                return;
            }
        }
        // 添加到设备列表
        bwt901clList.add(bwt901cl);

        // 注册数据记录
        bwt901cl.registerRecordObserver(this);

        // 打开设备
        try {
            bwt901cl.open();
        } catch (OpenDeviceException e) {
            // 打开设备失败
            e.printStackTrace();
        }
    }

    /**
     * 当需要记录数据时会回调这个方法
     *
     * @author huangyajun
     * @date 2022/6/29 8:46
     */
    @Override
    public void onRecord(Bwt901cl bwt901cl) {
        String deviceData = getDeviceData(bwt901cl);
        Log.d(TAG, "device data [ " + bwt901cl.getDeviceName() + "] = " + deviceData);
    }

    /**
     * 自动刷新数据线程
     *
     * @author huangyajun
     * @date 2022/6/29 13:41
     */
    private void refreshDataTh() {

        while (!destroyed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < bwt901clList.size(); i++) {
                // 让所有设备进行加计校准
                Bwt901cl bwt901cl = bwt901clList.get(i);
                String deviceData = getDeviceData(bwt901cl);
                text.append(deviceData);
            }

            TextView deviceDataTextView = findViewById(R.id.deviceDataTextView);
            runOnUiThread(() -> {
                deviceDataTextView.setText(text.toString());
            });
        }
    }

    /**
     * 获得一个设备的数据
     *
     * @author huangyajun
     * @date 2022/6/29 11:37
     */
    private String getDeviceData(Bwt901cl bwt901cl) {
        StringBuilder builder = new StringBuilder();
        builder.append(bwt901cl.getDeviceName()).append("\n");
        builder.append(getString(R.string.accX)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AccX)).append("g \t");
        builder.append(getString(R.string.accY)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AccY)).append("g \t");
        builder.append(getString(R.string.accZ)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AccZ)).append("g \n");
        builder.append(getString(R.string.asX)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AsX)).append("°/s \t");
        builder.append(getString(R.string.asY)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AsY)).append("°/s \t");
        builder.append(getString(R.string.asZ)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AsZ)).append("°/s \n");
        builder.append(getString(R.string.angleX)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AngleX)).append("° \t");
        builder.append(getString(R.string.angleY)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AngleY)).append("° \t");
        builder.append(getString(R.string.angleZ)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AngleZ)).append("° \n");
        builder.append(getString(R.string.hX)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.HX)).append("\t");
        builder.append(getString(R.string.hY)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.HY)).append("\t");
        builder.append(getString(R.string.hZ)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.HZ)).append("\n");
        builder.append(getString(R.string.t)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.T)).append("\n");
        builder.append(getString(R.string.electricQuantityPercentage)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.ElectricQuantityPercentage)).append("\n");
        builder.append(getString(R.string.versionNumber)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.VersionNumber)).append("\n");
        return builder.toString();
    }

    /**
     * 让所有设备加计校准
     *
     * @author huangyajun
     * @date 2022/6/29 10:25
     */
    private void handleAppliedCalibration() {
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            // 解锁寄存器
            bwt901cl.unlockReg();
            // 发送命令
            bwt901cl.appliedCalibration();
        }
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
    }

    /**
     * 让所有设备开始磁场校准
     *
     * @author huangyajun
     * @date 2022/6/29 10:25
     */
    private void handleStartFieldCalibration() {
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            // 解锁寄存器
            bwt901cl.unlockReg();
            // 发送命令
            bwt901cl.startFieldCalibration();
        }
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
    }

    /**
     * 让所有设备结束磁场校准
     *
     * @author huangyajun
     * @date 2022/6/29 10:25
     */
    private void handleEndFieldCalibration() {
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            // 解锁寄存器
            bwt901cl.unlockReg();
            // 发送命令
            bwt901cl.endFieldCalibration();
        }
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
    }



    /**
     * 读取03寄存器的数据
     *
     * @author huangyajun
     * @date 2022/6/29 10:25
     */
    private void handleReadReg03() {
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            // 必须使用 sendProtocolData 方法，使用此方法设备才会将寄存器值读取上来
            int waitTime = 200;
            // 发送指令的命令,并且等待200ms
            bwt901cl.sendProtocolData(new byte[]{(byte) 0xff, (byte) 0xAA, (byte) 0x27, (byte) 0x03, (byte) 0x00}, waitTime);
            // 获得寄存器03的值
            String reg03Value = bwt901cl.getDeviceData("03");
            // 如果读上来了 reg03Value 就是寄存器的值，如果没有读上来可以将 waitTime 放大,或者多读几次
            Toast.makeText(this, bwt901cl.getDeviceName() + " reg03Value: " + reg03Value, Toast.LENGTH_LONG).show();
        }
    }
}
package com.wit.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wit.example.spp.Bwt901cl;
import com.wit.example.spp.data.WitSensorKey;
import com.wit.example.spp.interfaces.IBwt901clRecordObserver;
import com.wit.witsdk.sensor.dkey.ShortKey;
import com.wit.witsdk.sensor.dkey.StringKey;
import com.wit.witsdk.sensor.modular.connector.modular.bluetooth.BluetoothBLE;
import com.wit.witsdk.sensor.modular.connector.modular.bluetooth.BluetoothSPP;
import com.wit.witsdk.sensor.modular.connector.modular.bluetooth.WitBluetoothManager;
import com.wit.witsdk.sensor.modular.connector.modular.bluetooth.exceptions.BluetoothBLEException;
import com.wit.witsdk.sensor.modular.connector.modular.bluetooth.interfaces.IBluetoothFoundObserver;
import com.wit.witsdk.sensor.modular.device.exceptions.OpenDeviceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 功能：主界面
 * Function: main interface
 * 说明：
 * Explanation：
 * 本程序是维特智能开发的蓝牙2.0sdk使用示例
 * This program is an example base on Bluetooth 2.0sdk developed by WitMotion
 * 本程序适用于维特智能以下产品
 * This program is applicable to the following products of WitMotion
 * BWT901CL
 * BWT61CL
 * <p>
 * 本程序只有一个页面，没有其它页面
 * This program has only one page and no other pages
 *
 * @author huangyajun
 * @date 2022/6/29 11:35
 */
public class MainActivity extends AppCompatActivity implements IBluetoothFoundObserver, IBwt901clRecordObserver {

    /**
     * 日志标签
     * log tag
     */
    private static final String TAG = "MainActivity";

    /**
     * 设备列表
     * Device List
     */
    private List<Bwt901cl> bwt901clList = new ArrayList<>();

    /**
     * 控制自动刷新线程是否工作
     * Controls whether the auto-refresh thread works
     */
    private boolean destroyed = true;

    /**
     * activity 创建时
     * activity when created
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
            // Initialize the Bluetooth manager, here will apply for Bluetooth permissions
            WitBluetoothManager.initInstance(this);
        } catch (Exception e) {
            Log.e("", e.getMessage());
            e.printStackTrace();
        }

        // 开始搜索按钮
        // start search button
        Button startSearchButton = findViewById(R.id.startSearchButton);
        startSearchButton.setOnClickListener((v) -> {
            startDiscovery();
        });

        // 停止搜索按钮
        // stop search button
        Button stopSearchButton = findViewById(R.id.stopSearchButton);
        stopSearchButton.setOnClickListener((v) -> {
            stopDiscovery();
        });

        // 加计校准按钮
        // Acceleration calibration button
        Button appliedCalibrationButton = findViewById(R.id.appliedCalibrationButton);
        appliedCalibrationButton.setOnClickListener((v) -> {
            handleAppliedCalibration();
        });

        // 开始磁场校准按钮
        // Start Magnetic Field Calibration button
        Button startFieldCalibrationButton = findViewById(R.id.startFieldCalibrationButton);
        startFieldCalibrationButton.setOnClickListener((v) -> {
            handleStartFieldCalibration();
        });

        // 结束磁场校准按钮
        // End Magnetic Field Calibration button
        Button endFieldCalibrationButton = findViewById(R.id.endFieldCalibrationButton);
        endFieldCalibrationButton.setOnClickListener((v) -> {
            handleEndFieldCalibration();
        });

        // 读取03寄存器按钮
        // Read 03 register button
        Button readReg03Button = findViewById(R.id.readReg03Button);
        readReg03Button.setOnClickListener((v) -> {
            handleReadReg03();
        });

        // 自动刷新数据线程
        // Auto refresh data thread
        Thread thread = new Thread(this::refreshDataTh);
        destroyed = false;
        thread.start();
    }

    /**
     * activity 销毁时
     * activity perish
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
     * Start searching for devices
     *
     * @author huangyajun
     * @date 2022/6/29 10:04
     */
    public void startDiscovery() {

        // 关闭所有设备
        // Turn off all device
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            bwt901cl.removeRecordObserver(this);
            bwt901cl.close();
        }

        // 清除所有设备
        // Erase all devices
        bwt901clList.clear();

        // 开始搜索蓝牙
        // Start searching for bluetooth
        try {
            // 获得蓝牙管理器
            // get bluetooth manager
            WitBluetoothManager bluetoothManager = WitBluetoothManager.getInstance();
            // 注册监听蓝牙
            // Monitor communication signals
            bluetoothManager.registerObserver(this);
            // 指定要搜索的蓝牙名称
            // Specify the Bluetooth name to search for
            WitBluetoothManager.DeviceNameFilter = Arrays.asList("HC-06");
            // 开始搜索
            // start search
            bluetoothManager.startDiscovery();
        } catch (BluetoothBLEException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 停止搜索设备
     * Stop searching for devices
     *
     * @author huangyajun
     * @date 2022/6/29 10:04
     */
    public void stopDiscovery() {
        // 停止搜索蓝牙
        // stop searching for bluetooth
        try {
            // 获得蓝牙管理器
            // acquire Bluetooth manager
            WitBluetoothManager bluetoothManager = WitBluetoothManager.getInstance();
            // 取消注册监听蓝牙
            // Cancel monitor communication signals
            bluetoothManager.removeObserver(this);
            // 停止搜索
            // stop searching
            bluetoothManager.stopDiscovery();
        } catch (BluetoothBLEException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当搜到蓝牙5.0设备时会回调这个方法
     * This method will be called back when a Bluetooth 5.0 device is found
     *
     * @author huangyajun
     * @date 2022/6/29 8:46
     */
    @Override
    public void onFoundBle(BluetoothBLE bluetoothBLE) {
    }

    /**
     * 当搜索到蓝牙2.0设备时会回调这个方法
     * This method will be called back when a Bluetooth 2.0 device is found
     *
     * @author huangyajun
     * @date 2022/6/29 10:01
     */
    @Override
    public void onFoundSPP(BluetoothSPP bluetoothSPP) {
    }

    /**
     * 找到双模蓝牙时
     * This method will be called back when data needs to be recorded
     *
     * @author huangyajun
     * @date 2022/6/29 8:46
     */
    @Override
    public void onFoundDual(BluetoothBLE bluetoothBLE) {
        // 创建蓝牙2.0传感器连接对象
        // Create a Bluetooth 2.0 sensor connection object
        Bwt901cl bwt901cl = new Bwt901cl(bluetoothBLE);
        // 避免重复连接
        // Avoid duplicate connections
        for (int i = 0; i < bwt901clList.size(); i++) {
            if (Objects.equals(bwt901clList.get(i).getDeviceName(), bwt901cl.getDeviceName())) {
                return;
            }
        }
        // 添加到设备列表
        // add to device list
        bwt901clList.add(bwt901cl);

        // 注册数据记录
        // Registration data record
        bwt901cl.registerRecordObserver(this);

        // 打开设备
        // Turn on the device
        try {
            bwt901cl.open();
        } catch (OpenDeviceException e) {
            // 打开设备失败
            // Failed to open device
            e.printStackTrace();
        }
    }

    /**
     * 当需要记录数据时会回调这个方法
     * This method will be called back when data needs to be recorded
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
     * Auto refresh data thread
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
                // Make all devices accelerometer calibrated
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
     * Get a device's data
     *
     * @author huangyajun
     * @date 2022/6/29 11:37
     */
    private String getDeviceData(Bwt901cl bwt901cl) {
        StringBuilder builder = new StringBuilder();
        builder.append(bwt901cl.getDeviceName()).append("\n");
        builder.append(getString(R.string.accX)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AccX))
                .append(WitSensorKey.AccX.getUnit()).append("\t");
        builder.append(getString(R.string.accY)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AccY))
                .append(WitSensorKey.AccY.getUnit()).append("\t");
        builder.append(getString(R.string.accZ)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AccZ))
                .append(WitSensorKey.AccZ.getUnit()).append("\n");
        builder.append(getString(R.string.asX)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AsX))
                .append(WitSensorKey.AsX.getUnit()).append("\t");
        builder.append(getString(R.string.asY)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AsY))
                .append(WitSensorKey.AsY.getUnit()).append("\t");
        builder.append(getString(R.string.asZ)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AsZ))
                .append(WitSensorKey.AsZ.getUnit()).append("\n");
        builder.append(getString(R.string.angleX)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AngleX))
                .append(WitSensorKey.AngleX.getUnit()).append("\t");
        builder.append(getString(R.string.angleY)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AngleY))
                .append(WitSensorKey.AngleY.getUnit()).append("\t");
        builder.append(getString(R.string.angleZ)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.AngleZ))
                .append(WitSensorKey.AngleZ.getUnit()).append("\n");
        builder.append(getString(R.string.hX)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.HX))
                .append(WitSensorKey.HX.getUnit()).append("\t");
        builder.append(getString(R.string.hY)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.HY))
                .append(WitSensorKey.HY.getUnit()).append("\t");
        builder.append(getString(R.string.hZ)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.HZ))
                .append(WitSensorKey.HZ.getUnit()).append("\n");
        builder.append(getString(R.string.t)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.T))
                .append(WitSensorKey.T.getUnit()).append("\n");
        builder.append(getString(R.string.versionNumber)).append(":").append(bwt901cl.getDeviceData(WitSensorKey.VersionNumber))
                .append(WitSensorKey.VersionNumber.getUnit()).append("\n");
        return builder.toString();
    }

    /**
     * 让所有设备加计校准
     * Make all devices accelerometer calibrated
     *
     * @author huangyajun
     * @date 2022/6/29 10:25
     */
    private void handleAppliedCalibration() {
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            // 解锁寄存器
            // unlock register
            bwt901cl.unlockReg();
            // 发送命令
            // send command
            bwt901cl.appliedCalibration();
        }
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
    }

    /**
     * 让所有设备开始磁场校准
     * Let all devices begin magnetic field calibration
     *
     * @author huangyajun
     * @date 2022/6/29 10:25
     */
    private void handleStartFieldCalibration() {
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            // 解锁寄存器
            // unlock register
            bwt901cl.unlockReg();
            // 发送命令
            // send command
            bwt901cl.startFieldCalibration();
        }
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
    }

    /**
     * 让所有设备结束磁场校准
     * Let's all devices end the magnetic field calibration
     *
     * @author huangyajun
     * @date 2022/6/29 10:25
     */
    private void handleEndFieldCalibration() {
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            // 解锁寄存器
            // unlock register
            bwt901cl.unlockReg();
            // 发送命令
            // send command
            bwt901cl.endFieldCalibration();
        }
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
    }


    /**
     * 读取03寄存器的数据
     * Read 03 register data
     *
     * @author huangyajun
     * @date 2022/6/29 10:25
     */
    private void handleReadReg03() {
        for (int i = 0; i < bwt901clList.size(); i++) {
            Bwt901cl bwt901cl = bwt901clList.get(i);
            // 必须使用 sendProtocolData 方法，使用此方法设备才会将寄存器值读取上来
            // Must be used sendProtocolData method, and the device will read the register value when you using this method
            int waitTime = 200;
            // 发送指令的命令,并且等待200ms
            // The command to send the command, and wait 200ms
            bwt901cl.sendProtocolData(new byte[]{(byte) 0xff, (byte) 0xAA, (byte) 0x27, (byte) 0x03, (byte) 0x00}, waitTime);
            // 获得寄存器03的值
            // get the value of register 03
            Short reg03Value = bwt901cl.getDeviceData(new ShortKey("03"));
            // 如果读上来了 reg03Value 就是寄存器的值，如果没有读上来可以将 waitTime 放大,或者多读几次
            // If it is read up, reg03Value is the value of the register. If it is not read up, you can enlarge waitTime, or read it several times.v
            Toast.makeText(this, bwt901cl.getDeviceName() + " reg03Value: " + reg03Value, Toast.LENGTH_LONG).show();
        }
    }
}
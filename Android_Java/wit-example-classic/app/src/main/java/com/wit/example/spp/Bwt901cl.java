package com.wit.example.spp;

import com.wit.example.spp.components.JY901DataProcessor;
import com.wit.example.spp.components.JY901ProtocolResolver;
import com.wit.example.spp.interfaces.IBwt901clRecordObserver;
import com.wit.witsdk.api.interfaces.IAttitudeSensorApi;
import com.wit.witsdk.sensor.dkey.DoubleKey;
import com.wit.witsdk.sensor.dkey.ShortKey;
import com.wit.witsdk.sensor.dkey.StringKey;
import com.wit.witsdk.sensor.modular.connector.enums.ConnectType;
import com.wit.witsdk.sensor.modular.connector.modular.bluetooth.BluetoothBLE;
import com.wit.witsdk.sensor.modular.connector.modular.bluetooth.BluetoothSPP;
import com.wit.witsdk.sensor.modular.connector.roles.WitCoreConnect;
import com.wit.witsdk.sensor.modular.device.DeviceModel;
import com.wit.witsdk.sensor.modular.device.exceptions.OpenDeviceException;
import com.wit.witsdk.sensor.modular.device.interfaces.IDeviceSendCallback;
import com.wit.witsdk.sensor.modular.device.interfaces.IListenKeyUpdateObserver;

import java.util.ArrayList;
import java.util.List;

public class Bwt901cl implements IListenKeyUpdateObserver, IAttitudeSensorApi {

    /**
     * 设备模型
     */
    private DeviceModel deviceModel;

    /**
     * 蓝牙连接
     */
    private BluetoothSPP bluetoothSPP;

    /**
     * 蓝牙连接
     */
    private BluetoothBLE bluetoothBLE;

    /**
     * 监控数据的人
     */
    private List<IBwt901clRecordObserver> recordObservers = new ArrayList<>();

    /**
     * 构造方法
     *
     * @author maoqiang
     * @date 2022/7/12 10:57
     */
    public Bwt901cl(BluetoothSPP bluetoothSPP) {
        // 创建一个连接蓝牙的设备模型
        DeviceModel deviceModel = new DeviceModel(bluetoothSPP.getName() + "(" + bluetoothSPP.getMac() + ")",
                new JY901ProtocolResolver(),
                new JY901DataProcessor(),
                "51_0");
        WitCoreConnect witCoreConnect = new WitCoreConnect();
        witCoreConnect.setConnectType(ConnectType.BluetoothSPP);
        witCoreConnect.getConfig().getBluetoothSPPOption().setMac(bluetoothSPP.getMac());
        deviceModel.setCoreConnect(witCoreConnect);
        deviceModel.setDeviceData("Mac", bluetoothSPP.getMac());
        this.deviceModel = deviceModel;
        this.bluetoothSPP = bluetoothSPP;
    }

    /**
     * 构造方法
     *
     * @author maoqiang
     * @date 2022/7/12 10:57
     */
    public Bwt901cl(BluetoothBLE bluetoothBLE) {
        // 创建一个连接蓝牙的设备模型
        DeviceModel deviceModel = new DeviceModel(bluetoothBLE.getName() + "(" + bluetoothBLE.getMac() + ")",
                new JY901ProtocolResolver(),
                new JY901DataProcessor(),
                "51_0");
        WitCoreConnect witCoreConnect = new WitCoreConnect();
        witCoreConnect.setConnectType(ConnectType.BluetoothBLE);
        witCoreConnect.getConfig().getBluetoothBLEOption().setMac(bluetoothBLE.getMac());
        deviceModel.setCoreConnect(witCoreConnect);
        deviceModel.setDeviceData("Mac", bluetoothBLE.getMac());
        this.deviceModel = deviceModel;
        this.bluetoothBLE = bluetoothBLE;
    }

    /**
     * 打开连接
     *
     * @author maoqiang
     * @date 2022/7/12 10:57
     */
    public void open() throws OpenDeviceException {
        deviceModel.openDevice();
    }

    /**
     * 关闭连接
     *
     * @author maoqiang
     * @date 2022/7/12 10:57
     */
    public void close() {
        deviceModel.closeDevice();
    }

    /**
     * 是否打开的
     *
     * @author maoqiang
     * @date 2022/7/12 10:57
     */
    public boolean isOpen() {
        return deviceModel.isOpen();
    }

    /**
     * 发送数据
     *
     * @author maoqiang
     * @date 2022/7/12 10:57
     */
    public void sendData(byte[] data, IDeviceSendCallback callback, int waitTime, int repetition) {
        deviceModel.sendData(data, callback, waitTime, repetition);
    }

    /**
     * 发送带协议的数据，使用默认等待时长
     *
     * @author maoqiang
     * @date 2022/7/12 10:58
     */
    public void sendProtocolData(byte[] data) {
        deviceModel.sendProtocolData(data);
    }

    /**
     * 发送带协议的数据,并且指定等待时长
     *
     * @author maoqiang
     * @date 2022/7/12 10:58
     */
    public void sendProtocolData(byte[] data, int waitTime) {
        deviceModel.sendProtocolData(data, waitTime);
    }

    /**
     * 解锁寄存器
     *
     * @author maoqiang
     * @date 2022/7/12 10:58
     */
    public void unlockReg() {
        sendProtocolData(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x69, (byte) 0x88, (byte) 0xB5,});
    }

    /**
     * 保存寄存器
     *
     * @author maoqiang
     * @date 2022/7/12 10:58
     */
    @Override
    public void saveReg() {
        sendProtocolData(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x00, (byte) 0x00, (byte) 0x00,});
    }

    /**
     * 加计校准
     *
     * @author maoqiang
     * @date 2022/7/12 10:58
     */
    public void appliedCalibration() {
        sendProtocolData(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x01, (byte) 0x00,});
    }

    /**
     * 开始磁场校准
     *
     * @author maoqiang
     * @date 2022/7/12 10:58
     */
    public void startFieldCalibration() {
        sendProtocolData(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x07, (byte) 0x00,});
    }

    /**
     * 结束磁场校准
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public void endFieldCalibration() {
        sendProtocolData(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x00, (byte) 0x00,});
    }

    /**
     * 设置回传速率
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public void setReturnRate(byte rate) {
        sendProtocolData(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x03, rate, (byte) 0x00,});
    }

    /**
     * 获得设备名称
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public String getDeviceName() {
        return deviceModel.getDeviceName();
    }

    /**
     * 获得设备名称
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public String getMac() {
        if (bluetoothSPP != null) {
            return bluetoothSPP.getMac();
        }
        return bluetoothBLE.getMac();
    }

    /**
     * 获得设备数据
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public String getDeviceData(String key) {
        return deviceModel.getDeviceData(key);
    }

    /**
     * 获得设备数据
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public String getDeviceData(StringKey key) {
        return deviceModel.getDeviceData(key);
    }

    /**
     * 获得设备数据
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public Double getDeviceData(DoubleKey key) {
        return deviceModel.getDeviceData(key);
    }

    /**
     * 获得设备数据
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public Short getDeviceData(ShortKey key) {
        return deviceModel.getDeviceData(key);
    }

    /**
     * 注册数据记录
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public void registerRecordObserver(IBwt901clRecordObserver record) {
        deviceModel.registerListenKeyUpdateObserver(this);
        recordObservers.add(record);
    }

    /**
     * 移除数据记录监听
     *
     * @author maoqiang
     * @date 2022/7/12 10:59
     */
    public void removeRecordObserver(IBwt901clRecordObserver record) {
        deviceModel.removeListenKeyUpdateObserver(this);

        if (!recordObservers.isEmpty()) {
            recordObservers.remove(record);
        }
    }

    /**
     * 记录数据
     *
     * @author maoqiang
     * @date 2022/7/12 11:00
     */
    @Override
    public void update(DeviceModel deviceModel) {
        deviceModel_OnListenKeyUpdate(deviceModel);
    }

    /**
     * 记录数据
     *
     * @author maoqiang
     * @date 2022/7/12 11:00
     */
    @Override
    public void deviceModel_OnListenKeyUpdate(DeviceModel deviceModel) {
        for (int i = 0; i < recordObservers.size(); i++) {
            IBwt901clRecordObserver iBwt901clRecordObserver = recordObservers.get(i);
            iBwt901clRecordObserver.onRecord(this);
        }
    }
}

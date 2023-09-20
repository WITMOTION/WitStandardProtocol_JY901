package com.wit.example.spp.components;

import com.wit.witsdk.sensor.dkey.ShortKey;
import com.wit.witsdk.sensor.modular.device.DeviceModel;
import com.wit.witsdk.sensor.modular.resolver.entity.SendDataResult;
import com.wit.witsdk.sensor.modular.resolver.interfaces.IProtocolResolver;
import com.wit.witsdk.sensor.modular.resolver.interfaces.ISendDataCallback;
import com.wit.witsdk.sensor.utils.WitProtocolUtils;
import com.wit.witsdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * wt901wifi协议解析器
 *
 * @Author haungyajun
 * @Date 2022/4/26 15:26 （可以根据需要修改）
 */
public class JY901ProtocolResolver implements IProtocolResolver {

    /**
     * 要解析的数据
     */
    private List<Byte> activeByteDataBuffer = new ArrayList<>();

    /**
     * 临时Byte
     */
    private List<Byte> activeByteTemp = new ArrayList<>();

    /**
     * 发送数据
     *
     * @author huangyajun
     * @date 2022/5/23 13:53
     */
    @Override
    public void sendData(byte[] sendData, DeviceModel deviceModel, int waitTime, ISendDataCallback callback) {

        if (waitTime < 0) {
            waitTime = 200;
        }
        try {
            deviceModel.sendData(sendData, (rtnBytes) -> {
                Byte[] returnData = rtnBytes;
                if (sendData != null && sendData.length >= 5 && sendData[2] == 0x27 && returnData != null && returnData.length >= 11) {
                    returnData = WitProtocolUtils.findReturnData(returnData);

                    if (returnData != null && returnData.length == 11) {
                        int readReg = sendData[4] << 8 | sendData[3];

                        short[] Pack = new short[9];
                        Pack[0] = (short) (((short) returnData[4] << 8) | ((short) returnData[3] & 0xff));
                        //BitConverter.ToInt16(returnData, 4);
                        Pack[1] = (short) (((short) returnData[6] << 8) | ((short) returnData[5] & 0xff));
                        Pack[2] = (short) (((short) returnData[8] << 8) | ((short) returnData[7] & 0xff));
                        Pack[3] = (short) (((short) returnData[10] << 8) | ((short) returnData[9] & 0xff));
                        deviceModel.setDeviceData(new ShortKey(Integer.toHexString(readReg + 0).toUpperCase()), Pack[0]);
                        deviceModel.setDeviceData(new ShortKey(Integer.toHexString(readReg + 1).toUpperCase()), Pack[1]);
                        deviceModel.setDeviceData(new ShortKey(Integer.toHexString(readReg + 2).toUpperCase()), Pack[2]);
                        deviceModel.setDeviceData(new ShortKey(Integer.toHexString(readReg + 3).toUpperCase()), Pack[3]);
                    }
                }
                // 调用回调方法
                Thread th = new Thread(() -> {
                    callback.run(new SendDataResult(true));
                });
                th.start();
            }, waitTime, 1);
        }
        catch (Exception ex){
            // 调用回调方法
            Thread th = new Thread(() -> {
                callback.run(new SendDataResult(false));
            });
            th.start();
        }

    }

    /**
     * 发送数据
     *
     * @author huangyajun
     * @date 2022/6/29 17:06
     */
    @Override
    public void sendData(byte[] sendData, DeviceModel deviceModel) {
        sendData(sendData, deviceModel, -1, (res) -> {
        });
    }

    /**
     * 解析主动回传的数据
     *
     * @author huangyajun
     * @date 2022/5/23 14:33
     */
    @Override
    public void passiveReceiveData(byte[] data, DeviceModel deviceModel) {

        if (data.length < 1) {
            return;
        }

        for (int i = 0; i < data.length; i++) {
            activeByteDataBuffer.add(data[i]);
        }

        while (activeByteDataBuffer.size() > 0 && activeByteDataBuffer.get(0) != 0x55) {
            activeByteDataBuffer.remove(0);
        }

        while (activeByteDataBuffer.size() > 11) {
            activeByteTemp = new ArrayList<>(activeByteDataBuffer.subList(0, 11));
            activeByteDataBuffer = new ArrayList<>(activeByteDataBuffer.subList(11, activeByteDataBuffer.size()));


            if (WitProtocolUtils.checkSum(activeByteTemp)) {
                short[] Pack = new short[4];

                Pack[0] = (short) ((((short) activeByteTemp.get(3)) << 8) | ((short) activeByteTemp.get(2) & 0xff));
                Pack[1] = (short) ((((short) activeByteTemp.get(5)) << 8) | ((short) activeByteTemp.get(4) & 0xff));
                Pack[2] = (short) ((((short) activeByteTemp.get(7)) << 8) | ((short) activeByteTemp.get(6) & 0xff));
                Pack[3] = (short) ((((short) activeByteTemp.get(9)) << 8) | ((short) activeByteTemp.get(8) & 0xff));

                String Identify = Integer.toHexString(activeByteTemp.get(1));
                Identify = StringUtils.padLeft(Identify, 2, '0');
                deviceModel.setDeviceData(new ShortKey(Identify + "_0"), Pack[0]);
                deviceModel.setDeviceData(new ShortKey(Identify + "_1"), Pack[1]);
                deviceModel.setDeviceData(new ShortKey(Identify + "_2"), Pack[2]);
                deviceModel.setDeviceData(new ShortKey(Identify + "_3"), Pack[3]);
            } else {
                // Log.i("", "校验位不通过" + ByteArrayConvert.ByteArrayToHexString(activeByteTemp));
            }
        }
    }
}

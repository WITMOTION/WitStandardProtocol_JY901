package com.wit.example.spp.components;

import com.wit.example.spp.data.WitSensorKey;
import com.wit.witsdk.sensor.dkey.ShortKey;
import com.wit.witsdk.sensor.modular.device.DeviceModel;
import com.wit.witsdk.sensor.modular.processor.interfaces.IDataProcessor;
import com.wit.witsdk.sensor.utils.DipSensorMagHelper;
import com.wit.witsdk.utils.BitConvert;
import com.wit.witsdk.utils.NumberFormat;
import com.wit.witsdk.utils.StringUtils;

/**
 * 数据解析
 *
 * @Author haungyajun
 * @Date 2022/4/26 15:25
 */
public class JY901DataProcessor implements IDataProcessor {

    @Override
    public void OnOpen(DeviceModel deviceModel) {

        Thread th = new Thread(() -> {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < 10; i++) {

                String magType = deviceModel.getDeviceData("72");// 磁场类型
                if (StringUtils.IsNullOrEmpty(magType)) {
                    // 读取72磁场类型寄存器,后面解析磁场的时候要用到
                    sendProtocolData(deviceModel, new byte[]{(byte) 0xff, (byte) 0xaa, 0x27, 0x72, 0x00}, 100);
                }

                String reg2e = deviceModel.getDeviceData("2E");// 版本号
                String reg2f = deviceModel.getDeviceData("2F");// 版本号
                if (StringUtils.IsNullOrEmpty(reg2e) || StringUtils.IsNullOrEmpty(reg2f)) {
                    // 读版本号
                    sendProtocolData(deviceModel, new byte[]{(byte) 0xff, (byte) 0xaa, 0x27, 0x2E, 0x00}, 100);
                }

                try {
                    // 防止读取速度太快
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();

    }

    /**
     * 发送协议数据
     *
     * @author huangyajun
     * @date 2023/2/27 19:23
     */
    private void sendProtocolData(DeviceModel deviceModel, byte[] bytes, int delay) {
        deviceModel.sendProtocolData(bytes, delay);// 磁场
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return;
        }
    }

    @Override
    public void OnClose() {

    }

    /**
     * 解算数据
     *
     * @author huangyajun
     * @date 2022/4/27 14:39
     */
    @Override
    public void OnUpdate(DeviceModel deviceModel) {

        // 版本号
        String reg2e = deviceModel.getDeviceData("2E");// 版本号
        String reg2f = deviceModel.getDeviceData("2F");// 版本号

        // 如果有版本号
        if (StringUtils.IsNullOrEmpty(reg2e) == false &&
                StringUtils.IsNullOrEmpty(reg2f) == false) {
            short reg2eValue = Short.parseShort(reg2e);
            short reg2fValue = Short.parseShort(reg2f);

            int tempVerSion = BitConvert.byte2Int(new byte[]{
                    BitConvert.short2byte(reg2fValue)[0],
                    BitConvert.short2byte(reg2fValue)[1],
                    BitConvert.short2byte(reg2eValue)[0],
                    BitConvert.short2byte(reg2eValue)[1]
            });

            // UInt32 tempVerSion = BitConverter.ToUInt32(Buffer, 2);
            String sbinary = Integer.toBinaryString(tempVerSion);// Convert.ToString(tempVerSion, 2);
            sbinary = StringUtils.padLeft(sbinary, 32, '0');
            if (sbinary.substring(0, 1).equals("1"))//新版本号
            {
                String tempNewVS = Integer.parseInt(sbinary.substring(2, 18), 2) + "";
                tempNewVS += "." + Integer.parseInt(sbinary.substring(19, 19 + 5), 2);
                tempNewVS += "." + Integer.parseInt(sbinary.substring(25), 2);
                //Public.Common.Version_Number = tempNewVS;
                deviceModel.setDeviceData(WitSensorKey.VersionNumber, tempNewVS);
            } else {
                int tempNewVS = BitConvert.byte2Int(new byte[]{
                        0,
                        0,
                        BitConvert.short2byte(reg2eValue)[0],
                        BitConvert.short2byte(reg2eValue)[1]
                });
                deviceModel.setDeviceData(WitSensorKey.VersionNumber, tempNewVS + "");
            }
        }

        // 51_0代表传感器回传的51包数据的第一个数据,51_2代表第二个,其它的同理
        String reg50_0 = deviceModel.getDeviceData("50_0");// 年月
        String reg50_1 = deviceModel.getDeviceData("50_1");// 日时
        String reg50_2 = deviceModel.getDeviceData("50_2");// 分秒
        String reg50_3 = deviceModel.getDeviceData("50_3");// 毫秒
        // 如果回传了时间数据包就解算时间
        if (!StringUtils.IsNullOrEmpty(reg50_0) &&
                !StringUtils.IsNullOrEmpty(reg50_1) &&
                !StringUtils.IsNullOrEmpty(reg50_2) &&
                !StringUtils.IsNullOrEmpty(reg50_3)
        ) {
            // 解算数据,并且保存到设备数据里
            String yy = 2000 + (byte) Integer.parseInt(reg50_0) + "";
            String MM = (byte) (Integer.parseInt(reg50_0) >> 8) + "";
            String dd = (byte) Integer.parseInt(reg50_1) + "";
            String hh = (byte) (Integer.parseInt(reg50_1) >> 8) + "";
            String mm = (byte) Integer.parseInt(reg50_2) + "";

            String ss = (byte) (Integer.parseInt(reg50_2) >> 8) + "";
            String ms = Integer.parseInt(reg50_3) + "";

            deviceModel.setDeviceData(WitSensorKey.ChipTime, String.format("%s-%s-%s %s:%s:%s.%s", yy, mm, dd, hh, mm, ss, ms));
        }


        Short reg51_0 = deviceModel.getDeviceData(new ShortKey("51_0"));// 加速度X
        Short reg51_1 = deviceModel.getDeviceData(new ShortKey("51_1"));// 加速度X
        Short reg51_2 = deviceModel.getDeviceData(new ShortKey("51_2"));// 加速度X
        // 如果回传了加速度数据包
        if (reg51_0 != null &&
                reg51_1 != null &&
                reg51_2 != null
        ) {
            // 解算数据,并且保存到设备数据里
            deviceModel.setDeviceData(WitSensorKey.AccX, round(reg51_0 / 32768.0 * 16, 3));
            deviceModel.setDeviceData(WitSensorKey.AccY, round(reg51_1 / 32768.0 * 16, 3));
            deviceModel.setDeviceData(WitSensorKey.AccZ, round(reg51_2 / 32768.0 * 16, 3));
        }

        Short reg52_0 = deviceModel.getDeviceData(new ShortKey("52_0"));// 角速度X
        Short reg52_1 = deviceModel.getDeviceData(new ShortKey("52_1"));// 角速度Y
        Short reg52_2 = deviceModel.getDeviceData(new ShortKey("52_2"));// 角速度Z
        // 如果回传了角速度数据包
        if (reg52_0 != null &&
                reg52_1 != null &&
                reg52_2 != null
        ) {
            // 解算数据,并且保存到设备数据里
            deviceModel.setDeviceData(WitSensorKey.AsX, round(reg52_0 / 32768.0 * 2000, 3));
            deviceModel.setDeviceData(WitSensorKey.AsY, round(reg52_1 / 32768.0 * 2000, 3));
            deviceModel.setDeviceData(WitSensorKey.AsZ, round(reg52_2 / 32768.0 * 2000, 3));
        }

        Short reg53_0 = deviceModel.getDeviceData(new ShortKey("53_0"));// 角度X
        Short reg53_1 = deviceModel.getDeviceData(new ShortKey("53_1"));// 角度Y
        Short reg53_2 = deviceModel.getDeviceData(new ShortKey("53_2"));// 角度Z
        // 如果回传了角度数据包
        if (reg53_0 != null &&
                reg53_1 != null &&
                reg53_2 != null
        ) {
            // 解算数据,并且保存到设备数据里
            deviceModel.setDeviceData(WitSensorKey.AngleX, round(reg53_0 / 32768.0 * 180, 3));
            deviceModel.setDeviceData(WitSensorKey.AngleY, round(reg53_1 / 32768.0 * 180, 3));
            deviceModel.setDeviceData(WitSensorKey.AngleZ, round(reg53_2 / 32768.0 * 180, 3));
        }

        Short reg54_0 = deviceModel.getDeviceData(new ShortKey("54_0"));// 磁场X
        Short reg54_1 = deviceModel.getDeviceData(new ShortKey("54_1"));// 磁场Y
        Short reg54_2 = deviceModel.getDeviceData(new ShortKey("54_2"));// 磁场Z
        Short magType = deviceModel.getDeviceData(new ShortKey("72"));// 磁场类型
        // 如果回传了磁场数据包
        if (reg54_0 != null &&
                reg54_1 != null &&
                reg54_2 != null &&
                magType != null
        ) {
            // 解算数据,并且保存到设备数据里
            deviceModel.setDeviceData(WitSensorKey.HX, DipSensorMagHelper.GetMagToUt(magType, reg54_0));
            deviceModel.setDeviceData(WitSensorKey.HY, DipSensorMagHelper.GetMagToUt(magType, reg54_1));
            deviceModel.setDeviceData(WitSensorKey.HZ, DipSensorMagHelper.GetMagToUt(magType, reg54_2));
        }

        Short reg54_3 = deviceModel.getDeviceData(new ShortKey("54_3"));// 温度
        // 如果回传了温度数据包
        if (reg54_3 != null) {
            deviceModel.setDeviceData(WitSensorKey.T, round(reg54_3 / 100.0, 2));
        }

        Short reg55_0 = deviceModel.getDeviceData(new ShortKey("55_0"));// 端口0
        Short reg55_1 = deviceModel.getDeviceData(new ShortKey("55_1"));// 端口1
        Short reg55_2 = deviceModel.getDeviceData(new ShortKey("55_2"));// 端口2
        Short reg55_3 = deviceModel.getDeviceData(new ShortKey("55_3"));// 端口3
        // 如果回传了端口状态数据包
        if (reg55_0 != null &&
                reg55_1 != null &&
                reg55_2 != null &&
                reg55_3 != null
        ) {
            deviceModel.setDeviceData(WitSensorKey.D0, reg55_0);
            deviceModel.setDeviceData(WitSensorKey.D1, reg55_1);
            deviceModel.setDeviceData(WitSensorKey.D2, reg55_2);
            deviceModel.setDeviceData(WitSensorKey.D3, reg55_3);
        }

        Short reg59_0 = deviceModel.getDeviceData(new ShortKey("59_0"));// GPS高度
        Short reg59_1 = deviceModel.getDeviceData(new ShortKey("59_1"));// GPS航向
        Short reg59_2 = deviceModel.getDeviceData(new ShortKey("59_2"));// 地速低位
        Short reg59_3 = deviceModel.getDeviceData(new ShortKey("59_3"));// 地速高位
        // 如果有四元数输出
        if (reg59_0 != null &&
                reg59_1 != null &&
                reg59_2 != null &&
                reg59_3 != null
        ) {
            deviceModel.setDeviceData(WitSensorKey.Q0, round(reg59_0 / 32768.0, 5));
            deviceModel.setDeviceData(WitSensorKey.Q1, round(reg59_1 / 32768.0, 5));
            deviceModel.setDeviceData(WitSensorKey.Q2, round(reg59_2 / 32768.0, 5));
            deviceModel.setDeviceData(WitSensorKey.Q3, round(reg59_3 / 32768.0, 5));
        }

        Short reg5A_0 = deviceModel.getDeviceData(new ShortKey("5A_0"));// GPS高度
        Short reg5A_1 = deviceModel.getDeviceData(new ShortKey("5A_1"));// GPS航向
        Short reg5A_2 = deviceModel.getDeviceData(new ShortKey("5A_2"));// 地速低位
        Short reg5A_3 = deviceModel.getDeviceData(new ShortKey("5A_3"));// 地速高位
        // 如果有位置精度输出
        if (reg5A_0 != null &&
                reg5A_1 != null &&
                reg5A_2 != null &&
                reg5A_3 != null
        ) {
            deviceModel.setDeviceData(WitSensorKey.SN, reg5A_0);
            deviceModel.setDeviceData(WitSensorKey.PDOP, reg5A_1 / 100.0);
            deviceModel.setDeviceData(WitSensorKey.HDOP, reg5A_2 / 100.0);
            deviceModel.setDeviceData(WitSensorKey.VDOP, reg5A_3 / 100.0);
        }
    }

    /**
     * 保留小数
     */
    public double round(double value, int b) {
        double q = Math.pow(10, b);
        if (b == 0) {
            Math.round(value * q);
        }
        long round = Math.round(value * q);
        double v = round / q;
        return v;
    }
}

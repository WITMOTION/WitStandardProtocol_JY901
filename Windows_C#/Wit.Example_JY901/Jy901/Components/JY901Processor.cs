using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Wit.SDK.Device.Device.Device.DKey;
using Wit.SDK.Modular.Sensor.Device;
using Wit.SDK.Modular.Sensor.Modular.DataProcessor.Constant;
using Wit.SDK.Modular.Sensor.Modular.DataProcessor.Interface;
using Wit.SDK.Modular.Sensor.Modular.DataProcessor.Utils;
using Wit.SDK.Modular.Sensor.Utils;
using Wit.SDK.Utils;

namespace Wit.SDK.Modular.Sensor.Modular.DataProcessor.Role
{
    /// <summary>
    /// JY901S数据处理器
    /// </summary>
    public class JY901Processor : IDataProcessor
    {

        /// <summary>
        /// 记录key值切换器
        /// </summary>
        private RecordKeySwitch RecordKeySwitch = new RecordKeySwitch();

        /// <summary>
        /// 数据刷新的key值
        /// </summary>
        List<string> UpdateKeys = new List<string>() { "50_0", "51_0", "52_0", "53_0", "54_0", "55_0", "56_0", "57_0", "58_0", "59_0", "5A_0" };

        /// <summary>
        /// 设备
        /// </summary>
        public DeviceModel DeviceModel { get; private set; }

        /// <summary>
        /// 当传感器打开时
        /// </summary>
        /// <param name="deviceModel"></param>
        public override void OnOpen(DeviceModel deviceModel)
        {
            // 传入刷新数据的key值
            RecordKeySwitch.Open(deviceModel, UpdateKeys);

            this.DeviceModel = deviceModel;
            Thread thread = new Thread(ReadDataThread) { IsBackground = true };
            thread.Start();
        }

        /// <summary>
        /// 读取数据线程
        /// </summary>
        private void ReadDataThread()
        {
            for (int i = 0; i < 10; i++)
            {
                ReadMagType(DeviceModel);
                ReadVersionNumberReg(DeviceModel);
                ReadSerialNumberReg(DeviceModel);
            }
        }

        /// <summary>
        /// 当传感器关闭时
        /// </summary>
        public override void OnClose()
        {
            // 关闭key值切换器
            RecordKeySwitch.Close();
        }

        /// <summary>
        /// 当触发数据更新时
        /// </summary>
        /// <param name="deviceModel"></param>
        public override void OnUpdate(DeviceModel deviceModel)
        {
            // 版本号
            var reg2e = deviceModel.GetDeviceData("2E");// 版本号
            var reg2f = deviceModel.GetDeviceData("2F");// 版本号

            // 如果有版本号
            if (string.IsNullOrEmpty(reg2e) == false &&
                string.IsNullOrEmpty(reg2f) == false)
            {
                var reg2eValue = (ushort)short.Parse(reg2e);
                var vbytes = BitConverter.GetBytes((ushort)short.Parse(reg2e)).Concat(BitConverter.GetBytes((ushort)short.Parse(reg2f))).ToArray();
                UInt32 tempVerSion = BitConverter.ToUInt32(vbytes, 0);
                string sbinary = Convert.ToString(tempVerSion, 2);
                sbinary = ("").PadLeft((32 - sbinary.Length), '0') + sbinary;
                if (sbinary.StartsWith("1"))//新版本号
                {
                    string tempNewVS = Convert.ToUInt32(sbinary.Substring(4 - 3, 14 + 3), 2).ToString();
                    tempNewVS += "." + Convert.ToUInt32(sbinary.Substring(18, 6), 2);
                    tempNewVS += "." + Convert.ToUInt32(sbinary.Substring(24), 2);
                    deviceModel.SetDeviceData(WitSensorKey.VersionNumber, tempNewVS);
                }
                else
                {
                    deviceModel.SetDeviceData(WitSensorKey.VersionNumber, reg2eValue.ToString());
                }
            }

            // 序列号
            var reg7f = deviceModel.GetDeviceData("7F");// 序列号
            var reg80 = deviceModel.GetDeviceData("80");// 序列号
            var reg81 = deviceModel.GetDeviceData("81");// 序列号
            var reg82 = deviceModel.GetDeviceData("82");// 序列号
            var reg83 = deviceModel.GetDeviceData("83");// 序列号
            var reg84 = deviceModel.GetDeviceData("84");// 序列号
            if (string.IsNullOrEmpty(reg7f) == false &&
                string.IsNullOrEmpty(reg80) == false &&
                string.IsNullOrEmpty(reg81) == false &&
                string.IsNullOrEmpty(reg82) == false &&
                string.IsNullOrEmpty(reg83) == false &&
                string.IsNullOrEmpty(reg84) == false)
            {
                var sbytes = BitConverter.GetBytes(short.Parse(reg7f))
                    .Concat(BitConverter.GetBytes(short.Parse(reg80)))
                    .Concat(BitConverter.GetBytes(short.Parse(reg81)))
                    .Concat(BitConverter.GetBytes(short.Parse(reg82)))
                    .Concat(BitConverter.GetBytes(short.Parse(reg83)))
                    .Concat(BitConverter.GetBytes(short.Parse(reg84)))
                    .ToArray();
                string sn = Encoding.Default.GetString(sbytes);
                deviceModel.SetDeviceData(WitSensorKey.SerialNumber, sn);
            }

            // 51_0代表传感器回传的51包数据的第一个数据,51_2代表第二个,其它的同理
            var reg50_0 = deviceModel.GetDeviceData("50_0");// 年月
            var reg50_1 = deviceModel.GetDeviceData("50_1");// 日时
            var reg50_2 = deviceModel.GetDeviceData("50_2");// 分秒
            var reg50_3 = deviceModel.GetDeviceData("50_3");// 毫秒
            // 如果回传了时间数据包就解算时间
            if (!string.IsNullOrEmpty(reg50_0) &&
                !string.IsNullOrEmpty(reg50_1) &&
                !string.IsNullOrEmpty(reg50_2) &&
                !string.IsNullOrEmpty(reg50_3)
                )
            {
                // 解算数据,并且保存到设备数据里
                var yy = 2000 + (byte)int.Parse(reg50_0);
                var MM = (byte)(int.Parse(reg50_0) >> 8);
                var dd = (byte)int.Parse(reg50_1);
                var hh = (byte)(int.Parse(reg50_1) >> 8);
                var mm = (byte)int.Parse(reg50_2);

                var ss = (byte)(int.Parse(reg50_2) >> 8);
                var ms = int.Parse(reg50_3).ToString("000");


                deviceModel.SetDeviceData(WitSensorKey.ChipTime, $"{yy}-{MM}-{dd} {hh}:{mm}:{ss}.{ms}");
            }


            var reg51_0 = deviceModel.GetDeviceData("51_0");// 加速度X
            var reg51_1 = deviceModel.GetDeviceData("51_1");// 加速度X
            var reg51_2 = deviceModel.GetDeviceData("51_2");// 加速度X
            // 如果回传了加速度数据包
            if (!string.IsNullOrEmpty(reg51_0) &&
                !string.IsNullOrEmpty(reg51_1) &&
                !string.IsNullOrEmpty(reg51_2)
                )
            {
                // 解算数据,并且保存到设备数据里
                deviceModel.SetDeviceData(WitSensorKey.AccX, Math.Round((double.Parse(reg51_0) / 32768.0 * 16),3));
                deviceModel.SetDeviceData(WitSensorKey.AccY, Math.Round((double.Parse(reg51_1) / 32768.0 * 16),3));
                deviceModel.SetDeviceData(WitSensorKey.AccZ, Math.Round((double.Parse(reg51_2) / 32768.0 * 16),3));
            }

            var reg51_3 = deviceModel.GetDeviceData("51_3");// 温度
            // 如果回传了温度数据包
            if (!string.IsNullOrEmpty(reg51_3))
            {
                deviceModel.SetDeviceData(WitSensorKey.T, Math.Round((double.Parse(reg51_3) / 100.0),3));
            }

            var reg52_0 = deviceModel.GetDeviceData("52_0");// 角速度X
            var reg52_1 = deviceModel.GetDeviceData("52_1");// 角速度Y
            var reg52_2 = deviceModel.GetDeviceData("52_2");// 角速度Z
            // 如果回传了角速度数据包
            if (!string.IsNullOrEmpty(reg52_0) &&
                !string.IsNullOrEmpty(reg52_1) &&
                !string.IsNullOrEmpty(reg52_2)
                )
            {
                // 解算数据,并且保存到设备数据里
                deviceModel.SetDeviceData(WitSensorKey.AsX, Math.Round((double.Parse(reg52_0) / 32768.0 * 2000),3));
                deviceModel.SetDeviceData(WitSensorKey.AsY, Math.Round((double.Parse(reg52_1) / 32768.0 * 2000),3));
                deviceModel.SetDeviceData(WitSensorKey.AsZ, Math.Round((double.Parse(reg52_2) / 32768.0 * 2000),3));
            }

            var reg53_0 = deviceModel.GetDeviceData("53_0");// 角度X 
            var reg53_1 = deviceModel.GetDeviceData("53_1");// 角度Y
            var reg53_2 = deviceModel.GetDeviceData("53_2");// 角度Z
            // 如果回传了角度数据包
            if (!string.IsNullOrEmpty(reg53_0) &&
                !string.IsNullOrEmpty(reg53_1) &&
                !string.IsNullOrEmpty(reg53_2)
                )
            {
                // 解算数据,并且保存到设备数据里
                deviceModel.SetDeviceData(WitSensorKey.AngleX, Math.Round((double.Parse(reg53_0) / 32768.0 * 180),3));
                deviceModel.SetDeviceData(WitSensorKey.AngleY, Math.Round((double.Parse(reg53_1) / 32768.0 * 180),3));
                deviceModel.SetDeviceData(WitSensorKey.AngleZ, Math.Round((double.Parse(reg53_2) / 32768.0 * 180),3));
            }

            var reg54_0 = deviceModel.GetDeviceData("54_0");// 磁场X
            var reg54_1 = deviceModel.GetDeviceData("54_1");// 磁场Y
            var reg54_2 = deviceModel.GetDeviceData("54_2");// 磁场Z
            var magType = deviceModel.GetDeviceData("72");// 磁场类型
            
            // 如果回传了磁场数据包
            if (!string.IsNullOrEmpty(reg54_0) &&
                !string.IsNullOrEmpty(reg54_1) &&
                !string.IsNullOrEmpty(reg54_2) &&
                !string.IsNullOrEmpty(magType)
                )
            {
                short type = short.Parse(magType);
                // 解算数据,并且保存到设备数据里 
                deviceModel.SetDeviceData(WitSensorKey.HX, DipSensorMagHelper.GetMagToUt(type, double.Parse(reg54_0)));
                deviceModel.SetDeviceData(WitSensorKey.HY, DipSensorMagHelper.GetMagToUt(type, double.Parse(reg54_1)));
                deviceModel.SetDeviceData(WitSensorKey.HZ, DipSensorMagHelper.GetMagToUt(type, double.Parse(reg54_2)));
                deviceModel.SetDeviceData(WitSensorKey.HM, Math.Round(Math.Sqrt(Math.Pow(DipSensorMagHelper.GetMagToUt(type, double.Parse(reg54_0)), 2) + 
                                                           Math.Pow(DipSensorMagHelper.GetMagToUt(type, double.Parse(reg54_1)), 2) + 
                                                           Math.Pow(DipSensorMagHelper.GetMagToUt(type, double.Parse(reg54_2)), 2)), 2));
            }


            var reg55_0 = deviceModel.GetDeviceData(new ShortKey("55_0"));// 端口0
            var reg55_1 = deviceModel.GetDeviceData(new ShortKey("55_1"));// 端口1
            var reg55_2 = deviceModel.GetDeviceData(new ShortKey("55_2"));// 端口2
            var reg55_3 = deviceModel.GetDeviceData(new ShortKey("55_3"));// 端口3
            // 如果回传了端口状态数据包
            if (reg55_0!=null &&
                reg55_1!=null &&
                reg55_2!=null &&
                reg55_3!=null
                )
            {
                deviceModel.SetDeviceData(WitSensorKey.D0, (short)reg55_0);
                deviceModel.SetDeviceData(WitSensorKey.D1, (short)reg55_1);
                deviceModel.SetDeviceData(WitSensorKey.D2, (short)reg55_2);
                deviceModel.SetDeviceData(WitSensorKey.D3, (short)reg55_3);
            }

            var reg56_0 = deviceModel.GetDeviceData("56_0");// 气压低位
            var reg56_1 = deviceModel.GetDeviceData("56_1");// 气压高位
            var reg56_2 = deviceModel.GetDeviceData("56_2");// 高度低位
            var reg56_3 = deviceModel.GetDeviceData("56_3");// 高度高位
            // 如果回传了气压高度数据包
            if (!string.IsNullOrEmpty(reg56_0) &&
                !string.IsNullOrEmpty(reg56_1) &&
                !string.IsNullOrEmpty(reg56_2) &&
                !string.IsNullOrEmpty(reg56_3)
                ) {

                var pbytes =  BitConverter.GetBytes(short.Parse(reg56_0)).Concat(BitConverter.GetBytes(short.Parse(reg56_1))).ToArray();
                var hbytes = BitConverter.GetBytes(short.Parse(reg56_2)).Concat(BitConverter.GetBytes(short.Parse(reg56_3))).ToArray();
                deviceModel.SetDeviceData(WitSensorKey.P, BitConverter.ToInt32(pbytes, 0));
                deviceModel.SetDeviceData(WitSensorKey.H, (BitConverter.ToInt32(hbytes, 0) / 100.0));
            }

            var reg57_0 = deviceModel.GetDeviceData("57_0");// 经度低位
            var reg57_1 = deviceModel.GetDeviceData("57_1");// 经度高位
            var reg57_2 = deviceModel.GetDeviceData("57_2");// 纬度低位
            var reg57_3 = deviceModel.GetDeviceData("57_3");// 纬度高位
            // 如果有经纬度输出
            if (!string.IsNullOrEmpty(reg57_0) &&
                !string.IsNullOrEmpty(reg57_1) &&
                !string.IsNullOrEmpty(reg57_2) &&
                !string.IsNullOrEmpty(reg57_3)
                )
            {
                var lonbytes = BitConverter.GetBytes(short.Parse(reg57_0)).Concat(BitConverter.GetBytes(short.Parse(reg57_1))).ToArray();
                var lon = BitConverter.ToInt32(lonbytes, 0);
                var latbytes = BitConverter.GetBytes(short.Parse(reg57_2)).Concat(BitConverter.GetBytes(short.Parse(reg57_3))).ToArray();
                var lat = BitConverter.ToInt32(latbytes, 0);

                double lon_D = GpsUtils.DmsToD((double)lon / 100000.0);
                deviceModel.SetDeviceData(WitSensorKey.Lon, $"{lon_D}°");
                deviceModel.SetDeviceData(WitSensorKey.LonDeg, Math.Round(lon_D,6));
                double lat_D = GpsUtils.DmsToD((double)lat / 100000.0);
                deviceModel.SetDeviceData(WitSensorKey.Lat, $"{lat_D}°");
                deviceModel.SetDeviceData(WitSensorKey.LatDeg, Math.Round(lat_D,6));
            }

            var reg58_0 = deviceModel.GetDeviceData("58_0");// GPS高度
            var reg58_1 = deviceModel.GetDeviceData("58_1");// GPS航向
            var reg58_2 = deviceModel.GetDeviceData("58_2");// 地速低位
            var reg58_3 = deviceModel.GetDeviceData("58_3");// 地速高位
            // 如果有地速包输出
            if (!string.IsNullOrEmpty(reg58_0) &&
                !string.IsNullOrEmpty(reg58_1) &&
                !string.IsNullOrEmpty(reg58_2) &&
                !string.IsNullOrEmpty(reg58_3)
                ) {
                var svbytes = BitConverter.GetBytes(short.Parse(reg58_2)).Concat(BitConverter.GetBytes(short.Parse(reg58_3))).ToArray();

                deviceModel.SetDeviceData(WitSensorKey.GPSHeight, Math.Round(short.Parse(reg58_0)/ 10.0,3));
                byte[] temp = BitConverter.GetBytes(short.Parse(reg58_1));    // 航向解算不要负数
                uint reg58_1Value = BitConverter.ToUInt16(temp, 0);
                deviceModel.SetDeviceData(WitSensorKey.GPSYaw, Math.Round(reg58_1Value / 100.0,3));
                deviceModel.SetDeviceData(WitSensorKey.GPSV, Math.Round(BitConverter.ToInt32(svbytes, 0) / 1e3,3));
            }

            var reg59_0 = deviceModel.GetDeviceData("59_0");// 四元数
            var reg59_1 = deviceModel.GetDeviceData("59_1");// 四元数
            var reg59_2 = deviceModel.GetDeviceData("59_2");// 四元数
            var reg59_3 = deviceModel.GetDeviceData("59_3");// 四元数
            // 如果有四元数输出
            if (!string.IsNullOrEmpty(reg59_0) &&
                !string.IsNullOrEmpty(reg59_1) &&
                !string.IsNullOrEmpty(reg59_2) &&
                !string.IsNullOrEmpty(reg59_3)
                )
            {
                deviceModel.SetDeviceData(WitSensorKey.Q0, Math.Round(short.Parse(reg59_0) / 32768.0,5));
                deviceModel.SetDeviceData(WitSensorKey.Q1, Math.Round(short.Parse(reg59_1) / 32768.0,5));
                deviceModel.SetDeviceData(WitSensorKey.Q2, Math.Round(short.Parse(reg59_2) / 32768.0,5));
                deviceModel.SetDeviceData(WitSensorKey.Q3, Math.Round(short.Parse(reg59_3) / 32768.0,5));
            }

            var reg5A_0 = deviceModel.GetDeviceData("5A_0");// 卫星数量
            var reg5A_1 = deviceModel.GetDeviceData("5A_1");// 位置定位精度
            var reg5A_2 = deviceModel.GetDeviceData("5A_2");// 水平定位精度
            var reg5A_3 = deviceModel.GetDeviceData("5A_3");// 垂直定位精度
            // 如果有位置精度输出
            if (!string.IsNullOrEmpty(reg5A_0) &&
                !string.IsNullOrEmpty(reg5A_1) &&
                !string.IsNullOrEmpty(reg5A_2) &&
                !string.IsNullOrEmpty(reg5A_3)
                )
            {
                deviceModel.SetDeviceData(WitSensorKey.SN, short.Parse(reg5A_0));
                deviceModel.SetDeviceData(WitSensorKey.PDOP, Math.Round(short.Parse(reg5A_1) / 100.0,2)); 
                deviceModel.SetDeviceData(WitSensorKey.HDOP, Math.Round(short.Parse(reg5A_2) / 100.0,2));//水平定位精度
                deviceModel.SetDeviceData(WitSensorKey.VDOP, Math.Round(short.Parse(reg5A_3) / 100.0,2));//垂直定位精度
            }

        }

        /// <summary>
        /// 读取磁场类型寄存器
        /// </summary>
        private void ReadMagType(DeviceModel deviceModel) {
            // 读磁场类型
            if (deviceModel.GetDeviceData("72") == null) {
                // 读取72磁场类型寄存器,后面解析磁场的时候要用到
                deviceModel.ReadData(new byte[] { 0xff, 0xaa, 0x27, 0x72, 0x00 });
                Thread.Sleep(20);
            }
        }

        /// <summary>
        /// 读取版本号寄存器
        /// </summary>
        /// <param name="deviceModel"></param>
        private void ReadVersionNumberReg(DeviceModel deviceModel)
        {
            // 读版本号
            if (deviceModel.GetDeviceData("2E") == null)
            {
                // 读版本号
                deviceModel.ReadData(new byte[] { 0xff, 0xaa, 0x27, 0x2E, 0x00 });
                Thread.Sleep(20);
            }
        }

        /// <summary>
        /// 读取序列号寄存器
        /// </summary>
        /// <param name="deviceModel"></param>
        private void ReadSerialNumberReg(DeviceModel deviceModel)
        {
            // 读序列号
            if (deviceModel.GetDeviceData("7F") == null && deviceModel.GetDeviceData("82") == null)
            {
                // 读序列号
                deviceModel.ReadData(new byte[] { 0xff, 0xaa, 0x27, 0x7F, 0x00 });
                deviceModel.ReadData(new byte[] { 0xff, 0xaa, 0x27, 0x7F + 3, 0x00 });
            }
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Wit.SDK.Device.Device.Device.DKey;

namespace Wit.SDK.Modular.Sensor.Modular.DataProcessor.Constant
{
    /// <summary>
    /// 倾角传感器标准key常量
    /// </summary>
    public static class WitSensorKey
    {
        // 芯片时间
        public static StringKey ChipTime { get; } = new StringKey("ChipTime");

        // 加速度X
        public static DoubleKey AccX { get; } = new DoubleKey("AccX");

        // 加速度Y
        public static DoubleKey AccY { get; } = new DoubleKey("AccY");

        // 加速度Z
        public static DoubleKey AccZ { get; } = new DoubleKey("AccZ");

        // 加速度矢量和
        public static DoubleKey AccM { get; } = new DoubleKey("AccM");

        // 角速度
        public static DoubleKey AsX { get; } = new DoubleKey("AsX");

        // 角速度
        public static DoubleKey AsY { get; } = new DoubleKey("AsY");

        // 角速度
        public static DoubleKey AsZ { get; } = new DoubleKey("AsZ");

        // 角速度Z矢量和
        public static DoubleKey AsM { get; } = new DoubleKey("AsM");

        // 角度X
        public static DoubleKey AngleX { get; } = new DoubleKey("AngleX");

        // 角度Y
        public static DoubleKey AngleY { get; } = new DoubleKey("AngleY");

        // 角度Z
        public static DoubleKey AngleZ { get; } = new DoubleKey("AngleZ");

        // 磁场X
        public static DoubleKey HX { get; } = new DoubleKey("HX");

        // 磁场Y
        public static DoubleKey HY { get; } = new DoubleKey("HY");

        // 磁场Z
        public static DoubleKey HZ { get; } = new DoubleKey("HZ");

        // 磁场矢量和
        public static DoubleKey HM { get; } = new DoubleKey("HM");

        // 温度
        public static DoubleKey T { get; } = new DoubleKey("T");

        // 扩展端口 1
        public static ShortKey D0 { get; } = new ShortKey("D0");

        // 扩展端口 2
        public static ShortKey D1 { get; } = new ShortKey("D1");

        // 扩展端口 3
        public static ShortKey D2 { get; } = new ShortKey("D2");

        // 扩展端口 4
        public static ShortKey D3 { get; } = new ShortKey("D3");

        // 气压
        public static DoubleKey P { get; } = new DoubleKey("P");

        // 高度
        public static DoubleKey H { get; } = new DoubleKey("H");

        // 经度
        public static StringKey Lon { get; } = new StringKey("Lon");

        // 经度度表现形式
        public static DoubleKey LonDeg { get; } = new DoubleKey("LonDeg");

        // 纬度
        public static StringKey Lat { get; } = new StringKey("Lat");

        // 纬度度表现形式
        public static DoubleKey LatDeg { get; } = new DoubleKey("LatDeg");

        /// <summary>
        /// GPS状态
        /// </summary>
        public static DoubleKey GPSStatus { get; } = new DoubleKey("GPSStatus");


        // GPS高度
        public static DoubleKey GPSHeight { get; } = new DoubleKey("GPSHeight");

        // GPS航向
        public static DoubleKey GPSYaw { get; } = new DoubleKey("GPSYaw");

        // GPS地速
        public static DoubleKey GPSV { get; } = new DoubleKey("GPSV");

        // 四元数0
        public static DoubleKey Q0 { get; } = new DoubleKey("Q0");

        // 四元数1
        public static DoubleKey Q1 { get; } = new DoubleKey("Q1");

        // 四元数2
        public static DoubleKey Q2 { get; } = new DoubleKey("Q2");

        // 四元数3
        public static DoubleKey Q3 { get; } = new DoubleKey("Q3");

        // 卫星数量
        public static IntKey SN { get; } = new IntKey("SN");

        // 位置定位精度
        public static DoubleKey PDOP { get; } = new DoubleKey("PDOP");

        // 水平定位精度
        public static DoubleKey HDOP { get; } = new DoubleKey("HDOP");

        // 垂直定位精度
        public static DoubleKey VDOP { get; } = new DoubleKey("VDOP");

        // 版本号
        public static StringKey VersionNumber { get; } = new StringKey("VersionNumber");

        // 序列号
        public static StringKey SerialNumber { get; } = new StringKey("SerialNumber");

        // 电量
        public static DoubleKey PowerPercent { get; } = new DoubleKey("PowerPercent");
    }
}

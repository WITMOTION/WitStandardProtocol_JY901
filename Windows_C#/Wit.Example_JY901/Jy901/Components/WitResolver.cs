using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using Wit.SDK.Device.Device.Device.DKey;
using Wit.SDK.Modular.Sensor.Device;
using Wit.SDK.Modular.Sensor.Modular.ProtocolResolver.Interface;
using Wit.SDK.Modular.Sensor.Modular.Resolver.Utils;
using Wit.SDK.Modular.Sensor.Utils;

namespace Wit.SDK.Modular.Sensor.Modular.ProtocolResolver.Roles
{

    /**
     * 维特协议解析器
     */
    public class WitResolver : IProtocolResolver
    {
        /// <summary>
        /// 接收的原始数据
        /// </summary>
        private string ActiveStringDataBuffer = "";

        /// <summary>
        /// 主动接收的byte缓存
        /// </summary>
        private List<byte> ActiveByteDataBuffer = new List<byte>();


        /// <summary>
        /// 临时Byte
        /// </summary>
        private byte[] ActiveByteTemp = new byte[1000];

        /// <summary>
        /// 解算数据锁
        /// </summary>
        private object lockobj = new object();

        /// <summary>
        /// 处理被动接收的数据
        /// </summary>
        /// <param name="data"></param>
        /// <param name="deviceModel"></param>
        public override void OnReceiveData(DeviceModel deviceModel, byte[] data)
        {
            lock (lockobj) { 
                ActiveByteDataBuffer.AddRange(data);

                while (ActiveByteDataBuffer.Count > 0 && ActiveByteDataBuffer[0] != 0x55)
                {
                    ActiveByteDataBuffer.RemoveAt(0);
                }

                while (ActiveByteDataBuffer.Count >= 11)
                {
                    ActiveByteTemp = ActiveByteDataBuffer.GetRange(0, 11).ToArray();
                    ActiveByteDataBuffer.RemoveRange(0, 11);

                    if ((ActiveByteTemp[0] + ActiveByteTemp[1] + ActiveByteTemp[2] + ActiveByteTemp[3] + ActiveByteTemp[4] + ActiveByteTemp[5] + ActiveByteTemp[6] + ActiveByteTemp[7] + ActiveByteTemp[8] + ActiveByteTemp[9] & 0xff) == ActiveByteTemp[10])
                    {
                        short[] Pack = new short[4];
                        Pack[0] = BitConverter.ToInt16(ActiveByteTemp, 2);
                        Pack[1] = BitConverter.ToInt16(ActiveByteTemp, 4);
                        Pack[2] = BitConverter.ToInt16(ActiveByteTemp, 6);
                        Pack[3] = BitConverter.ToInt16(ActiveByteTemp, 8); 
                        string Identify = ActiveByteTemp[1].ToString("X");
                        deviceModel.SetDeviceData(new ShortKey(Identify + "_0"), Pack[0]);
                        deviceModel.SetDeviceData(new ShortKey(Identify + "_1"), Pack[1]);
                        deviceModel.SetDeviceData(new ShortKey(Identify + "_2"), Pack[2]);
                        deviceModel.SetDeviceData(new ShortKey(Identify + "_3"), Pack[3]);
                    }
                }
            }
        }

        /// <summary>
        /// 发送数据
        /// </summary>
        /// <param name="outData"></param>
        /// <param name="deviceModel"></param>
        public override void OnReadData(DeviceModel deviceModel, byte[] outData, int delay = -1)
        {
            delay = AutoDelayUtils.GetAutoDelay(delay, deviceModel);
            byte[] returnData;
            deviceModel.SendData(outData, out returnData, true, delay);

            if (outData != null && outData.Length >= 5 && outData[2] == 0x27 && returnData != null && returnData.Length >= 11)
            {
                returnData = WitProtocolUtils.FindReturnData(returnData);
                if (returnData != null && returnData.Length == 11)
                {
                    int reg = outData[4] << 8 | outData[3];
                    byte[] regData = returnData.Skip(2).Take(8).ToArray();
                    for (int j = 0; regData != null && j < regData.Length - 1; j += 2)
                    {
                        string key = string.Format("{0:X2}", reg++);
                        short value = (short)(regData[j + 1] << 8 | regData[j]);
                        deviceModel.SetDeviceData(new ShortKey(key), value);
                    }
                }
            }

        }

    }
}

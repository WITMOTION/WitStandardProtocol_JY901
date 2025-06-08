import lib.device_model as deviceModel
from lib.data_processor.roles.jy901s_dataProcessor import JY901SDataProcessor
from lib.data_processor.roles.jy901s_dataProcessor import JY901SDataProcessor
from lib.protocol_resolver.roles.wit_protocol_resolver import WitProtocolResolver
import time
import threading
import datetime
import argparse
import platform
import asyncio

from scipy.spatial.transform import Rotation as R
import numpy as np


def parse_args():
  args = argparse.ArgumentParser()
  args.add_argument("--port", type=str, default="/dev/ttyUSB0", help="Serial port")
  args.add_argument("--baudrate", type=int, default=(115200*2), help="Baud rate")
  return args.parse_args()

def normalize(v):
    return v / np.linalg.norm(v)

def readConfig(device):
    """
    读取配置信息示例    Example of reading configuration information
    :param device: 设备模型 Device model
    :return:
    """
    tVals = device.readReg(0x02,3)  #读取数据内容、回传速率、通讯速率   Read data content, return rate, communication rate
    if (len(tVals)>0):
        print("返回结果：" + str(tVals))
    else:
        print("无返回")
    tVals = device.readReg(0x23,2)  #读取安装方向、算法  Read the installation direction and algorithm
    if (len(tVals)>0):
        print("返回结果：" + str(tVals))
    else:
        print("无返回")

def setConfig(device):
    """
    设置配置信息示例    Example setting configuration information
    :param device: 设备模型 Device model
    :return:
    """
    device.unlock()                # 解锁 unlock
    time.sleep(0.1)                # 休眠100毫秒    Sleep 100ms
    device.writeReg(0x03, 6)       # 设置回传速率为10HZ    Set the transmission back rate to 10HZ
    time.sleep(0.1)                # 休眠100毫秒    Sleep 100ms
    device.writeReg(0x23, 0)       # 设置安装方向:水平、垂直   Set the installation direction: horizontal and vertical
    time.sleep(0.1)                # 休眠100毫秒    Sleep 100ms
    device.writeReg(0x24, 0)       # 设置安装方向:九轴、六轴   Set the installation direction: nine axis, six axis
    time.sleep(0.1)                # 休眠100毫秒    Sleep 100ms
    device.save()                  # 保存 Save

def AccelerationCalibration(device):
    """
    加计校准    Acceleration calibration
    :param device: 设备模型 Device model
    :return:
    """
    device.AccelerationCalibration()                 # Acceleration calibration
    print("加计校准结束")

def FiledCalibration(device):
    """
    磁场校准    Magnetic field calibration
    :param device: 设备模型 Device model
    :return:
    """
    device.BeginFiledCalibration()                   # 开始磁场校准   Starting field calibration
    if input("请分别绕XYZ轴慢速转动一圈，三轴转圈完成后，结束校准（Y/N)？").lower()=="y":
        device.EndFiledCalibration()                 # 结束磁场校准   End field calibration
        print("结束磁场校准")

G_WORLD = np.array([0, 0, -1])
G_PRINT_CTRL = 50
def onUpdate(deviceModel):
    """
    数据更新事件  Data update event
    :param deviceModel: 设备模型    Device model
    :return:
    """
    acc = (deviceModel.getDeviceData("accX"), deviceModel.getDeviceData("accY"), deviceModel.getDeviceData("accZ"))
    acc = np.array(acc)
    g_measured = normalize(acc)
    rot = R.align_vectors([g_measured], [G_WORLD])[0]
    euler_angles = rot.as_euler('zxy', degrees=True)
    global G_PRINT_CTRL
    if G_PRINT_CTRL == 50:
        print("Euler angles (zxy):", euler_angles)
        G_PRINT_CTRL = 0
    G_PRINT_CTRL += 1
    # print(euler_angles)
    
    # print("芯片时间:" + str(deviceModel.getDeviceData("Chiptime"))
    #      , " 温度:" + str(deviceModel.getDeviceData("temperature"))
    #      , " 加速度：" + str(deviceModel.getDeviceData("accX")) +","+  str(deviceModel.getDeviceData("accY")) +","+ str(deviceModel.getDeviceData("accZ"))
    #      ,  " 角速度:" + str(deviceModel.getDeviceData("gyroX")) +","+ str(deviceModel.getDeviceData("gyroY")) +","+ str(deviceModel.getDeviceData("gyroZ"))
    #      , " 角度:" + str(deviceModel.getDeviceData("angleX")) +","+ str(deviceModel.getDeviceData("angleY")) +","+ str(deviceModel.getDeviceData("angleZ"))
    #     , " 磁场:" + str(deviceModel.getDeviceData("magX")) +","+ str(deviceModel.getDeviceData("magY"))+","+ str(deviceModel.getDeviceData("magZ"))
    #     , " 经度:" + str(deviceModel.getDeviceData("lon")) + " 纬度:" + str(deviceModel.getDeviceData("lat"))
    #     , " 航向角:" + str(deviceModel.getDeviceData("Yaw")) + " 地速:" + str(deviceModel.getDeviceData("Speed"))
    #      , " 四元素:" + str(deviceModel.getDeviceData("q1")) + "," + str(deviceModel.getDeviceData("q2")) + "," + str(deviceModel.getDeviceData("q3"))+ "," + str(deviceModel.getDeviceData("q4"))
    #       )
    

def startRecord():
    """
    开始记录数据  Start recording data
    :return:
    """
    global _writeF
    global _IsWriteF
    _writeF = open(str(datetime.datetime.now().strftime('%Y%m%d%H%M%S')) + ".txt", "w")    #新建一个文件
    _IsWriteF = True                                                                        #标记写入标识
    Tempstr = "Chiptime"
    Tempstr +=  "\tax(g)\tay(g)\taz(g)"
    Tempstr += "\twx(deg/s)\twy(deg/s)\twz(deg/s)"
    Tempstr += "\tAngleX(deg)\tAngleY(deg)\tAngleZ(deg)"
    Tempstr += "\tT(°)"
    Tempstr += "\tmagx\tmagy\tmagz"
    Tempstr += "\tlon\tlat"
    Tempstr += "\tYaw\tSpeed"
    Tempstr += "\tq1\tq2\tq3\tq4"
    Tempstr += "\r\n"
    _writeF.write(Tempstr)
    print("开始记录数据")

def endRecord():
    """
    结束记录数据  End record data
    :return:
    """
    global _writeF
    global _IsWriteF
    _IsWriteF = False             # 标记不可写入标识    Tag cannot write the identity
    _writeF.close()               #关闭文件 Close file
    print("结束记录数据")



if __name__ == '__main__':
  args = parse_args()
  
  device = deviceModel.DeviceModel(
      "我的JY901",
      WitProtocolResolver(),
      JY901SDataProcessor(),
      "51_0"
  )                                  #设置传感器ID   Setting the Sensor ID
  device.serialConfig.portName = args.port                 #设置串口  Set serial port
  device.serialConfig.baud = args.baudrate                 #设置波特率 Set baud rate
  print(f"Open device {args.port} with {args.baudrate}")
  device.openDevice()                                      #打开串口  Open serial port
  readConfig(device)                                       #读取配置信息    Read configuration information
  device.dataProcessor.onVarChanged.append(onUpdate)       #数据更新事件    Data update event
  input()


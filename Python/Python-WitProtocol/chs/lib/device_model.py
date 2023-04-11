# coding:UTF-8
import threading
import _thread
import time
import struct
import serial
from serial import SerialException
'''
    串口配置
'''


class SerialConfig:
    # 端口
    portName = ''

    # 波特率
    baud = 9600

'''
设备模型
'''


class DeviceModel:
    # 设备名称
    deviceName = "我的设备"

    #设备ID
    ADDR = 0x50

    # 设备数据字典
    deviceData = {}

    # 是否卡开
    isOpen = False

    # 串口
    serialPort = None

    # 串口配置
    serialConfig = SerialConfig()

    # 更新触发器
    dataUpdateListener = ""

    # 数据解析器
    dataProcessor = None

    # 协议解析器
    protocolResolver = None

    def __init__(self, deviceName, protocolResolver, dataProcessor, dataUpdateListener):
        print("初始化设备模型")
        self.deviceName = deviceName
        self.protocolResolver = protocolResolver
        self.dataProcessor = dataProcessor
        self.dataUpdateListener = dataUpdateListener
        # _thread.start_new_thread(self.readDataTh, ("Data-Received-Thread", 10, ))

    def setDeviceData(self, key, value):
        """
        设置设备数据
        :param key: 数据key
        :param value: 数据值
        :return: 无返回
        """
        self.deviceData[key] = value

    def getDeviceData(self, key):
        """
        获得设备数据
        :param key: 数据key
        :return: 返回数据值，不存在的数据key则返回None
        """
        if ( key in self.deviceData):
            return self.deviceData[key]
        else:
            return None

    def removeDeviceData(self, key):
        """
        删除设备数据
        :param key: 数据key
        :return: 无反回
        """
        del self.deviceData[key]

    def readDataTh(self, threadName, delay):
        """
        读取数据线程
        :return:
        """
        print("启动" + threadName)
        while True:
            # 如果串口打开了
            if self.isOpen:
                try:
                    tlen = self.serialPort.inWaiting()
                    if (tlen>0):
                        data = self.serialPort.read(tlen)
                        self.onDataReceived(data)
                except Exception as ex:
                    print(ex)
            else:
                time.sleep(0.1)
                print("暂停")
                break

    def openDevice(self):
        """
        打开设备
        :return: 无返回
        """

        # 先关闭端口
        self.closeDevice()
        try:
            self.serialPort = serial.Serial(self.serialConfig.portName, self.serialConfig.baud, timeout=0.5)
            self.isOpen = True
            t = threading.Thread(target=self.readDataTh, args=("Data-Received-Thread",10,))          # 开启一个线程接收数据
            t.start()
        except SerialException:
            print("打开" + self.serialConfig.portName + self.serialConfig.baud + "失败")

    def closeDevice(self):
        """
        关闭设备
        :return: 无返回
        """
        if self.serialPort is not None:
            self.serialPort.close()
            print("端口关闭了")
        self.isOpen = False
        print("设备关闭了")

    def onDataReceived(self, data):
        """
        接收数据时
        :param data: 收到的数据
        :return: 无返回
        """
        if self.protocolResolver is not None:
            self.protocolResolver.passiveReceiveData(data, self)

    def get_int(self,dataBytes):
        """
        int转换有符号整形   = C# BitConverter.ToInt16
        :param dataBytes: 字节数组
        :return:
        """
        #return -(data & 0x8000) | (data & 0x7fff)
        return  int.from_bytes(dataBytes, "little", signed=True)

    def get_unint(self,dataBytes):
        """
        int转换无符号整形
        :param data:
        :return:
        """
        return  int.from_bytes(dataBytes, "little")


    def sendData(self, data):
        """
        发送数据
        :return: 是否发送成功
        """
        if self.protocolResolver is not None:
            self.protocolResolver.sendData(data, self)

    def readReg(self, regAddr,regCount):
        """
        读取寄存器
        :param regAddr: 寄存器地址
        :param regCount: 寄存器个数
        :return:
        """
        if self.protocolResolver is not None:
            return self.protocolResolver.readReg(regAddr,regCount, self)
        else:
            return none

    def writeReg(self, regAddr,sValue):
        """
        写入寄存器
        :param regAddr: 寄存器地址
        :param sValue: 写入值
        :return:
        """
        if self.protocolResolver is not None:
            self.protocolResolver.writeReg(regAddr,sValue, self)

    def unlock(self):
        """
        解锁
        :return:
        """
        if self.protocolResolver is not None:
            self.protocolResolver.unlock(self)

    def save(self):
        """
        保存
        :return:
        """
        if self.protocolResolver is not None:
            self.protocolResolver.save(self)

    def AccelerationCalibration(self):
        """
        加计校准
        :return:
        """
        if self.protocolResolver is not None:
            self.protocolResolver.AccelerationCalibration(self)

    def BeginFiledCalibration(self):
        """
        开始磁场校准
        :return:
        """
        if self.protocolResolver is not None:
            self.protocolResolver.BeginFiledCalibration(self)

    def EndFiledCalibration(self):
        """
        结束磁场校准
        :return:
        """
        if self.protocolResolver is not None:
            self.protocolResolver.EndFiledCalibration(self)

    def sendProtocolData(self, data):
        """
        发送带协议的数据
        :return:
        """
        if self.protocolResolver is not None:
            self.protocolResolver.sendData(data)



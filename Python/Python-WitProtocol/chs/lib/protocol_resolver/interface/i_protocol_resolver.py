# coding:UTF-8
from abc import abstractmethod, ABCMeta

"""
协议解析器接口类
"""


class IProtocolResolver(metaclass=ABCMeta):

    @abstractmethod
    def setConfig(self, config):
        """
        设置配置
        :param config: 配置信息
        :return:
        """
        pass

    @abstractmethod
    def sendData(self, sendData, deviceModel):
        """
        发送数据
        :param sendData: 要发送的数据
        :param deviceModel: 设备模型
        :return:
        """
        pass

    @abstractmethod
    def readReg(self, regAddr,regCount, deviceModel):
        """
        读取寄存器
        :param regAddr: 寄存器地址
        :param regCount: 寄存器个数
        :param deviceModel: 设备模型
        :return:
        """
        pass

    @abstractmethod
    def writeReg(self, regAddr,sValue, deviceModel):
        """
        写入寄存器
        :param regAddr: 寄存器地址
        :param sValue: 写入值
        :param deviceModel: 设备模型
        :return:
        """
        pass

    @abstractmethod
    def passiveReceiveData(self, data, deviceModel):
        """
        解析主动回传的数据
        :param data:
        :param deviceModel:
        :return:
        """
        pass

    @abstractmethod
    def unlock(self,deviceModel):
        """
        解锁
        :return:
        """
        pass

    @abstractmethod
    def save(self,deviceModel):
        """
        保存
        :return:
        """
        pass

    @abstractmethod
    def AccelerationCalibration(self,deviceModel):
        """
        加计校准
        :return:
        """
        pass

    @abstractmethod
    def BeginFiledCalibration(self,deviceModel):
        """
        开始磁场校准
        :return:
        """
        pass

    @abstractmethod
    def EndFiledCalibration(self,deviceModel):
        """
        结束磁场校准
        :return:
        """
        pass
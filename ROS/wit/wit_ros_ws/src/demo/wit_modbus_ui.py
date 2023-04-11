#!/usr/bin/env python
# -*- coding:utf-8 -*-
import serial
import struct
import platform
import serial.tools.list_ports
import math
import sys
import time
import modbus_tk
import modbus_tk.defines as cst
from modbus_tk import modbus_rtu

# 查找 ttyUSB* 设备
def find_ttyUSB():
    print('linux默认串口为 /dev/ttyUSB0,windows中默认串口为COM3, 若识别多个串口设备, 请在 launch 文件中修改 imu 对应的串口')
    posts = [port.device for port in serial.tools.list_ports.comports() if 'USB' in port.device]
    print('当前电脑所连接的 {} 串口设备共 {} 个: {}'.format('USB', len(posts), posts))


# 校验
def checkSum(list_data, check_data):
    return sum(list_data) & 0xff == check_data


# 16 进制转 ieee 浮点数
def hex_to_short(raw_data):
    return list(struct.unpack("hhhh", bytearray(raw_data)))


# 处理串口数据
def handleSerialData(raw_data):
    global buff, key, angle_degree, magnetometer, acceleration, angularVelocity, pub_flag
    if python_version == '2':
        buff[key] = ord(raw_data)
    if python_version == '3':
        buff[key] = raw_data

    key += 1
    if buff[0] != 0x55:
        key = 0
        return
    if key < 11:  # 根据数据长度位的判断, 来获取对应长度数据
        return
    else:
        data_buff = list(buff.values())  # 获取字典所有 value

        if buff[1] == 0x51 and pub_flag[0]:
            if checkSum(data_buff[0:10], data_buff[10]):
                acceleration = [hex_to_short(data_buff[2:10])[i] / 32768.0 * 16 * 9.8 for i in range(0, 3)]
            else:
                print('0x51 校验失败')
            pub_flag[0] = False

        elif buff[1] == 0x52 and pub_flag[1]:
            if checkSum(data_buff[0:10], data_buff[10]):
                angularVelocity = [hex_to_short(data_buff[2:10])[i] / 32768.0 * 2000 * math.pi / 180 for i in range(0, 3)]

            else:
                print('0x52 校验失败')
            pub_flag[1] = False

        elif buff[1] == 0x53 and pub_flag[2]:
            if checkSum(data_buff[0:10], data_buff[10]):
                angle_degree = [hex_to_short(data_buff[2:10])[i] / 32768.0 * 180 for i in range(0, 3)]
            else:
                print('0x53 校验失败')
            pub_flag[2] = False
        elif buff[1] == 0x54 and pub_flag[3]:
            if checkSum(data_buff[0:10], data_buff[10]):
                magnetometer = hex_to_short(data_buff[2:10])
            else:
                print('0x54 校验失败')
            pub_flag[3] = False

        else:
            print("该数据处理类没有提供该 " + str(buff[1]) + " 的解析")
            print("或数据错误")
            buff = {}
            key = 0

        buff = {}
        key = 0
        if pub_flag[0] == True or pub_flag[1] == True or pub_flag[2] == True or pub_flag[3] == True:
            return
        pub_flag[0] = pub_flag[1] = pub_flag[2] = pub_flag[3] = True

        


def startUI():
    window.mainloop()


def showText(text):
    show_text.delete(0.0, tk.END)  # 删除
    show_text.insert(tk.INSERT, text)  # 插入


def loopData(wt_imu):
    master = modbus_rtu.RtuMaster(wt_imu)
    master.set_timeout(0.1)
    master.set_verbose(True)
    while True:
        time.sleep(0.01)
        try:
            reg = master.execute(80,cst.READ_HOLDING_REGISTERS,52,12)
        except Exception as e:
            print(e)
            print("\033[31mread register time out, please check connection or baundrate set!\033[0m")
        else:   
            v=[0]*12
            for i in range(0,12):
                if (reg[i]>32767):
                    v[i]=reg[i]-65536
                else:

                    v[i]=reg[i]

            acceleration = [v[i] / 32768.0 * 16 * 9.8 for i in range(0, 3)]
            angularVelocity = [v[i] / 32768.0 * 2000 * math.pi / 180 for i in range(3, 6)]
            magnetometer = v[6:9]
            angle_degree = [v[i] / 32768.0 * 180 for i in range(9, 12)]
            text = '''
    加速度(m/s²)：
        x轴：%.2f
        y轴：%.2f
        z轴：%.2f

    角速度(rad/s)：
        x轴：%.2f
        y轴：%.2f
        z轴：%.2f

    欧拉角(°)：
        x轴：%.2f
        y轴：%.2f
        z轴：%.2f

    磁场：
        x轴：%.2f
        y轴：%.2f
        z轴：%.2f
    ''' % (acceleration[0], acceleration[1], acceleration[2],
        angularVelocity[0], angularVelocity[1], angularVelocity[2],
        angle_degree[0], angle_degree[1], angle_degree[2],
        magnetometer[0], magnetometer[1], magnetometer[2]
        )
            showText(text)
    


def threadLoopData(imu_ser):
    import threading
    # 开启数据解析线程
    t = threading.Thread(target=loopData, args=[imu_ser,])
    # 将当前线程设为子线程t的守护线程，这样一来，当前线程结束时会强制子线程结束
    t.setDaemon(True)
    t.start()


key = 0
flag = 0
buff = {}
angularVelocity = [0, 0, 0]
acceleration = [0, 0, 0]
magnetometer = [0, 0, 0]
angle_degree = [0, 0, 0]
pub_flag = [True, True, True, True]


if __name__ == "__main__":
    python_version = platform.python_version()[0]

    # ui 库版本判断
    if python_version == '2':
        import Tkinter as tk
    if python_version == '3':
        import tkinter as tk

    # init UI
    window = tk.Tk()
    window.title('wit imu')
    window.geometry('640x360')
    show_frame = tk.Frame(window)
    show_frame.config(height=345, width=625)
    show_frame.place(x=5, y=5)
    show_text = tk.Text(show_frame, height=700, bg='white', font=('Arial', 12))
    show_text.place(x=4, y=4)

    find_ttyUSB()

    if (platform.system().find("Linux")>=0):
        port = "/dev/ttyUSB0"
    else:
        port = "COM3"
        
    baudrate = 9600

    try:
        wt_imu = serial.Serial(port=port, baudrate=baudrate, timeout=0.5)
        if wt_imu.isOpen():
            print("\033[32mport open success...\033[0m")
        else:
            wt_imu.open()
            print("\033[32mport open success...\033[0m")
    except Exception as e:
        print(e)
        print("\033[31mport open failed\033[0m")
        exit(0)
    else:
        threadLoopData(wt_imu)
        startUI()

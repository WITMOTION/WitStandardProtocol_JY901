#!/usr/bin/env python
# -*- coding:utf-8 -*-
import serial
import struct
import platform
import serial.tools.list_ports
import math
import sys

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
    angle_flag=False
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
        if buff[1] == 0x51 :
            if checkSum(data_buff[0:10], data_buff[10]):
                acceleration = [hex_to_short(data_buff[2:10])[i] / 32768.0 * 16 * 9.8 for i in range(0, 3)]
            else:
                print('0x51 校验失败')

        elif buff[1] == 0x52:
            if checkSum(data_buff[0:10], data_buff[10]):
                angularVelocity = [hex_to_short(data_buff[2:10])[i] / 32768.0 * 2000 * math.pi / 180 for i in range(0, 3)]

            else:
                print('0x52 校验失败')

        elif buff[1] == 0x53:
            if checkSum(data_buff[0:10], data_buff[10]):
                angle_degree = [hex_to_short(data_buff[2:10])[i] / 32768.0 * 180 for i in range(0, 3)]
                angle_flag = True
            else:
                print('0x53 校验失败')
        elif buff[1] == 0x54:
            if checkSum(data_buff[0:10], data_buff[10]):
                magnetometer = hex_to_short(data_buff[2:10])
            else:
                print('0x54 校验失败')

        else:
            print("该数据处理类没有提供该 " + str(buff[1]) + " 的解析")
            print("或数据错误")
            buff = {}
            key = 0

        buff = {}
        key = 0
        if angle_flag:   
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

def startUI():
    window.mainloop()


def showText(text):
    show_text.delete(0.0, tk.END)  # 删除
    show_text.insert(tk.INSERT, text)  # 插入


def loopData(wt_imu):
    while True:
        try:
            buff_count = wt_imu.inWaiting()
        except Exception as e:
            print("exception:" + str(e))
            print("imu 失去连接，接触不良，或断线")
            showText('传入showtext数据后，会立即退出窗口')
            window.quit()
            exit(0)
        else:
            if buff_count > 0:
                buff_data = wt_imu.read(buff_count)
                for i in range(0, buff_count):
                    handleSerialData(buff_data[i])


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

    if (sys.platform.find("linux")>=0):
        port = "/dev/ttyUSB0"
    else:
        port = "COM3"
        
    baudrate = 9600

    try:
        wt_imu = serial.Serial(port=port, baudrate=baudrate, timeout=0.5)
        if wt_imu.isOpen():
            print("\033[32m串口打开成功...\033[0m")
        else:
            wt_imu.open()
            print("\033[32m打开串口成功...\033[0m")
    except Exception as e:
        print(e)
        print("\033[31m串口打开失败\033[0m")
        exit(0)
    else:
        threadLoopData(wt_imu)
        startUI()

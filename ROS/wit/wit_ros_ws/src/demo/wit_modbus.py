#!/usr/bin/env python
# -*- coding:utf-8 -*-
import serial
import struct
import platform
import serial.tools.list_ports
import math
import time
import modbus_tk
import modbus_tk.defines as cst
from modbus_tk import modbus_rtu
# 查找 ttyUSB* 设备
def find_ttyUSB():
    print('imu 默认串口为 /dev/ttyUSB0, 若识别多个串口设备, 请在 launch 文件中修改 imu 对应的串口')
    posts = [port.device for port in serial.tools.list_ports.comports() if 'USB' in port.device]
    print('当前电脑所连接的 {} 串口设备共 {} 个: {}'.format('USB', len(posts), posts))


angularVelocity = [0, 0, 0]
acceleration = [0, 0, 0]
magnetometer = [0, 0, 0]
angle_degree = [0, 0, 0]


if __name__ == "__main__":
    python_version = platform.python_version()[0]

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
            

                print(
                '''
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
                    ))

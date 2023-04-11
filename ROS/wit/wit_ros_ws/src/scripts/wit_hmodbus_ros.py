#!/usr/bin/env python
# -*- coding:utf-8 -*-
import serial
import struct
import rospy
import math
import platform
import serial.tools.list_ports
from sensor_msgs.msg import Imu
from sensor_msgs.msg import MagneticField
from tf.transformations import quaternion_from_euler
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
    rospy.init_node("imu")
    port = rospy.get_param("~port", "/dev/ttyUSB0")
    baudrate = rospy.get_param("~baud", 115200)
    print("IMU Type: Modbus Port:%s baud:%d" %(port,baudrate))
    imu_msg = Imu()
    mag_msg = MagneticField()
    try:
        wt_imu = serial.Serial(port=port, baudrate=baudrate, timeout=0.5)
        if wt_imu.isOpen():
            rospy.loginfo("\033[32mport open success...\033[0m")
        else:
            wt_imu.open()
            rospy.loginfo("\033[32mport open success...\033[0m")
    except Exception as e:
        print(e)
        rospy.loginfo("\033[31mport open failed\033[0m")
        exit(0)
    else:
        imu_pub = rospy.Publisher("wit/imu", Imu, queue_size=10)
        mag_pub = rospy.Publisher("wit/mag", MagneticField, queue_size=10)

        master = modbus_rtu.RtuMaster(wt_imu)
        master.set_timeout(1)
        master.set_verbose(True)
        while not rospy.is_shutdown():            
            
            try:
                reg = master.execute(80,cst.READ_HOLDING_REGISTERS,52,15)
            except Exception as e:
                print(e)
                rospy.loginfo("\033[31mread register time out, please check connection or baundrate set!\033[0m")
                time.sleep(0.1)
            else:
                v=[0]*12
                for i in range(0,9):
                    if (reg[i]>32767):
                        v[i]=reg[i]-65536
                    else:

                        v[i]=reg[i]
                        
                        
                v[9] = reg[9]+reg[10]*65536
                v[10] = reg[11]+reg[12]*65536
                v[11] = reg[13]+reg[14]*65536                 
            
                acceleration = [v[i] / 32768.0 * 16 * 9.8 for i in range(0, 3)]
                angularVelocity = [v[i] / 32768.0 * 2000 * math.pi / 180 for i in range(3, 6)]
                magnetometer = v[6:9]
                

                for i in range(9,12):
                    if(v[i]> 2147483647):
                        v[i] = v[i] - 4294967296      
                    angle_degree[i-9] = v[i] / 1000.0 

            
                stamp = rospy.get_rostime()

                imu_msg.header.stamp = stamp
                imu_msg.header.frame_id = "base_link"

                mag_msg.header.stamp = stamp
                mag_msg.header.frame_id = "base_link"

                angle_radian = [angle_degree[i] * math.pi / 180 for i in range(3)]
                qua = quaternion_from_euler(angle_radian[0], angle_radian[1], angle_radian[2])

                imu_msg.orientation.x = qua[0]
                imu_msg.orientation.y = qua[1]
                imu_msg.orientation.z = qua[2]
                imu_msg.orientation.w = qua[3]

                imu_msg.angular_velocity.x = angularVelocity[0]
                imu_msg.angular_velocity.y = angularVelocity[1]
                imu_msg.angular_velocity.z = angularVelocity[2]

                imu_msg.linear_acceleration.x = acceleration[0]
                imu_msg.linear_acceleration.y = acceleration[1]
                imu_msg.linear_acceleration.z = acceleration[2]

                mag_msg.magnetic_field.x = magnetometer[0]
                mag_msg.magnetic_field.y = magnetometer[1]
                mag_msg.magnetic_field.z = magnetometer[2]

                imu_pub.publish(imu_msg)
                mag_pub.publish(mag_msg)
           


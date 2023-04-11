#!/usr/bin/env python
# -*- coding:utf-8 -*-
import rospy
from std_msgs.msg import String
from sensor_msgs.msg import MagneticField

def callback(data):
    rospy.loginfo(data)


def showhelp():
	print("----------------------------")
	print("0:exti cali mode")
	print("9:enter mag cali mode")
	print("h:show cmd help")
	print("e:exti sys")
	print("v:show version")
	print("b:begin recording")
	print("s:stop recording")
	print("rate:set 0.2~200Hz output")
	print("baud:set 4800~230400 baud")
	print("rsw:set output data <time,acc,gyro,angle,mag>")
	print("----------------------------")

if __name__ == "__main__":

	rospy.init_node("Set")
	set_pub = rospy.Publisher("wit/cali", String, queue_size=10)
	print("please input your cmd:")
	showhelp()
	while not rospy.is_shutdown():
		try:
			string = raw_input("input cmd:")
			if 'rate' in string:
				set_pub.publish(string)
				print("change " + string)
			elif 'rsw' in string:
				set_pub.publish('rsw')
			elif 'baud' in string:
				set_pub.publish(string)
				print("change " + string)
			elif 'b' in string:
				set_pub.publish("begin")
				print("begin recording")
			elif 's' in string:
				set_pub.publish("stop")
				print("stop recording")
			elif 'v' in string:
				set_pub.publish("version")
				print("show sensor version")
			elif 'h' in string:
				showhelp()
			elif 'e' in string:
				print("exti sys\n")
				exit(0)
			elif '0' in string:
				set_pub.publish("exti")
				print("exti cali mode")
			elif '9' in string:
				set_pub.publish("mag")
				print("enter mag cali mode")
			else :
				print("{} cmd no support".format(string))
		except Exception as e:
			print(e)

# 1.ROS环境安装
(如果已经安装好ROS运行环境，可以跳过本章，直接进入第二章IMU软件包使用)
Robot Operating System (ROS) 是一个得到广泛应用机器人系统的软件框架，它包含了一系列的软件库和工具用于构建机器人应用。从驱动到最先进的算法，以及强大的开发者工具，ROS 包含了开发一个机器人项目所需要的所有东西。且它们都是开源的。

ROS 虽然名为机器人操作系统，但它与我们一般概念中的操作系统，如 Windows，Linux，iOS 和 Android 这些。Windows，Linux，iOS 和 Android 这些操作系统为我们管理计算机的物理硬件资源，如 CPU、内存、磁盘、网络及外设，提供如进程、线程和文件这样的抽象，并提供如读文件、写文件、创建进程、创建线程及启动线程这样的操作。ROS 所工作的层级并没有这么低，它基于一般概念中的操作系统来运行，官方推荐基于 Ubuntu Linux 运行，并在 Ubuntu Linux 操作系统提供的抽象和操作的基础之上，提供了更高层的抽象，如节点、服务、消息、主题等，以及更高层的操作，如主题的发布、主题的订阅、服务的查询与连接等操作。同时 ROS 还提供开发机器人项目所需的工具和功能库。

ROS 发行版是一个版本标识的 ROS 包集合，这些与 Linux 发行版（如 Ubuntu）类似。ROS 发行版的目的是让开发者可以基于一个相对稳定的代码库来工作，直到他们可以平稳地向前演进。一旦发行版发布，官方就会限制对其的改动，而仅仅提供对于核心包的 bug fixes 和非破坏性的增强。

当前（2018-01-28） ROS 系统已经发布了多个版本。ROS 最新的一些版本如下：

![Description](https://witpic-1253369323.cos.ap-guangzhou.myqcloud.com/img-md/7cef600e-32d8-4171-ac18-b71e70e80221){{{width="auto" height="auto"}}}

ROS 基本上保持每年一个新版本，每两年一个长期发行版的发布节奏。关于 ROS 版本发布的更多内容，如更多的发行版的介绍，发布的计划等，可以参考 ROS 官方站点的 Distributions 主页。

目前官方推荐使用最近的一个长期支持版本，即 ROS Kinetic Kame，求新的同时兼顾稳定性无疑应该采用这一版本，如果想要尝试最新的功能特性则可以使用最新的发行版 ROS Lunar Loggerhead。

ROS 的安装步骤如下：

ROS kinetic官网：[http://wiki.ros.org/kinetic/Installation/Ubuntu](http://)
## 1.1 配置资源库
"restricted"，"universe"和"multiverse"。一般情况是不用配置的，参考下图红色框部分。如果没有配置，可以参考Ubuntu官网。

![Description](https://witpic-1253369323.cos.ap-guangzhou.myqcloud.com/img-md/2628eb89-6b72-4d7c-b115-0505e36081de){{{width="auto" height="auto"}}}

## 1.2 安装

1.设置安装源
sudo sh -c 'echo "deb http://packages.ros.org/ros/ubuntu $(lsb_release -sc) main" > /etc/apt/sources.list.d/ros-latest.list'
2.设置key
sudo apt-key adv --keyserver hkp://ha.pool.sks-keyservers.net:80 --recv-key 421C365BD9FF1F717815A3895523BAEEB01FA116
3.安装
sudo apt-get update
sudo apt-get install ros-kinetic-desktop-full

独立的包安装：可以安装一个特定的 ROS 包（用实际的包名来替换下面的命令中的 “PACKAGE”）。
sudo apt-get install ros-kinetic-PACKAGE
如：sudo apt-get install ros-kinetic-slam-gmapping
要找到可用的包，可以使用：apt-cache search ros-kinetic

## 1.3 初始化
``` 
sudo rosdep init  （安装一次后只能运行一次，若重新安装选择跳过或卸载）
rosdep update结果：
Add distro "groovy"
Add distro "hydro"
Add distro "indigo"
Add distro "jade"
Add distro "kinetic"
Add distro "lunar"
Add distro "melodic"
updated cache in /home/ubuntu1604/.ros/rosdep/sources.cache
```
若出现time out 或错误，重试几遍
## 1.4 配置环境变量
``` C
echo "source /opt/ros/kinetic/setup.bash" >> ~/.bashrc打开另一个终端：
source ~/.bashrc
```
 如果安装了多个 ROS 发行版，则 ~/.bashrc 必须只 source 当前正在使用的那一版的 setup.bash。

如果你只想要修改当前 shell 的环境，则输入如下的命令来替换上面的命令：source /opt/ros/kinetic/setup.bash

## 1.5 安装rosinstall 
sudo apt-get install python-rosinstall
构建包所需的依赖
到这一步，应该已经安装好了运行核心 ROS 包的所有东西。要创建和管理你自己的 ROS workspace，还有单独发布的许多的工具。比如，rosinstall 是一个常用的命令行工具，使你可以通过一个命令为 ROS 包简单地下载许多源码树。
要安装这个工具及其它的依赖以构建 ROS 包，则运行：
sudo apt-get install python-rosinstall python-rosinstall-generator python-wstool build-essential
完成完整的 ROS 安装之后，可以对安装做一个简单的测试。可以通过 roscore 和 turtlesim 来做测试。

## 1.6 测试
``` 
1) 打开Termial，输入以下命令，初始化ROS环境：
roscore
2) 打开新的Termial，输入以下命令，弹出一个小乌龟窗口：
rosrun turtlesim turtlesim_node
3) 打开新的Termial，输入以下命令，可以在Termial中通过方向键控制小乌龟的移动：
rosrun turtlesim turtle_teleop_key
```
# 2.IMU软件包使用

## 2.1 安装 ROS IMU 依赖
请在终端运行对应的命令

   如果你使用的是 ubuntu 16.04，ROS kinetic，python2 :

   ```
   sudo apt-get install ros-kinetic-imu-tools ros-kinetic-rviz-imu-plugin
   sudo apt-get install python-visual
   ```

   如果你使用的是 ubuntu 18.04，ROS Melodic，python2 :

   ```
   sudo apt-get install ros-melodic-imu-tools ros-melodic-rviz-imu-plugin
   ```

   如果你使用的是 ubuntu 20.04，ROS Noetic，python3 :

   ```
   sudo apt-get install ros-noetic-imu-tools ros-noetic-rviz-imu-plugin
   pip3 install pyserial
   ```

## 2.2 建立工作空间
下载示例程序，将文件放到home根目录，右击提取到此处。
打开命令终端，运行下面指令：
 ```
   cd ~/wit/wit_ros_ws/
   catkin_make
   cd ~/wit/wit_ros_ws/src/scripts/
   sudo chmod 777 *.py
   echo "source ~/wit/wit_ros_ws/devel/setup.sh" >> ~/.bashrc
   source ~/.bashrc
   ```

## 2.3 ROS 驱动和可视化
以 ubuntu16.04，JY901S，python2.7 为例

1. 查看USB端口号。先不要插 IMU 的 USB ，在终端输入 `ls  /dev/ttyUSB*` 来检测一下，然后在将 USB 插入电脑，再在终端输入 `ls  /dev/ttyUSB*` 来检测一下，多出来的 ttyUSB 设备就是 IMU 的串口。

![USB端口号](https://witpic-1253369323.cos.ap-guangzhou.myqcloud.com/img-md/8491ff53-3d0c-4f4e-89c0-f06a37085cdc){{{width="auto" height="auto"}}}

2. 修改参数配置。需要修改的参数包括设备类型，USB端口号和波特率。进入脚本目录~/wit/wit_ros_imu/src/launch，修改对应的 launch 文件中的配置参数。设备类型如果是modbus协议的就填modbus，使用wit标准协议的填normal,如果是MODBUS高精度协议的填hmodbus，如果是CAN协议的就填can，如果是CAN高精度协议的填hcan。设备号/dev/ttyUSB0（脚本默认用的 /dev/ttyUSB0）为你电脑识别出来的数字。波特率根据实际使用设定，JY6x系列模块默认波特率为115200,CAN模块为230400，其他模块为9600,如果用户通过上位机修改了波特率，需要对应修改成修改后的波特率。

![参数修改](https://witpic-1253369323.cos.ap-guangzhou.myqcloud.com/img-md/43e74867-4ec5-4237-804c-32a417390f61){{{width="auto" height="auto"}}}


3. 给对应的串口管理员权限，在终端输入： sudo chmod 777 /dev/ttyUSB0，提示你输入管理员密码，输入密码后回车即可。注意每次重新插入USB口都需要重新给串口赋管理员权限。

4. 如果使用的产品是Modbus协议的，还需要安装一下Modbus的依赖库，在终端输入：pip install modbus_tk

5. 打开终端，运行launch文件

   ```
   roslaunch wit_ros_imu display_and_imu.launch
   ```

![Description](https://witpic-1253369323.cos.ap-guangzhou.myqcloud.com/img-md/2a7ddca1-016c-49d1-a85d-b4568a468027){{{width="auto" height="auto"}}}
   
打开两个新终端输入分别输入下面几行命令

   ```
   rostopic echo /wit/imu
   rostopic echo /wit/mag
   rostopic echo /wit/location     #经纬度解析只有WIT私有协议 即normal才有
   ```

   如下图，驱动运行成功后和 `rostop echo` 输出的信息

![Description](https://witpic-1253369323.cos.ap-guangzhou.myqcloud.com/img-md/8522c2d4-da59-4580-810d-dd3f2a4c7aa5){{{width="auto" height="auto"}}}


6. 同理，如需要运行其他 launch 文件，需要先确保 launch 文件中的 /dev/ttyUSB0 设备修改对。

7. 相关文件说明

   * display_and_imu.launch，打开打开 IMU 驱动节点和用 visual 编写的可视化模型。（仅支持 ubuntu 16.04）
   * wit_imu.launch，打开用 IMU 驱动节点。
   * rviz_and_imu.launch，打开 IMU 驱动节点和 Rviz 可视化。

# 3. 串口助手测试通讯

以 ubuntu16.04 为例，波特率 9600

1. 安装 cutecom（你也可以安装其他的串口助手进行调试）。

   ```
   apt-get install cutecom -y
   ```

2. 插入串口模块，需要通过命令ls /dev/ttyU*查看新加入的串口模块的端口号，然后对其赋予读写权限，以/dev/ttyUSB0为例，输入指令：sudo chmod 777 /dev/ttyUSB0，然后根据提示输入管理员密码。注意每次新插入USB模块都需要重新赋读写权限。

3. 安装成功后在终端输入 cutecom，打开串口助手，然后进行一些设置，注意从下拉框中选择的串口号是不对的，需要根据上面第二步中获取到的串口号进行修改，比如/dev/ttyUSB0，如图中所示。  

![Description](https://witpic-1253369323.cos.ap-guangzhou.myqcloud.com/img-md/f827f353-7ad0-4c5f-83e2-0d117ad34d41){{{width="auto" height="auto"}}}
   

4. 然后我们点击`open device` ，此时下面的空白面板会有 imu 的数据打印。

5. 我们可以等待 imu 的数据打印一会儿，然后点击 `close device`来查看。

6. 如果可以找到`55 51`、 `55 52`、 `55 53`开头的信息，那么模块发送的数据是没有问题的，如果有数据但没有找到正确包头的数据，则需要核对一下波特率设置，切换到正确的波特率就可以显示了。

   


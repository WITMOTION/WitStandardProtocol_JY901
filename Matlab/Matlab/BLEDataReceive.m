clear all;
close all;
instrreset;
disp('Press Ctrl+C to stop collecting data!')              %按Ctrl+C,终止数据的获取
s=serial('com42','baudrate',115200) ;fopen(s) ;               %请将COM44换成电脑识别到的COM口，波特率9600换成传感器对应的波特率    Please replace COM44 with the COM port recognized by the PC, and change the baud rate 9600 to the baud rate corresponding to the sensor
f = 20;         %DataFrequce
t=0;
cnt = 1;
aa=[0 0 0];     %加速度XYZ    Acceleration X, Y, Z axis
ww=[0 0 0];     %角速度XYZ    Angular velocity X, Y, Z axis
AA = [0 0 0];   %角度XYZ      Angle X, Y, Z axis
tt = 0;
a=[0 0 0]';
w=[0 0 0]';
A=[0 0 0]';

disp('Wait 10s, BLE2.0 Connecting!')
fwrite(s,'AT','uint8');  
pause(2);
fwrite(s,'AT+BAUD8','uint8');  
pause(2);  
fwrite(s,'AT+ROLE=S','uint8');  
pause(2);  
fwrite(s,'AT+ROLE=M','uint8');  
pause(5);  

while(1)
    Head = fread(s,2,'uint8');                              %获取串口数据，其中s文件，已经在上面提及到  Getting serial data, the S file has been mentioned above
    if (Head(1)~=uint8(85))                                 %如果串口的第一个数据不等于85(0x55)，证明不符合协议，不进行数据解析  If the first data of the serial is not equal to 85 (0x55), it proves that it isn't conform to the protocol and haven't perform data analysis
        continue;
    end   
    Head(2)
    switch(Head(2))                                         %获取串口第二个数据   Getting the second data of the serial
        case 81                                             %81(0x51):加速度包   81(0x51): Acceleration data packet
            a = fread(s,3,'int16')/32768*16;                %获取3个16bit的加速度数据，请参考协议说明   Getting three 16bit acceleration data, please refer to the protocol
        case 82                                             %82(0x52):角速度包   82 (0x52): Angular velocity data packet
            w = fread(s,3,'int16')/32768*2000;              %获取3个16bit的角速度数据，请参考协议说明   Getting three 16bit angular velocity data, please refer to the protocol
        case 83                                             %83(0x53):角度包     83 (0x53): Angular data packet
            A = fread(s,3,'int16')/32768*180;               %获取3个16bit的角度数据，请参考协议说明     Getting three 16bit angle data, please refer to the protocol.
            aa = [aa;a'];
            ww = [ww;w'];
            AA = [AA;A'];
            tt = [tt;t];
            
            subplot(3,1,1);plot(tt,aa);title(['Acceleration = ' num2str(a') 'm2/s']);ylabel('m2/s');
            subplot(3,1,2);plot(tt,ww);title(['Gyro = ' num2str(w') '°/s']);ylabel('°/s');
            subplot(3,1,3);plot(tt,AA);title(['Angle = ' num2str(A') '°']);ylabel('°');              
            cnt=0;
            drawnow;
            if (size(aa,1)>5*f)                              %清空历史数据   Clear history data
                aa = aa(f:5*f,:);
                ww = ww(f:5*f,:);
                AA = AA(f:5*f,:);
                tt = tt(f:5*f,:);
            end
            t=t+0.1;                                         %数据默认是10Hz，也就是0.1s，如果更改了产品的输出速率，请把0.1改为其他输出速率   The data default is 10Hz, which is 0.1s. If you change the output rate of the product, please change 0.1 to other output rates
    end 
        
            End = fread(s,3,'uint8');
end
fclose(s);
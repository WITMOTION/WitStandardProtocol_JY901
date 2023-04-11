import time
import sys
import struct

argc = len(sys.argv)

if argc < 2:
    print('please input convert file name')
    exit(1)
filename = sys.argv[1]
convertfilename = time.strftime("%Y%m%d%H%M%S", time.localtime()) + '.txt'
headmsg = ''
msg = ''
headindex = 0x50
headflag = 0
key = 0
raw_data = bytearray(11)
fd = open(convertfilename, 'w+')

def checkSum(list_data, check_data):
    return sum(list_data) & 0xff == check_data


def hex_to_short(raw_data):
    return list(struct.unpack("hhhh", bytearray(raw_data)))


def ConvertData(val):
    global headflag, headindex, fd, msg, headmsg, key, raw_data

    raw_data[key] = val
    key += 1
    if raw_data[0] != 0x55: key = 0
    if key < 11: return

    if checkSum(raw_data[0:10], raw_data[10]):
        if headindex == raw_data[1]:
            if headflag == 1:
                fd.write(headmsg+'\n')
                headflag = 2
            fd.write(msg+'\n')
            msg = ''

        if headflag == 0:
            headflag = 1
            headindex = raw_data[1]

        val = hex_to_short(raw_data[2:10])
        if raw_data[1] == 0x50:
            if headflag == 1:   headmsg += 'Chip-Time\t'
            ms = raw_data[9] * 256 + raw_data[8]
            msg += '20{:0>2d}-{:0>2d}-{:0>2d} {:0>2d}:{:0>2d}:{:0>2d}.{:0>3d}\t'.format(raw_data[2], raw_data[3], raw_data[4], raw_data[5], raw_data[6], raw_data[7], ms)
        elif raw_data[1] == 0x51:
            if headflag == 1:   headmsg += 'ax(g)\tay(g)\taz(g)\t'
            msg += "{:.3f}\t{:.3f}\t{:.3f}\t".format(val[0]/2048.0, val[1]/2048.0, val[2]/2048.0)
        elif raw_data[1] == 0x52:
            if headflag == 1:   headmsg += 'wx(deg/s)\twy(deg/s)\twz(deg/s)\t'
            msg += "{:.3f}\t{:.3f}\t{:.3f}\t".format(val[0]/32768.0*2000.0, val[1]/32768.0*2000.0, val[2]/32768.0*2000.0)
        elif raw_data[1] == 0x53:
            if headflag == 1:   headmsg += 'AngleX(deg)\tAngleY(deg)\tAngleZ(deg)\t'
            msg += "{:.3f}\t{:.3f}\t{:.3f}\t".format(val[0]/32768.0*180.0, val[1]/32768.0*180.0, val[2]/32768.0*180.0)
        elif raw_data[1] == 0x54:
            if headflag == 1:   headmsg += ' hx\thy\thz\t'
            msg += "{:.0f}\t{:.0f}\t{:.0f}\t".format(val[0], val[1], val[2])
    key = 0


f = open(filename, 'rb')
try:
    while True:
        data = f.read(1024)
        datalen = len(data)
        if datalen == 0:
            break
        key = 0
        index = 0
        for val in data:
            ConvertData(val)
except Exception as e:
    print(e)


f.close()
fd.close()
print('convert {} file finish, output {} file'.format(filename, convertfilename))

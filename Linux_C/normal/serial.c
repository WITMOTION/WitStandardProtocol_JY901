#include "serial.h"


int serial_open(unsigned char* dev, unsigned int baud)
{
    int fd;
    fd = open(dev, O_RDWR|O_NOCTTY);
    if (fd < 0) return fd;
    if(isatty(STDIN_FILENO)==0)
      {
   	    printf("standard input is not a terminal device\n");
      }
    else
      {
        printf("isatty success!\n");
      }

    struct termios newtio,oldtio;
    if (tcgetattr( fd,&oldtio) != 0)
      {
        perror("SetupSerial 1");
    	  printf("tcgetattr( fd,&oldtio) -> %d\n",tcgetattr( fd,&oldtio));
        return -1;
      }
    bzero( &newtio, sizeof( newtio ) );
    newtio.c_cflag  |=  CLOCAL | CREAD;
    newtio.c_cflag |= CS8;
    newtio.c_cflag &= ~PARENB;

    /*设置波特率*/
    switch( baud )
     {
       case 2400:
         cfsetispeed(&newtio, B2400);
         cfsetospeed(&newtio, B2400);
         break;
       case 4800:
         cfsetispeed(&newtio, B4800);
         cfsetospeed(&newtio, B4800);
         break;
       case 9600:
         cfsetispeed(&newtio, B9600);
         cfsetospeed(&newtio, B9600);
         break;
       case 115200:
         cfsetispeed(&newtio, B115200);
         cfsetospeed(&newtio, B115200);
         break;
       case 230400:
         cfsetispeed(&newtio, B230400);
         cfsetospeed(&newtio, B230400);
         break;
       case 460800:
         cfsetispeed(&newtio, B460800);
         cfsetospeed(&newtio, B460800);
         break;
       case 921600:
         cfsetispeed(&newtio, B921600);
         cfsetospeed(&newtio, B921600);
         break;
       default:
         cfsetispeed(&newtio, B9600);
         cfsetospeed(&newtio, B9600);
         break;
      }
     newtio.c_cflag &=  ~CSTOPB;
     newtio.c_cc[VTIME]  = 0;
     newtio.c_cc[VMIN] = 0;
     tcflush(fd,TCIFLUSH);

     if((tcsetattr(fd,TCSANOW,&newtio))!=0)
       {
          perror("com set error");
          return -1;
       }

    return fd;
}

void serial_close(int fd)
{
    close(fd);
}

#include <stdio.h>
int serial_read_data(int fd, unsigned char *val, int len)
{
  int resultlen = read(fd, val, len);

  // if (resultlen > 0) {
  //   printf("Read raw data: ");
  //   for (int i = 0; i < resultlen; i++) {
  //     printf("%02X ", val[i]);
  //   }
  //   printf("\n");
  // }

  return resultlen;
}
int serial_write_data(int fd, const char *val, int len)
{
	int resultlen=write(fd,val,len);

  if(resultlen > 0){
    printf("Wrote raw data: ");
    for (int i = 0; i < resultlen; i++) {
      printf("%02X ", (unsigned char)val[i]);
    }
    printf("\n");
  }

	return resultlen;
}

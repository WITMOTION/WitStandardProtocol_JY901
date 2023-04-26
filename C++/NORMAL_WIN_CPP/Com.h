#ifndef __UART_NET_H
#define __UART_NET_H

signed char	SendUARTMessageLength(const char chrMessage[],const unsigned short usLen);
signed char SetBaundrate(const unsigned long ulBaundrate);
signed char OpenCOMDevice(const unsigned long ulPortNo,const unsigned long ulBaundrate);
void CloseCOMDevice(void);

#endif

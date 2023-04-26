#ifndef __UART2_H
#define __UART2_H
#include "stm32f10x_usart.h"
#include "stdint.h"
void Usart2Init(unsigned int uiBaud);
void Uart2Send(unsigned char *p_data, unsigned int uiSize);


#endif

//------------------End of File----------------------------


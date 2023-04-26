#ifndef __IOI2C_H
#define __IOI2C_H
#include "stm32f10x.h"

//IO Port Operation Macro Definition
#define BITBAND(addr, bitnum) ((addr & 0xF0000000)+0x2000000+((addr &0xFFFFF)<<5)+(bitnum<<2)) 
#define MEM_ADDR(addr)  *((volatile unsigned long  *)(addr)) 
#define BIT_ADDR(addr, bitnum)   MEM_ADDR(BITBAND(addr, bitnum))  

//IO Port Address Mapping
#define GPIOA_ODR_Addr    (GPIOA_BASE+12) //0x4001080C 
#define GPIOB_ODR_Addr    (GPIOB_BASE+12) //0x40010C0C 
#define GPIOC_ODR_Addr    (GPIOC_BASE+12) //0x4001100C 
#define GPIOD_ODR_Addr    (GPIOD_BASE+12) //0x4001140C 
#define GPIOE_ODR_Addr    (GPIOE_BASE+12) //0x4001180C 
#define GPIOF_ODR_Addr    (GPIOF_BASE+12) //0x40011A0C    
#define GPIOG_ODR_Addr    (GPIOG_BASE+12) //0x40011E0C    

#define GPIOA_IDR_Addr    (GPIOA_BASE+8) //0x40010808 
#define GPIOB_IDR_Addr    (GPIOB_BASE+8) //0x40010C08 
#define GPIOC_IDR_Addr    (GPIOC_BASE+8) //0x40011008 
#define GPIOD_IDR_Addr    (GPIOD_BASE+8) //0x40011408 
#define GPIOE_IDR_Addr    (GPIOE_BASE+8) //0x40011808 
#define GPIOF_IDR_Addr    (GPIOF_BASE+8) //0x40011A08 
#define GPIOG_IDR_Addr    (GPIOG_BASE+8) //0x40011E08 

#define PCout(n)   BIT_ADDR(GPIOC_ODR_Addr,n)  //output 
#define PCin(n)    BIT_ADDR(GPIOC_IDR_Addr,n)  //input 

#define PBout(n)   BIT_ADDR(GPIOB_ODR_Addr,n)  //output 
#define PBin(n)    BIT_ADDR(GPIOB_IDR_Addr,n)  //input  
   	   		   
//IO direction setting
#define SDA_IN()  {GPIOB->CRH&=0XFFFF0FFF;GPIOB->CRH|=0x00008000;}
#define SDA_OUT() {GPIOB->CRH&=0XFFFF0FFF;GPIOB->CRH|=0x00003000;}
//#define SDA_IN()  {GPIOC->CRH&=0XFFF0FFFF;GPIOC->CRH|=0x00080000;}
//#define SDA_OUT() {GPIOC->CRH&=0XFFF0FFFF;GPIOC->CRH|=0x00030000;}
//IO operation function	 
#define IIC_SCL    PBout(10) //SCL
#define IIC_SDA    PBout(11) //SDA
#define READ_SDA   PBin(11)  //inputSDA
//#define IIC_SCL    PCout(11) //SCL
//#define IIC_SDA    PCout(12) //SDA
//#define READ_SDA   PCin(12)  //inputSDA

//IIC All Operation Functions
void IIC_Init(void);                //Initialize the IO port of IIC				 
void IIC_Start(void);				//Send IIC start signal
void IIC_Stop(void);	  			//Sending IIC stop signal
void IIC_Send_Byte(u8 txd);			//IIC sends a byte
u8 IIC_Read_Byte(unsigned char ack);//IIC reads one byte
u8 IIC_Wait_Ack(void); 				//IIC waiting for ACK signal
void IIC_Ack(void);					//IIC sends ACK signal
void IIC_NAck(void);				//IIC does not send ACK signal

void IIC_Write_One_Byte(u8 daddr,u8 addr,u8 data);
u8 IIC_Read_One_Byte(u8 daddr,u8 addr);	 
unsigned char I2C_Readkey(unsigned char I2C_Addr);

unsigned char I2C_ReadOneByte(unsigned char I2C_Addr,unsigned char addr);
unsigned char IICwriteByte(unsigned char dev, unsigned char reg, unsigned char data);
unsigned char IICwriteCmd(unsigned char dev, unsigned char cmd);
u8 IICwriteBytes(u8 dev, u8 reg, u8 length, u8* data);
u8 IICwriteBits(u8 dev,u8 reg,u8 bitStart,u8 length,u8 data);
u8 IICwriteBit(u8 dev,u8 reg,u8 bitNum,u8 data);
u8 IICreadBytes(u8 dev, u8 reg, u8 length, u8 *data);

#endif

//------------------End of File----------------------------

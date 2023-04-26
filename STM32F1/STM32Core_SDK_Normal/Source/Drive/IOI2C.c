#include "IOI2C.h"
#include "delay.h"
#include "stm32f10x_gpio.h"
#include "stm32f10x_rcc.h"
/**************************Implementation function********************************************
*Function prototype:		void IIC_Init(void)
*Function: Initialize the interface pins corresponding to I2C.
*******************************************************************************/

void Delay(u32 count)//The delay required to generate a 400KHzIIC signal
{
	while (count--);
}
void IIC_Init(void)
{			
	GPIO_InitTypeDef GPIO_InitStructure;
 	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);			     
 	//Configure PB6 and PB7 as open drain output with a refresh frequency of 10Mhz
 	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10 | GPIO_Pin_11;	
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;       
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	//Apply configuration to GPIOB
	GPIO_Init(GPIOB, &GPIO_InitStructure);
	
	SDA_OUT();     //SDA line output
	IIC_SDA=1;	  	  
	IIC_SCL=1;
}

/**************************Implementation function********************************************
*Function prototype:	void IIC_Start(void)
*Function: Generate IIC start signal
*******************************************************************************/
void IIC_Start(void)
{
	SDA_OUT();     //SDA line output
	IIC_SDA=1;	  	  
	IIC_SCL=1;
	
	Delay(5);
 	IIC_SDA=0;//START:when CLK is high,DATA change form high to low 
	
	Delay(5);
	IIC_SCL=0;//Clamp the I2C bus and prepare to send or receive data 
}

/**************************Implementation function********************************************
*Function prototype:		void IIC_Stop(void)
*Function://Generate IIC stop signal
*******************************************************************************/	  
void IIC_Stop(void)
{
	SDA_OUT();//SDA line output
	IIC_SCL=0;
	IIC_SDA=0;//STOP:when CLK is high DATA change form low to high
 	
		Delay(5);
	IIC_SCL=1; 
	IIC_SDA=1;//Send I2C bus end signal
	
		Delay(5);							   	
}

/**************************Implementation function********************************************
*Function prototype:		u8 IIC_Wait_Ack(void)
*Function: Wait for the response signal to arrive
//Return value: 1, failed to receive response
//        0£¨Received response successfully
*******************************************************************************/
u8 IIC_Wait_Ack(void)
{
	u8 ucErrTime=0; 
	SDA_IN();      //SDA set as input  
	IIC_SDA=1;
		Delay(5);	  
	while(READ_SDA)
	{
		ucErrTime++;
		if(ucErrTime>50)
		{
			IIC_Stop();
			return 1;
		}
		Delay(5);
	}  
	IIC_SCL=1;
	Delay(5); 
	IIC_SCL=0;// ±÷” ‰≥ˆ0  
	return 0;  
} 

/**************************Implementation function********************************************
*Function prototype:		void IIC_Ack(void)
*Function: Generate ACK response
*******************************************************************************/
void IIC_Ack(void)
{
	IIC_SCL=0;
	SDA_OUT();
	IIC_SDA=0;
		Delay(5);
	IIC_SCL=1;
		Delay(5);
	IIC_SCL=0;
}
	
/**************************Implementation function********************************************
*Function prototype:		void IIC_NAck(void)
*Function: Generate NACK response
*******************************************************************************/	    
void IIC_NAck(void)
{
	IIC_SCL=0;
	SDA_OUT();
	IIC_SDA=1;
	
		Delay(5);
	IIC_SCL=1;
		Delay(5);
	IIC_SCL=0;
}					 				     

/**************************Implementation function********************************************
*Function prototype:		void IIC_Send_Byte(u8 txd)
*Function: IIC sends one byte
*******************************************************************************/		  
void IIC_Send_Byte(u8 txd)
{                        
    u8 t; 
		SDA_OUT(); 	    
    IIC_SCL=0;//Lower the clock to start data transmission
    for(t=0;t<8;t++)
    {              
        IIC_SDA=(txd&0x80)>>7;
        txd<<=1; 	  
			
		Delay(2);   
		IIC_SCL=1;
		Delay(5);
		IIC_SCL=0;	
		Delay(3);
    }	 
} 	 
   
/**************************Implementation function********************************************
*Function prototype:		u8 IIC_Read_Byte(unsigned char ack)
*Function://Read 1 byte, when ack=1, send ACK, when ack=0, send nACK
*******************************************************************************/  
u8 IIC_Read_Byte(unsigned char ack)
{
	unsigned char i,receive=0;
	SDA_IN();//SDA set as input
    for(i=0;i<8;i++ )
	{
        IIC_SCL=0; 
        
		Delay(5);
		IIC_SCL=1;
        receive<<=1;
        if(READ_SDA)receive++;   
		
		Delay(5); 
    }					 
    if (ack)
        IIC_Ack(); //Send ACK
    else
        IIC_NAck();//Send nACK 
    return receive;
}

/**************************Implementation function********************************************
*Function prototype:		u8 IICreadBytes(u8 dev, u8 reg, u8 length, u8 *data)
*Function:	    Read the length values of the specified device's specified register
Enter the dev target device address
		reg	  Register address
		length Number of bytes to read
		*data  Pointer to store the read data
Returns the number of bytes read out
*******************************************************************************/ 
u8 IICreadBytes(u8 dev, u8 reg, u8 length, u8 *data){
    u8 count = 0;
	
	IIC_Start();
	IIC_Send_Byte(dev<<1);	   //Send write command
	IIC_Wait_Ack();
	IIC_Send_Byte(reg);   //Sending Address
  IIC_Wait_Ack();	  
	IIC_Start();
	IIC_Send_Byte((dev<<1)+1);  //Entering reception mode	
	IIC_Wait_Ack();
	
    for(count=0;count<length;count++){
		 
		 if(count!=length-1)data[count]=IIC_Read_Byte(1);  //Read data with ACK
		 	else  data[count]=IIC_Read_Byte(0);	 //Last Byte NACK
	}
    IIC_Stop();//Generate a stop condition
    return count;
}

/**************************Implementation function********************************************
*Function prototype:   		u8 IICwriteBytes(u8 dev, u8 reg, u8 length, u8* data)
*Function: Write multiple bytes into the specified device register
Enter the dev target device address
		reg	  Register address
		length Number of bytes to write
		*data  The first address of the data to be written
Whether the return was successful
*******************************************************************************/ 
u8 IICwriteBytes(u8 dev, u8 reg, u8 length, u8* data){
  
 	u8 count = 0;
	IIC_Start();
	IIC_Send_Byte(dev<<1);	   //Send write command
	IIC_Wait_Ack();
	IIC_Send_Byte(reg);   //Sending Address
	IIC_Wait_Ack();	  
	for(count=0;count<length;count++){
		IIC_Send_Byte(data[count]); 
		IIC_Wait_Ack(); 
 }
	IIC_Stop();//Generate a stop condition

    return 1; //status == 0;
	
}

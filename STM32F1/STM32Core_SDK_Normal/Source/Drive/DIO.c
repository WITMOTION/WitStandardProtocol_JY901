#include "DIO.h"

#define LED_GPIO_CLK   RCC_APB2Periph_GPIOA 
#define LED_PORT   	   GPIOA
#define LED_PIN        GPIO_Pin_15

void DIO_Initial(GPIO_TypeDef* GPIOx, uint32_t GPIO_Pin,GPIOMode_TypeDef GPIO_Mode,GPIOSpeed_TypeDef GPIO_Speed)
{
	GPIO_InitTypeDef GPIO_InitStructure;
	
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOA, ENABLE);
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOB, ENABLE);
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOC, ENABLE);
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOD, ENABLE);
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOF, ENABLE);  
	
	GPIO_StructInit(&GPIO_InitStructure); 
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed;
	GPIO_InitStructure.GPIO_Pin =  GPIO_Pin;
	GPIO_Init(GPIOx, &GPIO_InitStructure);	
}	

void IOSleep()
{
	GPIO_InitTypeDef GPIO_InitStructure;
	
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOA, ENABLE);
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOB, ENABLE);
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOC, ENABLE);
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOD, ENABLE);
	RCC_AHBPeriphClockCmd( RCC_APB2Periph_GPIOF, ENABLE);  
	
	GPIO_StructInit(&GPIO_InitStructure); 
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AIN;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_2MHz;
	GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_All;
	GPIO_Init(GPIOA, &GPIO_InitStructure);
	GPIO_Init(GPIOF, &GPIO_InitStructure);
	
	GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_0|GPIO_Pin_1|GPIO_Pin_2|GPIO_Pin_3|GPIO_Pin_4;
	
	//GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_All;
	GPIO_Init(GPIOB, &GPIO_InitStructure);
	WriteDO(GPIOB,GPIO_Pin_8,0);//EN
	WriteDO(GPIOB,GPIO_Pin_6,1);//??BRTS*
	
}	

void WriteDO(GPIO_TypeDef* GPIOx,uint32_t GPIO_Pin,unsigned char ucData)
{
	if (ucData>0)
		GPIOx->BSRR = GPIO_Pin;
	else
		GPIOx->BRR = GPIO_Pin;
}
unsigned char ReadDI(GPIO_TypeDef * GPIOx,uint32_t GPIO_Pin)
{
	return (GPIOx->IDR&GPIO_Pin)==GPIO_Pin;
}

/****************************************************
Function function:LED initialization
Input parameters:none
Output parameters:none
Note: Before calling this function, it is necessary to modify the macro definition LED pin in LED. h
****************************************************/
unsigned char ucLEDInitial=0;
void LED_Init(void)
{
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_AFIO,ENABLE);
	GPIO_PinRemapConfig(GPIO_Remap_SWJ_JTAGDisable, ENABLE);
	RCC_AHBPeriphClockCmd(LED_GPIO_CLK, ENABLE);
	DIO_Initial(LED_PORT,LED_PIN,GPIO_Mode_Out_PP,GPIO_Speed_10MHz);
	ucLEDInitial=1;
}

/****************************************************
Function function: LED on
Input parameter: None
Output parameter: None
****************************************************/
void LED_ON(void)
{
	if (ucLEDInitial==0) LED_Init();
	GPIO_SetBits(LED_PORT, LED_PIN);
	
}

/****************************************************
Function function: LED off
Input parameter: None
Output parameter: None
****************************************************/
void LED_OFF(void)
{
	if (ucLEDInitial==0) LED_Init();
	GPIO_ResetBits(LED_PORT, LED_PIN);
}

void LED_REVERSE(void)
{
	if (ucLEDInitial==0) LED_Init();
	if (GPIO_ReadOutputDataBit(LED_PORT, LED_PIN))	
		GPIO_ResetBits(LED_PORT, LED_PIN);
	else
		GPIO_SetBits(LED_PORT, LED_PIN);
}

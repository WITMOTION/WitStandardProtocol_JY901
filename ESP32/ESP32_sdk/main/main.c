/*
@ link : http://wit-motion.cn

@ Function:
1. Power on automatic detection sensor
2. Read acceleration, angular velocity, angle and magnetic field data
3. Set switching baud rate parameters

     ESP32              		JY901s
       +5        ----        	 VCC
       RX1  (GPIO_5)			  TX
       TX1  (GPIO_4)			  RX
       GND       ----       	 GND
------------------------------------
*/

#include <string.h>
#include <stdio.h>
#include "wit_c_sdk.h"
#include "driver/uart.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

#define BUF_SIZE 1024

#define ACC_UPDATE		0x01
#define GYRO_UPDATE		0x02
#define ANGLE_UPDATE	0x04
#define MAG_UPDATE		0x08
#define READ_UPDATE		0x80
static volatile char s_cDataUpdate = 0;
const uint32_t c_uiBaud[10] = {0, 4800, 9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600};
static void CmdProcess(char);
static void AutoScanSensor(void);
static void SensorUartSend(uint8_t *p_data, uint32_t uiSize);
static void SensorDataUpdata(uint32_t uiReg, uint32_t uiRegNum);
static void Delayms(uint16_t ucMs);
static void Usart1Init(uint32_t baud_rate);
static void CopeCmdData(unsigned char ucData);

static void Usart1_task(void *pvParameters)
{
	unsigned char ucTemp;

	Usart1Init(9600);

	while(1)
	{
		if(uart_read_bytes(UART_NUM_1, &ucTemp, 1, portMAX_DELAY) == 1)
			WitSerialDataIn(ucTemp);
	}
}

static void Usart0_task(void *pvParameters)
{
	unsigned char c;

	while(1)
	{
		if(scanf("%c", &c) != -1)
		{
			CopeCmdData(c);
		}
		else
		{
			vTaskDelay(100 / portTICK_PERIOD_MS);
		}
	}
}

void app_main(void)
{
	float fAcc[3], fGyro[3], fAngle[3];
	int i;

	xTaskCreate(Usart0_task, "Usart0_task", 4096, NULL, 5, NULL);
	xTaskCreate(Usart1_task, "Usart1_task", 4096, NULL, 5, NULL);

	WitInit(WIT_PROTOCOL_NORMAL, 0x50);
	WitSerialWriteRegister(SensorUartSend);
	WitRegisterCallBack(SensorDataUpdata);
	WitDelayMsRegister(Delayms);
	printf("\r\n********************** wit-motion normal example  ************************\r\n");
	AutoScanSensor();
	while (1)
	{
		if(s_cDataUpdate)
		{
			for(i = 0; i < 3; i++)
			{
				fAcc[i] = sReg[AX+i] / 32768.0f * 16.0f;
				fGyro[i] = sReg[GX+i] / 32768.0f * 2000.0f;
				fAngle[i] = sReg[Roll+i] / 32768.0f * 180.0f;
			}
			if(s_cDataUpdate & ACC_UPDATE)
			{
				printf("acc:%.3f %.3f %.3f\r\n", fAcc[0], fAcc[1], fAcc[2]);
				s_cDataUpdate &= ~ACC_UPDATE;
			}
			if(s_cDataUpdate & GYRO_UPDATE)
			{
				printf("gyro:%.3f %.3f %.3f\r\n", fGyro[0], fGyro[1], fGyro[2]);
				s_cDataUpdate &= ~GYRO_UPDATE;
			}
			if(s_cDataUpdate & ANGLE_UPDATE)
			{
				printf("angle:%.3f %.3f %.3f\r\n", fAngle[0], fAngle[1], fAngle[2]);
				s_cDataUpdate &= ~ANGLE_UPDATE;
			}
			if(s_cDataUpdate & MAG_UPDATE)
			{
				printf("mag:%d %d %d\r\n", sReg[HX], sReg[HY], sReg[HZ]);
				s_cDataUpdate &= ~MAG_UPDATE;
			}
		}
	}
}

static void Usart1Init(uint32_t baud_rate)
{
    /* Configure parameters of an UART driver,
     * communication pins and install the driver */
    uart_config_t uart_config = {
        .baud_rate = baud_rate,
        .data_bits = UART_DATA_8_BITS,
        .parity    = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
        .source_clk = UART_SCLK_DEFAULT,
    };

    ESP_ERROR_CHECK(uart_driver_install(UART_NUM_1, BUF_SIZE * 2, 0, 0, NULL, 0));
    ESP_ERROR_CHECK(uart_param_config(UART_NUM_1, &uart_config));
    ESP_ERROR_CHECK(uart_set_pin(UART_NUM_1, 4, 5, -1, -1));
}

void CopeCmdData(unsigned char ucData)
{
	static unsigned char s_ucData[50], s_ucRxCnt = 0;

	s_ucData[s_ucRxCnt++] = ucData;
	if(s_ucRxCnt<3)return;										//Less than three data returned
	if(s_ucRxCnt >= 50) s_ucRxCnt = 0;
	if(s_ucRxCnt >= 3)
	{
		if((s_ucData[1]=='\r'||s_ucData[1]=='\n') && (s_ucData[2]=='\r' || s_ucData[2]=='\n'))
		{
			CmdProcess(s_ucData[0]);
			memset(s_ucData,0,50);
			s_ucRxCnt = 0;
		}
		else
		{
			s_ucData[0] = s_ucData[1];
			s_ucData[1] = s_ucData[2];
			s_ucRxCnt = 2;

		}
	}

}
static void ShowHelp(void)
{
	printf("\r\n************************	 WIT_SDK_DEMO	************************");
	printf("\r\n************************          HELP           ************************\r\n");
	printf("UART SEND:a\\r\\n   Acceleration calibration.\r\n");
	printf("UART SEND:m\\r\\n   Magnetic field calibration,After calibration send:   e\\r\\n   to indicate the end\r\n");
	printf("UART SEND:U\\r\\n   Bandwidth increase.\r\n");
	printf("UART SEND:u\\r\\n   Bandwidth reduction.\r\n");
	printf("UART SEND:B\\r\\n   Baud rate increased to 115200.\r\n");
	printf("UART SEND:b\\r\\n   Baud rate reduction to 9600.\r\n");
	printf("UART SEND:R\\r\\n   The return rate increases to 10Hz.\r\n");
	printf("UART SEND:r\\r\\n   The return rate reduction to 1Hz.\r\n");
	printf("UART SEND:C\\r\\n   Basic return content: acceleration, angular velocity, angle, magnetic field.\r\n");
	printf("UART SEND:c\\r\\n   Return content: acceleration.\r\n");
	printf("UART SEND:h\\r\\n   help.\r\n");
	printf("******************************************************************************\r\n");
}

static void CmdProcess(char s_cCmd)
{
	switch(s_cCmd)
	{
		case 'a':
			if(WitStartAccCali() != WIT_HAL_OK)
				printf("\r\nSet AccCali Error\r\n");
			break;
		case 'm':
			if(WitStartMagCali() != WIT_HAL_OK)
				printf("\r\nSet MagCali Error\r\n");
			break;
		case 'e':
			if(WitStopMagCali() != WIT_HAL_OK)
				printf("\r\nSet MagCali Error\r\n");
			break;
		case 'u':
			if(WitSetBandwidth(BANDWIDTH_5HZ) != WIT_HAL_OK)
				printf("\r\nSet Bandwidth Error\r\n");
			break;
		case 'U':
			if(WitSetBandwidth(BANDWIDTH_256HZ) != WIT_HAL_OK)
				printf("\r\nSet Bandwidth Error\r\n");
			break;
		case 'B':
			if(WitSetUartBaud(WIT_BAUD_115200) != WIT_HAL_OK)
				printf("\r\nSet Baud Error\r\n");
			else
				uart_set_baudrate(UART_NUM_1, 115200);
			break;
		case 'b':
			if(WitSetUartBaud(WIT_BAUD_9600) != WIT_HAL_OK)
				printf("\r\nSet Baud Error\r\n");
			else
				uart_set_baudrate(UART_NUM_1, 9600);
			break;
		case 'R':
			if(WitSetOutputRate(RRATE_10HZ) != WIT_HAL_OK)
				printf("\r\nSet Rate Error\r\n");
			break;
		case 'r':
			if(WitSetOutputRate(RRATE_1HZ) != WIT_HAL_OK)
				printf("\r\nSet Rate Error\r\n");
			break;
		case 'C':
			if(WitSetContent(RSW_ACC|RSW_GYRO|RSW_ANGLE|RSW_MAG) != WIT_HAL_OK)
				printf("\r\nSet RSW Error\r\n");
			break;
		case 'c':
			if(WitSetContent(RSW_ACC) != WIT_HAL_OK)
				printf("\r\nSet RSW Error\r\n");
			break;
		case 'h':
			ShowHelp();
			break;
	}
}

static void SensorUartSend(uint8_t *p_data, uint32_t uiSize)
{
	uart_write_bytes(UART_NUM_1, (const char*)p_data, uiSize);
}

static void Delayms(uint16_t usMs)
{
	vTaskDelay(usMs / portTICK_PERIOD_MS);
}

static void SensorDataUpdata(uint32_t uiReg, uint32_t uiRegNum)
{
	int i;
    for(i = 0; i < uiRegNum; i++)
    {
        switch(uiReg)
        {
//            case AX:
//            case AY:
            case AZ:
				s_cDataUpdate |= ACC_UPDATE;
            break;
//            case GX:
//            case GY:
            case GZ:
				s_cDataUpdate |= GYRO_UPDATE;
            break;
//            case HX:
//            case HY:
            case HZ:
				s_cDataUpdate |= MAG_UPDATE;
            break;
//            case Roll:
//            case Pitch:
            case Yaw:
				s_cDataUpdate |= ANGLE_UPDATE;
            break;
            default:
				s_cDataUpdate |= READ_UPDATE;
			break;
        }
		uiReg++;
    }
}

static void AutoScanSensor(void)
{
	int i, iRetry;

	for(i = 1; i < 10; i++)
	{
		uart_set_baudrate(UART_NUM_1, c_uiBaud[i]);
		iRetry = 2;
		do
		{
			s_cDataUpdate = 0;
			WitReadReg(AX, 3);
			Delayms(100);
			if(s_cDataUpdate != 0)
			{
				printf("%lu baud find sensor\r\n\r\n", c_uiBaud[i]);
				ShowHelp();
				return ;
			}
			iRetry--;
		}while(iRetry);
	}
	printf("can not find sensor\r\n");
	printf("please check your connection\r\n");
}


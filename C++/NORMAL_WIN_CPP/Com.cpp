#include <stdio.h>
#include <tchar.h>
#include <windows.h>
#include "Com.h"


#define		iBufferSize 1000
#define     UARTBufferLength 1000
#undef  SYNCHRONOUS_MODE

static HANDLE		 hComDev         =NULL;
static HANDLE		 hCOMThread      =NULL;
static OVERLAPPED	 stcWriteStatus  = {0};
static OVERLAPPED	 stcReadStatus   = {0};

#ifdef SYNCHRONOUS_MODE
static HANDLE		 hReceiveEvent[TOTAL_PORT_NUM]   ={NULL};
#endif

static volatile char chrUARTBuffers[UARTBufferLength]={0};
static volatile unsigned long ulUARTBufferStart={0}, ulUARTBufferEnd=0;

signed char SendUARTMessageLength(const char chrSendBuffer[],const unsigned short usLen)
{
	DWORD iR;
	DWORD dwRes;
	DCB dcb;

	GetCommState(hComDev ,&dcb);
	dcb.fDtrControl = 0;//DTR = 1;sending
	SetCommState(hComDev ,&dcb);

	if ( WriteFile(hComDev,chrSendBuffer,usLen,&iR,&(stcWriteStatus)) || GetLastError() != ERROR_IO_PENDING  ) 
		return -1;
	dwRes = WaitForSingleObject(stcWriteStatus.hEvent,1000);
	Sleep(10);
	dcb.fDtrControl = 1;//DTR = 0;receive
	SetCommState(hComDev ,&dcb);
	Sleep(10);

	if(dwRes != WAIT_OBJECT_0 || ! GetOverlappedResult(hComDev, &stcWriteStatus, &iR, FALSE))
		return 0;
	return 0;
}

DWORD WINAPI ReceiveCOMData(PVOID pParam)
{
	unsigned long uLen;
	DWORD	dwRes;
	COMSTAT Comstat;
	DWORD dwErrorFlags;
	char chrBuffer[iBufferSize]={0};


	while (1)
	{
		if ( ! ReadFile(hComDev,chrBuffer,iBufferSize-1,&uLen,&(stcReadStatus)) )
		{
			dwRes = GetLastError() ;
			if ( dwRes != ERROR_IO_PENDING)
			{
				ClearCommError(hComDev,&dwErrorFlags,&Comstat);
				continue;
			}

			WaitForSingleObject(stcReadStatus.hEvent,INFINITE);
			if ( !GetOverlappedResult(hComDev, &(stcReadStatus), &uLen, FALSE))continue;
			if(uLen <= 0)continue;


			extern void ComRxCallBack(char *p_data, UINT32 uiSize);
			ComRxCallBack(chrBuffer, uLen);

#ifdef SYNCHRONOUS_MODE
			SetEvent(hReceiveEvent[ucComNumber]);
#endif
			continue;
		}

		if(uLen <= 0)			continue;

#ifdef SYNCHRONOUS_MODE
		SetEvent(hReceiveEvent[ucComNumber]);
#endif

	}
	return 0;
}

signed char OpenCOMDevice(const unsigned long ulPortNo,const unsigned long ulBaundrate)
{
	DWORD dwThreadParam;
	COMSTAT Comstat;
	DWORD dwErrorFlags;
	DWORD dwRes;
	DCB dcb;
	COMMTIMEOUTS comTimeOut;
	TCHAR PortName[10] = {'\\','\\','.','\\','C','O','M',0,0,0};//"\\\\.\\COM";
	TCHAR chrTemple[5]={0};



	_itot_s(ulPortNo,chrTemple,10);
	_tcscat_s(PortName,chrTemple);

	if((hComDev = CreateFile(PortName,GENERIC_READ|GENERIC_WRITE,0,NULL,OPEN_EXISTING,FILE_FLAG_OVERLAPPED ,NULL))==INVALID_HANDLE_VALUE)
	{
		dwRes=GetLastError();
		return -1;
	}

	SetupComm(hComDev ,iBufferSize,iBufferSize);
	GetCommState(hComDev ,&dcb);
	dcb.BaudRate = ulBaundrate;
	dcb.fParity = NOPARITY;
	dcb.ByteSize=8;
	dcb.fDtrControl = 0;//DTR = 0;receive
	dcb.fRtsControl = 0;//RTS = 0;receive
	dcb.StopBits=ONESTOPBIT;

	SetCommState(hComDev ,&dcb);
	ClearCommError(hComDev ,&dwErrorFlags,&Comstat);
	dwRes = GetLastError();

	comTimeOut.ReadIntervalTimeout = 5;				
	comTimeOut.ReadTotalTimeoutMultiplier = 10;		
	comTimeOut.ReadTotalTimeoutConstant = 100;		
	comTimeOut.WriteTotalTimeoutMultiplier = 5;		
	comTimeOut.WriteTotalTimeoutConstant = 5;		
	SetCommTimeouts(hComDev ,&comTimeOut);	

	stcWriteStatus .hEvent = CreateEvent(NULL,TRUE,FALSE,NULL);
	stcReadStatus .hEvent = CreateEvent(NULL,TRUE,FALSE,NULL);
	stcReadStatus.Internal = 0;
	stcReadStatus.InternalHigh = 0;
	stcReadStatus.Offset = 0;
	stcReadStatus.OffsetHigh = 0;
	hCOMThread = CreateThread(NULL,0,(LPTHREAD_START_ROUTINE)ReceiveCOMData,&dwThreadParam,0,NULL);
	SetThreadPriority(hCOMThread,THREAD_PRIORITY_NORMAL);
	Sleep(200);

	return 0;

} 

signed char SetBaundrate(const unsigned long ulBaundrate)
{

	DCB dcb;	
	GetCommState(hComDev ,&dcb);
	dcb.BaudRate = ulBaundrate;
	SetCommState(hComDev ,&dcb);
	return 0;

} 
void CloseCOMDevice()
{
	ulUARTBufferEnd = 0;
	ulUARTBufferStart = 0;
	TerminateThread(hCOMThread,0);
	WaitForSingleObject(hCOMThread,10000);
	PurgeComm(hComDev,PURGE_TXABORT|PURGE_RXABORT|PURGE_TXCLEAR|PURGE_RXCLEAR);
	CloseHandle(stcReadStatus.hEvent);
	CloseHandle(stcWriteStatus.hEvent);
	CloseHandle(hComDev);
}

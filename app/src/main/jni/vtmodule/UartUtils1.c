/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <pthread.h>
#include <jni.h>

#include "com_android_uart_api_UartUtils.h"
#include "android/log.h"

static const char *TAG = "JniUartUtils";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static int uartFd;

static speed_t getBaudrate(int baudrate)
{
	switch(baudrate) {
        case 2400: return B2400;
        case 4800: return B4800;
        case 9600: return B9600;
        case 19200: return B19200;
        case 38400: return B38400;
        case 57600: return B57600;
        case 115200: return B115200;
        case 576000: return B576000;
        case 1152000: return B1152000;
        case 1500000: return B1500000;
        default: return -1;
	}
}

/*
 * Class:     com_android_uart_api_UartUtils
 * Method:    NativeUartConfig
 * Signature: (IIICI)I
 */
JNIEXPORT jint JNICALL Java_com_android_uart_1api_UartUtils_NativeUartConfig
  (JNIEnv *env, jclass thiz, jint baud_rate, jint data_bits, jchar parity, jint stop_bits)
{
    int speed;
    struct termios new_cfg, old_cfg;

    tcflush(uartFd, TCIFLUSH);

    /*保存并测试现有串口参数设置，在这里如果串口号等出错，会有相关出错信息*/
    if(tcgetattr(uartFd, &old_cfg))  {
        LOGE("tcgetttr() failed");
        return -1;
    }

    /*配置为原始模式*/
    new_cfg = old_cfg;
    cfmakeraw(&new_cfg);

    /*设置波特率*/
    speed = getBaudrate(baud_rate);
    if (speed < 0) {
        LOGE("Invalid baudrate");
        return -1;
    }
    cfsetispeed(&new_cfg, speed);
    cfsetospeed(&new_cfg, speed);

    /*清空数据位的设置*/
    new_cfg.c_cflag &= ~CSIZE;

    /*设置数据位数*/
    switch(data_bits)
    {
       case 5:
            new_cfg.c_cflag |= CS5;
            break;
       case 6:
            new_cfg.c_cflag |= CS6;
            break;
        case 7:
            new_cfg.c_cflag |= CS7;
            break;
        case 8:
        default:
            new_cfg.c_cflag |= CS8;
            break;
    }

    /*设置奇偶校验位*/
    switch(parity)
    {
        case 'o':
        case 'O':
            new_cfg.c_cflag |= (PARODD | PARENB);
            new_cfg.c_iflag |= INPCK;
            break;
        case 'e':
        case 'E':
            new_cfg.c_cflag |=  PARENB;
            new_cfg.c_cflag &= ~PARODD;
            new_cfg.c_iflag |= INPCK;
            break;
        case 'n':
        case 'N':
        default:
            new_cfg.c_cflag &= ~PARENB;
            new_cfg.c_iflag &= ~INPCK;
            break;
    }

    /*设置停止位*/
    switch(stop_bits)
    {
        case 2:
            new_cfg.c_cflag |= CSTOPB;
            break;
        case 1:
        default:
            new_cfg.c_cflag &= ~CSTOPB;
            break;
    }

    /*设置等待时间和最小接收字符*/
    new_cfg.c_cc[VTIME] = 0;
    new_cfg.c_cc[VMIN] = 1;

    /*处理未接收字符*/
    tcflush(uartFd,TCIFLUSH);

    /*设置新配置*/
    if((tcsetattr(uartFd,TCSANOW, &new_cfg))!=0){
        LOGE("tcsetattr() failed");
        return -1;
    }

    return 0;
}

/*
 * Class:     com_android_uart_api_UartUtils
 * Method:    NativeUartOpen
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_android_uart_1api_UartUtils_NativeUartOpen
  (JNIEnv *env, jclass obj, jstring path)
{
    jobject mFileDescriptor;
    jboolean iscopy;

	const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);

	LOGD("Opening serial port %s with flags O_RDWR | O_NOCTTY | O_NONBLOCK", path_utf);
	//uartFd = open(path_utf, O_RDWR | O_NOCTTY | O_NONBLOCK);
	uartFd = open(path_utf, O_RDWR);
	LOGD("open uart fd=%d", uartFd);
	(*env)->ReleaseStringUTFChars(env, path, path_utf);
	if (uartFd < 0)
	{
	    LOGE("Can't open serial device %s\n", path_utf);
	}else{
        LOGI("open %s successfully!!! fd=%d", path_utf, uartFd);
    }

    return uartFd;
}

/*
 * Class:     com_android_uart_api_UartUtils
 * Method:    NativeUartClose
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_android_uart_1api_UartUtils_NativeUartClose
  (JNIEnv *env, jclass thiz)
{
    LOGD("close(fd = %d)", uartFd);
    close(uartFd);
}

/*
 * Class:     com_android_uart_api_UartUtils
 * Method:    NativeUartRead
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_com_android_uart_1api_UartUtils_NativeUartRecv
     (JNIEnv *env, jclass thiz, jint fd, jbyteArray jread_arr, jint len)
{
       int ret;
       int  i;

       unsigned char *buf = (unsigned char*)((*env)->GetByteArrayElements(env, jread_arr, NULL));

       ret = read(fd, buf, len);

       LOGD("JNI uart(fd=%d) recv data:", fd);
       for(i=0; i<ret; i++){
           LOGD("%#x", buf[i]);
       }

       (*env)->ReleaseByteArrayElements(env, jread_arr, (jbyte *)buf, 0);

       return ret;
}

/*
 * Class:     com_android_uart_api_UartUtils
 * Method:    NativeUartWrite
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_com_android_uart_1api_UartUtils_NativeUartSend
  (JNIEnv *env, jclass thiz, jint fd, jbyteArray jwrite_arr, jint len)
{
    int status;
    int i = 0;

    unsigned char *buf = (unsigned char*)((*env)->GetByteArrayElements(env, jwrite_arr, NULL));

    for(i = 0; i<len; i++){
        LOGD("JNI uart (fd=%d) write data : %#x\n", fd, buf[i]);
    }

    tcflush(fd, TCIFLUSH);
    status = write(fd, buf, len);

    return status;
}




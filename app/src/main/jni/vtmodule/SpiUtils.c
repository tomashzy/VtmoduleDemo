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

 #include <jni.h>
 #include <stdio.h>
 #include <fcntl.h>
 #include <stdlib.h>
 #include <assert.h>
 #include <termios.h>
 #include <unistd.h>
 #include <sys/types.h>
 #include <sys/stat.h>
 #include <fcntl.h>
 #include <string.h>
 #include <pthread.h>

#include "com_android_spi_api_SpiUtils.h"
#include "android/log.h"

static const char *TAG = "JniSpiUtils";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

#define SPI_IOC_MAGIC			'k'

/* Read / Write of SPI mode (SPI_MODE_0..SPI_MODE_3) (limited to 8 bits) */
#define SPI_IOC_RD_MODE			_IOR(SPI_IOC_MAGIC, 1, __u8)
#define SPI_IOC_WR_MODE			_IOW(SPI_IOC_MAGIC, 1, __u8)

/* Read / Write SPI bit justification */
#define SPI_IOC_RD_LSB_FIRST		_IOR(SPI_IOC_MAGIC, 2, __u8)
#define SPI_IOC_WR_LSB_FIRST		_IOW(SPI_IOC_MAGIC, 2, __u8)

/* Read / Write SPI device word length (1..N) */
#define SPI_IOC_RD_BITS_PER_WORD	_IOR(SPI_IOC_MAGIC, 3, __u8)
#define SPI_IOC_WR_BITS_PER_WORD	_IOW(SPI_IOC_MAGIC, 3, __u8)

/* Read / Write SPI device default max speed hz */
#define SPI_IOC_RD_MAX_SPEED_HZ		_IOR(SPI_IOC_MAGIC, 4, __u32)
#define SPI_IOC_WR_MAX_SPEED_HZ		_IOW(SPI_IOC_MAGIC, 4, __u32)

/* Read / Write of the SPI mode field */
#define SPI_IOC_RD_MODE32		_IOR(SPI_IOC_MAGIC, 5, __u32)
#define SPI_IOC_WR_MODE32		_IOW(SPI_IOC_MAGIC, 5, __u32)

static int spiFd;

/*
 * Class:     com_android_spi_api_SpiUnits
 * Method:    open
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_android_spi_1api_SpiUtils_NativeSpiOpen
  (JNIEnv *env, jobject thiz, jstring path)
{
	jboolean iscopy;

	const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
	LOGD("Opening spi port %s with flags O_RDWR | O_NOCTTY | O_NONBLOCK", path_utf);
	spiFd = open(path_utf, O_RDWR | O_NOCTTY | O_NONBLOCK);
	LOGD("open() fd = %d", spiFd);
	(*env)->ReleaseStringUTFChars(env, path, path_utf);
	if (spiFd < 0)
	{
	    LOGE("Can't open serial device %s\n", path_utf);
	}else{
        LOGI("open %s successfully!!! fd=%d", path_utf, spiFd);
    }

    return spiFd;
}

/*
 * Class:     com_android_spi_api_SpiUnits
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_spi_1api_SpiUtils_NativeSpiClose
  (JNIEnv *env, jobject thiz)
{
	LOGD("close(fd = %d)", spiFd);

    close(spiFd);
}

/*
 * Class:     com_android_spi_api_SpiUnits
 * Method:    read
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_com_android_spi_1api_SpiUtils_NativeSpiRead
(JNIEnv *env, jobject thiz, jint fd, jbyteArray jread_arr, jint len)
{
    int status;
    int  i = 0;

    unsigned char *buf = (unsigned char*)((*env)->GetByteArrayElements(env, jread_arr, NULL));

    status = read(fd, buf, len);

    for(i=0; i<status; i++){
        LOGD("JNI spi read data: %#x", buf[i]);
    }

    (*env)->ReleaseByteArrayElements(env, jread_arr, (jbyte *)buf, 0);

    return status;
}

/*
 * Class:     com_android_spi_api_SpiUnits
 * Method:    write
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_com_android_spi_1api_SpiUtils_NativeSpiWrite
  (JNIEnv *env, jobject thiz, jint fd, jbyteArray jwrite_arr, jint len)
{
    int status;
    int i = 0;

    unsigned char *buf = (unsigned char*)((*env)->GetByteArrayElements(env, jwrite_arr, NULL));

    for(i = 0; i<len; i++){
        LOGD("JNI spi write data : %#x\n", buf[i]);
    }

    status = write(fd, buf, len);

    return status;
}

/*
 * Class:     com_android_spi_api_SpiUnits
 * Method:    ioctl
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_android_spi_1api_SpiUtils_NativeSpiIoctl
  (JNIEnv *env, jobject thiz, jint fd, jint code, jint arg)
{
    int ret = -1;
    int cmd;

    LOGD("ioctl: code = %d, arg = %d, ret = %d\n", code, arg, ret);

    switch(code){
        case 1:cmd = SPI_IOC_RD_MODE;break;
        case 2:cmd = SPI_IOC_WR_MODE;break;
        case 3:cmd = SPI_IOC_RD_LSB_FIRST;break;
        case 4:cmd = SPI_IOC_WR_LSB_FIRST;break;
        case 5:cmd = SPI_IOC_RD_BITS_PER_WORD;break;
        case 6:cmd = SPI_IOC_WR_BITS_PER_WORD;break;
        case 7:cmd = SPI_IOC_RD_MAX_SPEED_HZ;break;
        case 8:cmd = SPI_IOC_WR_MAX_SPEED_HZ;break;
        case 9:cmd = SPI_IOC_RD_MODE32;break;
        case 10:cmd = SPI_IOC_WR_MODE32;break;
        default:cmd = 0;break;
    }

    ret = ioctl(fd, cmd, &arg);
    if(ret < 0) {
        LOGE("ioctl failure");
        return -1;
    }


    LOGD("ioctl: code = %#x, arg = %d, ret = %d\n", cmd, arg, ret);

    return arg;
}


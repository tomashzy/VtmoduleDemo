/*
 * Copyright 2021-2030 Cedric Priscal
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

#include "com_android_gpio_api_GpioUtils.h"
#include "android/log.h"

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

#define GPIO_IOC_MAGIC			'g'
#define GPIO_IO1_IOC_RD			_IOR(GPIO_IOC_MAGIC, 1, __u8)
#define GPIO_IO2_IOC_RD			_IOR(GPIO_IOC_MAGIC, 2, __u8)
#define GPIO_IO3_IOC_RD			_IOR(GPIO_IOC_MAGIC, 3, __u8)
#define GPIO_IO4_IOC_RD			_IOR(GPIO_IOC_MAGIC, 4, __u8)
#define GPIO_RST_IOC_RD			_IOR(GPIO_IOC_MAGIC, 5, __u8)

#define GPIO_IO1_IOC_WR			_IOW(GPIO_IOC_MAGIC, 6, __u8)
#define GPIO_IO2_IOC_WR			_IOW(GPIO_IOC_MAGIC, 7, __u8)
#define GPIO_IO3_IOC_WR			_IOW(GPIO_IOC_MAGIC, 8, __u8)
#define GPIO_IO4_IOC_WR			_IOW(GPIO_IOC_MAGIC, 9, __u8)
#define GPIO_RST_IOC_WR			_IOW(GPIO_IOC_MAGIC, 10, __u8)

#define GPIO_IO1_IOC_IN			_IOW(GPIO_IOC_MAGIC, 11, __u8)
#define GPIO_IO2_IOC_IN			_IOW(GPIO_IOC_MAGIC, 12, __u8)
#define GPIO_IO3_IOC_IN			_IOW(GPIO_IOC_MAGIC, 13, __u8)
#define GPIO_IO4_IOC_IN			_IOW(GPIO_IOC_MAGIC, 14, __u8)
#define GPIO_RST_IOC_IN			_IOW(GPIO_IOC_MAGIC, 15, __u8)

#define GPIO_IO1_IOC_OUT		_IOW(GPIO_IOC_MAGIC, 16, __u8)
#define GPIO_IO2_IOC_OUT		_IOW(GPIO_IOC_MAGIC, 17, __u8)
#define GPIO_IO3_IOC_OUT		_IOW(GPIO_IOC_MAGIC, 18, __u8)
#define GPIO_IO4_IOC_OUT		_IOW(GPIO_IOC_MAGIC, 19, __u8)
#define GPIO_RST_IOC_OUT		_IOW(GPIO_IOC_MAGIC, 20, __u8)

static const char *TAG = "JniGpioUnits";
static int fd;

/*
 * Class:     com_android_gpio_api_GpioUtils
 * Method:    NativeGpioOpen
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_android_gpio_1api_GpioUtils_NativeGpioOpen
  (JNIEnv *env, jclass thiz, jstring path)
{
	jboolean iscopy;

	const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
	LOGD("Opening gpio port %s with flags 0x%x", path_utf, O_RDWR);
	fd = open(path_utf, O_RDWR);
	LOGD("open() fd = %d", fd);
	(*env)->ReleaseStringUTFChars(env, path, path_utf);
	if (fd < 0)
	{
		/* Throw an exception */
		LOGE("Cannot open port");
		/* TODO: throw an exception */
	}

    return fd;
}

/*
 * Class:     com_android_gpio_api_GpioUtils
 * Method:    NativeGpioIoctl
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_android_gpio_1api_GpioUtils_NativeGpioIoctl
  (JNIEnv *env, jclass obj, jint code, jint arg)
{
    int ret = -1;
    int cmd;

   LOGD("----> code = %d, arg = %d, ret = %d", code, arg, ret);

    switch(code){
        case 1:cmd = GPIO_IO1_IOC_RD;break;
        case 2:cmd = GPIO_IO2_IOC_RD;break;
        case 3:cmd = GPIO_IO3_IOC_RD;break;
        case 4:cmd = GPIO_IO4_IOC_RD;break;
        case 5:cmd = GPIO_RST_IOC_RD;break;

        case 6:cmd = GPIO_IO1_IOC_WR;break;
        case 7:cmd = GPIO_IO2_IOC_WR;break;
        case 8:cmd = GPIO_IO3_IOC_WR;break;
        case 9:cmd = GPIO_IO4_IOC_WR;break;
        case 10:cmd = GPIO_RST_IOC_WR;break;

        case 11:cmd = GPIO_IO1_IOC_IN;break;
        case 12:cmd = GPIO_IO2_IOC_IN;break;
        case 13:cmd = GPIO_IO3_IOC_IN;break;
        case 14:cmd = GPIO_IO4_IOC_IN;break;
        case 15:cmd = GPIO_RST_IOC_IN;break;

        case 16:cmd = GPIO_IO1_IOC_OUT;break;
        case 17:cmd = GPIO_IO2_IOC_OUT;break;
        case 18:cmd = GPIO_IO3_IOC_OUT;break;
        case 19:cmd = GPIO_IO4_IOC_OUT;break;
        case 20:cmd = GPIO_RST_IOC_OUT;break;

        default:cmd = 0;break;
    }

    if(arg > 1)
        arg = 1;
    if(arg < 0)
        arg = 0;

    ret = ioctl(fd, cmd, &arg);

    if(ret < 0) {
    	LOGE("ioctl failure");
    	return -1;
    }

    LOGD("<---- code = %#x, arg = %d, ret = %d", cmd, arg, ret);

    return arg;
}

/*
 * Class:     com_android_gpio_api_GpioUtils
 * Method:    NativeGpioClose
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_com_android_gpio_1api_GpioUtils_NativeGpioClose
  (JNIEnv *env, jclass thiz)
{
	LOGD("close(fd = %d)", fd);

    close(fd);
}

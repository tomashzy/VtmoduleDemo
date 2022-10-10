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

#include "com_android_fan_api_FanUtils.h"
#include "android/log.h"

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

#define PWM_IOC_MAGIC		'p'
#define PWM_IOC_ON			_IOR(PWM_IOC_MAGIC, 1, __u32)
#define PWM_IOC_OFF		    _IOR(PWM_IOC_MAGIC, 2, __u8)
#define PWM_IOC_STATUS		_IOR(PWM_IOC_MAGIC, 3, __u32)

static const char *TAG = "JniFanUnits";
static int fd;

/*
 * Class:     com_android_fan_api_FanUtils
 * Method:    NativeFanOpen
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_android_fan_1api_FanUtils_NativeFanOpen
  (JNIEnv *env, jclass thiz, jstring path)
{
	jboolean iscopy;

	const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
	LOGD("Opening pwm port %s with flags 0x%x", path_utf, O_RDWR);
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
 * Class:     com_android_fan_api_FanUtils
 * Method:    NativeFanIoctl
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_android_fan_1api_FanUtils_NativeFanIoctl
  (JNIEnv *env, jclass obj, jint code, jint arg)
{
    int ret = -1;
    int cmd;

   LOGD("----> code = %d, arg = %d, ret = %d", code, arg, ret);

    switch(code){
        case 1:cmd = PWM_IOC_ON;break;
        case 2:cmd = PWM_IOC_OFF;break;
        case 3:cmd = PWM_IOC_STATUS;break;
        default:cmd = 0;break;
    }

    ret = ioctl(fd, cmd, &arg);
    if(ret < 0) {
    	LOGE("ioctl failure");
    	return -1;
    }

    LOGD("<---- code = %#x, arg = %d, ret = %d", cmd, arg, ret);

    return arg;
}

/*
 * Class:     com_android_fan_api_FanUtils
 * Method:    NativeFanClose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_fan_1api_FanUtils_NativeFanClose
  (JNIEnv *env, jclass thiz)
{
	LOGD("close(fd = %d)", fd);

    close(fd);
}

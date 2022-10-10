#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <jni.h>
#include <pthread.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "com_android_net_api_NetUtils.h"
#include "android/log.h"

static const char *TAG = "JniNetUtils";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static int server_fd = 0;
static int client_fd = 0;
static struct sockaddr_in servaddr,cliaddr;

/*
 * Class:     com_android_net_api_NetUtils
 * Method:    NativeNetOpen
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_android_net_1api_NetUtils_NativeNetOpen
  (JNIEnv *env, jobject thiz, jstring ip, jint port)
{
    int on = 1;

    if((server_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        goto error;
    }

    if((client_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        goto error;
    }

    memset(&cliaddr, 0, sizeof(cliaddr));
    cliaddr.sin_family = AF_INET;
    cliaddr.sin_addr.s_addr = inet_addr(ip);
    cliaddr.sin_port = htons(port);

    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(port);

    if(setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        LOGE("setsockopt failed");
        goto error;
    }

    if(bind(server_fd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        LOGE("socket bind failed");
        goto error;
    }

    return 0;

error:
    LOGE("socket init failed");
    if(server_fd > 0)
        close(server_fd);
    server_fd = 0;

    if(client_fd > 0)
        close(client_fd);
    client_fd = 0;

    return -1;
}

/*
 * Class:     com_android_net_api_NetUtils
 * Method:    NativeNetClose
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_android_net_1api_NetUtils_NativeNetClose
  (JNIEnv *env, jobject thiz)
{
    if(server_fd > 0){
        LOGD("close(server_fd = %d)", server_fd);
        close(server_fd);
        server_fd = 0;
    }

    if(client_fd > 0){
        LOGD("close(client_fd = %d)", client_fd);
        close(client_fd);
        client_fd = 0;
    }
}

/*
 * Class:     com_android_net_api_NetUtils
 * Method:    NativeNetRead
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_com_android_net_1api_NetUtils_NativeNetRead
  (JNIEnv *env, jobject thiz, jbyteArray jread_arr, jint length)
{
    int status;
    int i;
    socklen_t peerlen = sizeof(servaddr);

    unsigned char *buf = (unsigned char*)((*env)->GetByteArrayElements(env, jread_arr, NULL));

    status = recvfrom(server_fd, buf, length, 0, (struct sockaddr *)&servaddr, &peerlen);
    if(status < 0){
        LOGE("recv_from_socket failed and return %d",status);
        return -1;
    }

    //for(i=0; i<status; i++){
        //LOGD("JNI Net recv data: %#x", buf[i]);
    //}

    (*env)->ReleaseByteArrayElements(env, jread_arr, (jbyte *)buf, 0);

    return status;
}

/*
 * Class:     com_android_net_api_NetUtils
 * Method:    NativeNetWrite
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_com_android_net_1api_NetUtils_NativeNetWrite
  (JNIEnv *env, jobject thiz, jbyteArray jwrite_arr, jint length)
{
    int i;
	int status;
	socklen_t peerlen = sizeof(cliaddr);

	unsigned char *buf = (unsigned char*)((*env)->GetByteArrayElements(env, jwrite_arr, NULL));

	status = sendto(client_fd, buf, length, 0, (struct sockaddr *)&cliaddr, peerlen);
	if(status < 0) {
	    LOGE("send_to_socket failed and return %d",status);
	    return -1;
	}
	else{
		for(i=0; i<length; i++){
            LOGD("JNI Net send data: %#x", buf[i]);
        }
	}

	return status;
}


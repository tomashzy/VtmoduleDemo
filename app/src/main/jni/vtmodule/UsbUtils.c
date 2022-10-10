#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <jni.h>

#include "../libusb/libusb/libusb.h"
#include "com_android_usb_api_UsbUtils.h"
#include "android/log.h"

static const char *TAG = "UsbUtils";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

#define VENDOR_ID       		0x0483
#define PRODUCT_ID      		0x5868

#define	EP_IN  					0x02
#define EP_OUT 					0x81

#define BMREQUEST_HOST_TO_DEVICE        0
#define BMREQUEST_DEVICE_TO_HOST        1

#define BMREQUEST_TO_DEVICE             0
#define BMREQUEST_TO_INTERFACE          1
#define BMREQUEST_TO_ENDPOINT           2
#define BMREQUEST_TO_OTHER              3

static pthread_mutex_t usb_mutex;
static libusb_device_handle*	usb_handle = NULL;
static uint8_t                 idx, rc;
static libusb_device 			**devs;

/*
 * Class:     com_android_usb_api_UsbUtils
 * Method:    NativeUsbOpen
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_android_usb_1api_UsbUtils_NativeUsbOpen
  (JNIEnv *env, jobject thiz)
{
    int32_t devCount;

	rc = libusb_init(NULL);
	if(rc != 0) {
		LOGE("libusb init failed");
		LOGE("libusb_init error no:%d" , rc);
		return -1;
	}

	//Get usb device list
	devCount = libusb_get_device_list(NULL , &devs);
	if(devCount < 0)
		LOGE("Get device list error");
	for(idx = 0; idx < devCount; ++idx) {
		libusb_device *device = devs[idx];
		struct libusb_device_descriptor desc;
		rc = libusb_get_device_descriptor(device, &desc);
		if(rc != 0)
			LOGE("libusb_get_device_descriptor error no:%d", rc);
		LOGD("Vendor:Device = %04x:%04x\n" , desc.idVendor , desc.idProduct);
	}

	usb_handle = libusb_open_device_with_vid_pid(NULL, VENDOR_ID, PRODUCT_ID);
	if(usb_handle == NULL) {
		LOGE("Could't find vtmodule usb device");
		libusb_free_device_list(devs, 1);
		libusb_exit(NULL);
		return -1;
	} else {
		LOGD("Device opened");
		libusb_free_device_list(devs, 1);
		if(libusb_kernel_driver_active(usb_handle, 0) == 1) {
			LOGD("Kernel Driver Active");
			if(libusb_detach_kernel_driver(usb_handle , 0) == 0)
				LOGD("kernel Driver Detached!");
		}
	}
	rc = libusb_claim_interface(usb_handle, 0);
	if(rc < 0) {
	 	LOGE("Cannot Claim Interface 0");
		 return -2;
	}

	LOGD("Usb device open success");

	return 0;
}

/*
 * Class:     com_android_usb_api_UsbUtils
 * Method:    NativeUsbClose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_usb_1api_UsbUtils_NativeUsbClose
  (JNIEnv *env, jobject thiz)
{
	if(usb_handle) {
		libusb_release_interface(usb_handle, 0);
        libusb_close(usb_handle);
        usb_handle = NULL;
        libusb_exit(NULL);
    }
}

/*
 * Class:     com_android_usb_api_UsbUtils
 * Method:    NativeUsbControlTransfer
 * Signature: (IIII[BI)I
 */
JNIEXPORT jint JNICALL Java_com_android_usb_1api_UsbUtils_NativeUsbControlTransfer
  (JNIEnv *env, jobject thiz, jint direction, jint request, jint value, jint index, jbyteArray arry, jint length)
{
    int ret;

	if (usb_handle == NULL)
    {
        return -1;
    }

    uint8_t RequestType = 0x40 | (direction << 7) | BMREQUEST_TO_INTERFACE;

	unsigned char *buf = (unsigned char*)((*env)->GetByteArrayElements(env, arry, NULL));

	ret = libusb_control_transfer(usb_handle, (uint8_t)RequestType, (uint8_t)request, (uint16_t)value, (uint16_t)index, buf, length, 5000);

    if(RequestType & 0x80)
        (*env)->ReleaseByteArrayElements(env, arry, (jbyte *)buf, 0);

    return ret;
}

/*
 * Class:     com_android_usb_api_UsbUtils
 * Method:    NativeUsbBulkTransfer
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_com_android_usb_1api_UsbUtils_NativeUsbBulkTransfer
  (JNIEnv *env, jobject thiz, jint pID, jbyteArray arry, jint len, jint actual_len)
{
    int ret;

    if (usb_handle == NULL || !pID)
    {
        return -1;
		LOGD("usb_bulk_write error");
    }

	unsigned char *buf = (unsigned char*)((*env)->GetByteArrayElements(env, arry, NULL));

	ret = libusb_bulk_transfer(usb_handle, (uint8_t)pID, buf, len, &actual_len, 5000);

    if(pID == EP_IN)
        (*env)->ReleaseByteArrayElements(env, arry, (jbyte *)buf, 0);

	return ret;
}
















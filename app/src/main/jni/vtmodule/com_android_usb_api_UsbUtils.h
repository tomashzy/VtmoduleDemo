/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_android_usb_api_UsbUtils */

#ifndef _Included_com_android_usb_api_UsbUtils
#define _Included_com_android_usb_api_UsbUtils
#ifdef __cplusplus
extern "C" {
#endif
#undef com_android_usb_api_UsbUtils_EP_IN
#define com_android_usb_api_UsbUtils_EP_IN 2L
#undef com_android_usb_api_UsbUtils_EP_OUT
#define com_android_usb_api_UsbUtils_EP_OUT 129L
#undef com_android_usb_api_UsbUtils_BMREQUEST_HOST_TO_DEVICE
#define com_android_usb_api_UsbUtils_BMREQUEST_HOST_TO_DEVICE 0L
#undef com_android_usb_api_UsbUtils_BMREQUEST_DEVICE_TO_HOST
#define com_android_usb_api_UsbUtils_BMREQUEST_DEVICE_TO_HOST 1L
#undef com_android_usb_api_UsbUtils_WRITE_DATA
#define com_android_usb_api_UsbUtils_WRITE_DATA 0L
#undef com_android_usb_api_UsbUtils_READ_DATA
#define com_android_usb_api_UsbUtils_READ_DATA 1L
/*
 * Class:     com_android_usb_api_UsbUtils
 * Method:    NativeUsbOpen
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_android_usb_1api_UsbUtils_NativeUsbOpen
  (JNIEnv *, jclass);

/*
 * Class:     com_android_usb_api_UsbUtils
 * Method:    NativeUsbClose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_android_usb_1api_UsbUtils_NativeUsbClose
  (JNIEnv *, jclass);

/*
 * Class:     com_android_usb_api_UsbUtils
 * Method:    NativeUsbControlTransfer
 * Signature: (IIII[BI)I
 */
JNIEXPORT jint JNICALL Java_com_android_usb_1api_UsbUtils_NativeUsbControlTransfer
  (JNIEnv *, jclass, jint, jint, jint, jint, jbyteArray, jint);

/*
 * Class:     com_android_usb_api_UsbUtils
 * Method:    NativeUsbBulkTransfer
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_com_android_usb_1api_UsbUtils_NativeUsbBulkTransfer
  (JNIEnv *, jclass, jint, jbyteArray, jint, jint);

#ifdef __cplusplus
}
#endif
#endif

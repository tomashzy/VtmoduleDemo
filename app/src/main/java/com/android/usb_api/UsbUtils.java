/*
 * Copyright 2022 Cedric Priscal
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

package com.android.usb_api;

public class UsbUtils {

	//private static final String TAG = "UsbUtils";

	private static final int EP_IN  = 0x02;
	private static final int EP_OUT = 0x81;

	private static final int BMREQUEST_HOST_TO_DEVICE =  0;
	private static final int BMREQUEST_DEVICE_TO_HOST =  1;

	private static final int WRITE_DATA =  0;
	private static final int READ_DATA =  1;

    private int[] actual_len = {0};

	public UsbUtils() {
	}

	public int UsbOpen(){
		return NativeUsbOpen();
	}

	public int UsbSend(final byte[] buf, int len) {
        return NativeUsbControlTransfer(BMREQUEST_HOST_TO_DEVICE, WRITE_DATA,0,0,buf, len);
	}

	public int UsbRecv(final byte[] buf, int len) {
        return NativeUsbControlTransfer(BMREQUEST_DEVICE_TO_HOST, READ_DATA,0,0,buf, len);
	}

	public int UsbBulkSend(final byte[] buf, int len)
	{
        return NativeUsbBulkTransfer(EP_IN, buf, len, actual_len[0]);
	}

	public int UsbBulkRecv(final byte[] buf, int len)
	{
        return NativeUsbBulkTransfer(EP_OUT, buf, len, actual_len[0]);
	}

	public void UsbClose() {
	    NativeUsbClose();
	}

	// JNI
	private native static int NativeUsbOpen();
	private native static void NativeUsbClose();
	private native static int NativeUsbControlTransfer(int direction, int request, int value, int index, byte[] buffer, int length);
	private native static int NativeUsbBulkTransfer(int pID, byte[] buffer, int bufferSize, int actual_len);

	static {
		System.loadLibrary("vtmodule");
	}
}

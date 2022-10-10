/*
 * Copyright 2009 Cedric Priscal
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

package com.android.spi_api;

import android.util.Log;

public class SpiUtils {

	private static final String TAG = "NativeSpiUtils";

	private int spiFd;

    public SpiUtils() {

    }

	public int SpiOpen(String path){

		spiFd = NativeSpiOpen(path);

		if (spiFd < 0) {
			Log.e(TAG, "native open returns null");
		}

		return spiFd;
	}

    public int SpiSend(int fd, final byte[] buffer, int len) {

    	return NativeSpiWrite(fd, buffer, len);
    }

	public int SpiRecv(int fd, final byte[] buffer, int len) {

    	return NativeSpiRead(fd, buffer, len);
	}

	public int SpiIoctl(int fd, final int code, int arg) {
		int ret;

		ret = NativeSpiIoctl(fd, code, arg);
		if(ret < 0) {
			Log.e(TAG,"SpiIoctl failure");
		}

		Log.d(TAG,"code = " + code  + ", arg = " + arg  + ", ret = " + ret);

		return ret;
	}

	public void SpiClose() {
		if(spiFd > 0) {
			NativeSpiClose();
			spiFd = -1;
			Log.i(TAG, "Spi Close");
		}
	}

	// JNI
	private native static int NativeSpiOpen(String path);
	private native static void NativeSpiClose();
	private native static int NativeSpiRead(int fd, byte[] buf, int len);
	private native static int NativeSpiWrite(int fd, byte[] buf, int len);
	private native static int NativeSpiIoctl(int fd, int cmd, int arg);

	static {
		System.loadLibrary("vtmodule");
	}
}

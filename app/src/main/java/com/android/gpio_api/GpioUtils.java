/*
 * Copyright 2020 Cedric Priscal
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

package com.android.gpio_api;

import android.util.Log;

import java.io.File;
import java.io.IOException;

public class GpioUtils {

	private static final String TAG = "NativeGpioUtils";

	public GpioUtils(){

	}

	public int GpioOpen(String path) {
		return NativeGpioOpen(path);
	}

	public int GpioIoctl(final int code, int arg) {

		int ret = NativeGpioIoctl(code, arg);
		if(ret < 0) {
			Log.e(TAG,"GpioIoctl failure");
		}

		Log.d(TAG,"code = " + code  + ", arg = " + arg  + ", ret = " + ret);

		return ret;
	}

	public void GpioClose() {
		NativeGpioClose();
	}

	public static native int NativeGpioOpen(String path);
	public static native int NativeGpioIoctl(int code, int arg);
	public static native void NativeGpioClose();

	static {
		System.loadLibrary("vtmodule");
	}
}

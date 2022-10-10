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

package com.android.fan_api;

import android.util.Log;

public class FanUtils {

	private static final String TAG = "NativeFanUtils";

	public FanUtils() {

	}

	public int FanOpen(String path){
		 return NativeFanOpen(path);
	}

	public int FanIoctl(final int code, int arg) {

		int ret = NativeFanIoctl(code, arg);
		if(ret < 0) {
			Log.e(TAG,"PwmIoctl failure");
		}

		Log.d(TAG,"code = " + code  + ", arg = " + arg  + ", ret = " + ret);

		return ret;
	}

	public void FanClose() {
		NativeFanClose();
	}

	public static native int NativeFanOpen(String path);
	public static native int NativeFanIoctl(int code, int arg);
	public static native void NativeFanClose();

	static {
		System.loadLibrary("vtmodule");
	}
}

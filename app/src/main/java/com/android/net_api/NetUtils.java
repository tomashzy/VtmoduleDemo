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

package com.android.net_api;

public class NetUtils {

	//private static final String TAG = "NativeNetUtils";

	public NetUtils() {

	}

	public int NetOpen(String ip, int port) {
		return NativeNetOpen(ip, port);
	}

	public int NetSend(final byte[] buf, int len)
	{
		return NativeNetWrite(buf, len);
	}

	public int NetRecv(final byte[] buf, int len)
	{
		return NativeNetRead(buf, len);
	}

	public void NetClose() {
		NativeNetClose();
	}

	// JNI
	private native static int NativeNetOpen(String ip, int port);
	private native static void NativeNetClose();
	private native static int NativeNetRead(byte[] buf, int len);
	private native static int NativeNetWrite(byte[] buf, int len);

	static {
		System.loadLibrary("vtmodule");
	}
}

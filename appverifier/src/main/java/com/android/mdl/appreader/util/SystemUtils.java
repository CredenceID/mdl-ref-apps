package com.android.mdl.appreader.util;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

public class SystemUtils {

	public static boolean isUSBDeviceAttached(Context context, int VID, int PID) {

		final UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		if (null == manager)
			return false;

		final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

		for (UsbDevice device : deviceList.values()) {
			if (device.getVendorId() == VID && device.getProductId() == PID)
				return true;
		}
		return false;
	}


	public static void excGreenLeft() {
		Log.d("TAG", "exec gren left..");
		String value = "255";
		String d2 =  "echo "+ value +" > /sys/class/leds/d2/brightness";
		String d5 =  "echo"+ value +" > /sys/class/leds/d5/brightness";

		String d8 =  "echo "+ value +" > /sys/class/leds/d8/brightness";
		String d5_1 =  "echo "+value+" > /sys/class/leds/d5_1/brightness";
		String d8_1 =  "echo "+value+" > /sys/class/leds/d8_1/brightness";
		String d2_1 =  "echo "+value+" > /sys/class/leds/d2_1/brightness";
		String d5_2 =  "echo "+value+" > /sys/class/leds/d5_2/brightness";

		String d8_2 =  "echo "+value+" > /sys/class/leds/d8_2/brightness";
		String d2_2 =  "echo "+value+" > /sys/class/leds/d2_2/brightness";

		execRoot(d2);
		execRoot(d5);
		execRoot(d8);
		execRoot(d5_1);
		execRoot(d8_1);
		execRoot(d2_1);
		execRoot(d5_2);
		execRoot(d8_2);
		execRoot(d2_2);
	}


	@SuppressWarnings("UnusedReturnValue")
	public static boolean execRoot(String cmd) {
		boolean success = true;
		Process p = null;
		try {
			/* Open new process with sudo. */
			p = Runtime.getRuntime().exec("su");

			/* Enter command for process to execute. */
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			os.close();

			/* Wait for command execution to complete. */
			SystemUtils.sleep(200);
			p.waitFor();

		} catch (IOException | InterruptedException ignore) {
			Log.w("TAG", "SystemUtils.execRoot(): Failed to execute command. "+ignore.getLocalizedMessage());
			success = false;
		} finally {
			if (null != p)
				p.destroy();
		}
		return success;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean exec(String cmd) {

		boolean success = true;
		Process p = null;

		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();


		} catch (IOException | InterruptedException ignore) {
			Log.w("TAG", "SystemUtils.exec(): Failed to execute command.");
			success = false;
		} finally {
			if (null != p)
				p.destroy();
		}

		return success;
	}

	public static String propReader(String prop) {
		Process process = null;
		String returnLine =null;
		try {

			process = Runtime.getRuntime().exec("/system/bin/getprop "+prop);
			process.waitFor();
			//InputStream in = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			returnLine = bufferedReader.readLine();
			bufferedReader.close();


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("TAG", "SystemUtils.propReader() failed to do /system/bin/getprop "+prop);
		}

		process.destroy();
		return returnLine;
	}

	public static void sleep(long ms) {

		try {
			Thread.sleep(ms);
		} catch (Exception ignored) {
			Log.w("TAG", "SystemUtils.sleep(): Error in sleeping thread.");
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean writeToSysFile(String fileName, String value) {

		try {
			FileOutputStream is = new FileOutputStream(new File(fileName));
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
			w.write(value);
			w.close();

		} catch (IOException ignore) {
			Log.w("TAG", "SystemUtils.writeToSysFile(): Error writing to file="+fileName);
			ignore.printStackTrace();

			return false;
		}
		return true;
	}
}
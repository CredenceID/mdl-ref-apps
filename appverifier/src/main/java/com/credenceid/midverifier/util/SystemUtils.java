package com.credenceid.midverifier.util;

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

    public static int EMPTY_STATE = 100;
    public static final int STATE_WAITING_FOR_TAP = 101;
    public static final int STATE_WAITING_FOR_EXCHANGE = 102;
    public static final int STATE_WAITING_FOR_TRANSFER = 103;
    volatile public static int systemState = EMPTY_STATE;

    public static int getSystemState() {
        return systemState;
    }

    public static void setSystemState(int systemState) {
        SystemUtils.systemState = systemState;
        switch (systemState) {
            case STATE_WAITING_FOR_TAP:
                Log.d("TAG", "stating BLUE LEDs");
                startCircularBlueLED();
                break;
            case STATE_WAITING_FOR_EXCHANGE:
                Log.d("TAG", "stating RED LEDs");
                startRedLights();
                break;
            case STATE_WAITING_FOR_TRANSFER:
                Log.d("TAG", "stating GREEN LEDs");
                startGreenProgressForLED();
                break;

        }
    }

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

    public static void blueRight(String value) {
        // turn on 255 and for turn off 0
        String d1 = "echo " + value + " > /sys/class/leds/d9_2/brightness";// top
        String d2 = "echo " + value + " > /sys/class/leds/d3_2/brightness";// bottom
    }

    public static void blueLeft(String value) {
        // turn on 255 and for turn off 0
        String d1 = "echo " + value + " > /sys/class/leds/d3/brightness"; // top
        String d2 = "echo " + value + " > /sys/class/leds/d9/brightness"; // bottom
    }

    public static void blueBottom(String value) {
        // turn on 255 and for turn off 0
        String d1 = "echo " + value + " > /sys/class/leds/d9/brightness"; // bottom_left1
        String d2 = "echo " + value + " > /sys/class/leds/d6_1/brightness"; // bottom_left2
        String d3 = "echo " + value + " > /sys/class/leds/d9_1/brightness"; // bottom_left3
        String d4 = "echo " + value + " > /sys/class/leds/d3_1/brightness"; // bottom_left4
        String d5 = "echo " + value + " > /sys/class/leds/d6_2/brightness"; // bottom_left5
    }

    public static void greenLeft(String value) {
        String d1 = "echo " + value + " > /sys/class/leds/d1/brightness"; // top
        String d2 = "echo " + value + " > /sys/class/leds/d4/brightness"; // bottom
    }

    public static void greenRight(String value) {
        String d1 = "echo " + value + " > /sys/class/leds/d7_2/brightness"; // top
        String d2 = "echo " + value + " > /sys/class/leds/d1_2/brightness"; // bottom
    }

    public static void greenBottom(String value) {
        String d1 = "echo " + value + " > /sys/class/leds/d7/brightness"; // bottom_left 1
        String d2 = "echo " + value + " > /sys/class/leds/d4_1/brightness"; // bottom_left 2
        String d3 = "echo " + value + " > /sys/class/leds/d7_1/brightness"; // bottom_left 3
        String d4 = "echo " + value + " > /sys/class/leds/d1_1/brightness"; // bottom_left 4
        String d5 = "echo " + value + " > /sys/class/leds/d4_2/brightness"; // bottom_left 5
    }

    public static void redLeft(String value) {
        String d1 = "echo " + value + " > /sys/class/leds/d2/brightness"; // top
        String d2 = "echo " + value + " > /sys/class/leds/d5/brightness"; // bottom
    }

    public static void redRight(String value) {
        String d1 = "echo " + value + " > /sys/class/leds/d8_2/brightness"; // top
        String d2 = "echo " + value + " > /sys/class/leds/d2_2/brightness"; // bottom
    }

    public static void redBottom(String value) {
        String d1 = "echo " + value + " > /sys/class/leds/d8/brightness"; // bottom_left 1
        String d2 = "echo " + value + " > /sys/class/leds/d5_1/brightness"; // bottom_left 2
        String d3 = "echo " + value + " > /sys/class/leds/d8_1/brightness"; // bottom_left 3
        String d4 = "echo " + value + " > /sys/class/leds/d2_1/brightness"; // bottom_left 4
        String d5 = "echo " + value + " > /sys/class/leds/d5_2/brightness"; // bottom_left 5
    }


    public static void turnOffLights() {
        Log.d("TAG", "exec turn off..");
        String value = "0";
        String[] d2 = {"sh", "-c", "echo 0 > /sys/class/leds/d2/brightness"};
        String[] d5 = {"sh", "-c", "echo 0 > /sys/class/leds/d5/brightness"};
        String[] d8 = {"sh", "-c", "echo 0 > /sys/class/leds/d8/brightness"};
        String[] d5_1 = {"sh", "-c", "echo 0 > /sys/class/leds/d5_1/brightness"};
        String[] d8_1 = {"sh", "-c", "echo 0 > /sys/class/leds/d8_1/brightness"};
        String[] d2_1 = {"sh", "-c", "echo 0 > /sys/class/leds/d2_1/brightness"};
        String[] d5_2 = {"sh", "-c", "echo 0 > /sys/class/leds/d5_2/brightness"};
        String[] d2_2 = {"sh", "-c", "echo 0 > /sys/class/leds/d2_2/brightness"};
        String[] d8_2 = {"sh", "-c", "echo 0 > /sys/class/leds/d8_2/brightness"};

        String[] d1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d1/brightness"};
        String[] d3 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d3/brightness"};
        String[] d4 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d4/brightness"};
        String[] d6 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d6/brightness"};
        String[] d7 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d7/brightness"};
        String[] d9 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d9/brightness"};
        String[] d1_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d1_1/brightness"};
        String[] d1_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d1_2/brightness"};
        String[] d3_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d3_1/brightness"};
        String[] d3_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d3_2/brightness"};
        String[] d4_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d4_1/brightness"};
        String[] d4_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d4_2/brightness"};
        String[] d6_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d6_1/brightness"};
        String[] d6_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d6_2/brightness"};
        String[] d7_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d7_1/brightness"};
        String[] d7_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d7_2/brightness"};
        String[] d9_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d9_1/brightness"};
        String[] d9_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d9_2/brightness"};

        exec(d3);
        exec(d6);
        exec(d9);
        exec(d3_1);
        exec(d3_2);
        exec(d6_1);
        exec(d6_2);
        exec(d9_1);
        exec(d9_2);

        exec(d1);
        exec(d2);
        exec(d4);
        exec(d5);
        exec(d7);
        exec(d8);
        exec(d1_1);
        exec(d1_2);
        exec(d2_1);
        exec(d2_2);
        exec(d4_1);
        exec(d4_2);
        exec(d5_1);
        exec(d5_2);
        exec(d7_1);
        exec(d7_2);
        exec(d8_1);
        exec(d8_2);
    }


    public static void execBlueCircle() {
        try {
            Log.d("TAG", "exec green ..");
            String value = "255";
            long delay = 150L;
            String[] d2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d3/brightness"};
            String[] d5 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d6/brightness"};
            String[] d8 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d9/brightness"};
            String[] d5_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d6_1/brightness"};
            String[] d8_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d9_1/brightness"};
            String[] d2_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d3_1/brightness"};
            String[] d5_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d6_2/brightness"};
            String[] d8_2_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d3_2/brightness"};
            String[] d8_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d9_2/brightness"};

            String emptyValue = "0";
            String[][] d2_E = {{"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d3/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d6/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d9/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d6_1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d9_1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d3_1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d6_2/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d3_2/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d9_2/brightness"}};
            Log.d("TAG", "System state is ===" + getSystemState());
            while (STATE_WAITING_FOR_TAP == getSystemState()) {
                exec(d2);
                SystemUtils.sleep(delay);
                exec(d5);
                SystemUtils.sleep(delay);
                exec(d8);
                SystemUtils.sleep(delay);
                exec(d5_1);
                SystemUtils.sleep(delay);
                exec(d8_1);
                SystemUtils.sleep(delay);
                exec(d2_1);
                SystemUtils.sleep(delay);
                exec(d5_2);
                SystemUtils.sleep(delay);
                exec(d8_2_2);
                SystemUtils.sleep(delay);
                exec(d8_2);
                SystemUtils.sleep(delay);
                turnOffGreen(d2_E);
            }
            turnOffLights();
        } catch (Exception ex) {

        }
    }

    public static void execRedCircle() {
        try {
            Log.d("TAG", "exec green ..");
            String value = "255";

            String[] d2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d2/brightness"};
            String[] d5 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d5/brightness"};
            String[] d8 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d8/brightness"};
            String[] d5_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d5_1/brightness"};
            String[] d8_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d8_1/brightness"};
            String[] d2_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d2_1/brightness"};
            String[] d5_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d5_2/brightness"};
            String[] d8_2_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d2_2/brightness"};
            String[] d8_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d8_2/brightness"};

            String emptyValue = "0";
            String[][] d2_E = {{"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d2/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d5/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d8/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d5_1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d8_1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d2_1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d5_2/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d2_2/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d8_2/brightness"}};
            SystemUtils.sleep(100);
            while (STATE_WAITING_FOR_EXCHANGE == getSystemState()) {
                exec(d2);
                exec(d5);
                exec(d8);
                exec(d5_1);
                exec(d8_1);
                //-----------------------
                exec(d8_2);
                exec(d8_2_2);
                exec(d5_2);
                exec(d2_1);

                SystemUtils.sleep(200);
                turnOffGreen(d2_E);
                SystemUtils.sleep(200);
            }
        } catch (Exception ex) {

        }
    }

    public static void execGreenCircle() {
        try {
            Log.d("TAG", "exec green ..");
            String value = "255";
            String[] d2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d1/brightness"};
            String[] d5 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d4/brightness"};
            String[] d8 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d7/brightness"};
            String[] d5_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d4_1/brightness"};
            String[] d8_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d7_1/brightness"};
            String[] d2_1 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d1_1/brightness"};
            String[] d5_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d4_2/brightness"};
            String[] d8_2_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d1_2/brightness"};
            String[] d8_2 = {"sh", "-c", "echo " + value + " > /sys/class/leds/d7_2/brightness"};

            String emptyValue = "0";
            String[][] d2_E = {{"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d4/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d7/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d4_1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d7_1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d1_1/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d4_2/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d1_2/brightness"},
                    {"sh", "-c", "echo " + emptyValue + " > /sys/class/leds/d7_2/brightness"}};

            while (STATE_WAITING_FOR_TRANSFER == getSystemState()) {
                //exec(d2);
                //SystemUtils.sleep(200);
                //exec(d5);
                //SystemUtils.sleep(200);
                exec(d8);
                SystemUtils.sleep(350);
                exec(d5_1);
                SystemUtils.sleep(350);
                exec(d8_1);
                SystemUtils.sleep(350);
                exec(d2_1);
                SystemUtils.sleep(350);
                exec(d5_2);
                SystemUtils.sleep(350);
                //exec(d8_2_2);
                //SystemUtils.sleep(200);
                //exec(d8_2);
                //SystemUtils.sleep(200);
                turnOffGreen(d2_E);
            }
        } catch (Exception ex) {

        }
    }

    public static void turnOffGreen(String[][] values) {
        for (String[] value : values) {
            exec(value);
            System.out.println("");
        }
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
            os.writeBytes("echo 255 > /sys/class/leds/d2/brightness" + "\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();

            /* Wait for command execution to complete. */
            SystemUtils.sleep(200);
            p.waitFor();

        } catch (IOException | InterruptedException ignore) {
            Log.w("TAG", "SystemUtils.execRoot(): Failed to execute command. " + ignore.getLocalizedMessage());
            success = false;
        } finally {
            if (null != p)
                p.destroy();
        }
        return success;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean exec(String[] cmd) {

        boolean success = true;
        Process p = null;

        try {
            String[] cmdline = cmd;//{ "sh", "-c", "echo 255 > /sys/class/leds/d2/brightness", "echo 255 > /sys/class/leds/d5/brightness" };
            p = Runtime.getRuntime().exec(cmdline);
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
        String returnLine = null;
        try {

            process = Runtime.getRuntime().exec("/system/bin/getprop " + prop);
            process.waitFor();
            //InputStream in = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            returnLine = bufferedReader.readLine();
            bufferedReader.close();


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("TAG", "SystemUtils.propReader() failed to do /system/bin/getprop " + prop);
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
            Log.w("TAG", "SystemUtils.writeToSysFile(): Error writing to file=" + fileName);
            ignore.printStackTrace();

            return false;
        }
        return true;
    }

    private static void startCircularBlueLED() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                SystemUtils.turnOffLights();
                SystemUtils.execBlueCircle();
            }
        });
        /*DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
            }
        });*/
    }

    private static void startGreenProgressForLED() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                SystemUtils.execGreenCircle();
            }
        });
    }

    private static void startRedLights() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                //SystemUtils.turnOffLights();
                SystemUtils.execRedCircle();
            }
        });
    }
}
package com.android.vtmoduledemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.android.fan_api.FanUtils;
import com.android.gpio_api.GpioUtils;
import com.android.net_api.NetUtils;
import com.android.spi_api.SpiUtils;
import com.android.uart_api.UartUtils;
import com.android.usb_api.UsbUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    //private static final String TAG= "MainActivity";

    /* Read / Write of SPI mode (SPI_MODE_0..SPI_MODE_3) (limited to 8 bits) */
    private static final int SPI_IOC_RD_MODE = 1;
    private static final int SPI_IOC_WR_MODE = 2;

    /* Read / Write SPI bit justification */
    private static final int SPI_IOC_RD_LSB_FIRST = 3;
    private static final int SPI_IOC_WR_LSB_FIRST = 4;

    /* Read / Write SPI device word length (1..N) */
    private static final int SPI_IOC_RD_BITS_PER_WORD = 5;
    private static final int SPI_IOC_WR_BITS_PER_WORD = 6;

    /* Read / Write SPI device default max speed hz */
    private static final int SPI_IOC_RD_MAX_SPEED_HZ = 7;
    private static final int SPI_IOC_WR_MAX_SPEED_HZ = 8;

    /* Read / Write of the SPI mode field */
    //private static final int SPI_IOC_RD_MODE32 = 9;
    //private static final int SPI_IOC_WR_MODE32 = 10;

    private static final int GPIO_IO1_IOC_RD = 1;
    private static final int GPIO_IO2_IOC_RD = 2;
    private static final int GPIO_IO3_IOC_RD = 3;
    private static final int GPIO_IO4_IOC_RD = 4;
    private static final int GPIO_RST_IOC_RD = 5;

    private static final int GPIO_IO1_IOC_WR = 6;
    private static final int GPIO_IO2_IOC_WR = 7;
    private static final int GPIO_IO3_IOC_WR = 8;
    private static final int GPIO_IO4_IOC_WR = 9;
    private static final int GPIO_RST_IOC_WR = 10;

    private static final int GPIO_IO1_IOC_IN  = 11;
    private static final int GPIO_IO2_IOC_IN  = 12;
    private static final int GPIO_IO3_IOC_IN  = 13;
    private static final int GPIO_IO4_IOC_IN  = 14;
    private static final int GPIO_RST_IOC_IN  = 15;

    private static final int GPIO_IO1_IOC_OUT = 16;
    private static final int GPIO_IO2_IOC_OUT = 17;
    private static final int GPIO_IO3_IOC_OUT = 18;
    private static final int GPIO_IO4_IOC_OUT = 19;
    private static final int GPIO_RST_IOC_OUT = 20;

    private static final int PWM_IOC_ON     = 1;
    private static final int PWM_IOC_OFF    = 2;
    private static final int PWM_IOC_STATUS = 3;

    private Button openSpiDevBtn;
    private Button openGpioDevBtn;
    private Button openUsbDevBtn;
    private Button openNetDevBtn;
    private Button openUartDevBtn;
    private Button openFanDevBtn;
    private Button updateBtn;
    private TextView mTvVersion;

    private EditText  editTextPwmPeriod;
    private EditText  editTextPwmDuty;

    private EditText  editTextNetIp;
    private EditText  editTextNetPort;

    private EditText editTextSendData;
    private TextView textViewRecvData;

    private Button mGpioDirBtn;
    private Button mGpioLevelBtn;

    private Button mPwmSwBtn;

    private String uartDevPath  = "/dev/ttyS0";
    private String spiDevPath  = "/dev/XdSpiDev32766.0";
    private static final String gpioDevPath = "/dev/XdGpioDev";
    private static final String fanDevPath  = "/dev/XdFanDev";

    private int mGpioFd = -1;
    private int mSpiFd = -1;
    private int mNetFd = -1;
    private int mUsbFd = -1;
    private int mFanFd = -1;

    private GpioUtils mGpioUtils = null;
    private SpiUtils mSpiUtils = null;
    private UsbUtils mUsbUtils = null;
    private NetUtils mNetUtils = null;
    private UartUtils mUartUtils = null;
    private FanUtils mFanUtils = null;

    private boolean SpiFlag = false;
    private boolean GpioFlag = false;
    private boolean UsbFlag = false;
    private boolean NetFlag = false;
    private boolean UartFlag = false;
    private boolean FanFlag = false;

    private String gpioName = "IO1";
    private int gpioDir = 0;
    private int gpioLevel = 0;

    private String pwmName = "PWM0";
    private int isPwmOn = 0;
    private int pwmNum = 0;
    private int pwmPeriod = 0;
    private int pwmDuty = 0;

    private String newSpiDevName = "SPI0";
    private String oldSpiDevName = null;
    private int spiMode = 0;
    private int spiLsb = 0;
    private int spiBpw = 8;
    private int spiSpeed = 1000000;
    private int uartSpeed = 115200;

    private String netIp = "192.168.1.111";
    private int netPort = 8001;

    private String newUartDevName = "UART0";
    private String oldUartDevName = null;

    private boolean dataSendType = false;
    private boolean dataRecvType = false;

    //private int tmpData = 0;
    //private int indexData = 0;

    private boolean recvThreadFlag = false;
    private boolean sendThreadFlag = false;
    private ReadThread mReadThread;
    private SendingThread mSendingThread;

    protected OutputStream mOutputStream;
    private InputStream mInputStream;

    private byte[] sendBuf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button gpioGetBtn = findViewById(R.id.button_gpio_get);

        Button fanGetBtn = findViewById(R.id.button_pwm_get);

        Button spiSetBtn = findViewById(R.id.button_spi_set);
        Button spiGetBtn = findViewById(R.id.button_spi_get);

        Button usbSendBtn = findViewById(R.id.button_usb_send);
        Button netSendBtn = findViewById(R.id.button_net_send);
        Button uartSendBtn = findViewById(R.id.button_uart_send);
        Button spiSendBtn = findViewById(R.id.button_spi_send);

        final Button sendTypeBtn = findViewById(R.id.button_send_type);
        final Button startSendBtn = findViewById(R.id.button_start_send);
        final Button startRecvBtn = findViewById(R.id.button_start_recv);

        openGpioDevBtn = findViewById(R.id.button_open_gpio);
        openSpiDevBtn = findViewById(R.id.button_open_spi);
        openUsbDevBtn = findViewById(R.id.button_open_usb);
        openNetDevBtn = findViewById(R.id.button_open_net);
        openUartDevBtn = findViewById(R.id.button_open_uart);
        openFanDevBtn = findViewById(R.id.button_open_fan);

        updateBtn = findViewById(R.id.button_update);

        Spinner mSpinnerGpioSelect = findViewById(R.id.spinner_gpio_select);
        Spinner mSpinnerSpiSelect = findViewById(R.id.spinner_spi_select);
        Spinner mSpinnerSpiMode = findViewById(R.id.spinner_spi_mode);
        Spinner mSpinnerSpiLsb = findViewById(R.id.spinner_spi_lsb);
        Spinner mSpinnerSpiBpw = findViewById(R.id.spinner_spi_bpw);
        Spinner mSpinnerSpiSpeed = findViewById(R.id.spinner_spi_speed);
        Spinner mSpinnerUsbSpeed = findViewById(R.id.spinner_usb_speed);
        Spinner mSpinnerUartSelect = findViewById(R.id.spinner_uart_select);
        Spinner mSpinnerUartSpeed = findViewById(R.id.spinner_uart_speed);
        Spinner mSpinnerPwmSelect = findViewById(R.id.spinner_pwm_select);

        editTextNetIp = findViewById(R.id.editText_net_ip);
        editTextNetPort = findViewById(R.id.editText_net_port);

        editTextPwmPeriod = findViewById(R.id.editText_period);
        editTextPwmDuty = findViewById(R.id.editText_duty);

        editTextSendData = findViewById(R.id.editText_send_data);
        textViewRecvData = findViewById(R.id.textView_recv_data);
        textViewRecvData.setMovementMethod(ScrollingMovementMethod.getInstance());

        mGpioDirBtn = findViewById(R.id.button_gpio_dir);
        mGpioLevelBtn = findViewById(R.id.button_gpio_level);

        mPwmSwBtn = findViewById(R.id.button_pwn_sw);

        mSpinnerGpioSelect.setSelection(0,true);
        mSpinnerSpiSelect.setSelection(0,true);
        mSpinnerSpiMode.setSelection(0,true);
        mSpinnerSpiLsb.setSelection(0,true);
        mSpinnerSpiBpw.setSelection(0,true);
        mSpinnerSpiSpeed.setSelection(1,true);
        mSpinnerUsbSpeed.setSelection(2,true);
        mSpinnerUartSpeed.setSelection(6,true);
        mSpinnerUartSelect.setSelection(0,true);
        mSpinnerPwmSelect.setSelection(0,true);

        sendBuf = new byte[1024];

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
                startActivity(intent);
            }
        });

        openGpioDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GpioFlag = !GpioFlag;
                if(GpioFlag ) {
                    mGpioUtils = new GpioUtils();
                    mGpioFd = mGpioUtils.GpioOpen(gpioDevPath);
                    if(mGpioFd > 0) {
                        openGpioDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_open, null));
                        Toast.makeText(MainActivity.this, "You successfully opened " + gpioDevPath, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "You failed to open " + gpioDevPath, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(mGpioUtils != null) {
                        if(mGpioFd > 0) {
                            mGpioUtils.GpioClose();
                            openGpioDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_close, null));
                            Toast.makeText(MainActivity.this, "You successfully closed XdGpioDev", Toast.LENGTH_SHORT).show();
                        }
                        mGpioUtils = null;
                    }
                }
            }
        });

        mSpinnerGpioSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//注册点击监听
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Spinner spinner = (Spinner) adapterView;
                gpioName = (String) spinner.getItemAtPosition(position);
                if(mGpioUtils != null) {
                    if(textViewRecvData.getLineCount()> 15)
                        textViewRecvData.setText("");
                    textViewRecvData.append("You have selected gpio" + gpioName + "\r\n");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
            }
        });

        mGpioDirBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (mGpioUtils != null) {
                    int code = 0;

                    if(mGpioDirBtn.getText().toString().equals("OUT")){
                        gpioDir = 0;
                    }else{
                        gpioDir = 1;
                    }

                    if(mGpioLevelBtn.getText().toString().equals("0")){
                        gpioLevel = 0;
                    }else{
                        gpioLevel = 1;
                    }

                    if (gpioName.equals("IO1")) {
                        if (gpioDir == 0) {
                            code = GPIO_IO1_IOC_IN;
                        } else {
                            code = GPIO_IO1_IOC_OUT;
                        }
                    }

                    if (gpioName.equals("IO2")) {
                        if (gpioDir == 0) {
                            code = GPIO_IO2_IOC_IN;
                        } else {
                            code = GPIO_IO2_IOC_OUT;
                        }
                    }

                    if (gpioName.equals("IO3")) {
                        if (gpioDir == 0) {
                            code = GPIO_IO3_IOC_IN;
                        } else {
                            code = GPIO_IO3_IOC_OUT;
                        }
                    }

                    if (gpioName.equals("IO4")) {
                        if (gpioDir == 0) {
                            code = GPIO_IO4_IOC_IN;
                        } else {
                            code = GPIO_IO4_IOC_OUT;
                        }
                    }

                    if (gpioName.equals("RST")) {
                        if (gpioDir == 0) {
                            code = GPIO_RST_IOC_IN;
                        } else {
                            code = GPIO_RST_IOC_OUT;
                        }
                    }

                    if(textViewRecvData.getLineCount()> 15)
                        textViewRecvData.setText("");

                    long ret = mGpioUtils.GpioIoctl(code, gpioLevel);
                    if (ret >= 0) {
                        if (gpioDir == 0) {
                            mGpioDirBtn.setText("IN");
                            textViewRecvData.append("Set gpio " + gpioName + " dir=input\r\n");
                        } else {
                            mGpioDirBtn.setText("OUT");
                            textViewRecvData.append("Set gpio " + gpioName + " dir=output\r\n");
                        }
                    } else {
                        textViewRecvData.append("Set gpio " + gpioName +  " dir failed\r\n");
                    }
                }
            }
        });

        mGpioLevelBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (mGpioUtils != null) {
                    int code = 0;
                    if(mGpioDirBtn.getText().toString().equals("OUT")) {
                        if (mGpioLevelBtn.getText().toString().equals("0")) {
                            gpioLevel = 1;
                        } else {
                            gpioLevel = 0;
                        }

                        if (gpioName.equals("IO1")) {
                            code = GPIO_IO1_IOC_WR;
                        }
                        if (gpioName.equals("IO2")) {
                            code = GPIO_IO2_IOC_WR;
                        }
                        if (gpioName.equals("IO3")) {
                            code = GPIO_IO3_IOC_WR;
                        }
                        if (gpioName.equals("IO4")) {
                            code = GPIO_IO4_IOC_WR;
                        }
                        if (gpioName.equals("RST")) {
                            code = GPIO_RST_IOC_WR;
                        }

                        if(textViewRecvData.getLineCount()> 15)
                            textViewRecvData.setText("");

                        if(gpioDir == 1) {
                            long ret = mGpioUtils.GpioIoctl(code, gpioLevel);
                            if (ret >= 0) {
                                if (gpioLevel == 0) {
                                    mGpioLevelBtn.setText("0");
                                } else {
                                    mGpioLevelBtn.setText("1");
                                }
                                textViewRecvData.append("Set gpio " + gpioName + " level=" + gpioLevel + "\r\n");
                            } else {
                                textViewRecvData.append("Set gpio " + gpioName + " level failed\r\n");
                            }
                        }
                    }
                }
            }
        });

        gpioGetBtn.setOnClickListener(new View.OnClickListener(){
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onClick(View view) {
                if(mGpioUtils != null) {
                    int code =0;
                    int arg = 0;
                    if(gpioName.equals("IO1")) { code = GPIO_IO1_IOC_RD;}
                    if(gpioName.equals("IO2")) { code = GPIO_IO2_IOC_RD;}
                    if(gpioName.equals("IO3")) { code = GPIO_IO3_IOC_RD;}
                    if(gpioName.equals("IO4")) { code = GPIO_IO4_IOC_RD;}
                    if(gpioName.equals("RST")) { code = GPIO_RST_IOC_RD;}

                    if(textViewRecvData.getLineCount()> 15)
                        textViewRecvData.setText("");

                    int ret = mGpioUtils.GpioIoctl(code, arg);
                    if(ret >= 0) {
                        gpioDir = ((ret >> 1) & 0x01);
                        gpioLevel = (ret & 0x01);

                        if (gpioDir == 0) {
                            mGpioDirBtn.setText("IN");
                        } else {
                            mGpioDirBtn.setText("OUT");
                        }
                        if(gpioLevel == 0) {
                            mGpioLevelBtn.setText("0");
                        }else{
                            mGpioLevelBtn.setText("1");
                        }
                        if (gpioDir == 0)
                            textViewRecvData.append("Read gpio " + gpioName + " dir=input" + " level=" + gpioLevel + "\r\n");
                        else
                            textViewRecvData.append("Read gpio " + gpioName + " dir=output" + " level=" + gpioLevel + "\r\n");
                    }
                    else {
                        textViewRecvData.append("Read gpio " + gpioName + " failed\r\n");
                    }
                }
            }
        });

        openSpiDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpiFlag = !SpiFlag;
                if( SpiFlag ) {
                    mSpiUtils = new SpiUtils();
                    oldSpiDevName = newSpiDevName;
                    if(newSpiDevName.equals("SPI0")) {
                        spiDevPath = "/dev/XdSpiDev32766.0";
                    }
                    if(newSpiDevName.equals("SPI1")) {
                        spiDevPath = "/dev/XdSpiDev32765.0";
                    }
                    if(newSpiDevName.equals("SPI2")) {
                        spiDevPath = "/dev/XdSpiDev32764.0";
                    }
                    if(newSpiDevName.equals("SPI3")) {
                        spiDevPath = "/dev/XdSpiDev32763.0";
                    }
                    if(newSpiDevName.equals("SPI4")) {
                        spiDevPath = "/dev/XdSpiDev32762.0";
                    }
                    if(newSpiDevName.equals("SPI5")) {
                        spiDevPath = "/dev/XdSpiDev32761.0";
                    }
                    mSpiFd = mSpiUtils.SpiOpen(spiDevPath);
                    if(mSpiFd > 0) {
                        openSpiDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_open, null));
                        Toast.makeText(MainActivity.this, "You successfully opened "+ newSpiDevName, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "You failed to open " + newSpiDevName, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if(mSpiUtils != null) {
                        if(mSpiFd > 0) {
                            mSpiUtils.SpiClose();
                            mSpiFd = -1;
                            openSpiDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_close, null));
                            Toast.makeText(MainActivity.this, "You successfully closed "+ oldSpiDevName, Toast.LENGTH_SHORT).show();
                        }
                        mSpiUtils = null;
                    }
                }
            }
        });

        mSpinnerSpiSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//注册点击监听
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Spinner spinner = (Spinner) adapterView;
                newSpiDevName = (String) spinner.getItemAtPosition(position);
                if(textViewRecvData.getLineCount()> 15)
                    textViewRecvData.setText("");
                textViewRecvData.append("You have selected " + newSpiDevName + "\r\n");
                //Toast.makeText(MainActivity.this, "You have selected "+ newSpiDevName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mSpinnerSpiMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Spinner spinner = (Spinner) adapterView;
                String data = (String) spinner.getItemAtPosition(position);
                spiMode = Integer.parseInt(data);
                if(textViewRecvData.getLineCount()> 15)
                    textViewRecvData.setText("");
                textViewRecvData.append("You have selected spi mode=" + spiMode + "\r\n");
                //Toast.makeText(MainActivity.this, "Spi mode="+ spiMode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mSpinnerSpiLsb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Spinner spinner = (Spinner) adapterView;
                String data = (String) spinner.getItemAtPosition(position);
                spiLsb = Integer.parseInt(data);
                if(textViewRecvData.getLineCount()> 15)
                    textViewRecvData.setText("");
                textViewRecvData.append("You have selected spi lsb=" + spiLsb + "\r\n");
                //Toast.makeText(MainActivity.this, "Spi LSB=" + spiLsb, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mSpinnerSpiBpw.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Spinner spinner = (Spinner) adapterView;
                String data = (String) spinner.getItemAtPosition(position);
                spiBpw = Integer.parseInt(data);
                if(textViewRecvData.getLineCount()> 15)
                    textViewRecvData.setText("");
                textViewRecvData.append("You have selected spi bpw=" + spiBpw + "\r\n");
                //Toast.makeText(MainActivity.this, "Spi BPW=" + spiBpw, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mSpinnerSpiSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Spinner spinner = (Spinner) adapterView;
                String data = (String) spinner.getItemAtPosition(position);
                spiSpeed = Integer.parseInt(data);
                if(textViewRecvData.getLineCount()> 15)
                    textViewRecvData.setText("");
                textViewRecvData.append("You have selected spi speed=" + spiSpeed + "\r\n");
                //Toast.makeText(MainActivity.this, "Spi speed=" + spiSpeed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        spiSetBtn.setOnClickListener(new View.OnClickListener(){
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onClick(View view) {
                if(mSpiUtils != null) {
                    int ret;

                    if(textViewRecvData.getLineCount()> 15)
                        textViewRecvData.setText("");

                    ret = mSpiUtils.SpiIoctl(mSpiFd, SPI_IOC_WR_MODE, spiMode);
                    if(ret >= 0) {
                        textViewRecvData.append("Set spi mode=" + spiMode + " succeeded\r\n");
                    } else{
                        textViewRecvData.append("Set spi mode=" + spiMode + " failed\r\n");
                    }

                    ret = mSpiUtils.SpiIoctl(mSpiFd, SPI_IOC_WR_LSB_FIRST, spiLsb);
                    if(ret >= 0){
                        textViewRecvData.append("Set spi lsb=" + spiLsb + " succeeded\r\n");
                    }else{
                        textViewRecvData.append("Set spi lsb=" + spiLsb + " failed\r\n");
                    }

                    ret = mSpiUtils.SpiIoctl(mSpiFd, SPI_IOC_WR_BITS_PER_WORD, spiBpw);
                    if(ret >= 0){
                        textViewRecvData.append("Set spi bpw=" + spiBpw + " succeeded\r\n");
                    }else{
                        textViewRecvData.append("Set spi bpw=" + spiBpw + " failed\r\n");
                    }

                    ret = mSpiUtils.SpiIoctl(mSpiFd, SPI_IOC_WR_MAX_SPEED_HZ, spiSpeed);
                    if(ret >= 0){
                        textViewRecvData.append("Set spi speed=" + spiSpeed + " succeeded\r\n");
                    }else{
                        textViewRecvData.append("Set spi speed=" + spiSpeed + " failed\r\n");
                    }
                }
            }
        });

        spiGetBtn.setOnClickListener(new View.OnClickListener(){
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onClick(View view) {
                if(mSpiUtils != null) {
                    int var = 0;
                    int ret;

                    if(textViewRecvData.getLineCount()> 15)
                        textViewRecvData.setText("");

                    ret = mSpiUtils.SpiIoctl(mSpiFd, SPI_IOC_RD_MODE, var);
                    if(ret >= 0){
                        textViewRecvData.append("Read spi mode=" + ret + " succeeded\r\n");
                    }else{
                        textViewRecvData.append("Read spi mode failed\r\n");
                    }

                    ret =mSpiUtils.SpiIoctl(mSpiFd, SPI_IOC_RD_LSB_FIRST, var);
                    if(ret >= 0){
                        textViewRecvData.append("Read spi lsb=" + ret + " succeeded\r\n");
                    }else{
                        textViewRecvData.append("Read spi lsb failed\r\n");
                    }

                    ret = mSpiUtils.SpiIoctl(mSpiFd, SPI_IOC_RD_BITS_PER_WORD, var);
                    if(ret >= 0){
                        textViewRecvData.append("Read spi bpw=" + ret + " succeeded\r\n");
                    }else{
                        textViewRecvData.append("Read spi bpw failed\r\n");
                    }

                    ret =mSpiUtils.SpiIoctl(mSpiFd, SPI_IOC_RD_MAX_SPEED_HZ, var);
                    if(ret >= 0){
                        textViewRecvData.append("Read spi speed=" + ret + " succeeded\r\n");
                    }else{
                        textViewRecvData.append("Read spi speed failed\r\n");
                    }
                }
            }
        });

        spiSendBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if(mSpiUtils != null) {
                    byte[] SendBuf;
                    String str = editTextSendData.getText().toString()+"\r\n";
                    int size = str.length();
                    SendBuf = str.getBytes();
                    mSpiUtils.SpiSend(mSpiFd, SendBuf, size);
                    if(!recvThreadFlag) {
                        byte[] recvBuf = new byte[1024];
                        int len = mSpiUtils.SpiRecv(mSpiFd, recvBuf, size);
                        if(len>0) {
                            String str1 = new String(recvBuf, 0, size, StandardCharsets.US_ASCII);
                            if(textViewRecvData.getLineCount()> 15) {
                                textViewRecvData.setText(" ");
                            }
                            textViewRecvData.append(str1);
                        }
                    }
                }
            }
        });

        mSpinnerUsbSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                //Spinner spinner = (Spinner) adapterView;
                //String data = (String) spinner.getItemAtPosition(position);
                //usbSpeed = Integer.parseInt(data);
                //Toast.makeText(MainActivity.this, "usb speed="+ usbSpeed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        openUsbDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsbFlag = !UsbFlag;
                if( UsbFlag ) {
                    mUsbUtils = new UsbUtils();
                    mUsbFd = mUsbUtils.UsbOpen();
                    if(mUsbFd == 0) {
                        openUsbDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_open, null));
                        Toast.makeText(MainActivity.this, "You successfully opened usb", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "You failed to open usb", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(mUsbUtils != null) {
                        if(mUsbFd == 0) {
                            mUsbUtils.UsbClose();
                            mUsbFd = -1;
                            openUsbDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_close, null));
                            Toast.makeText(MainActivity.this, "You successfully closed usb", Toast.LENGTH_SHORT).show();
                        }
                        mUsbUtils = null;
                    }
                }
            }
        });

        usbSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUsbUtils != null) {
                    String str = editTextSendData.getText().toString();
                    int size = str.length();
                    mUsbUtils.UsbSend(str.getBytes(), size);
                }
            }
        });

        openNetDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetFlag = !NetFlag;
                if( NetFlag ) {
                    netIp = editTextNetIp.getText().toString();
                    netPort = Integer.parseInt(editTextNetPort.getText().toString());
                    mNetUtils = new NetUtils();
                    mNetFd = mNetUtils.NetOpen(netIp, netPort);
                    if(mNetFd == 0) {
                        openNetDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_open, null));
                        Toast.makeText(MainActivity.this, "You successfully opened net", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "You failed to open net", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(mNetUtils != null) {
                        if(mNetFd == 0) {
                            mNetUtils.NetClose();
                            mNetFd = -1;
                            openNetDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_close, null));
                            Toast.makeText(MainActivity.this, "You successfully closed net", Toast.LENGTH_SHORT).show();
                        }
                        mNetUtils = null;
                    }
                }
            }
        });

        netSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mNetUtils != null) {
                    String str = editTextSendData.getText().toString();
                    int size = str.length();
                    mNetUtils.NetSend(str.getBytes(), size);
                }
            }
        });

        mSpinnerUartSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Spinner spinner = (Spinner) adapterView;
                newUartDevName = (String) spinner.getItemAtPosition(position);
                if(textViewRecvData.getLineCount()> 15)
                    textViewRecvData.setText("");
                textViewRecvData.append("You have selected " + newUartDevName + "\r\n");
                //Toast.makeText(MainActivity.this, "You have selected " + newUartDevName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mSpinnerUartSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Spinner spinner = (Spinner) adapterView;
                String data = (String) spinner.getItemAtPosition(position);
                uartSpeed = Integer.parseInt(data);
                if(textViewRecvData.getLineCount()> 15)
                    textViewRecvData.setText("");
                textViewRecvData.append("You have selected uart speed=" + uartSpeed + "\r\n");
                //Toast.makeText(MainActivity.this, "Uart speed="+ uartSpeed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        openUartDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UartFlag = !UartFlag;
                if( UartFlag ) {
                    oldUartDevName = newUartDevName;
                    if(newUartDevName.equals("UART0")){
                        uartDevPath = "/dev/ttyS0";
                    }
                    if(newUartDevName.equals("UART1")){
                        uartDevPath = "/dev/ttyS1";
                    }
                    if(newUartDevName.equals("UART2")){
                        uartDevPath = "/dev/ttyS2";
                    }
                    if(newUartDevName.equals("UART3")){
                        uartDevPath = "/dev/ttyS3";
                    }
                    if(newUartDevName.equals("UARTWK0")){
                        uartDevPath = "/dev/ttysWK0";
                    }
                    if(newUartDevName.equals("UARTWK1")){
                        uartDevPath = "/dev/ttysWK1";
                    }
                    if(newUartDevName.equals("UARTWK2")){
                        uartDevPath = "/dev/ttysWK2";
                    }
                    if(newUartDevName.equals("UARTWK3")){
                        uartDevPath = "/dev/ttysWK3";
                    }
                    try {
                        mUartUtils = new UartUtils(new File(uartDevPath), uartSpeed, 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mInputStream = mUartUtils.getInputStream();
                    mOutputStream = mUartUtils.getOutputStream();
                    if(mUartUtils != null) {
                        openUartDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_open, null));
                        Toast.makeText(MainActivity.this, "You successfully opened " + newUartDevName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "You failed to open " + newUartDevName, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(mUartUtils != null) {
                        mUartUtils.UartClose();
                        mUartUtils = null;
                        openUartDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_close, null));
                        Toast.makeText(MainActivity.this, "You successfully closed " + oldUartDevName, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        uartSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str;
                if(mUartUtils != null) {
                    if(!dataSendType) {
                        str = editTextSendData.getText().toString() + "\r\n";
                        sendBuf = str.getBytes();
                    }else{
                        int j = 0;
                        String str1;
                        StringBuilder sbuf = new StringBuilder();
                        str = editTextSendData.getText().toString() + " ";
                        int size = str.length();
                        char[] cc = str.toCharArray();
                        for(int i=0; i<size; i++) {
                            if(cc[i] != 0x20) {
                                sbuf.append(cc[i]);
                            }else{
                                str1 = sbuf.toString();
                                if(sbuf.length() <= 7) {
                                    sendBuf[j++] = (byte) Integer.parseInt(str1, 16);
                                }
                                sbuf.delete(0, sbuf.length());
                            }
                        }
                        sendBuf[j++] = '\r';
                        sendBuf[j] = '\n';
                    }
                    try {
                        if(mOutputStream != null)
                            mOutputStream.write(sendBuf);
                        if (!recvThreadFlag) {
                            byte[] buffer = new byte[1024];
                            if(mInputStream != null) {
                                int size = mInputStream.read(buffer);
                                if (size > 0) {
                                    onDataReceived("uart", buffer, size);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //mSendingThread = new SendingThread();
                    //mSendingThread.start();
                }
            }
        });

        sendTypeBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                dataSendType = !dataSendType;
                if(dataSendType) {
                    sendTypeBtn.setText("ASCII发送");
                }else{
                    sendTypeBtn.setText("HEX发送");
                }
            }
        });

        startSendBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                sendThreadFlag = !sendThreadFlag;
                if(sendThreadFlag) {
                    startSendBtn.setText("停止接收");
                    sendThreadFlag = true;
                    mSendingThread = new SendingThread();
                    mSendingThread.start();
                    startSendBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_open, null));
                }else {
                    startSendBtn.setText("启动接收");
                    sendThreadFlag = false;
                    mSendingThread.interrupt();
                    startSendBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_close, null));
                }
            }
        });

        startRecvBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                dataRecvType = !dataRecvType;
                if(dataRecvType) {
                    startRecvBtn.setText("停止接收");
                    recvThreadFlag = true;
                    mReadThread = new ReadThread();
                    startRecvBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_open, null));
                    mReadThread.start();
                }else {
                    startRecvBtn.setText("启动接收");
                    recvThreadFlag = false;
                    mReadThread.interrupt();
                    startRecvBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_close, null));
                }
            }
        });

        openFanDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FanFlag = !FanFlag;
                if(FanFlag) {
                    mFanUtils = new FanUtils();
                    mFanFd = mFanUtils.FanOpen(fanDevPath);
                    if(mFanFd > 0) {
                        openFanDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_open, null));
                        Toast.makeText(MainActivity.this, "You successfully opened " + fanDevPath, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "You failed to open " + fanDevPath, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(mFanUtils != null) {
                        if(mFanFd > 0) {
                            mFanUtils.FanClose();
                            mFanFd = -1;
                            openFanDevBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_close, null));
                            Toast.makeText(MainActivity.this, "You successfully closed " + fanDevPath, Toast.LENGTH_SHORT).show();
                        }
                        mFanUtils = null;
                    }
                }
            }
        });

        mSpinnerPwmSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Spinner spinner = (Spinner) adapterView;
                pwmName = (String) spinner.getItemAtPosition(position);
                pwmNum = position;
                textViewRecvData.append("You have selected " + pwmName + "\r\n");
                //Toast.makeText(MainActivity.this, "You have selected "+ pwmName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mPwmSwBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (mFanUtils != null) {
                    int code;
                    int val;

                    if(mPwmSwBtn.getText().toString().equals("OFF")){
                        isPwmOn = 0;
                        val =  pwmNum;
                        code = PWM_IOC_OFF;
                    }else{
                        isPwmOn = 1;
                        code = PWM_IOC_ON;
                        pwmPeriod = Integer.parseInt(editTextPwmPeriod.getText().toString());
                        pwmDuty = Integer.parseInt(editTextPwmDuty.getText().toString());
                        if(pwmPeriod>1000) pwmPeriod = 1000;
                        if(pwmDuty>100) pwmDuty = 100;
                        val =  (pwmNum <<24) | (pwmDuty<<16) | pwmPeriod;
                    }

                    if(textViewRecvData.getLineCount()> 15)
                        textViewRecvData.setText("");

                    long ret = mFanUtils.FanIoctl(code, val);
                    if (ret >= 0) {
                        if (isPwmOn == 0) {
                            mPwmSwBtn.setText("ON");
                            textViewRecvData.append("Set " + pwmName + " OFF\r\n");
                        } else {
                            mPwmSwBtn.setText("OFF");
                            textViewRecvData.append("Set " + pwmName + " ON period=" + pwmPeriod + " duty=" + pwmDuty + "%\r\n");
                        }
                    } else {
                        textViewRecvData.append("Set " + pwmName +  " failed\r\n");
                    }
                }
            }
        });

        fanGetBtn.setOnClickListener(new View.OnClickListener(){
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onClick(View view) {
                if(mFanUtils != null) {
                    int arg = pwmNum;

                    if(textViewRecvData.getLineCount()> 15)
                        textViewRecvData.setText("");

                    int ret = mFanUtils.FanIoctl(PWM_IOC_STATUS, arg);
                    if(ret >= 0) {
                        //isPwmOn = ((ret >> 24) & 0xff);
                        pwmDuty = ((ret >> 16) & 0xff);
                        pwmPeriod = (ret & 0xffff);

                        if(pwmPeriod == 0)
                            isPwmOn = 0;
                        else
                            isPwmOn = 1;

                        editTextPwmPeriod.setText("" + pwmPeriod);
                        editTextPwmDuty.setText("" + pwmDuty);
                        if(isPwmOn == 0) {
                            mPwmSwBtn.setText("ON");
                            textViewRecvData.append("Read " + pwmName + " off" + " period=" + pwmPeriod + " duty=" + pwmDuty + "%\r\n");
                        }else{
                            mPwmSwBtn.setText("OFF");
                            textViewRecvData.append("Read " + pwmName + " on" + " period=" + pwmPeriod + " duty=" + pwmDuty + "%\r\n");
                        }
                    }
                    else {
                        textViewRecvData.append("Read " + pwmName + " failed\r\n");
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_quit) {
            finish();
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        if(mReadThread != null)
            mReadThread.interrupt();

        if(mSpiUtils != null) {
            mSpiUtils.SpiClose();
            mSpiUtils = null;
        }

        if(mUartUtils != null) {
            mUartUtils.UartClose();
            mUartUtils = null;
        }

        if(mFanUtils != null) {
            mFanUtils.FanClose();
            mFanUtils = null;
        }

        if(mNetUtils != null) {
            if(mNetFd == 0) {
                mNetUtils.NetClose();
            }
            mNetUtils = null;
        }

        if(mUsbUtils != null) {
            mUsbUtils.UsbClose();
            mUsbUtils = null;
        }
        super.onDestroy();
    }

    private void onDataReceived(final String devName, final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                if (textViewRecvData != null) {
                    if(textViewRecvData.getLineCount()> 15)
                        textViewRecvData.setText("");
                    textViewRecvData.append(devName + " recv: ");
                    if(!dataSendType) {
                        textViewRecvData.append(new String(buffer, 0, size));
                    }else{
                        for(int i=0; i<size; i++){
                            textViewRecvData.append("0x" + numToHex8(buffer[i]&0xff) + " ");
                        }
                        textViewRecvData.append("\r\n");
                    }
                }
            }
        });
    }

    private static String numToHex8(int b) {
        return String.format("%02x", b);
    }

    /*private static String numToChar(int b) {
        return String.format("%c", b);
    }*/

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mUartUtils != null) {
                        if (mInputStream == null) return;
                        size = mInputStream.read(buffer);
                        if (size > 0) {
                            onDataReceived("uart", buffer, size);
                        }
                        sleep(110);
                    }
                    if (mSpiFd > 0) {
                        size = mSpiUtils.SpiRecv(mSpiFd, buffer, 255);
                        if (size > 0) {
                            onDataReceived("spi", buffer, size);
                        }
                    }
                    if (mNetFd == 0) {
                        size = mNetUtils.NetRecv(buffer, 255);
                        if (size > 0) {
                            onDataReceived("net", buffer, size);
                        }
                    }
                    if (mUsbFd == 0) {
                        size = mUsbUtils.UsbRecv(buffer, 255);
                        if (size > 0) {
                            onDataReceived("usb", buffer, size);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private class SendingThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(sendBuf);
                        sleep(100);
                    } else {
                        return;
                    }
                    if (mSpiFd > 0) {
                        int size = mSpiUtils.SpiRecv(mSpiFd, sendBuf, 255);
                        if (size > 0) {
                            onDataReceived("spi", sendBuf, size);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
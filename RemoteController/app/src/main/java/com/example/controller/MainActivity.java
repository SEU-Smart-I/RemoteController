package com.example.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.interfaces.OnSelectListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    private static Context context;

    private boolean ifConnectCommand = false;
    private boolean ifConnectImage = false;
    private boolean ifParameterError = false;
//    private boolean ifXEditing = false;
//    private boolean ifYEditing = false;
//    private boolean ifZEditing = false;
//    private boolean ifXFine = false;
//    private boolean ifYFine = false;
//    private boolean ifZFine = false;
    private boolean isMagnification = false;//false: x4  ,  true: x40
    private String currentControlElement = "microscope 1";
//        private String xPosition = "0";
//    private String yPosition = "0";
//    private String zPosition = "0";
    private int switchIndex = 0;
    private float[][] positionStore = new float[9][3];
    private float[][] prePositionStore = new float[9][3];//刚开始从远程PC获取，实现初始化
    private int preNum;
    private int preNumAbs;

    Button switchButton;
    Button magnification;
    Button sendCommand;
    ImageView imageView;
    CheckBox x_CheckBox;
    TextView x_TextView;
    EditText x_TextInputEditText;
    Button x_plus;
    Button x_minus;
    CheckBox y_CheckBox;
    TextView y_TextView;
    EditText y_TextInputEditText;
    Button y_plus;
    Button y_minus;
    CheckBox z_CheckBox;
    TextView z_TextView;
    EditText z_TextInputEditText;
    Button z_plus;
    Button z_minus;

    private Socket socketCommand = null;
    private static final String Host = "192.168.1.121";
    private static final int PortCommand = 12345;
    private ServerInteraction mServerInteractionCommand;
    private Socket socketImage = null;
    private static final int PortImage = 12000;
    private ServerInteraction mServerInteractionImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        myRequetPermission();

        //初始化界面元件
        initiateViewElements();

        //远程控制
//        remoteControlFunction();

        //图像传输
        remoteImageInteraction();
    }

    //元件初始化
    private void initiateViewElements() {
        switchButton = findViewById(R.id.switchButton);
        magnification = findViewById(R.id.magnification);
        sendCommand = findViewById(R.id.sendCommand);
        imageView = findViewById(R.id.imageView);
        /*FrameLayout.LayoutParams params_dimension_switch = new FrameLayout.LayoutParams(230, 120);
        params_dimension_switch.gravity = Gravity.TOP | Gravity.LEFT;
        params_dimension_switch.setMargins(50,20,0,0);
        this.addContentView(switchButton,params_dimension_switch);*/
        x_CheckBox = findViewById(R.id.x_fine);
        x_TextView = findViewById(R.id.x_textView); //无用
        x_TextInputEditText = findViewById(R.id.x_textInputEditText);
        x_minus = findViewById(R.id.x_minus);
        x_plus = findViewById(R.id.x_plus);

        y_CheckBox = findViewById(R.id.y_fine);
        y_TextView = findViewById(R.id.y_textView);
        y_TextInputEditText = findViewById(R.id.y_textInputEditText);
        y_minus = findViewById(R.id.y_minus);
        y_plus = findViewById(R.id.y_plus);

        z_CheckBox = findViewById(R.id.z_fine);
        z_TextView = findViewById(R.id.z_textView);
        z_TextInputEditText = findViewById(R.id.z_textInputEditText);
        z_minus = findViewById(R.id.z_minus);
        z_plus = findViewById(R.id.z_plus);

        ////////////////////////////////文字框输入监听///////////////////////////////////////////////
        TextWatcher x_afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
                preNum = z_TextInputEditText.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                editTextInputAction(x_TextInputEditText);
            }
        };
        x_TextInputEditText.addTextChangedListener(x_afterTextChangedListener);
//        x_TextInputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    if (ifXEditing) {
//                        String xPosition = getFloatWithDecimalFormat2(Float.parseFloat(x_TextInputEditText.toString()));
//                        x_TextInputEditText.setText(xPosition);
//                        savePositionData();
//                        ifXEditing = false;
//                    }
//                }
//                return false;
//            }
//        });

        TextWatcher y_afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
                preNum = z_TextInputEditText.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                editTextInputAction(y_TextInputEditText);
            }
        };
        y_TextInputEditText.addTextChangedListener(y_afterTextChangedListener);
//        y_TextInputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    if (ifYEditing) {
//                        String yPosition = getFloatWithDecimalFormat2(Float.parseFloat(y_TextInputEditText.toString()));
//                        y_TextInputEditText.setText(yPosition);
//                        savePositionData();
//                        ifYEditing = false;
//                    }
//                }
//                return false;
//            }
//        });

        TextWatcher z_afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
                preNum = z_TextInputEditText.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                editTextInputAction(z_TextInputEditText);
            }
        };
        z_TextInputEditText.addTextChangedListener(z_afterTextChangedListener);
//        System.out.println("z_afterTextChangedListener: "+z_afterTextChangedListener);
//        z_TextInputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                System.out.println(ifZEditing+"-------");
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    System.out.println(ifZEditing+"++++++++");
//                    if (ifZEditing) {
//                        String zPosition = getFloatWithDecimalFormat2(Float.parseFloat(z_TextInputEditText.toString()));
//                        z_TextInputEditText.setText(zPosition);
//                        savePositionData();
//                        ifZEditing = false;
//                    }
//                }
//                return false;
//            }
//        });
        ////////////////////////////////////////////////////////////////////////////////////////////

        //switchButton,切换控制对象
        switchButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("switchButton:", "Switch the control element!");
                savePositionData();
                SwitchControlElement(v);
            }
        });

        //物镜倍数切换，独立于位置设置以外的命令发送
        magnification.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("magnification: ", "Change the magnification of the microscope.");
                isMagnification = !isMagnification;
                if (isMagnification) {
                    magnification.setText("x 40");
                } else {
                    magnification.setText("x 4");
                }
                //发送切换指令
                /////////////////////////////////////////////////////////

                ////////////////////////////////////////////////////////
            }
        });

        sendCommand.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("send command", "Send the command to the server.");
                ///////////////////////////////////////////////////////
//                String generateNewPositionCommand(); //生成新的命令字符串
                ///////////////////////////////////////////////////////
            }
        });


        ///x_plus
        x_plus.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("x plus:", "X position plus.");
                x_TextInputEditText.setText(positionPlus(x_TextInputEditText.getText().toString(), x_CheckBox.isChecked()));
                savePositionData();
            }
        });

        //x_minus
        x_minus.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("x minus:", "X position minus.");
                x_TextInputEditText.setText(positionMinus(x_TextInputEditText.getText().toString(), x_CheckBox.isChecked()));
                savePositionData();
            }
        });

        ///y_plus
        y_plus.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("y plus:", "Y position plus.");
                y_TextInputEditText.setText(positionPlus(y_TextInputEditText.getText().toString(), y_CheckBox.isChecked()));
                savePositionData();
            }
        });

        //y_minus
        y_minus.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("y minus:", "Y position minus.");
                y_TextInputEditText.setText(positionMinus(y_TextInputEditText.getText().toString(), y_CheckBox.isChecked()));
                savePositionData();
            }
        });

        ///z_plus
        z_plus.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("z plus:", "Z position plus.");
                z_TextInputEditText.setText(positionPlus(z_TextInputEditText.getText().toString(),z_CheckBox.isChecked()));
                savePositionData();
            }
        });

        //z_minus
        z_minus.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("z minus:", "Z position minus.");
                z_TextInputEditText.setText(positionMinus(z_TextInputEditText.getText().toString(), z_CheckBox.isChecked()));
                savePositionData();
            }
        });

    }

    private String positionPlus(String prePosition, boolean ifFine) {
        System.out.println(prePosition);
        float num = Float.parseFloat(prePosition);
        if (ifFine) {
            num += 0.01;
        } else {
            num += 1;
        }
        return getFloatWithDecimalFormat2(num);
    }

    private String positionMinus(String prePosition, boolean ifFine) {
        float num = Float.parseFloat(prePosition);
        if (ifFine) {
            num -= 0.01;
        } else {
            num -= 1;
        }
        return getFloatWithDecimalFormat2(num);
    }

    //保留二位小数
    private String getFloatWithDecimalFormat2(float fNum) {
        DecimalFormat decimalFormat =new DecimalFormat("0.00");
        return decimalFormat.format(fNum);
    }

    private void savePositionData() {
        prePositionStore[switchIndex][0] = positionStore[switchIndex][0];
        prePositionStore[switchIndex][1] = positionStore[switchIndex][1];
        prePositionStore[switchIndex][2] = positionStore[switchIndex][2];

        positionStore[switchIndex][0] = Float.parseFloat(x_TextInputEditText.getText().toString());
        positionStore[switchIndex][1] = Float.parseFloat(y_TextInputEditText.getText().toString());
        positionStore[switchIndex][2] = Float.parseFloat(z_TextInputEditText.getText().toString());
    }

    private void setPositionData() {
        x_TextInputEditText.setText(getFloatWithDecimalFormat2(positionStore[switchIndex][0]));
        y_TextInputEditText.setText(getFloatWithDecimalFormat2(positionStore[switchIndex][1]));
        z_TextInputEditText.setText(getFloatWithDecimalFormat2(positionStore[switchIndex][2]));
//        xPosition = String.valueOf(positionStore[switchIndex][0]);
//        yPosition = String.valueOf(positionStore[switchIndex][1]);
//        zPosition = String.valueOf(positionStore[switchIndex][2]);
    }

    /**
     * function for the switchButton
     *
     * @param v the button: Point
     */
    private void SwitchControlElement(View v) {

        new XPopup.Builder(this)
                .popupPosition(PopupPosition.Bottom)
                .atView(v)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(new String[]{"Microscope", "Injection 1", "Injection 2","Injection 3",
                                "Injection 4", "Injection 5", "Injection 6", "Injection 7", "Injection 8"},


                        new int[]{},
                        new OnSelectListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onSelect(int position, String text) {

                                switch (text) {

                                    case "Microscope":
                                        //DIC显微镜
                                        currentControlElement = "microscope 1";
                                        switchIndex = 0;
                                        switchButton.setText("Microscope");
                                        break;

                                    case "Injection 1":
                                        currentControlElement = "injection 1";
                                        switchIndex = 1;
                                        switchButton.setText("Injection "+switchIndex);
                                        break;

                                    case "Injection 2":
                                        currentControlElement = "injection 2";
                                        switchIndex = 2;
                                        switchButton.setText("Injection "+switchIndex);
                                        break;

                                    case "Injection 3":
                                        currentControlElement = "injection 3";
                                        switchIndex = 3;
                                        switchButton.setText("Injection "+switchIndex);
                                        break;
                                    case "Injection 4":
                                        currentControlElement = "injection 4";
                                        switchIndex = 4;
                                        switchButton.setText("Injection "+switchIndex);
                                        break;

                                    case "Injection 5":
                                        currentControlElement = "injection 5";
                                        switchButton.setText("Injection "+switchIndex);
                                        switchIndex = 5;
                                        break;

                                    case "Injection 6":
                                        currentControlElement = "injection 6";
                                        switchIndex = 6;
                                        switchButton.setText("Injection "+switchIndex);
                                        break;

                                    case "Injection 7":
                                        currentControlElement = "injection 7";
                                        switchIndex = 7;
                                        switchButton.setText("Injection "+switchIndex);
                                        break;

                                    case "Injection 8":
                                        currentControlElement = "injection 8";
                                        switchIndex = 8;
                                        switchButton.setText("Injection "+switchIndex);
                                        break;

                                }
                                setPositionData();
                            }
                        })
                .show();
    }

    private void positionInitiate() throws IOException {
        String positionGetCommand = "111";
        System.out.println("111");
        mServerInteractionCommand.sentMessageToServer(positionGetCommand);
        String returnInfo = mServerInteractionCommand.getMessageFromServer();
        String[] strArray = returnInfo.split(" ");
        if (strArray.length == 29) {
            for (int i = 0; i < 27; i++) {
                positionStore[i/3][i%3] = Float.parseFloat(strArray[i+2]);
                System.out.println(strArray[i+2]+"--"+Float.parseFloat(strArray[i+2]));
                System.out.println("prePositionStore["+i+"/3]["+i+"%3] = "+prePositionStore[i/3][i%3]);
            }
            prePositionStore = positionStore.clone();
            setPositionData();
        }
        System.out.println("!!!: "+returnInfo);

    }

    //远程控制交互函数
    private void remoteControlFunction() {
        //命令通信子线程
        new Thread() {
            @Override
            public void run() {
                //连接服务器
                try {
                    socketCommand = new Socket(Host, PortCommand);
                    ifConnectCommand = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context,"Fail to connect the server!", Toast.LENGTH_SHORT).show();
                }
                if (ifConnectCommand) {
                    mServerInteractionCommand = new ServerInteraction(Host, PortCommand,socketCommand);

                    try {
                        //连接服务器
                        mServerInteractionCommand.connectTheServer();
                        //position初始化
                        positionInitiate();

                        Log.v("MainActivity", "Remote control initialization finished.");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    //远程图像传输函数
    private void remoteImageInteraction() {
        //图像通信子线程
        new Thread() {
            @Override
            public void run() {
                //连接服务器
                try {
                    socketImage = new Socket(Host, PortImage);
                    ifConnectImage = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (ifConnectImage) {
                    //初始化文件夹
                    ImageBufferManager imageBufferManager = new ImageBufferManager(context);
                    String savePath = imageBufferManager.imageCacheInitiate();
                    //初始化服务通信类
                    mServerInteractionImage = new ServerInteraction(Host, PortImage,socketImage);

                    System.out.println("***********----------************");
                    try {
                        //连接服务器
                        boolean ifConnected = mServerInteractionImage.connectTheServer();
                        System.out.println("************");
                        String fileName = mServerInteractionImage.imageLoadTest(savePath+"/");
                        System.out.println("fileNumber: "+imageBufferManager.getFilesNumber(savePath));
                        //显示图像
                        imageShow(fileName);

                        //运行远程控制交互函数
                        if (ifConnected) {
//                        remoteControlFunction();
                        }
                        System.out.println("++++Remote camera is running.++++");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }

    private void imageShow(String Path) {
        Bitmap bitmap = getLoacalBitmap(Path);
        imageView.setImageBitmap(bitmap);	//设置Bitmap
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    //判断输入是否结束，以换行符"\n"结尾
    private boolean ifTextInputFinished(int preNum, String str) {
        if (preNum < str.length()) {
            String strFinal = str.substring(str.length()-1);
            System.out.println("strFinal = "+strFinal);
            System.out.println("strFinal.equals(\"/n\"): "+strFinal.equals("\n"));
            if (strFinal.equals("\n") && str.length() > 1) {
                return true;
            }
        }
        return false;
    }

    //判断输入是否结束，任意位置输入换行符“\n”
    private boolean ifTextInputFinished0(int preNum, String str) {
        if (preNum < str.length()) {
            if (str.contains("\n") && str.length() > 1) {
                return true;
            }
        }
        return false;
    }

    //删除头部的“+”和尾部的“\n”字符
    private String getValidString(String str) {
        String strValid;
        String strFirst = str.substring(0,1);
        if (strFirst.equals("+")) {
            strValid = str.substring(1, str.length()-1);
        } else {
            strValid = str.substring(0, str.length()-1);
        }
        return strValid;
    }

    //删除头部的“+”和任意位置的“\n”字符
    private String getValidString0(String str) {
        String strValid;
        String strFirst = str.substring(0,1);
        String strEnd = str.substring(str.length()-1);
        if (strFirst.equals("+")) {
            strValid = str.substring(1);
        } else {
            strValid = str;
        }
        if (strEnd.equals("\n")) {
            strValid = strValid.substring(0,str.length()-1);
        } else {
            String[] strArray = strValid.split("\n");
            strValid = strArray[0] + strArray[1];
        }
        return strValid;
    }

    private void editTextInputAction(EditText editText) {//, boolean ifEditing
        String tempStr = editText.getText().toString();
        int index = tempStr.length();
        boolean ifEditing = !ifTextInputFinished0(preNum, tempStr);
        System.out.println("1: "+ifEditing);
        if (!ifEditing) {
            tempStr = getValidString0(tempStr);
            index -= tempStr.length();
            if (isNumber(tempStr)) {
                editText.setTextColor(Color.BLACK);
                String newstr = getFloatWithDecimalFormat2(Float.parseFloat(tempStr));
                if (!tempStr.equals(newstr)) {
                    Toast.makeText(context, "The max precision is 0.01.", Toast.LENGTH_SHORT).show();
                    Log.v("Z_EditText", "The max precision is 0.01.  "+newstr);
                }
                editText.setText(newstr);
                existInputEvent();
                editText.clearFocus();
            } else {
                index = editText.getSelectionStart()-index;
                editText.setText(tempStr);
                editText.setTextColor(Color.RED);
                editText.setSelection(index);
                ifEditing = true;
            }
        }
        System.out.println("2: "+ifEditing);
    }

    //关闭软键盘
    private void existInputEvent() {
        InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (MainActivity.this.getCurrentFocus() != null) {
            if (MainActivity.this.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 判断字符串是否是数字
     */
    public static boolean isNumber(String value) {
        return isInteger(value) || isDouble(value);
    }
    /**
     * 判断字符串是否是整数
     */
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否是浮点数
     */
    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            if (value.contains("."))
                return true;
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //动态申请权限
    private void myRequetPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else {
//            Toast.makeText(this,"您已经申请了权限!",Toast.LENGTH_SHORT).show();
        }
    }

}
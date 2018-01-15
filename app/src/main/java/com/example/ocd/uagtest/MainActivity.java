package com.example.ocd.uagtest;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ocd.uagtest.view.VerticalSeekBar;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    //起飞和降落线程
    Thread thread_tf;
    Thread thread_ld;

    Socket socket = null;
    OutputStream out;
    byte[] data = new byte[34]; //定义通信数组
    boolean flag = false;
    boolean flag2 = false,flag3 = false;

    int x = 0;  //油门
    //默认方向值
    int forward = 2000;
    int backward = 1500;
    int leftward = 1500;
    int rightward = 1150;
    int clockwise = 1500;
    int anticlockwise = 1500;

    TextView uptext, downtext, lefttext, righttext;
    TextView youtext;
    RelativeLayout rl;
    Button bt_setdt;
    Button bt_conn;
    Button bt_forward;
    Button bt_backward;
    Button bt_leftward;
    Button bt_rightward;
    Button bt_clockwise;
    Button bt_anticlockwise;
    Button bt_tf;
    Button bt_ld;

    Button btup, btdown;

    VerticalSeekBar sb;

    Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 隐藏Navigationbar
        window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        window.setAttributes(params);
        // 隐藏，触摸时出现
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        window.setAttributes(params);

        initdata();

        uptext = (TextView) findViewById(R.id.uptext);
        downtext = (TextView) findViewById(R.id.downtext);
        lefttext = (TextView) findViewById(R.id.lefttext);
        righttext = (TextView) findViewById(R.id.righttext);

        youtext = (TextView) findViewById(R.id.youtext);


//        bt_setdt = (Button) findViewById(R.id.bt_setdt);
        bt_conn = (Button) findViewById(R.id.wifi_btn);
        bt_forward = (Button) findViewById(R.id.up_btn);
        bt_backward = (Button) findViewById(R.id.down_btn);
        bt_leftward = (Button) findViewById(R.id.left_btn);
        bt_rightward = (Button) findViewById(R.id.right_btn);
        /*
        bt_clockwise = (Button) findViewById(R.id.bt_clockwise_rotation);
        bt_anticlockwise = (Button) findViewById(R.id.bt_anticlockwise_rotation);

        */
        bt_tf = (Button) findViewById(R.id.start_btn);
        bt_ld = (Button) findViewById(R.id.stop_btn);

        btup = (Button) findViewById(R.id.turnLeft);
        btdown = (Button) findViewById(R.id.turnRight);

        sb = (VerticalSeekBar) findViewById(R.id.verticalSeekBar);

        sb_setgas();
    }

    //初始化控制数据
    public void initdata()
    {
        //协议规定的固定值
        data[0]=(byte) 0xAA;
        data[1]=(byte) 0xC0;
        data[2]=(byte) 0x1c;

        //控制上下方向
        data[3]=(byte) (0>>8);        //给油门赋值，把二进制拆成高八位
        data[4]=(byte) (0&0xff);      //给油门赋值，把二进制拆成低八位

        //控制左右旋转
        data[5]=(byte) (1500>>8);        //给航向赋值，把二进制拆成高八位
        data[6]=(byte) (1500&0xff);      //给航向赋值，把二进制拆成低八位

        //控制左右方向
        data[7]=(byte) (1500>>8);        //给横滚赋值，把二进制拆成高八位
        data[8]=(byte) (1500&0xff);      //给横滚赋值，把二进制拆成低八位

        //控制前后方向
        data[9]=(byte) (1500>>8);        //给俯仰赋值，把二进制拆成高八位
        data[10]=(byte) (1500&0xff);      //给俯仰赋值，把二进制拆成低八位

        //协议规定的固定值
        data[31]=(byte) 0x1c;
        data[32]=(byte) 0x0D;
        data[33]=(byte) 0x0A;
    }

    // 点击按钮连接并启动(默认数据data)
    public void bt_conn(View view) {

        // 连接启动
        if (flag == false) {
            flag = true;
            // 调用线程类并指定要启动的线程
            Thread t1 = new Thread(new connThread());
            t1.start();
        } else {
            youtext.setText("已启动，禁止重复点击");
            bt_conn.setEnabled(false);
        }

    }

    // 创建连接启动线程
    public class connThread implements Runnable {

        @Override
        public void run() {

            try {
                // 发送连接请求
                socket = new Socket("192.168.4.1", 333);
                // 调用输出流给无人机发送连接信息
                out = socket.getOutputStream();
                // 调用write方法发送连接信息"GEC\r\n"
                out.write("GEC\r\n".getBytes());
                // 强制将缓冲区中的数据发送出去,不必等到缓冲区满
                out.flush();

                // 每隔5秒发送数据
                do {
                    out.write(data);
                    out.flush();
                    Thread.sleep(5);
                } while (flag);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    // 方向复位(参数)
    public void resetDerection() {
        forward = 1500;
        backward = 1500;
        leftward = 1500;
        rightward = 1500;
        clockwise = 1500;
        anticlockwise = 1500;
    }

    // 复位按钮
    public void bt_reset(View view) {

        // 重置数据
        resetDerection();
        // 显示信息
        ShowCurrentInfo();
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    // 停止
    public void bt_stop(View view) {

        // 油门设置为0,高八位，低八位
        x = 0;
        data[3] = (byte) (0>>8);
        data[4] = (byte) (0&0xff);

        // 重启连接判断，并且修改进度条
        flag = false;
        bt_conn.setEnabled(true);
        sb.setProgress(x);

    }

    // 点击加油
    public void bt_up(View view) {

        if (x<981) {
            x +=20;
            // 设置油门seekbar
            sb.setProgress(x);
            // 设置油门值，高八位，低八位
            data[3] = (byte) (x>>8);
            data[4] = (byte) (x&0xff);
            // 显示数据
            ShowCurrentInfo();
        } else {
            ShowCurrentInfo();
        }

    }

    // 点击减油
    public void bt_down(View view) {

        if (x>19) {
            x -= 20;
            // 设置油门seekbar
            sb.setProgress(x);
            // 设置油门值，高八位，低八位
            data[3] = (byte) (x>>8);
            data[4] = (byte) (x&0xff);
            // 显示数据
            ShowCurrentInfo();
        } else {
            ShowCurrentInfo();
        }

    }

    // 用滑动条设置油门参数
    public void sb_setgas() {

        sb.setMaxProgress(1000);

        sb.setOnSlideChangeListener(new VerticalSeekBar.SlideChangeListener() {
            @Override
            public void onStart(VerticalSeekBar slideView, int progress) {

            }

            @Override
            public void onProgress(VerticalSeekBar slideView, int progress) {

                // 油门赋值
                x = progress;
                // data赋值，高八位，低八位
                data[3] = (byte) (progress>>8);
                data[4] = (byte) (progress&0xff);
                // 数据更新显示
                ShowCurrentInfo();

            }

            @Override
            public void onStop(VerticalSeekBar slideView, int progress) {

            }
        });

    }

    // 点击向前
    public void bt_fw(View view) {

        // 先判定姿态
        while (backward<1500) {
            backward += 1;
        }

        if (forward == 3000) {
            forward -= 50;
        } else {
            forward += 50;
            // 给俯仰赋值，把二进制拆成高八位,低八位
            data[9] = (byte) (forward>>8);
            data[10] = (byte) (forward&0xff);

            // 数据更新显示
            ShowCurrentInfo();
        }

    }

    // 点击向后
    public void bt_bw(View view) {

        // 姿态判定
        while (forward>1500) {
            forward -= 1;
        }

        if (backward == 0) {
            backward += 50;
        } else {
            backward -= 50;
            // 给俯仰赋值，把二进制拆成高八位,低八位
            data[9] = (byte) (backward>>8);
            data[10] = (byte) (backward&0xff);

            // 数据更新显示
            ShowCurrentInfo();

        }

    }

    //点击向左
    public void bt_lw(View view)
    {

        // 姿态判定
        while (rightward<1500)
        {
            rightward += 1;
        }

        // 给俯仰赋值，把二进制拆成高八位,低八位
        if(leftward==3000)
        {
            leftward -= 50;
        }else {
            leftward += 50;
            data[7]=(byte) (leftward>>8);
            data[8]=(byte) (leftward&0xff);

            // 数据更新显示
            ShowCurrentInfo();

        }
    }

    //点击向右
    public void bt_rw(View view)
    {

        // 姿态判定
        while (leftward>1500)
        {
            leftward -= 1;
        }

        // 给俯仰赋值，把二进制拆成高八位,低八位
        if(rightward==0)
        {
            rightward += 50;
        }else {
            rightward -= 50;
            data[7]=(byte) (rightward>>8);
            data[8]=(byte) (rightward&0xff);

            //数据更新显示
            ShowCurrentInfo();

        }
    }

    // 点击起飞
    public void bt_tf(View view) {

        // 重制方向
        resetDerection();

        // 起飞到悬停值
        if (x<500) {

            // 中断降落
            flag2 = true;
            flag3 = false;
            if (thread_ld != null) {
                thread_ld.interrupt();
            }

            //创建起飞线程
            thread_tf = new Thread() {

                @Override
                public void run() {

                    while (x<500 && flag2) {

                        x += 1;

                        // 控制上下方向
                        // 油门赋值
                        data[3] = (byte) (x>>8);
                        data[4] = (byte) (x&0xff);

                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // 在子线程中开启子线程
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 显示信息
                                ShowCurrentInfo();
                                // 设置seekbar
                                sb.setProgress(x);
                            }
                        });

                    }
                    super.run();

                }

            };

            // 启动线程
            thread_tf.start();

        } else {
            youtext.setText("正常运行");
        }

    }

    // 点击降落
    public void bt_ld(View view) {

        // 重制方向
        resetDerection();

        // 开始降落
        if (x>0) {

            // 中断起飞
            flag2 = false;
            flag3 = true;

            // 创建降落线程
            thread_ld = new Thread() {
                @Override
                public void run() {
                    while (x>0 && flag3) {
                        x -= 1;

                        // 控制上下方向
                        data[3] = (byte) (x>>8);
                        data[4] = (byte) (x&0xff);

                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShowCurrentInfo();
                                sb.setProgress(x);
                            }
                        });
                    }
                    super.run();
                }
            };

            // 启动线程
            thread_ld.start();

        } else {
            youtext.setText("降落");
        }

    }

    // 显示实时数据
    public void ShowCurrentInfo()
    {

        uptext.setText("前：" + forward);
        downtext.setText("后：" + backward);
        lefttext.setText("左：" + leftward);
        righttext.setText("右：" + rightward);

        youtext.setText("油门：" + x);

    }

    @Override
    protected void onPostResume() {
        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onPostResume();
    }
}

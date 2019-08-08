package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.rabbitmq.HelloWorldClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class HelloWorldActivity extends AppCompatActivity {

    @BindView(R.id.et_server_address)
    EditText etServerAddress;
    @BindView(R.id.et_server_port)
    EditText etServerPort;
    @BindView(R.id.btn_init)
    Button btnInit;
    @BindView(R.id.btn_release)
    Button btnRelease;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.tv_receive)
    TextView tvReceive;
    @BindView(R.id.btn_clean)
    Button btnClean;
    @BindView(R.id.et_server_msg)
    EditText etServerMsg;
    @BindView(R.id.et_username)
    EditText etUsername;
    @BindView(R.id.et_password)
    EditText etPassword;
    private HelloWorldClient helloWorldClient;

    Unbinder unbinder;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);
        unbinder = ButterKnife.bind(this);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        if (tvReceive != null) {
                            String text = tvReceive.getText().toString();
                            text += "\n" + new String((byte[]) msg.obj);
                            tvReceive.setText(text);

                        }
                        break;
                }


            }
        };


    }


    private void init() {

        String url = etServerAddress.getText().toString().trim();
        int port = Integer.valueOf(etServerPort.getText().toString().trim());
        String userbane = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        new Thread() {
            @Override
            public void run() {
                helloWorldClient = new HelloWorldClient();
                helloWorldClient.setup(url, port, userbane, password);
                helloWorldClient.createSender();
                helloWorldClient.createReiver();
                helloWorldClient.setUiHanler(handler);
            }
        }.start();


    }


    @Override
    protected void onDestroy() {
        unbinder.unbind();
        handler.removeCallbacksAndMessages(null);
        handler = null;

        release();
        super.onDestroy();
    }

    private void release() {

        if (helloWorldClient != null) {
            helloWorldClient.release();
            helloWorldClient = null;
        }

    }

    private void send() {
        if (helloWorldClient != null) {
            helloWorldClient.sendMsg(etServerMsg.getText().toString());
        }

    }


    private void clean() {
        tvReceive.setText("");

    }

    @OnClick({R.id.btn_init, R.id.btn_release, R.id.btn_send, R.id.btn_clean})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_init:
                init();
                break;
            case R.id.btn_release:
                release();
                break;
            case R.id.btn_send:
                send();
                break;
            case R.id.btn_clean:
                clean();
                break;
        }
    }
}

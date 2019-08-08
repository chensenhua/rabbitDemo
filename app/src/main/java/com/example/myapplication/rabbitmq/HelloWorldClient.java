package com.example.myapplication.rabbitmq;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class HelloWorldClient {


    private ConnectionFactory factory;
    private Connection senderConn;
    private Channel senderChannll;

    private Connection receiveConn;
    private Channel receiveChannel;

    private HandlerThread handlerThread;

    private Handler handler;


    private Handler uiHanler;


    public void setUiHanler(Handler uiHanler) {
        this.uiHanler = uiHanler;
    }

    public void setup(String url, int port,String name,String pass) {
        factory = new ConnectionFactory();
        factory.setHost(url);
        factory.setPort(port);
        factory.setUsername(name);
        factory.setPassword(pass);
        handlerThread = new HandlerThread("helloDemo");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

    }


    public void createSender() {
        try {
            senderConn = factory.newConnection();
            Log.e("sen", " connect succedd ");
            senderChannll = senderConn.createChannel();
            Log.e("sen", " create channel succedd ");
            senderChannll.queueDeclare("hello", false, false, true, null);
            Log.e("sen", " create queue succedd ");

            if (uiHanler != null) {
                Message message = uiHanler.obtainMessage();
                message.obj = "连接成功";
                message.what=1;
                uiHanler.sendMessage(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (uiHanler != null) {
                Message message = uiHanler.obtainMessage();
                message.obj = "连接失败";
                message.what=1;
                uiHanler.sendMessage(message);
            }
        } catch (TimeoutException e) {
            e.printStackTrace();

        }

    }


    public void sendMsg(final String msg) {
        if (handler == null || senderChannll == null) {
            return;
        }

        handler.post(() -> {
            try {
                senderChannll.basicPublish("", "hello", null, msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("sen", "send msg-->" + msg);
        });

    }


    public void createReiver() {

        try {
            receiveConn = factory.newConnection();
            receiveChannel = receiveConn.createChannel();
            receiveChannel.queueDeclare("hello1", false, false, true, null);
            receiveChannel.basicConsume("hello1", new DefaultConsumer(receiveChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    Log.e("sen", "receive msg-->" + new String(body));
                    if (uiHanler != null) {
                        Message message = uiHanler.obtainMessage();
                        message.obj = body;
                        message.what=0;
                        uiHanler.sendMessage(message);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }

    public void release() {

        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (factory != null) {
                        factory = null;

                    }

                    if (senderChannll != null) {
                        try {
                            senderChannll.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                        senderChannll = null;
                    }


                    if (receiveChannel != null) {
                        try {
                            receiveChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                        receiveChannel = null;
                    }

                    if (senderConn != null) {
                        try {
                            senderConn.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        senderConn = null;
                    }
                    if (receiveConn != null) {
                        try {
                            receiveConn.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        receiveConn = null;
                    }

                    if (handler != null) {
                        handler.removeCallbacksAndMessages(null);
                        handler = null;
                    }

                    if (handlerThread != null) {
                        handlerThread.quitSafely();
                        handlerThread = null;
                    }
                }


            });
        }


    }
}

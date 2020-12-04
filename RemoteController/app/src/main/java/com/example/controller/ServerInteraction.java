package com.example.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;

public class ServerInteraction {

    private Socket socket;
    private String Host;
    private int Port;

    DataOutputStream out = null;

    DataInputStream getMessageStream = null;

//    private BufferedReader br = null;

    public ServerInteraction(String host, int port, Socket socket1) {
        Host = host;
        Port = port;
        socket = socket1;
    }

    //连接服务器
    public boolean connectTheServer() throws IOException {

        boolean ifSuccess = false;

        //获取客户端的IP地址
        InetAddress address = InetAddress.getLocalHost();
        String ip = address.getHostAddress();
        //2.向服务器端发送信息
        try {
            sentMessageToServer("客户端：~" + ip + "~ 接入服务器！！接口："+Port);
            ifSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ifSuccess;

    }

    //断开服务器
    public void disconnectTheServer() throws IOException {
        socket.close();
    }

    //向服务器发送信息
    public void sentMessageToServer(String message) throws IOException {
        if (socket.isOutputShutdown()) {
            socket = new Socket(Host, Port);
        }
        OutputStream os = socket.getOutputStream();//字节输出流
        PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流
        pw.write(message);
        pw.flush();
        socket.shutdownOutput();//关闭输出流
//        os.write(message.getBytes("utf-8"));
    }

    //读取服务器发送的信息
    public String getMessageFromServer() throws IOException {
        //InputStream is = socket.getInputStream();//字节输入流
        //将Socket对应的输入流封装成BufferedReader对象
        BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        String msg=br.readLine();
        System.out.println("socket.isOutputShutdown() = "+socket.isOutputShutdown());
        return msg;
    }

    /////////////////////////////////图像传输///////////////////////////////////

    public DataInputStream getMessageStream() throws Exception {
        try {
            getMessageStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            return getMessageStream;
        } catch (Exception e) {
            e.printStackTrace();
            if (getMessageStream != null)
                getMessageStream.close();
            throw e;
        }
    }

    //从服务器获取图像并保存到本地
    private void getMessage(String savePath) {
        if (socket == null)
            return;
        DataInputStream inputStream = null;
        try {
            inputStream = getMessageStream();
        } catch (Exception e) {
            System.out.print("接收消息缓存错误\n");
            return;
        }

        try {
            //本地保存路径，文件名会自动从服务器端继承而来。
            int bufferSize = 1024;
            byte[] buf = new byte[bufferSize];
//            int passedlen = 0;
//            long len=0;

//            savePath += inputStream.readUTF();
//            System.out.println(inputStream.readUTF());
//            DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savePath)));
            DataOutputStream fileOut = new DataOutputStream(new FileOutputStream(savePath));

//            len = inputStream.readLong();

//            System.out.println("文件的长度为:" + len + "\n");
//            System.out.println("开始接收文件!" + "\n");

            int i =0;
            while (true) {
                int read = 0;
                if (inputStream != null) {
                    read = inputStream.read(buf);
                }
//                passedlen += read;
                if (read == -1) {
                    break;
                }
                System.out.println("i = "+i+++"  read = "+read);
                //下面进度条本为图形界面的prograssBar做的，这里如果是打文件，可能会重复打印出一些相同的百分比
//                System.out.println("文件接收了" +  (passedlen * 100/ len) + "%\n");
                fileOut.write(buf, 0, read);
            }
            System.out.println("接收完成，文件存为" + savePath + "\n");

            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("接收消息错误" + "\n");
            return;
        }
    }

    private void loadImageFromServer(String savePath) {
        if (socket == null)
            return;
//        DataInputStream inputStream = null;
//        try {
//            inputStream = getMessageStream();
//        } catch (Exception e) {
//            System.out.print("接收消息缓存错误\n");
//            return;
//        }

        try {
            while(true) {
                System.out.println("Check whether output is shut down");
                if (socket.isOutputShutdown()) {
                    socket = new Socket(Host, Port);
                }
                System.out.println("Output is open");

//                InputStream in = socket.getInputStream();
//                BufferedInputStream bis = new BufferedInputStream(in);
//                Bitmap bitmap = BitmapFactory.decodeStream(bis);//这个好像是android里的
//                //首先看看文件是否存在
//                File bmpFile = new File(savePath);
//                if (bmpFile.exists()) {
//                    bmpFile.delete();
//                }
//                FileOutputStream out = new FileOutputStream(bmpFile);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,out);
//                bis.close();
//                in.close();



                DataInputStream dataInput = new DataInputStream(socket.getInputStream());
                int size = dataInput.readInt();
                byte[] data = new byte[size];
                int len = 0;
                while (len < size) {
                    len += dataInput.read(data, len, size - len);
                }


//                ByteArrayOutputStream outPut = new ByteArrayOutputStream();
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
//                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outPut);
                File file = new File(savePath);
                //文件输出流
                FileOutputStream fileOutputStream=new FileOutputStream(file);
                //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
                bmp.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                //写入，这里会卡顿，因为图片较大
                fileOutputStream.flush();
                //记得要关闭写入流
                fileOutputStream.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String imageLoadTest(String savePath) {
        try {
            //发送开始接收数据指令
            sentMessageToServer("windows");
            //服务器确认开始发送，返回文件名
            String message = getMessageFromServer();
            System.out.println("message: "+message);
            //生成完整文件路径
            savePath += message;
            System.out.println("savePath: "+savePath);
            System.out.println("Check whether output is shut down");
            if (socket.isOutputShutdown()) {
                socket = new Socket(Host, Port);
            }
            System.out.println("Output is open");
            //确认本地文件是否存在
            ImageBufferManager.checkFileStatus(savePath);
            //开始接收文件
            getMessage(savePath);
            Log.v("ServerInteraction", "File received successfully.");
            return savePath;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

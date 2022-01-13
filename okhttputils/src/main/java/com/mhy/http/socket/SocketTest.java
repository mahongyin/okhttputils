package com.mhy.http.socket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created By Mahongyin
 * Date    2021/5/11 16:37
 */
 class SocketTest {

    public static void printMsg(String msg) {
        boolean isExit = false;
       //SocketServer.startServer();
        while (!isExit) {
            if (msg.startsWith("exit")) {
                break;
            }
            if (msg.startsWith("send")) {
                sendMessage(msg);
            } else if (msg.startsWith("list")) {
                printTotal();
            } else if (msg.startsWith("all")) {
                allSendMsg(msg);
            } else {
             //("输入错误 请重新输入");
            }
        }
        // 关闭 清空
//        SocketServer.shutDown();
    }

    private static void allSendMsg(String line) {
        String[] field = line.split("//");
        if (field.length == 2) {
            SocketServer.sendMsgAll(field[1]);
            //("发送结果为：" + SocketServer.sendMsgAll(field[1]));
        } else {
            //("格式不正确 例：all//message");
        }
    }

    private static void printTotal() {
        List<String> totalClients = SocketServer.getTotalClients();
        //("连接数量为：" + totalClients.size());
        for (String totalClient : totalClients) {
            //(totalClient);
        }
    }

    private static void sendMessage(String line) {
        String[] field = line.split("//");
        if (field.length == 3) {
            // 格式正确
            SocketServer.sendMessage(field[1], field[2]);
            //("send结果为:" + SocketServer.sendMessage(field[1], field[2]));
        } else {
            //("命令不正确。例子：send//name//msg");
        }
    }
/**************************************************/

    protected void TCPServer() {
        try {
            //创建服务器端 Socket，指定监听端口
            ServerSocket serverSocket = new ServerSocket(8899);
            //等待客户端连接
            Socket clientSocket = serverSocket.accept();
            //获取客户端输入流，
            InputStream is = clientSocket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String data = null;
            //读取客户端数据
            while ((data = br.readLine()) != null) {
                //("服务器接收到客户端的数据：" + data);
            }
            //关闭输入流
            clientSocket.shutdownInput();
            //获取客户端输出流
            OutputStream os = clientSocket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            //向客户端发送数据
            pw.print("服务器给客户端回应的数据");
            pw.flush();
            //关闭输出流
            clientSocket.shutdownOutput();
            //关闭资源
            pw.checkError();
            os.close();
            br.close();
            isr.close();
            is.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void TCPClient(){
        try {
            //创建客户端Socket，指定服务器的IP地址和端口
            Socket socket = new Socket(InetAddress.getLocalHost(),8899);
            //获取输出流，向服务器发送数据
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.write("客户端给服务器端发送的数据");
            pw.flush();
            //关闭输出流
            socket.shutdownOutput();

            //获取输入流，接收服务器发来的数据
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String data = null;
            //读取客户端数据
            while((data = br.readLine()) != null){
                //("客户端接收到服务器回应的数据：" + data);
            }
            //关闭输入流
            socket.shutdownInput();

            //关闭资源
            br.close();
            isr.close();
            is.close();
            pw.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void UDPServer(){
        try {
            //创建服务器端 Socket，指定端口
            DatagramSocket socket = new DatagramSocket(8899);
            //创建数据报用于接收客户端发送的数据
            byte[] bytes = new byte[1024];
            DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
            //接收客户端发送的数据
            socket.receive(packet);
            //读取数据（也可以调用 packet.getData()）
            String info = new String(bytes,0,packet.getLength());

            //返回数据
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            byte[] data = "服务器返回的数据".getBytes();
            DatagramPacket dataPacket = new DatagramPacket(data,data.length,address,port);
            socket.send(dataPacket);
            //关闭 Socket
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void UDPClient(){
        try {
            //创建客户端 Socket
            DatagramSocket socket = new DatagramSocket();
            //创建数据包
            byte[] data = "向服务器发送的数据".getBytes();
            InetAddress address = InetAddress.getLocalHost();
            int port = 8899;
            DatagramPacket packet = new DatagramPacket(data,data.length,address,port);
            //发送数据包
            socket.send(packet);

            //接收服务器响应的数据包
            byte[] info = new byte[1024];
            DatagramPacket infoPacket = new DatagramPacket(info,info.length);
            String receiveInfo = new String(info,0,infoPacket.getLength());

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
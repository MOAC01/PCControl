package per.uyscuti.thread;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import per.pccontrol.MainActivity;

/**
 * Created by AlphaGo on 2017/12/23.
 */

public class SocketConnectThread extends Thread {
    private Message message;
    private SocketConnObj sco;
    private Handler handler;

    public SocketConnectThread(SocketConnObj sco, Handler handler) {
        this.sco = sco;
        this.handler = handler;

    }

    public void run(){
        message=new Message();
        message.what=1;
        MainActivity.socket=new Socket();
        SocketAddress address=new InetSocketAddress(sco.getHost(),sco.getPort());
        try {
            MainActivity.socket.connect(address,5000);
            DataInputStream in=new DataInputStream(MainActivity.socket.getInputStream());
            byte connState=in.readByte();
            if((int)connState==1)
                message.arg1=1;
            else message.arg1=0;

        } catch (IOException e) {
            message.arg1=-1;
            e.printStackTrace();
        }

        handler.sendMessage(message);


    }

}

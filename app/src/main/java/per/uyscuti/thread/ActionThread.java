package per.uyscuti.thread;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import per.op.Control;
import per.pccontrol.MainActivity;

/**
 * Created by AlphaGo on 2017/12/23.
 */

public class ActionThread extends Thread{

    private Control control;
    private Handler handler;
    public ActionThread(Control control, Handler handler) {
        this.control = control;
        this.handler=handler;
    }

    public void run(){
        String command=control.paraseAction();
        Log.e("Command", command);
        Message message=new Message();
        message.what=2;
        try {
            DataOutputStream outputStream=new DataOutputStream(MainActivity.socket.getOutputStream());
            byte[] bytes=command.getBytes();
            outputStream.write(bytes,0,bytes.length);
            message.arg1=1;
        } catch (IOException e) {
            e.printStackTrace();
            message.arg1=-1;
        }

        handler.sendMessage(message);
    }
}

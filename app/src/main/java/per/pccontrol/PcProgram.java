package per.pccontrol;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import per.op.Program;

public class PcProgram extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listView;
    private String responseXML;
    private List<Program> programList;
    private String backupHost;
    private int backupPort;
    private ProgressBar progressBar;
    private Socket socket=MainActivity.socket;
    private ProgramAdapter adapter;
    private int CLOSED=0;
    @SuppressLint("HandlerLeak")
    private Handler uiHandler=new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case 1:
                    break;
                case 2:
                    loaded();
                    break;
                case 3:
                    CLOSED=1;
                    break;
                case 0:
                case -1:
                case -2:
                    Toast.makeText(PcProgram.this,"请求服务器失败",Toast.LENGTH_SHORT).show();
                    break;
                case -3:
                    Toast.makeText(PcProgram.this,"该进程不存在或已经关闭",Toast.LENGTH_SHORT).show();
                    break;
                default:break;

            }

        }
    };

    public void loaded(){             //ListView加载完成
         adapter=new ProgramAdapter(PcProgram.this,
                android.R.layout.simple_list_item_2,programList);
        listView.setAdapter(adapter);
        Toast.makeText(PcProgram.this,"加载完成",Toast.LENGTH_SHORT).show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int index=position;
                Program program=programList.get(index);
                final String str=program.getPROCESS_NAME();
                final String []params=str.split(":");
                AlertDialog.Builder dialog=new AlertDialog.Builder(PcProgram.this);//创建对话框
                dialog.setTitle("关闭确认");
                dialog.setMessage("确认要关闭这个进程吗？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new KillProcessThread(params[1]).start();
                        if(CLOSED==1){
                            programList.remove(index);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(PcProgram.this,"关闭成功",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                dialog.show();  //弹出对话框

            }
        });

        progressBar.setVisibility(View.GONE);

        responseXML=null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_program);
        initViews();
        Intent intent=getIntent();
        backupHost=intent.getStringExtra("server");
        backupPort=intent.getIntExtra("port",0);

    }
    public void initViews(){
        toolbar=findViewById(R.id.tool_bar);
        listView=findViewById(R.id.list_program);
        setSupportActionBar(toolbar);
        toolbar.setTitle("电脑上正在运行的程序");
        progressBar=findViewById(R.id.progress_bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(socket==null || socket.isClosed()){
            Toast.makeText(this,"尚未连接至服务端",Toast.LENGTH_SHORT).show();
            this.finish();
        }
        else getPrograms();

    }

    public void getPrograms(){
        progressBar.setVisibility(View.VISIBLE);
        new GetThread().start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pro,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.refresh:
                getPrograms();
                break;
            case android.R.id.home:
                this.finish();
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("OnStop", "onStop: execute");
        Log.d("responseXML", String.valueOf(responseXML==null));
        Log.d("programList", String.valueOf(programList==null));
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("OnPause", "OnPause: execute");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "onDestroy: execute");

    }

    /**
     *   请求服务端获取电脑运行的程序主要是服务端发送一段关于进程的xml字符串，每
     *   项包含每个进程的进程名，窗口标题等，服务端以写字节流的形式发送xml到客户端。
     *   但由于服务端C#与java的byte范围不同，所以在请求服务端获取电脑进程时要分3次通信，
     *   主要是将服务端表示xml大小的int型转换成字符串，客户端接收后再进行还原，
     *   然后将此大小作为接收xml字节流的字节数组的大小.
     */

    public List<Program> parseXML(String xml){     //解析服务端发送过来的xml，并生成ListView对象
        List<Program> list=new ArrayList<>();
        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser parser=factory.newPullParser();
            parser.setInput(new StringReader(xml));
            int eventType=parser.getEventType();
            String appName="";
            String processName="";
            while (eventType!=XmlPullParser.END_DOCUMENT){
                String nodeName=parser.getName();
                Program program=new Program();
                switch (eventType){
                    case XmlPullParser.START_TAG:
                     if("name".equals(nodeName)){        //窗口标题
                         appName=parser.nextText();
                     }else if("pname".equals(nodeName)){   //进程名
                         processName=parser.nextText();
                     }
                     break;

                    case XmlPullParser.END_TAG:          //生成ListView项
                        program.setNAME("窗口标题:"+appName);
                        program.setPROCESS_NAME("进程:"+processName);
                        list.add(program);
                        break;

                }
                eventType=parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return list;

    }

    class GetThread extends Thread{
        Message message=new Message();
        DataOutputStream outputStream=null;
        DataInputStream inputStream=null;
        public void run(){
            if(socket.isClosed() || socket==null){
                Log.d("Socket close", "true");
              socket=new Socket();
              SocketAddress address=new InetSocketAddress(backupHost,backupPort);

                try {
                    socket.connect(address,5000);
                    inputStream=new DataInputStream(socket.getInputStream());
                    int connState=(int)inputStream.readByte();
                    message.what=connState;
                } catch (IOException e) {
                    message.what=0;
                    e.printStackTrace();
                }
            }
            try {                   //模拟“三次握手”协议
                if(outputStream==null)
                    outputStream=new DataOutputStream(socket.getOutputStream());
                byte[]bytes="get".getBytes();
                outputStream.write(bytes,0,bytes.length);   //第一次发送获取命令
                if(inputStream==null)
                    inputStream=new DataInputStream(socket.getInputStream());
                int one=(int)inputStream.readByte();  //获取xml字符串的大小的字符串的大小，例如xml的大小是720，则one为3，共3个字符，每个1字节
                byte[]result=new byte[one];           //存储大小的字节数组
                Log.d("ONE", String.valueOf(one));
                String status="ready";
                outputStream.write(status.getBytes(),0,status.getBytes().length);  //第二次发送，第一次准备完毕
                inputStream.read(result,0,result.length);  //获取真正的xml字符串的大小的字符串，例如xml的大小是720，则result为"720"
                String size=new String(result);   //将大小的字节流还原成字符串
                Log.d("SIZE", size);
                outputStream.write("OK".getBytes(),0,"OK".getBytes().length);  //一切准备就绪，第三次发送，告诉服务端可以发送xml
                byte []response=new byte[Integer.parseInt(size)];    //将大小的字符串例如"720"转换成整型并建立字节数组以准备接收xml字节流
                inputStream.read(response,0,response.length);   //读取xml字节流
                String tag=new String(response);     //将xml还原成字符串
                responseXML=new String(tag.getBytes("UTF-8"),"UTF-8"); //处理中文编码
                message.what=2;

                } catch (IOException e) {
                    e.printStackTrace();
                    message.what=-2;
                }
                if(message.what==2)
                    programList=parseXML(responseXML);
                uiHandler.sendMessage(message);

            }
        }


        class KillProcessThread extends Thread{          //杀进程
            private String process;
            public KillProcessThread(String process) {
                this.process = process;
            }

            public void run(){
                DataOutputStream out=null;
                DataInputStream in=null;
                Message message=new Message();
                message.what=3;
                if(socket.isClosed() || socket==null){
                    socket=new Socket();
                    SocketAddress address=new InetSocketAddress(backupHost,backupPort);

                    try {
                        socket.connect(address,5000);
                        in=new DataInputStream(socket.getInputStream());
                        int connState=(int)in.readByte();
                        message.what=connState;
                    } catch (IOException e) {
                        message.what=0;
                        e.printStackTrace();
                    }
                }

                try {
                    if(out==null) out=new DataOutputStream(socket.getOutputStream());
                    String command="k,"+process.trim();
                    byte[]data=command.getBytes();
                    out.write(data,0,data.length);
                    if(in==null) in=new DataInputStream(socket.getInputStream());
                    int isClosed=(int)in.readByte();
                    if(isClosed==2)
                        message.what=-3;
                }catch (IOException e){
                    message.what=-1;
                    e.printStackTrace();
                }

                uiHandler.sendMessage(message);
            }

        }
}

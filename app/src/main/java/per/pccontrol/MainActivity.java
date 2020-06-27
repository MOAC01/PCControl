package per.pccontrol;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import per.op.Control;
import per.uyscuti.thread.ActionThread;
import per.uyscuti.thread.SocketConnObj;
import per.uyscuti.thread.SocketConnectThread;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    /**
     * Created by UY Scuti on 2017/12/22.
     */
    private EditText ipAddress,port,future_time;
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ListView drawerListView;
    private Spinner timeSpinner;
    private Spinner opSpinner;
    private Button conn,oprate;
    private TextView connStatus;
    private CheckBox checkBox;
    private List<DrawerListItem> adapterList=new ArrayList<>();
    private List<String> timeList=new ArrayList<>();
    private List<String> opList=new ArrayList<>();
    private ActionBarDrawerToggle toggle;
    public static Socket socket;
    private String extraServer;
    private int extraPort;
    @SuppressLint("HandlerLeak")
    public Handler handler=new Handler(){           //通过线程返回的信息反馈给用户

        public void handleMessage(Message message){

           switch (message.what){
               case 1:
                    String msg="连接失败,请检查：手机与电脑是否连接在同一WiFi下；电脑服务端是否已经打开并监听；IP地址与端口号是否正确";
                    if(message.arg1==-1){
                        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
                        dialog.setMessage(msg);
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                        @Override
                         public void onClick(DialogInterface dialog, int which) {

                             }
                          });
                          dialog.show();
                          changeConnStatus(false);
                          socket=null;
                         }
                         else if(message.arg1==1){
                             Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
                             changeConnStatus(true);
                         }else if(message.arg1==0){
                             Toast.makeText(MainActivity.this,"连接失败，服务器无响应",Toast.LENGTH_SHORT).show();
                         }
                         break;

                     case 2:
                         if(message.arg1==1)
                             Toast.makeText(MainActivity.this,"执行成功！",Toast.LENGTH_SHORT).show();
                         else  if(message.arg1==-1)
                             Toast.makeText(MainActivity.this,"执行失败！",Toast.LENGTH_SHORT).show();
                         break;
                 }
        }

     };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            initList();
            initViews();
            SharedPreferences pref = getSharedPreferences("settings", MODE_PRIVATE);
            int isAutoConn=pref.getInt("autoConn",0);
            if(isAutoConn==1)
                doConnect();

        }

        public void initList(){             //drawer滑动抽屉的项
            DrawerListItem more=new DrawerListItem();
            more.setName("电脑上运行的程序");
            more.setAction(PcProgram.class);
            more.setimgId(R.mipmap.ic_desktop_mac_grey600_24dp);
            adapterList.add(more);

            DrawerListItem help=new DrawerListItem();
            help.setimgId(R.mipmap.ic_help_grey600_24dp);
            help.setAction(Object.class);
            help.setName("使用帮助");
            help.setExtra("http://www.vegetapage.com/?p=91");
            adapterList.add(help);

            DrawerListItem report=new DrawerListItem();
            report.setimgId(R.mipmap.ic_message_grey600_24dp);
            report.setName("错误反馈");
            help.setExtra("http://www.vegetapage.com");
            //report.setAction(Object.class);
            adapterList.add(report);

            DrawerListItem about=new DrawerListItem();
            about.setimgId(R.mipmap.ic_info_outline_grey600_24dp);
            about.setName("关于");
            about.setAction(About.class);
            adapterList.add(about);

            DrawerListItem settings=new DrawerListItem();
            settings.setimgId(R.mipmap.ic_settings_grey600_24dp);
            settings.setName("设置");
            settings.setAction(Settings.class);
            adapterList.add(settings);

            timeList.add("秒");
            timeList.add("分钟");
            timeList.add("小时");
            timeList.add("立即");

            opList.add("关机");
            opList.add("重启");
            opList.add("注销");

        }

       public void initViews(){                    //初始化控件
        toolbar=findViewById(R.id.tool_bar);       //too_bar
        mDrawerLayout=findViewById(R.id.my_drawer_layout);
        checkBox=findViewById(R.id.remember);
        checkBox.setChecked(true);
        setSupportActionBar(toolbar);
        toggle=new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,
                R.string.drawer_close);

        //主页菜单键与返回键的旋转切换特效
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        drawerListView=findViewById(R.id.lv_drawer);   //主页菜单的listview
        DrawerAdapter adapter=new DrawerAdapter(MainActivity.this,R.layout.drawer_list,adapterList);
        drawerListView.setAdapter(adapter);
        connStatus=findViewById(R.id.status);
        timeSpinner=findViewById(R.id.time_select);
        opSpinner=findViewById(R.id.op_select);
        setSpinnerData();
        ipAddress=findViewById(R.id.text_ip);
        port=findViewById(R.id.port);
        future_time=findViewById(R.id.future);
        conn=findViewById(R.id.btn_connect);
        oprate=findViewById(R.id.btn_go);
        conn.setOnClickListener(this);
        oprate.setOnClickListener(this);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==3)
                    future_time.setEnabled(false);
                else future_time.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
         });

        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   //这段写得有点乱了，以后再改进^_^
                DrawerListItem option=adapterList.get(position);
                if(option.getAction()!=null){
                    String extra=option.getExtra();
                    if(extra!=null && isURL(extra)){
                          if(isURL(extra)){
                                Intent intent= new Intent();
                                intent.setAction("android.intent.action.VIEW");
                                Uri content_url = Uri.parse(extra);
                                intent.setData(content_url);
                                startActivity(intent);
                            }


                    }
                    else{
                        Intent intent=new Intent(MainActivity.this,option.getAction());
                        intent.putExtra("server",extraServer);
                        intent.putExtra("port",extraPort);
                        startActivity(intent);
                    }
                }else {
                    //Toast.makeText(MainActivity.this,"NO Action",Toast.LENGTH_SHORT).show();
                    final Intent intent= new Intent();
                    AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("错误反馈");
                    dialog.setMessage("如果你在程序运行期间遇到了错误，" +
                            "可以到我的博客文章底下进行评论反馈，提交反馈或评论时不必填写真实姓名。");
                    dialog.setPositiveButton("去反馈", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse("http://www.vegetapage.com/?p=91");
                            intent.setData(content_url);
                            startActivity(intent);
                        }

                    });
                    dialog.setNegativeButton("放弃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                }
            }
        });

        SharedPreferences pref= getSharedPreferences("config",MODE_PRIVATE);
        boolean remember=pref.getBoolean("remember",false);
        if(remember){
            ipAddress.setText(pref.getString("ip",""));
            port.setText(pref.getString("port",""));
        }


      }

      public void setSpinnerData(){

          ArrayAdapter<String> timeAdapter=new ArrayAdapter<>(MainActivity.this,
                  android.R.layout.simple_spinner_dropdown_item,timeList);
          timeSpinner.setAdapter(timeAdapter);
          ArrayAdapter<String> opAdapter=new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,opList);
          opSpinner.setAdapter(opAdapter);

      }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_connect:       //连接按钮
                if(socket!=null) {
                    try {
                        socket.close();
                        socket=null;
                        changeConnStatus(false);
                        Toast.makeText(MainActivity.this,"已断开",Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else doConnect();
                break;
            case R.id.btn_go:       //执行命令按钮
                String unit,action; //时间单位和命令动作
                int future=0;
                Control control;   //封装时间和动作的类
                unit= (String) timeSpinner.getSelectedItem();
                action= (String) opSpinner.getSelectedItem();
                if(future_time.isEnabled()){
                    String temp=future_time.getText().toString();
                    if(temp.equals("")){
                        Toast.makeText(MainActivity.this,"请填入时间",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else future=Integer.parseInt(temp);
                }else {
                    future=0;
                }
                control=new Control(future,action,unit);
                if(socket!=null)
                    new ActionThread(control,handler).start();  //执行相关命令的线程
                else Toast.makeText(MainActivity.this,"你还没有连接到电脑哦",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void doConnect(){
        extraServer=ipAddress.getText().toString();
        String mPort=port.getText().toString();
        if(checkConnParameter(extraServer,mPort)==true){
            extraPort=Integer.parseInt(mPort);
            SocketConnObj obj=new SocketConnObj(extraServer,extraPort);
            new SocketConnectThread(obj,handler).start();
            SharedPreferences.Editor editor=getSharedPreferences("config",MODE_PRIVATE).edit();
            if(checkBox.isChecked()){
                editor.putString("ip",extraServer);
                editor.putString("port",mPort);
                editor.putBoolean("remember",true);

            }else{
                editor.clear();
            }

        }
        else Toast.makeText(MainActivity.this,"IP地址和端口号不能为空哦",Toast.LENGTH_SHORT).show();

    }

    public boolean checkConnParameter(String agr0,String arg1){
        if(agr0.equals("") || arg1.equals(""))
            return false;
        else return true;
    }


    public void changeConnStatus(boolean flag){
        if(flag==true){
            conn.setText("断开");
            ipAddress.setEnabled(false);
            port.setEnabled(false);
            connStatus.setText("连接状态:已连接");
        }else {
            conn.setText("连接");
            ipAddress.setEnabled(true);
            port.setEnabled(true);
            connStatus.setText("连接状态:未连接");
        }
    }

    public boolean isURL(String str){
        boolean flag = false;
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式

        Pattern pat = Pattern.compile(regex.trim());//比对
        Matcher mat = pat.matcher(str.trim());
        flag = mat.matches();//判断是否匹配

        return flag;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause execute ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(socket!=null && !socket.isClosed()){
            changeConnStatus(true);
        }
        Log.d("MainActivity", "onResume execute ");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String []command={"关机","重启","注销"};
        Control control=new Control(0,command[0],"秒");
        switch (item.getItemId()){
            case R.id.shutdown_s:
                control=new Control(0,command[0],"秒");
                break;
            case R.id.shutdown_r:
                control=new Control(0,command[1],"秒");
                break;
            case R.id.shutdown_l:
                control=new Control(0,command[2],"秒");
                break;
            default:break;
        }
        if(socket!=null)
            new ActionThread(control,handler).start();
        else Toast.makeText(MainActivity.this,"你还没有连接到电脑哦",Toast.LENGTH_SHORT).show();
        return true;
    }
}

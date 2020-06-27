package per.pccontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import per.op.DownloadUtils;
import per.op.Update;

public class About extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textView;
    private ListView listView;
    private HttpURLConnection conn;
    private Update obj=null;
    List<String> mList=new ArrayList<>();
    @SuppressLint("HandlerLeak")
    private Handler uiHandler=new Handler(){
        public void handleMessage(Message msg){
           switch (msg.what){
               case 6:
                   Toast.makeText(About.this,"当前已是最新版本",Toast.LENGTH_SHORT).show();
                   break;
               case 7:
                   findNewUpdate();
                   break;
               case 0:
                   Toast.makeText(About.this,"下载失败",Toast.LENGTH_SHORT).show();
               case -1:
               case -2:
               case -3:
                   Toast.makeText(About.this,"检查更新时出错",Toast.LENGTH_SHORT).show();
                   break;
           }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        toolbar=findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("关于");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textView=findViewById(R.id.text_version);
        textView.setText("当前版本:"+getCurrentVersionName());
        listView=findViewById(R.id.about_list);
        mList.add("检查新版本");
        mList.add("作者");
        mList.add("开放源代码");
        ArrayAdapter<String> adapter=new ArrayAdapter<>(About.this,android.R.layout.simple_list_item_1,mList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item=mList.get(position);
                switch (item){
                    case "检查新版本":
                        checkUpdate();
                        break;
                    case "作者":
                        authorPage();
                        break;
                    case "开放源代码":
                        openSource();
                        break;
                    default:break;
                }
            }
        });

    }

    public String getCurrentVersionName(){    //获取当前版本
        String verName = "";
        try {
            verName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;

    }

    public int getCurrentVersionCode(){
        PackageManager packageManager=this.getPackageManager();
        PackageInfo packageInfo;
        String versionCode="";
        try {
            packageInfo=packageManager.getPackageInfo(this.getPackageName(),0);
            versionCode=packageInfo.versionCode+"";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(versionCode);
    }

    public void checkUpdate(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message=new Message();
                message.what=2;
                try {
                    URL url=new URL("http://www.vegetapage.com/wp-content/uploads/software/info.xml");
                    conn= (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(3000);
                    conn.setRequestMethod("GET");
                    conn.setReadTimeout(3000);
                    InputStream in=conn.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb=new StringBuilder();
                    String line;
                    while ((line=reader.readLine())!=null)
                        sb.append(line);
                    Log.e("Thread", sb.toString());
                    obj=getUpdateInfo(sb.toString());
                    if(getCurrentVersionCode()<obj.getVersionCode()){
                        message.what=7;
                    }else{
                       message.what=6;
                    }

                } catch (MalformedURLException e) {
                    message.what=-2;
                    e.printStackTrace();
                } catch (IOException e) {
                    message.what=-1;
                    e.printStackTrace();
                }
                uiHandler.sendMessage(message);
            }
        }).start();

    }

    public void findNewUpdate(){
        AlertDialog.Builder dialog=new AlertDialog.Builder(About.this);
          dialog.setTitle("发现新版本");
          dialog.setMessage("更新内容:"+obj.getUpdatePlain());
          dialog.setCancelable(false);
          dialog.setPositiveButton("下载更新", new DialogInterface.OnClickListener() {
          @Override
           public void onClick(DialogInterface dialog, int which) {
              if (ContextCompat.checkSelfPermission(About.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                      != PackageManager.PERMISSION_GRANTED) {
                  //申请WRITE_EXTERNAL_STORAGE权限
                  ActivityCompat.requestPermissions(About.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                          1);
              }else{
                  DownloadUtils dlu=new DownloadUtils(About.this);
                  dlu.downloadAPK(obj.getResourceUri(),"电脑控.apk");
              }


             }
          });
         dialog.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {

              }
         });
        dialog.show();
    }

    public void authorPage(){
        Intent intent= new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse("http://www.vegetapage.com");
        intent.setData(content_url);
        startActivity(intent);
    }

    public Update getUpdateInfo(String XML) {
        Message message=new Message();
        message.what=1;
        XmlPullParserFactory factory= null;
        Update update=new Update();
        try {
            factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser=factory.newPullParser();
            parser.setInput(new StringReader(XML));
            int eventType=parser.getEventType();
            String name="1",plain="2",uri="3";
            int code=-1;
            while (eventType!=XmlPullParser.END_DOCUMENT){
                String nodeName=parser.getName();
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        if("name".equals(nodeName)){
                            name=parser.nextText();
                        }else if("code".equals(nodeName)){
                            code=Integer.parseInt(parser.nextText());
                        }else if("plain".equals(nodeName)){
                            plain=parser.nextText();
                        }else if("uri".equals(nodeName)){
                            uri=parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        update.setVersionName(name);
                        update.setVersionCode(code);
                        update.setUpdatePlain(plain);
                        update.setResourceUri(uri);
                    break;
                    default:break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            message.what=-2;
            e.printStackTrace();
        } catch (IOException e) {
            message.what=-3;
            e.printStackTrace();
        }
        uiHandler.sendMessage(message);
        return update;
    }

    public void openSource(){
        Intent intent= new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse("http://www.vegetapage.com/?p=95");
        intent.setData(content_url);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            this.finish();
        return true;
    }
}

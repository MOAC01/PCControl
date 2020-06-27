package per.pccontrol;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    private Toolbar toolbar;
    private Switch mSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar=findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSwitch=findViewById(R.id.my_switch);
        Log.e("Setting", String.valueOf(mSwitch==null));
        SharedPreferences pref = getSharedPreferences("settings", MODE_PRIVATE);
        if(pref.getBoolean("switchStatus",false))
            mSwitch.setChecked(true);
        else mSwitch.setChecked(false);

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor=getSharedPreferences("settings",MODE_PRIVATE).edit();
                if(isChecked){
                      editor.putInt("autoConn",1);
                      editor.putBoolean("switchStatus",true);
                }else{
                    editor.putInt("autoConn",0);
                    editor.putBoolean("switchStatus",false);
                }
                editor.commit();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            this.finish();
        return true;
    }
}

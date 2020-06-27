package per.pccontrol;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

/**
 * Created by AlphaGo on 2017/12/22.
 * 主页菜单ListView的适配器
 */

public class DrawerAdapter extends ArrayAdapter<DrawerListItem> {

    private int resourceId;
    public DrawerAdapter(@NonNull Context context,  int textViewResourceId, @NonNull List<DrawerListItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId=textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        DrawerListItem item=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,null);
        ImageView img=view.findViewById(R.id.img_plain);
        TextView text=view.findViewById(R.id.text_plain);
        img.setImageResource(item.getimgId());
        text.setText(item.getName());
        return  view;
    }
}

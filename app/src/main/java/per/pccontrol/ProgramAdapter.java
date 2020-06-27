package per.pccontrol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import per.op.Program;

/**
 * Created by AlphaGo on 2017/12/29.
 */

public class ProgramAdapter extends ArrayAdapter<Program> {

    private int resourceId;
    public ProgramAdapter(@NonNull Context context, int resource, @NonNull List<Program> objects) {
        super(context, resource, objects);
        resourceId=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Program program=getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view=LayoutInflater.from(getContext()).inflate(resourceId,null);
            viewHolder=new ViewHolder();
            viewHolder.textView1=view.findViewById(android.R.id.text1);
            viewHolder.textView2=view.findViewById(android.R.id.text2);
            view.setTag(viewHolder);
        }else {
            view=convertView;
            viewHolder= (ViewHolder) view.getTag();
        }
        viewHolder.textView1.setText(program.getPROCESS_NAME());
        viewHolder.textView2.setText(program.getNAME());
        return view;
    }

    class ViewHolder{
        TextView textView1,textView2;
    }
}

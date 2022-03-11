package com.example.simpleui;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class RecordingListAdapter extends ArrayAdapter<Recording> {
    private static final String TAG = "RecordingListAdapter";
    private Context mContext;
    private int mResource;
    private static class ViewHolder {
        TextView date;
        TextView time;
        TextView avgHR;
    }
    public RecordingListAdapter(Context context, int resource, List<Recording> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;}


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String date = getItem(position).getDate();
        String time = getItem(position).getTime();
        String avgHR = getItem(position).getAvgHR();

        Recording recording = new Recording(date, time, avgHR);

        final View result;
        ViewHolder holder;


        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder= new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.textView1);
            holder.time = (TextView) convertView.findViewById(R.id.textView2);
            holder.avgHR = (TextView) convertView.findViewById(R.id.textView3);

            result = convertView;

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        holder.date.setText(recording.getDate());
        holder.time.setText(recording.getTime());
        holder.avgHR.setText(recording.getAvgHR());


        return result;
            }
        }

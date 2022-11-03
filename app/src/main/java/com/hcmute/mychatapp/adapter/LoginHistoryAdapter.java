package com.hcmute.mychatapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.hcmute.mychatapp.R;
import com.hcmute.mychatapp.model.LoginHistory;

public class LoginHistoryAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<LoginHistory> lstHistory;

    public LoginHistoryAdapter(Context context, int layout, List<LoginHistory> lstHistory) {
        this.context = context;
        this.layout = layout;
        this.lstHistory = lstHistory;
    }

    @Override
    public int getCount() {
        return lstHistory.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder{
        TextView textviewDeviceName, textviewLoginDate;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if(view == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout,null);
            //ánh xạ các view
            holder.textviewDeviceName = (TextView) view.findViewById(R.id.textviewDeviceName);
            holder.textviewLoginDate = (TextView) view.findViewById(R.id.textviewLoginDate);

            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }
        //Lấy thông tin lịch đăng nhập và đưa vào các view
        final LoginHistory loginHistory = lstHistory.get(position);
        holder.textviewDeviceName.setText(loginHistory.getDeviceName());
        holder.textviewLoginDate.setText(loginHistory.getDateLogin());
        return view;
    }
}

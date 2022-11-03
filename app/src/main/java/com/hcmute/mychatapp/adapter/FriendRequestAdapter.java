package com.hcmute.mychatapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.transition.Hold;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.R;
import com.hcmute.mychatapp.model.FriendRequest;
import com.hcmute.mychatapp.model.Friends;
import com.hcmute.mychatapp.model.User;

public class FriendRequestAdapter extends BaseAdapter {

    public FriendRequestAdapter(ArrayList<FriendRequest> arrFriendRequest, Context context, int layout) {
        this.arrFriendRequest = arrFriendRequest;
        this.context = context;
        this.layout = layout;
    }

    private ArrayList<FriendRequest> arrFriendRequest;
    private Context context;
    private int layout ;
    @Override
    public int getCount() {
        return arrFriendRequest.size();
    }

    @Override
    public Object getItem(int i) {
        return arrFriendRequest.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
    class Holder {
        //Chứa các view
        CircleImageView senderRequestImageView;
        TextView txtSenderRequestName,txtRequestDay;
        Button btnAccep,btnDeny;
    }
    User main_user;
    User sender_user;

    DatabaseReference myRef;
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if(view == null) {
            holder = new Holder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout,null);
            //ánh xạ các view
            holder.senderRequestImageView = (CircleImageView) view.findViewById(R.id.senderRequestImageView);
            holder.txtSenderRequestName = (TextView) view.findViewById(R.id.txtSenderRequestName);
            holder.txtRequestDay = (TextView)view.findViewById(R.id.txtRequestDay);
            holder.btnAccep = (Button)view.findViewById(R.id.btnAccep);
            holder.btnDeny = (Button) view.findViewById(R.id.btnDeny);
            view.setTag(holder);
        }
        else{
            holder = (Holder) view.getTag();
        }
        //Định dạng kiểu ngày
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-mm-yyyy");
        //Lấy thông tin người dùng từ mẫu singleton
        main_user = User_SingeTon.getInstance().getUser();
        //Lấy thông tin những người đã được gửi kết bạn
        FriendRequest friendRequest = arrFriendRequest.get(i);
        holder.txtSenderRequestName.setText(friendRequest.getSenderName());
        holder.txtRequestDay.setText(simpleDateFormat.format(friendRequest.getDateRequest()));
        //Tạo kết nối đến FirebaseStorage
        myRef = FirebaseDatabase.getInstance().getReference("users");
        myRef.child(friendRequest.getSenderPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sender_user = snapshot.getValue(User.class);
                //Lấy địa chỉ ảnh avatar
                StorageReference myStorage = FirebaseStorage.getInstance().getReference(sender_user.getAvatar());
                //Tạo sự kiện khi lấy hình ảnh thành công
                myStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Lấy được Uri thành công. Dùng picasso để đưa hình vào Circle View
                        Picasso.get().load(uri).into(holder.senderRequestImageView);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Bấm nút chấp nhận để kết bạn
        holder.btnAccep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef = FirebaseDatabase.getInstance().getReference("friends");
                //Tạo bạn bè cho người gửi
                Friends friend = new Friends(friendRequest.getSenderPhone(),friendRequest.getSenderName(),new Date());
                myRef.child(friendRequest.getReceiverPhone()).child(friendRequest.getSenderPhone()).setValue(friend);
                //Tạo bạn bè cho người nhận
                Friends friend1 = new Friends(friendRequest.getReceiverPhone(),friendRequest.getReceiverName(),new Date());
                myRef.child(friendRequest.getSenderPhone()).child(friendRequest.getReceiverPhone()).setValue(friend1);
                //Xóa bảng request
                myRef = FirebaseDatabase.getInstance().getReference("friend_requests");
                myRef.orderByChild("senderPhone").startAt(friendRequest.getSenderPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            if(dataSnapshot.getValue(FriendRequest.class).getReceiverPhone().equals(main_user.getPhone()))
                            {
                                String req_id = dataSnapshot.getKey();
                                myRef.child(req_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        arrFriendRequest.remove(i);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Accept request successfully ! ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        //Bấm nút từ chối để từ chối lời mời kết bạn
        holder.btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Tìm kiếm trong bảng lời mời kết bạn
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("friend_requests");
                myRef.orderByChild("senderPhone").startAt(friendRequest.getSenderPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            if(dataSnapshot.getValue(FriendRequest.class).getReceiverPhone().equals(main_user.getPhone()))
                            {
                                String req_id = dataSnapshot.getKey();
                                //Xóa lời mời trên database
                                myRef.child(req_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        arrFriendRequest.remove(i);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Deny Friend Request Successfully !", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        return view;
    }
}

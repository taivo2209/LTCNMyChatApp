package com.hcmute.mychatapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.R;
import com.hcmute.mychatapp.model.LoginHistory;
import com.hcmute.mychatapp.model.MessageDetails;
import com.hcmute.mychatapp.model.Participants;
import com.hcmute.mychatapp.model.User;

public class MessageListAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<Participants> lstParticipant;

    public MessageListAdapter(Context context, int layout,List<Participants> lstParticipant) {
        this.context = context;
        this.layout = layout;
        this.lstParticipant = lstParticipant;
    }

    @Override
    public int getCount() {
        return lstParticipant.size();
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
        //Chứa các view
        TextView nameBoxChat, lastMessage;
        ImageView imageBoxChat;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);
            //ánh xạ các view
            holder.imageBoxChat = (ImageView) convertView.findViewById(R.id.imageBoxChat);
            holder.nameBoxChat = (TextView) convertView.findViewById(R.id.nameBoxChat);
            holder.lastMessage = (TextView) convertView.findViewById(R.id.lastMessage);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }
        //Lấy thông tin người mình nhắn tin
        final Participants participants = lstParticipant.get(position);
        holder.imageBoxChat.setImageBitmap(null);
        //Kết nối đến bảng users
        DatabaseReference myUserRef = FirebaseDatabase.getInstance().getReference("users");
        myUserRef.child(participants.getUserPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                holder.nameBoxChat.setText(user.getFullname());
                //Kiểm tra nếu đã có ảnh mới thực hiện lấy ảnh đại diện
                if(!user.getAvatar().equals("")) {
                    //Đưa dữ liệu cho ảnh đại diện dùng Firebase Storage
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference(user.getAvatar());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Lấy được Uri thành công. Dùng picasso để đưa hình vào Circle View ảnh đại diện
                            Picasso.get().load(uri).fit().centerCrop().into(holder.imageBoxChat);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Thất bại thì sẽ in ra lỗi
                            Log.d("TAG", "onFailure: " + e.getMessage());
                        }
                    });
                }else
                {
                    holder.imageBoxChat.setImageResource(R.drawable.man_placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Lấy thông tin người dùng hiện tại từ mẫu singleton
        User currentUser = User_SingeTon.getInstance().getUser();
        //Tạo kết nối đến bảng tin nhắn chi tiết
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("message_details");
        myRef.child(participants.getMessageid()).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MessageDetails messageDetails = dataSnapshot.getValue(MessageDetails.class);
                    //Kiểm tra tin nhắn cuối cùng là voice chat
                    if(messageDetails.getContent().startsWith("message_records/")){
                        //Nếu người dùng là người tin nhắn cuối cùng
                        if(currentUser.getPhone().equals(messageDetails.getSenderPhone())) {
                            holder.lastMessage.setText("You sent a audio");
                        }//Nếu người dùng không là người tin nhắn cuối cùng
                        else{
                            holder.lastMessage.setText("Your friend sent a audio");
                        }
                    }//Nếu tin nhắn cuối cùng là hình ảnh
                    else if(messageDetails.getContent().startsWith("message_images/")){
                        //Nếu người dùng là người tin nhắn cuối cùng
                        if(currentUser.getPhone().equals(messageDetails.getSenderPhone())) {
                            holder.lastMessage.setText("You sent a photo");
                        }//Nếu người dùng không là người tin nhắn cuối cùng
                        else{
                            holder.lastMessage.setText("Your friend send a photo");
                        }
                    }//Nếu tin nhắn cuối cùng là dạng text
                    else {
                        holder.lastMessage.setText(messageDetails.getContent());
                    }
                    //Lấy ngày gửi cho vào tin nhắn cuối cùng
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");
                    holder.lastMessage.append(" "+simpleDateFormat.format(messageDetails.getTimeSended()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return convertView;
    }
}

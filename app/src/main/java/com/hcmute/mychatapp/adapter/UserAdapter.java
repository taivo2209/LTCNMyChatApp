package com.hcmute.mychatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.UUID;

import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.R;
import com.hcmute.mychatapp.ViewUserPageActivity;
import com.hcmute.mychatapp.model.FriendRequest;
import com.hcmute.mychatapp.model.User;

public class UserAdapter extends BaseAdapter {

    public UserAdapter(Context context, int layout, ArrayList<User> arrUser) {
        this.context = context;
        this.layout = layout;
        this.arrUser = arrUser;
    }

    private Context context;
    private int layout;
    private ArrayList<User> arrUser;
    @Override
    public int getCount() {
        return arrUser.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
    private class ViewHolder{
        //Giữ các view
        //Textview hiện số điện thoại
        TextView txtUserPhone;
        //Nút kết bạn
        ImageView btnAddFriend;
        //Nút hiện hình ảnh của tin nhắn
        ShapeableImageView imageBoxChat;
        ConstraintLayout constraintRowUser;
        //Nút xem trang cá nhân
        Button btnViewPage;
    }
    //Lưu thông tin người dùng
    User main_user;

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //Lấy thông tin người dùng hiện tại từ mẫu singleton
        main_user = User_SingeTon.getInstance().getUser();
        UserAdapter.ViewHolder holder;
        if(view == null) {
            holder = new UserAdapter.ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout,null);
            //ánh xạ các view
            holder.constraintRowUser = (ConstraintLayout) view.findViewById(R.id.constraintRowUser);
            holder.txtUserPhone = (TextView) view.findViewById(R.id.txtUserPhone);
            holder.btnAddFriend = (ImageView) view.findViewById(R.id.btnAddFriend);
            holder.imageBoxChat = (ShapeableImageView) view.findViewById(R.id.imageBoxChat);
            holder.btnViewPage = (Button) view.findViewById(R.id.btnViewPage);
            view.setTag(holder);
        }
        else{
            holder = (UserAdapter.ViewHolder) view.getTag();
        }
        //Lấy thông tin người dùng từ mảng
        User user = arrUser.get(i);
        holder.txtUserPhone.setText(user.getFullname());
        //Kiểm tra xem kết bạn chưa nếu chưa thì hiện nút kết bạn
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("friends").child(main_user.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Kiểm tra đã kết bạn thì không hiện nút kết bạn
                if(snapshot.child(user.getPhone()).exists())
                {
                    holder.btnAddFriend.setVisibility(View.INVISIBLE);
                }else
                {
                    //Kiểm tra có gửi lời mời kết bạn chưa
                    DatabaseReference myRef1 = FirebaseDatabase.getInstance().getReference("friend_requests");
                    myRef1.orderByChild("senderPhone").startAt(main_user.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                FriendRequest friendRequest = dataSnapshot.getValue(FriendRequest.class);
                                //Không thể gửi lời mời kết bạn đến chình mình
                                if(!friendRequest.getSenderPhone().equals(main_user.getPhone()))
                                    break;
                                else {
                                    if(friendRequest.getSenderPhone().equals(main_user.getPhone()) && friendRequest.getReceiverPhone().equals(user.getPhone()))
                                    {
                                        //Đã gửi rồi
                                        holder.btnAddFriend.setVisibility(View.INVISIBLE);
                                        break;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Tạo sự kiện khi bấm nút kết bạn. Gửi lời mời kết bạn
        holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Tạo lời mời kết bạn
                String req_id = UUID.randomUUID().toString();
                FriendRequest fq = new FriendRequest(main_user.getPhone(),main_user.getFullname(),user.getPhone(),user.getFullname(),"Become friend with me");
                //Thêm dữ liệu người dùng vào bảng kết bạn
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("friend_requests");
                myRef.child(req_id).setValue(fq);
                holder.btnAddFriend.setVisibility(View.INVISIBLE);
                Toast.makeText(context, "Send Request Successfully to " + user.getFullname(), Toast.LENGTH_SHORT).show();
            }
        });
        //Tạo sự kiện khi bấm vào nút xem trang cá nhân. Hiện trang cá nhân của người dùng dược chọn khi nhấn vào
        holder.btnViewPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Lấy id người cần xem từ sharedPreferences
                SharedPreferences sharedPreferences = context.getSharedPreferences("dataCookie",Context.MODE_MULTI_PROCESS);
                sharedPreferences.edit().putString("user_id", user.getPhone()).commit();
                //Chuyển sang activity xem trang các nhân
                context.startActivity(new Intent(context, ViewUserPageActivity.class));
            }
        });
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
        }

        return view;
    }
}

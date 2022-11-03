package com.hcmute.mychatapp;

import static android.Manifest.permission.CAMERA;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import com.hcmute.mychatapp.Pattern.UserImageBitmap_SingleTon;
import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountInformationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AccountInformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountInformationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountInformationFragment newInstance(String param1, String param2) {
        AccountInformationFragment fragment = new AccountInformationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    //Khai báo các view
    private View view;
    //background chứa ảnh bìa, btnBack là nút trở lại, btnEditInfor nhấn vào để hiện thêm chức năng
    private ImageView background,btnBack,btnEditInfor;
    //avatar chứa ảnh đại diện
    private CircleImageView avatar;
    //View hiển thị tên và mô tả bản thân
    private TextView txtfullname, txtdescription;
    //Biến dùng để xác nhận hàm xử lý sau khi chọn ảnh thành công
    private final int PICK_IMAGE_REQUEST = 22;
    //Uri của hình ảnh sau khi chọn từ danh mục hình ảnh
    private Uri filePath;
    //Biến của Firebase. Để viết và đọc dữ liệu từ Firebase Storage
    FirebaseStorage storage;
    StorageReference storageReference;
    //Biến lưu thông tin người dùng
    User user;
    User_SingeTon user_singeTon;
    private int type;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_account_information, container, false);
        //Ánh xạ các view
        background = (ImageView) view.findViewById(R.id.background);
        avatar = (CircleImageView) view.findViewById(R.id.avatar);
        btnEditInfor = (ImageView) view.findViewById(R.id.btnEditInfor);
        txtfullname = view.findViewById(R.id.txtFullName);
        txtdescription = view.findViewById(R.id.txtDescription);
        //Tạo sự kiện onclick cho nút sửa thông tin (nút 3 chấm). Nhấn vào để hiện thêm chức năng thay đổi
        btnEditInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),AdjustInforActivity.class));
            }
        });
        //Tạo sự kiên click cho ảnh bìa. Nhấn vào show lên dialog để thay đổi ảnh bìa
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_anhbia);
                dialog.show();
                LinearLayout linearChooseBackgroundImage = dialog.findViewById(R.id.linearChooseBackgroundImage);
                linearChooseBackgroundImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Type = 0 la anh bia
                        SelectImage(0);
                    }
                });
                LinearLayout linearTakeNewImageBackground = dialog.findViewById(R.id.linearTakeNewImageBackground);
                linearTakeNewImageBackground.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Kiểm tra quyền sử dụng máy ảnh
                        if(CheckPermissions()) {
                            //Gắn loại ảnh là ảnh bìa
                            type = 0;
                            //Nếu được quyền thì chạy Activity chụp ảnh
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }else {
                            RequestPermissions();
                        }
                    }
                });
                //Trong dialog ảnh đại diện nhấn vào xem ảnh dể xem ảnh
                LinearLayout linearViewBackground = dialog.findViewById(R.id.linearViewBackground);
                linearViewBackground.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        //Mở dialog để có thể xem full hình ảnh
                        final Dialog viewPictureDialog = new Dialog(getActivity());
                        viewPictureDialog.setContentView(R.layout.dialog_zoom);
                        viewPictureDialog.show();
                        //lấy ảnh bìa lên view
                        ImageView mainpicture = viewPictureDialog.findViewById(R.id.mainpicture);
                        mainpicture.setImageBitmap(UserImageBitmap_SingleTon.getInstance().getAnhbia());
                    }
                });
            }
        });
        //Tạo sự kiên click cho ảnh đại diện. Nhấn vào show lên dialog để thay đổi ảnh đại diện
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_anhdaidien);
                dialog.show();
                //Trong dialog ảnh đại diện nhấn vào dòng chọn ảnh trong điện thoại sẽ hiện lên hình ảnh trong điện thoại cho người dùng chọn
                LinearLayout linearChooseImageAvatar = dialog.findViewById(R.id.linearChooseImageAvatar);
                linearChooseImageAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Hàm dùng để hiện lên hình ảnh
                        //Type = 1 la anh anh dai dien
                        SelectImage(1);
                    }
                });
                //Trong dialog ảnh đại diện nhấn vào dòng chụp ảnh sẽ cho phép người dùng chụp ảnh
                LinearLayout linearTakeNewImageAvatar = dialog.findViewById(R.id.linearTakeNewImageAvatar);
                linearTakeNewImageAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Kiểm tra quyền sử dụng máy ảnh
                        if(CheckPermissions()) {
                            //Gắn loại ảnh là ảnh đại diện
                            type = 1;
                            //Nếu được quyền thì chạy Activity chụp ảnh
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }else {
                            RequestPermissions();
                        }
                    }
                });
                //Trong dialog ảnh đại diện nhấn vào xem ảnh dể xem ảnh
                LinearLayout linearViewAvatar = dialog.findViewById(R.id.linearViewAvatar);
                linearViewAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        //Mở dialog để có thể xem full hình ảnh
                        final Dialog viewPictureDialog = new Dialog(getActivity());
                        viewPictureDialog.setContentView(R.layout.dialog_zoom);
                        viewPictureDialog.show();
                        //lấy ảnh đại diện lên view
                        ImageView mainpicture = viewPictureDialog.findViewById(R.id.mainpicture);
                        mainpicture.setImageBitmap(UserImageBitmap_SingleTon.getInstance().getAnhdaidien());
                    }
                });
            }
        });
        //Bấm nút back để quay lại
        btnBack = (ImageView) view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoreFragment moreFragment = new MoreFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, moreFragment).commit();
            }
        });

        //Đưa dữ liệu vào các view
        putDataToView();

        return view;
    }
    //Hàm này dùng để đưa thông tin người dùng vào các view để hiện thị
    void putDataToView(){
        //Dùng mẫu thiết kế singleTon để lưu lại user sau khi login
        user_singeTon = User_SingeTon.getInstance();

        user = user_singeTon.getUser();

        //Nếu không có user trả về trang login
        if(user == null)
        {
            startActivity(new Intent(getActivity(), loginActivity.class));
            getActivity().finish();
        }
        //Có user và bắt đầu đưa dữ liệu cho các view
        txtdescription.setText(user.getDescription());
        txtfullname.setText(user.getFullname());
        // Lấy ảnh bìa , ảnh đại diện
        //Gọi lớp singleTon
        UserImageBitmap_SingleTon userImageBitmap_singleTon = UserImageBitmap_SingleTon.getInstance();
        //Nếu chưa có ảnh thì dùng local storage tải lên - Tốn thời gian xử lý
        if(userImageBitmap_singleTon.getAnhbia() == null || userImageBitmap_singleTon.getAnhdaidien() == null)
        {
            //Kiểm tra nếu đã có ảnh mới thực hiện lấy ảnh đại diện
            if(!user.getAvatar().equals("")) {
                //Đưa dữ liệu cho ảnh đại diện dùng Firebase Storage
                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference(user.getAvatar());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Lấy được Uri thành công. Dùng picasso để đưa hình vào Circle View ảnh đại diện
                        Picasso.get().load(uri).fit().centerCrop().into(avatar);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Thất bại thì sẽ in ra lỗi
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //Kiểm tra nếu đã có ảnh mới thực hiện lấy ảnh bìa
            if(!user.getBackground().equals("")) {
                //Đưa dữ liệu cho ảnh bìa dùng Firebase Storage
                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference(user.getBackground());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Lấy được Uri thành công. Dùng picasso để đưa hình vào Circle View ảnh đại diện
                        Picasso.get().load(uri).fit().centerCrop().into(background);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Thất bại thì sẽ in ra lỗi
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else
        {
            //Nếu đã có ảnh thì set vào luôn
            avatar.setImageBitmap(userImageBitmap_singleTon.getAnhdaidien());
            background.setImageBitmap(userImageBitmap_singleTon.getAnhbia());
        }
    }
    //Khi trở lại Activity cập nhật lại cho các view
    @Override
    public void onResume() {
        super.onResume();
        putDataToView();
    }

    // Hàm để chọn hình ảnh
    private void SelectImage(int type)
    {
        this.type = type;
        // Xác định mở thư mục ảnh
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select image..."), PICK_IMAGE_REQUEST);
    }
    //Sau khi chọn ảnh, chụp ảnh xong chạy vào hàm này
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Chọn ảnh xong chạy vào điều kiện này
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == getActivity().RESULT_OK
                && data != null
                && data.getData() != null) {

            // Lấy được uri của ảnh
            filePath = data.getData();
            try {
                //Đưa ảnh lên Firebase Storage
                uploadImage();
                // Chuyển thành bitmap và đưa vào ảnh đại diện
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),filePath);
                UserImageBitmap_SingleTon userImageBitmap_singleTon = UserImageBitmap_SingleTon.getInstance();
                //Type 1 là ảnh đại diện, 0 là ảnh bìa
                if(this.type == 1) {
                    userImageBitmap_singleTon.setAnhdaidien(bitmap);
                    avatar.setImageBitmap(bitmap);
                }
                else {
                    userImageBitmap_singleTon.setAnhbia(bitmap);
                    background.setImageBitmap(bitmap);
                }
            }
            catch (IOException e) {
                // In ra lỗi
                e.printStackTrace();
            }
        }
        else if(requestCode == 0
                && resultCode == getActivity().RESULT_OK
                && data != null)
        {
            //Sau khi chụp ảnh bằng camera xong chạy vào block này
            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG,100,stream);
            byte[] byteArray = stream.toByteArray();
            //Từ camera
            Date today = new Date();
            String pic_id = Long.toString(today.getTime());
            // Hiện ProgressDialog trong khi đang tải lên
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");

            progressDialog.show();
            //Khai báo FirebaseStorage
            FirebaseStorage storage;
            StorageReference storageReference;

            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            // Đi vào nhánh con
            StorageReference ref;
            if(type == 1){
                ref = storageReference.child("images/" + user.getPhone() + "_avatar");
            }else
                ref = storageReference.child("images/" + user.getPhone() + "_background");
            // Tạo sự kiên cho việc upload file cả khi thành công hay thất bại và hiện thanh progress theo %
            ref.putBytes(byteArray)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    // Tải ảnh lên thành công
                                    // Tắt dialog progress đi
                                    progressDialog.dismiss();
                                    //Cập nhật lại cho bảng user về địa chỉ của avatar
                                    //Cập nhật lại cho cả user_singleTon
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users");
                                    if(type == 1) {
                                        user.setAvatar("images/" + user.getPhone() + "_avatar");
                                        myRef.child(user.getPhone()).setValue(user);
                                    }else {
                                        user.setBackground("images/" + user.getPhone() + "_background");
                                        myRef.child(user.getPhone()).setValue(user);
                                    }
                                    user_singeTon.setUser(user);
                                    Toast.makeText(getActivity(), "Update successful!!", Toast.LENGTH_SHORT).show();
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            // Lỗi, không tải lên thành công
                            // Tắt progress đi và in ra lỗi
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Update failed. Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        // Sự kiện cho Progress
                        // Hiển thị % hoàn thành
                        @Override
                        public void onProgress(
                                UploadTask.TaskSnapshot taskSnapshot)
                        {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Downloaded " + (int)progress + "%");
                        }
                    });
            // Chuyển thành bitmap và đưa vào ảnh đại diện
            //byte -> bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
            UserImageBitmap_SingleTon userImageBitmap_singleTon = UserImageBitmap_SingleTon.getInstance();

            if(this.type == 1) {
                userImageBitmap_singleTon.setAnhdaidien(bitmap);
            }
            else {
                userImageBitmap_singleTon.setAnhbia(bitmap);
            }
        }
    }
    //Hàm này dùng để đưa ảnh lên trên firebase storage
    private void uploadImage()
    {
        //Kiểm tra đường dẫn file
        if (filePath != null) {

            // Hiện ProgressDialog trong khi đang tải lên
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");

            progressDialog.show();
            //Khai báo FirebaseStorage
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            // Đi vào nhánh con
            StorageReference ref;
            if(type == 1){
                ref = storageReference.child("images/" + user.getPhone() + "_avatar");
            }else
                ref = storageReference.child("images/" + user.getPhone() + "_background");
            // Tạo sự kiên cho việc upload file cả khi thành công hay thất bại và hiện thanh progress theo %
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    // Tải ảnh lên thành công
                                    // Tắt dialog progress đi
                                    progressDialog.dismiss();
                                    //Cập nhật lại cho bảng user về địa chỉ của avatar
                                    //Cập nhật lại cho cả user_singleTon
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users");
                                    if(type == 1) {
                                        user.setAvatar("images/" + user.getPhone() + "_avatar");
                                        myRef.child(user.getPhone()).setValue(user);
                                    }else {
                                        user.setBackground("images/" + user.getPhone() + "_background");
                                        myRef.child(user.getPhone()).setValue(user);
                                    }
                                    user_singeTon.setUser(user);
                                    Toast.makeText(getActivity(), "Update successful!!", Toast.LENGTH_SHORT).show();
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            // Lỗi, không tải lên thành công
                            // Tắt progress đi và in ra lỗi
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Update failed. Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        // Sự kiện cho Progress
                        // Hiển thị % hoàn thành
                        @Override
                        public void onProgress(
                                UploadTask.TaskSnapshot taskSnapshot)
                        {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Downloaded " + (int)progress + "%");
                        }
                    });
        }
    }
    //Biến dùng để chia trường hợp xin permission từ hệ thống
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    //Hàm kiểm tra quyền của ứng dụng
    public boolean CheckPermissions() {
        // Kiểm tra các quyền có được cấp chưa
        //Quyền dùng máy ảnh
        int result = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    //Hàm dùng để xin quyền sử dụng camera
    private void RequestPermissions() {
        //Xin cấp quyền từ hệ thống
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    //Sau khi người dùng xác nhận hay từ chối cấp quyền sẽ chạy hàm này
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Sau khi ta chọn chấp nhận hay từ chối sẽ trả kết quả vào hàm này
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                //Nếu được chấp nhận hết
                if (grantResults.length > 0) {
                    boolean permissionToCamera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToCamera) {
                        //Thông báo thành công
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        //Thông báo từ chối
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
}
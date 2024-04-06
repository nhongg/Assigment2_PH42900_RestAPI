package com.example.asm_ph42900;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.asm_ph42900.Adapter.SinhVienAdapter;
import com.example.asm_ph42900.Api.ApiService;
import com.example.asm_ph42900.Modal.SinhVien;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Home extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 10;
    List<SinhVien> list = new ArrayList<>();

    FloatingActionButton fltadd;
    ImageView imagePiker;
    private  Uri mUri;

    public RecyclerView rcvSV ;
    public SinhVienAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fltadd = findViewById(R.id.fltadd);
        rcvSV = findViewById(R.id.rcvSV);

        loadData();

        fltadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(Home.this,new SinhVien(),1,list);
            }
        });
    }


    public void showDialog (Context context, SinhVien sinhVien, Integer type, List<SinhVien> list){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        LayoutInflater inflater= ((Activity) context).getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_add_sinhvien,null);
        builder.setView(view);
        Dialog dialog=builder.create();
        dialog.show();

        EditText edtMaSV = view.findViewById(R.id.edtMaSV);
        EditText edtNameSV = view.findViewById(R.id.edtNameSV);
        EditText edtDiemTB = view.findViewById(R.id.edtDiemTB);
        EditText edtAvatar = view.findViewById(R.id.edtAvatar);
        imagePiker = view.findViewById(R.id.imgAvatarSV);
        Button btnChonAnh =view.findViewById(R.id.btnChonAnh);
        Button btnSave =view.findViewById(R.id.btnSave);

        if (type == 0){
            edtMaSV.setText(sinhVien.getMasv());
            edtNameSV.setText(sinhVien.getName());
            edtDiemTB.setText(sinhVien.getPoint()+"");
            edtAvatar.setText(sinhVien.getAvatar());
            Glide.with(view).load(sinhVien.getAvatar()).into(imagePiker);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String masv = edtMaSV.getText().toString().trim();
                String name = edtNameSV.getText().toString().trim();
                String diemTB = edtDiemTB.getText().toString();
                String avatar = edtAvatar.getText().toString().trim();
                if (masv.isEmpty() || name.isEmpty()|| diemTB.isEmpty()){
                    Toast.makeText(context, "Không được bỏ trống", Toast.LENGTH_SHORT).show();
                } else if (!isDouble(diemTB)) {
                    Toast.makeText(context, "Điểm trung bình phải là số", Toast.LENGTH_SHORT).show();
                } else {
                    Double point = Double.parseDouble(diemTB);
                    if (point < 0 || point > 10){
                        Toast.makeText(context, "Điểm phải từ 0-10", Toast.LENGTH_SHORT).show();
                    }else {
                        SinhVien sv = new SinhVien(masv,name,point,avatar);

                        Call<SinhVien> call = ApiService.apiService.addStudent(sv);

                        if (type == 0){
                            call = ApiService.apiService.updateStudent(sinhVien.get_id(), sv);
                        }

                        call.enqueue(new Callback<SinhVien>() {
                            @Override
                            public void onResponse(Call<SinhVien> call, Response<SinhVien> response) {
                                if (response.isSuccessful()){
                                    String msg = "Add success";
                                    if (type == 0){
                                        msg = "Update success";
                                    }
                                    loadData();
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onFailure(Call<SinhVien> call, Throwable t) {
                                String msg = "Add fail";
                                if (type == 0){
                                    msg = "update fail";
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }
        });

        if (type == 1){
            btnChonAnh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestPermission();
                }
            });
        }
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void loadData (){

        Call<List<SinhVien>> call = ApiService.apiService.getData();

        call.enqueue(new Callback<List<SinhVien>>() {
            @Override
            public void onResponse(Call<List<SinhVien>> call, Response<List<SinhVien>> response) {
                if (response.isSuccessful()){
                    list = response.body();
                    adapter = new SinhVienAdapter(Home.this, list);
                    rcvSV.setLayoutManager(new LinearLayoutManager(Home.this));
                    rcvSV.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<SinhVien>> call, Throwable t) {
//                Toast.makeText(Home.this, "Call API Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void requestPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                openImagePicker();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(Home.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                TedPermission.create()
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("\n" +
                                "Nếu bạn từ chối quyền, bạn không thể sử dụng dịch vụ này\n\nVui lòng bật quyền tại [Cài đặt] > [Quyền]")
                        .setPermissions(Manifest.permission.READ_MEDIA_IMAGES)
                        .check();
            } else {
                TedPermission.create()
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("\n" +
                                "Nếu bạn từ chối quyền, bạn không thể sử dụng dịch vụ này\n\nVui lòng bật quyền tại [Cài đặt] > [Quyền]")
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();
            }
        }
    }

    private ActivityResultLauncher<Intent> mActivityRequestLauncher =registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK){
                        Intent data = o.getData();
                        if (data == null){
                            return;
                        }

                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                            imagePiker.setImageBitmap(bitmap);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            });

    public void openImagePicker(){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            mActivityRequestLauncher.launch(intent);
    }
}
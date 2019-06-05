package com.netease.image.compress;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.netease.image.compress.utils.UriParseUtils;
import com.netease.image.library.CompressImageManager;

import java.io.File;
import java.util.ArrayList;

import com.netease.image.library.bean.Photo;
import com.netease.image.library.config.CompressConfig;
import com.netease.image.library.listener.CompressImage;
import com.netease.image.library.utils.CachePathUtils;
import com.netease.image.library.utils.CommonUtils;
import com.netease.image.library.utils.Constants;

public class MainActivity extends AppCompatActivity implements CompressImage.CompressListener {

    private CompressConfig compressConfig; // 压缩配置
    private ProgressDialog dialog; // 压缩加载框
    private String cameraCachePath; // 拍照源文件路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 运行时权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(perms[1]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 200);
            }
        }

        compressConfig = CompressConfig.builder()
                .setUnCompressMinPixel(1000) // 最小像素不压缩，默认值：1000
                .setUnCompressNormalPixel(2000) // 标准像素不压缩，默认值：2000
                .setMaxPixel(1000) // 长或宽不超过的最大像素 (单位px)，默认值：1200
                .setMaxSize(100 * 1024) // 压缩到的最大大小 (单位B)，默认值：200 * 1024 = 200KB
                .enablePixelCompress(true) // 是否启用像素压缩，默认值：true
                .enableQualityCompress(true) // 是否启用质量压缩，默认值：true
                .enableReserveRaw(true) // 是否保留源文件，默认值：true
                .setCacheDir("") // 压缩后缓存图片路径，默认值：Constants.COMPRESS_CACHE
                .setShowCompressDialog(true) // 是否显示压缩进度条，默认值：false
                .create();
//        compressConfig = CompressConfig.getDefaultConfig();
        compressMore();
    }

    private void compressMore() {
        // 测试多张图片同时压缩
        ArrayList<Photo> photos = new ArrayList<>();
        photos.add(new Photo("/storage/emulated/0/DCIM/Camera/IMG_20171108_151541.jpg"));
        photos.add(new Photo("/storage/emulated/0/DCIM/Camera/IMG_20171011_095724.jpg"));
        photos.add(new Photo("/storage/emulated/0/DCIM/Camera/IMG_20171011_092207.jpg"));
        photos.add(new Photo("/storage/emulated/0/DCIM/Camera/IMG_20170608_113509.jpg"));
        photos.add(new Photo("/storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1535449679877.jpg"));
        photos.add(new Photo("/storage/emulated/0/autoLite/cameraImg/1535016551150.jpg"));
        photos.add(new Photo("/storage/emulated/0/Download/微信图片_20171205095927.jpg"));
        photos.add(new Photo("/storage/emulated/0/Pictures/camera_20181115_111332.jpg"));
        photos.add(new Photo("/storage/emulated/0/Pictures/camera_20180706_173207.jpg"));
        compress(photos);
    }

    // 点击拍照
    public void camera(View view) {
        // FileProvider
        Uri outputUri;
        File file = CachePathUtils.getCameraCacheFile();
        ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            outputUri = UriParseUtils.getCameraOutPutUri(this, file);
        } else {
            outputUri = Uri.fromFile(file);
        }
        cameraCachePath = file.getAbsolutePath();
        // 启动拍照
        CommonUtils.hasCamera(this, CommonUtils.getCameraIntent(outputUri), Constants.CAMERA_CODE);
    }

    // 点击相册
    public void album(View view) {
        CommonUtils.openAlbum(this, Constants.ALBUM_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拍照返回
        if (requestCode == Constants.CAMERA_CODE && resultCode == RESULT_OK) {
            // 压缩（集合？单张）
            preCompress(cameraCachePath);
        }

        // 相册返回
        if (requestCode == Constants.ALBUM_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String path = UriParseUtils.getPath(this, uri);
                // 压缩（集合？单张）
                preCompress(path);
            }
        }
    }

    // 准备压缩，封装图片集合
    private void preCompress(String photoPath) {
        ArrayList<Photo> photos = new ArrayList<>();
        photos.add(new Photo(photoPath));
        if (!photos.isEmpty()) compress(photos);
    }

    // 开始压缩
    private void compress(ArrayList<Photo> photos) {
        if (compressConfig.isShowCompressDialog()) {
            Log.e("netease >>> ", "开启了加载框");
            dialog = CommonUtils.showProgressDialog(this, "压缩中……");
        }
        CompressImageManager.build(this, compressConfig, photos, this).compress();
    }

    @Override
    public void onCompressSuccess(ArrayList<Photo> arrayList) {
        Log.e("netease >>> ", "压缩成功");
        if (dialog != null && !isFinishing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onCompressFailed(ArrayList<Photo> arrayList, String error) {
        Log.e("netease >>> ", error);
        if (dialog != null && !isFinishing()) {
            dialog.dismiss();
        }
    }
}

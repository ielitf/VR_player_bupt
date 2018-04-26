package com.martin.ads.pano360demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Wyf on 2016/9/19.
 */
public class ImageUtils {
    public static final String FOLDER_NAME = Environment
            .getExternalStorageDirectory().getAbsoluteFile()
            + File.separator
            + "cstvImg";
    /**
     * 由Activity跳转到图片集
     *
     * @param context
     * @param requestCode
     */
    public static void pickImage(Activity context, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        //此处调用了图片选择器
        //如果直接写intent.setDataAndType("image/*");
        //调用的是系统图库
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 由Activity跳到相机
     *
     * @param context
     * @param requestCode
     * @return
     */
    public static String tackImage(Activity context, int requestCode) {
        isHaveImgFolder();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");// 获取当前时间，进�?步转化为字符�?
        Date date = new Date();
        String str = format.format(date);
        String fileName = FOLDER_NAME + File.separator + str + ".jpg";
        File lFile = new File(fileName);
        // 调用系统相机，进行拍照的Action
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 设置相机拍照之后的图片保存的位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(lFile));
        // 启动相机
        context.startActivityForResult(intent, requestCode);
        return fileName;
    }

    // 获取相册图片
    public static String getRealPathFromURI(Context context, Uri mUri) {
        if (mUri.getScheme().equals("file")) {
            return mUri.getPath();
        } else {
            String imgPath;
            Cursor cursor = context.getContentResolver().query(mUri, null, null, null,
                    null);
            cursor.moveToFirst();
            imgPath = cursor.getString(1);
            return imgPath;
        }
    }

    /**
     * 此方法用来Activity跳到图片修剪，保存到指定路径
     *
     * @param activity
     * @param uri
     */
    public static String startPhotoZoomUrl(final Activity activity, Uri uri,
                                           int requestCode) {
        isHaveImgFolder();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("return-data", false);
        // aspectX , aspectY :宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX , outputY : 裁剪图片宽高
        intent.putExtra("outputX", 120);
        intent.putExtra("outputY", 120);
        if (!isSDCARDMounted()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");// 获取当前时间
        Date date = new Date();
        String str = format.format(date);
        String fileName = FOLDER_NAME + File.separator + str + ".jpg";
        File file = new File(fileName);
        Uri fromFile = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fromFile);
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (android.content.ActivityNotFoundException ex) {
            Log.d("=================", "startPhotoZoomUrl: 异常");
        }
        return fileName;
    }

    private static boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    /**
     * 判断有没有存储图片的文件夹
     */
    public static void isHaveImgFolder() {
        File folderFile = new File(FOLDER_NAME);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
    }
}

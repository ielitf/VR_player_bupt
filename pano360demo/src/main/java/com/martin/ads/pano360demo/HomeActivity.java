package com.martin.ads.pano360demo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.rubensousa.viewpagercards.CardItem;
import com.github.rubensousa.viewpagercards.CardPagerAdapter;
import com.github.rubensousa.viewpagercards.ShadowTransformer;
import com.martin.ads.vrlib.constant.MimeType;
import com.martin.ads.vrlib.ui.Pano360ConfigBundle;
import com.martin.ads.vrlib.ui.PanoPlayerActivity;
import com.martin.ads.vrlib.ext.GirlFriendNotFoundException;
import com.martin.ads.vrlib.utils.BitmapUtils;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.IOException;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private Context context;
    private boolean USE_DEFAULT_ACTIVITY = true;
    private ViewPager mViewPager;

    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;

    private CheckBox planeMode;
    private boolean flag;

    private String filePath = "~(～￣▽￣)～";
    private String videoHotspotPath;
    private boolean planeModeEnabled;

    private int mimeType;

    private static final int CODE_GALLERY_REQUEST = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = HomeActivity.this;
        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        mCardAdapter = new CardPagerAdapter();
        mCardAdapter.addCardItem(new CardItem(R.string.title_1, R.string.content_text_1));
        mCardAdapter.addCardItem(new CardItem(R.string.title_2, R.string.content_text_2));
        mCardAdapter.addCardItem(new CardItem(R.string.title_3, R.string.content_text_3));
        mCardAdapter.addCardItem(new CardItem(R.string.title_4, R.string.content_text_4));
        mCardAdapter.addCardItem(new CardItem(R.string.title_5, R.string.content_text_5));
        mCardAdapter.addCardItem(new CardItem(R.string.title_6, R.string.content_text_6));

        planeMode = (CheckBox) findViewById(R.id.plane_mode);

        mCardAdapter.setOnClickCallback(new CardPagerAdapter.OnClickCallback() {
            @Override
            public void onClick(int position) {
                videoHotspotPath = null;
                switch (position) {
                    case 0://播放样例视频（集成在app里面的视频）
                        //filePath= "gz256.mp4";
                        //mimeType= MimeType.ASSETS | MimeType.VIDEO;
                        filePath = "android.resource://" + getPackageName() + "/" + R.raw.demo_video;
                        mimeType = MimeType.RAW | MimeType.VIDEO;
                        break;
                    case 1://播放本地视频
                        if (Build.VERSION.SDK_INT >= 23 && (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                        } else {
                            Intent intent = new Intent(HomeActivity.this, FilePickerActivity.class);
                            intent.putExtra(FilePickerActivity.ARG_FILTER, Pattern.compile("(.*\\.mp4$)||(.*\\.avi$)||(.*\\.wmv$)"));
                            startActivityForResult(intent, 1);
                        }
                        return;
                    case 2:
                        filePath = "images/vr_cinema.jpg";
                        videoHotspotPath = "android.resource://" + getPackageName() + "/" + R.raw.demo_video;
                        mimeType = MimeType.ASSETS | MimeType.PICTURE;
                        break;
                    case 3://播放样例全景图片---本地相册
                        //filePath= "android.resource://" + getPackageName() + "/" + R.raw.vr_cinema;
                        //mimeType= MimeType.RAW | MimeType.PICTURE;

                        //mimeType= MimeType.BITMAP | MimeType.PICTURE;

                        //选择资源文件夹下的样例图片
//                        filePath="images/texture_360_n.jpg";
//                        filePath="/storage/emulated/0/MagazineUnlock/magazine-unlock-04-2.3.949-_d0775afb1df44abc9a5bf8676c657a6a.jpg";
//                        filePath="http://192.168.9.242/1.jpg ";
//                        mimeType= MimeType.ONLINE| MimeType.PICTURE;
                        if (Build.VERSION.SDK_INT >= 23 && (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                        } else{
                            choseHeadImageFromGallery();
                        }
                        return;
                    case 4://播放在线视频
//                        filePath="http://cache.utovr.com/201508270528174780.m3u8";
                        filePath = "http://192.168.9.242/demo.mp4 \n";
                        mimeType = MimeType.ONLINE | MimeType.VIDEO;
                        break;
                    case 5:
                        if (flag) throw new GirlFriendNotFoundException();
                        else {
                            Toast.makeText(HomeActivity.this, "再点会点坏的哦~", Toast.LENGTH_LONG).show();
                            flag = true;
                        }
                        return;
                }
                planeModeEnabled = planeMode.isChecked();
                start();
            }
        });
        mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);

        mViewPager.setAdapter(mCardAdapter);
        mViewPager.setPageTransformer(false, mCardShadowTransformer);

        mViewPager.setOffscreenPageLimit(3);

        mCardShadowTransformer.enableScaling(true);
    }

    /**
     * 从本地相册选取图片
     */

    private void choseHeadImageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK, null);
        // 此处调用了图片选择器
        // 如果直接写intent.setDataAndType("image/*");
        // 调用的是系统图库
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CODE_GALLERY_REQUEST);
    }

    private void start() {
        Pano360ConfigBundle configBundle = Pano360ConfigBundle
                .newInstance()
                .setFilePath(filePath)
                .setMimeType(mimeType)
                .setPlaneModeEnabled(planeModeEnabled)
                .setRemoveHotspot(true)//去除中间那个“智障科技图片的”
                .setVideoHotspotPath(videoHotspotPath);

        if ((mimeType & MimeType.BITMAP) != 0) {
            //add your own picture here
            // this interface may be removed in future version.
            configBundle.startEmbeddedActivityWithSpecifiedBitmap(
                    this, BitmapUtils.loadBitmapFromRaw(this, R.mipmap.ic_launcher));
            return;
        }

        if (USE_DEFAULT_ACTIVITY)
            configBundle.startEmbeddedActivity(this);
        else {
            Intent intent = new Intent(this, DemoWithGLSurfaceView.class);
            intent.putExtra(PanoPlayerActivity.CONFIG_BUNDLE, configBundle);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            mimeType = MimeType.LOCAL_FILE | MimeType.VIDEO;
            planeModeEnabled = planeMode.isChecked();
            start();
        }
        if (requestCode == CODE_GALLERY_REQUEST) {
            Uri uri = data.getData();
                filePath = ImageUtils.getRealPathFromURI(HomeActivity.this, uri);
            Log.e("===========","filePath:"+filePath);
            mimeType = MimeType.LOCAL_FILE | MimeType.PICTURE;
            planeModeEnabled = planeMode.isChecked();
            start();
        }
    }

}

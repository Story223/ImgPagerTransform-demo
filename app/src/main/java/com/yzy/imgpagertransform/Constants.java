package com.yzy.imgpagertransform;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2016/9/23.
 */
public class Constants {
    //拍照原图路径
    public static String MyAvatarDir = Environment.getExternalStorageDirectory().toString() + File.separator + "camera";
    //截图后图片保存路径
    public static String CropDir = Environment.getExternalStorageDirectory().toString() + File.separator + "savePic";

    //图片路径数组Extras key
    public static String EXTRAS_PICLIST = "PicUrlList";

    //管理好后图片路径数组Extras key
    public static String EXTRAS_NEW_PICLIST = "newPicUrlList";

    //管理照片排序回调
    public static final int REQUESTCODE_MANAGE_IMAGE = 4;//管理照片

    //SharedPreferences name
    public static final String APK_ENABLE_ARRAT = "SharedPre_convenientbanner";

    /**
     * 拍照回调
     */
    public static final int REQUESTCODE_UPLOADAVATAR_CAMERA = 1;//拍照修改头像
    public static final int REQUESTCODE_UPLOADAVATAR_LOCATION = 2;//本地相册修改头像
    public static final int REQUESTCODE_UPLOADAVATAR_CROP = 3;//系统裁剪头像

    public static final int REQUESTCODE_TAKE_CAMERA = 0x000001;//拍照
    public static final int REQUESTCODE_TAKE_LOCAL = 0x000002;//本地图片
    public static final int REQUESTCODE_TAKE_LOCATION = 0x000003;//位置
}

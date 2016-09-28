package com.yzy.imgpagertransform;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.yzy.imgpagertransform.convenientbanner.ConvenientBanner;
import com.yzy.imgpagertransform.convenientbanner.holder.CBViewHolderCreator;
import com.yzy.imgpagertransform.convenientbanner.listener.OnItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, ViewPager.OnPageChangeListener, OnItemClickListener {
    private static final String TAG = "MainActivity";
    private ConvenientBanner convenientBanner;//图片播放器控件
    private ArrayList<Integer> localImages = new ArrayList<Integer>();//初始化图片时所用

    private ArrayList<String> transformerList = new ArrayList<String>();//跳转效果

    private ArrayList<String> picNamelist = new ArrayList<String>();  //保存的图片名数组
    private ArrayList<String> picUrllist = new ArrayList<String>();   //图片轮播器显示图片的地址
    private ArrayList<String> newPicUrlList = new ArrayList<String>();   //使用图片管理器后新的图片轮播器显示图片

    private String filePath;

    private int width;
    private int height;
    private ArrayList<SoftReference<Bitmap>> bitmapLists = new ArrayList<SoftReference<Bitmap>>(); //本地图片的bitmap软引用

    //所需的权限
    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
    };

    private static final int REQUEST_CODE = 0; // 请求码

    private ImageView iv;   //图片管理器跳转标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //判断手机系统版本，如果6.0以上则动态获取权限
        int checkWriteSDPermission = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int checkReadSDPermission = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE);
        if(checkWriteSDPermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},123);
        }
        if(checkReadSDPermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},111);
        }

        initViews();
        initDatas();
        init();
    }
    //初始化数据
    private void initDatas(){
        filePath="";
        //获取sharedPreference图片路径
        picUrllist = getApkEnableArray(MainActivity.this);
        Log.e(TAG,"------- getApkEnableArray() == "+getApkEnableArray(MainActivity.this).toString());
    }

    private void initViews() {
        convenientBanner = (ConvenientBanner) findViewById(R.id.convenientBanner);


        //方法一
        ViewTreeObserver vto2 = convenientBanner.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                convenientBanner.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Log.e(TAG,"---- initView() 方法一 width == "+convenientBanner.getWidth()+"   height == "+convenientBanner.getHeight());
            }
        });
        //方法二
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        convenientBanner.measure(w, h);
        height =convenientBanner.getMeasuredHeight();
        width =convenientBanner.getMeasuredWidth();
        Log.e(TAG,"---- initView() 方法二 width == "+convenientBanner.getMeasuredWidth()+"   height == "+convenientBanner.getMeasuredHeight());



        iv = (ImageView) findViewById(R.id.iv);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"-------点击了图标");
                Intent intent = new Intent();
                if(!newPicUrlList.isEmpty()){
                    intent.putExtra(Constants.EXTRAS_PICLIST,newPicUrlList);

                }else{
                    intent.putExtra(Constants.EXTRAS_PICLIST,picUrllist);
                }
                intent.setClass(MainActivity.this,ManageImageActivity.class);
                startActivityForResult(intent,Constants.REQUESTCODE_MANAGE_IMAGE);
            }
        });

    }

    private void init(){
        initImageLoader();
        loadTestDatas();
        //本地图片例子
        convenientBanner.setPages(
                new CBViewHolderCreator<LocalImageHolderView>() {
                    @Override
                    public LocalImageHolderView createHolder() {
                        return new LocalImageHolderView();
                    }
                }, picUrllist)
                //设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
                .setPageIndicator(new int[]{R.drawable.ic_page_indicator, R.drawable.ic_page_indicator_focused})
                //设置指示器的方向
//                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
//                .setOnPageChangeListener(this)//监听翻页事件
                .setOnItemClickListener(this);

//        convenientBanner.setManualPageable(false);//设置不能手动影响

        //网络加载例子
//        networkImages=Arrays.asList(images);
//        convenientBanner.setPages(new CBViewHolderCreator<NetworkImageHolderView>() {
//            @Override
//            public NetworkImageHolderView createHolder() {
//                return new NetworkImageHolderView();
//            }
//        },networkImages);



//手动New并且添加到ListView Header的例子
//        ConvenientBanner mConvenientBanner = new ConvenientBanner(this,false);
//        mConvenientBanner.setMinimumHeight(500);
//        mConvenientBanner.setPages(
//                new CBViewHolderCreator<LocalImageHolderView>() {
//                    @Override
//                    public LocalImageHolderView createHolder() {
//                        return new LocalImageHolderView();
//                    }
//                }, localImages)
//                //设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
//                .setPageIndicator(new int[]{R.drawable.ic_page_indicator, R.drawable.ic_page_indicator_focused})
//                        //设置指示器的方向
//                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
//                .setOnItemClickListener(this);
//        listView.addHeaderView(mConvenientBanner);

    }

    //初始化网络图片缓存库
    private void initImageLoader(){
        //网络图片例子,结合常用的图片缓存库UIL,你可以根据自己需求自己换其他网络图片库
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().
                showImageForEmptyUri(R.drawable.ic_default_adimage)
                .cacheInMemory(true).cacheOnDisk(true).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
    }

    /*
    加入测试Views
    * */
    private void loadTestDatas() {
        //本地图片集合
        for (int position = 0; position < 7; position++)
            localImages.add(getResId("ic_test_" + position, R.drawable.class));



        // SoftReference<Bitmap> bitmap = new SoftReference<Bitmap>(loadSdCardPic());
        // iv.setImageBitmap(loadSdCardPic());

        //获取sdcard/savePic的jpg文件
        boolean sdCardExit = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if(sdCardExit){
            String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath(): "/mnt/sdcard";
            Log.e("MainActivity","------SAVE_PIC_PATH  ==="+SAVE_PIC_PATH);
            String BitmapBaseFile = Environment.getExternalStorageDirectory().toString() + File.separator ;
            String sd_File_Url = BitmapBaseFile + "savePic";
            Log.e("MainActivity","------sd_File_Url==="+sd_File_Url);
            /* ------sd_File_Url===/storage/emulated/0/savePic*/
            File file = new File(sd_File_Url);
            //如果不存在文件夹则创建，并把drawable内的初始图片存入，
            if(!file.exists()){
                file.mkdirs();

                FileOutputStream fos = null;
                try {
                    ArrayList<String> initPicList = new ArrayList<String>();
                    for(int i = 0;i<4;i++){
                        Bitmap initBitmap = BitmapFactory.decodeResource(getResources(),getResId("ic_test_" + i, R.drawable.class));
                        String initPicName = sd_File_Url + "/ic_test_" + i +".jpg";
                        Log.e(TAG,"-------initPicName:   "+initPicName+"  ------");
                        /* initPicName:   /storage/emulated/0/savePic/ic_test_0.jpg */
                        File f = new File(sd_File_Url,"ic_test_" + i +".jpg");
                        if(!f.exists()){
                            f.createNewFile();
                        }
                        //fos = new FileOutputStream(initPicName);
                        //fos = new FileOutputStream(f);
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
                        initBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        initBitmap.recycle();
                        initPicList.add(initPicName);
                    }
                    //保存到sharedpref
                    saveApkEnableArray(MainActivity.this,initPicList);
                    //获取文件夹内的所有JPG文件
                    File[] files = file.listFiles();
                    for (int i = 0;i<files.length;i++){
                        if(!files[i].isDirectory()){
                            String filename = files[i].getName();
                            if(filename.trim().toLowerCase().endsWith(".jpg")){
                                picNamelist.add(filename);
                                picUrllist.add(sd_File_Url+File.separator+filename);
                                Log.e(TAG,"---- picName "+i+" ==="+" "+filename+" --------");
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    if (fos != null) {
                        try {
                            fos.flush();
                            fos.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }else if(picUrllist.size() == 0){
                Log.e(TAG,"----sharedPreference 数据为空");
                //重新读取savePic内图片数据
                //获取文件夹内的所有JPG文件
                File[] savePicfiles = file.listFiles();
                for (int i = 0;i<savePicfiles.length;i++){
                    if(!savePicfiles[i].isDirectory()){
                        String filename = savePicfiles[i].getName();
                        if(filename.trim().toLowerCase().endsWith(".jpg")){
                            picNamelist.add(filename);
                            picUrllist.add(sd_File_Url+File.separator+filename);
                            Log.e(TAG,"---- picName "+i+" ==="+" "+filename+" --------");
                        }
                    }
                }
            }




            //将文件夹内的所有JPG文件转化成bitmap
            SoftReference<Bitmap> loadBitmap;
            for (int i = 0;i<picNamelist.size();i++){
                String loadPicUrl = sd_File_Url+File.separator+picNamelist.get(i);
                Log.e(TAG,"---- loadPicUrl "+i+" === "+loadPicUrl+"  -----");
                //获取原图片尺寸
                BitmapFactory.Options loadoptions = new BitmapFactory.Options();
                loadoptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(loadPicUrl,loadoptions);
                int imgHeight = loadoptions.outHeight;
                int imgWidth = loadoptions.outWidth;
                String imgType = loadoptions.outMimeType;
                Log.e(TAG,"---- decodeSampledBitmapFromFile width == "+width+"   --height =="+height);
                loadBitmap = new SoftReference<Bitmap>(decodeSampledBitmapFromFile(loadPicUrl,width,height));

                // loadBitmap = new SoftReference<Bitmap>(loadSdCardPic(loadPicUrl));
                bitmapLists.add(loadBitmap);
                if(loadBitmap !=null){
                    if(loadBitmap.get()!=null && !loadBitmap.get().isRecycled()){
                        loadBitmap.get().recycle();
                        loadBitmap = null;
                    }
                }
            }
        }

//        //各种翻页效果
//        transformerList.add(DefaultTransformer.class.getSimpleName());
//        transformerList.add(AccordionTransformer.class.getSimpleName());
//        transformerList.add(BackgroundToForegroundTransformer.class.getSimpleName());
//        transformerList.add(CubeInTransformer.class.getSimpleName());
//        transformerList.add(CubeOutTransformer.class.getSimpleName());
//        transformerList.add(DepthPageTransformer.class.getSimpleName());
//        transformerList.add(FlipHorizontalTransformer.class.getSimpleName());
//        transformerList.add(FlipVerticalTransformer.class.getSimpleName());
//        transformerList.add(ForegroundToBackgroundTransformer.class.getSimpleName());
//        transformerList.add(RotateDownTransformer.class.getSimpleName());
//        transformerList.add(RotateUpTransformer.class.getSimpleName());
//        transformerList.add(StackTransformer.class.getSimpleName());
//        transformerList.add(ZoomInTransformer.class.getSimpleName());
//        transformerList.add(ZoomOutTranformer.class.getSimpleName());
//
//        transformerArrayAdapter.notifyDataSetChanged();
    }

    //图片管理器返回数据
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUESTCODE_MANAGE_IMAGE:    //图片管理排序
                Log.e(TAG,"----- onActivityResult 管理图片排序 --");
                if(data == null){
                    Log.e(TAG,"----- onActivityResult 管理图片排序 data null--");
                    return;
                }else{

                    newPicUrlList = data.getStringArrayListExtra(Constants.EXTRAS_NEW_PICLIST);
                    Log.e(TAG,"----- onActivityResult newPicUrlList == "+newPicUrlList.toString()+"--");
                    //重新设置数组
                    convenientBanner.setPages(
                            new CBViewHolderCreator<LocalImageHolderView>() {
                                @Override
                                public LocalImageHolderView createHolder() {
                                    return new LocalImageHolderView();
                                }
                            }, newPicUrlList)
                            //设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
                            .setPageIndicator(new int[]{R.drawable.ic_page_indicator, R.drawable.ic_page_indicator_focused})
                            //设置指示器的方向
//                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
//                .setOnPageChangeListener(this)//监听翻页事件
                            .setOnItemClickListener(this);
                    //保存数组
                    saveApkEnableArray(MainActivity.this,newPicUrlList);
                }
            default:
                break;

        }
    }


    //获取sharedPreference图片路径
    public static ArrayList<String> getApkEnableArray(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.APK_ENABLE_ARRAT,Context.MODE_PRIVATE);
        ArrayList<String> resList = new ArrayList<String>();
        try {
            JSONArray jsonArray = new JSONArray(prefs.getString(Constants.APK_ENABLE_ARRAT,"[]"));
            for(int i = 0;i<jsonArray.length();i++){
                resList.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resList;
    }

    //存储sharedPreference图片路径
    public static void saveApkEnableArray(Context context,ArrayList<String> saveList){
        Log.e(TAG,"------saveApkEnableArray()  saveList == "+saveList.toString());


        SharedPreferences prefs = context.getSharedPreferences(Constants.APK_ENABLE_ARRAT, Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        for (String i:saveList){
            jsonArray.put(i);
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putString(Constants.APK_ENABLE_ARRAT ,jsonArray.toString());
        editor.commit();


    }

    /**
     * 通过文件名获取资源id 例子：getResId("icon", R.drawable.class);
     *
     * @param variableName
     * @param c
     * @return
     */
    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }
    //压缩图片
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onItemClick(int position) {

    }
}

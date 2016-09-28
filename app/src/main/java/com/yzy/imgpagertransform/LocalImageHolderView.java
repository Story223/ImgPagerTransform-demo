package com.yzy.imgpagertransform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.yzy.imgpagertransform.convenientbanner.holder.Holder;

import java.io.File;
import java.lang.ref.SoftReference;


/**
 * Created by Sai on 15/8/4.
 * 本地图片Holder例子
 */
public class LocalImageHolderView implements Holder<String> {
    private ImageView imageView;
    private int imageViewHeight;
    private int imageViewWidth;
    private static final String TAG = "LoacalImageHolderView";
    @Override
    public View createView(Context context) {
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }

//    @Override
//    public void UpdateUI(Context context, int position, Integer data) {
//        imageView.setImageResource(data);
//    }

    //根据图片路径获取图片并在imageView中显示
    @Override
    public void UpdateUI(Context context, int position, String data){
        //获得imageView控件的宽高
        //int imageViewHeight = imageView.getHeight();
        //int imageViewWidth = imageView.getWidth();

//        //方法二
//        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
//        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
//        imageView.measure(w, h);
//        imageViewHeight =imageView.getMeasuredHeight();
//        imageViewWidth =imageView.getMeasuredWidth();
        //方法三
        ViewTreeObserver vto2 = imageView.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Log.e(TAG,"---- UpdateUI()  width == "+imageView.getWidth()+"   height == "+imageView.getHeight() );
                imageViewHeight =imageView.getHeight();
                imageViewWidth =imageView.getWidth();

            }
        });
        //Log.e(TAG,"---- initView() 方法二 width == "+imageViewWidth+"   height == "+imageViewHeight);
        //Log.e(TAG,"---- UpdateUI()  width == "+imageViewWidth+"   height == "+imageViewHeight );
        Log.e(TAG,"---- UpdateUI() date == "+data+"  ----");

        //根据图片路径获取图片
        //获取原图片尺寸
        BitmapFactory.Options loadoptions = new BitmapFactory.Options();
        loadoptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(data,loadoptions);
        int imgHeight = loadoptions.outHeight;
        int imgWidth = loadoptions.outWidth;
        String imgType = loadoptions.outMimeType;
        Log.e(TAG,"---- UpdateUI() imgwidth == "+imgWidth+"   --imgheight =="+imgHeight);
        Bitmap loadBitmap = decodeSampledBitmapFromFile(data,imageViewWidth,imgHeight);
        imageView.setImageBitmap(loadBitmap);
//        if(loadBitmap != null && ! loadBitmap.isRecycled()){
//            loadBitmap.recycle();
//            loadBitmap=null;
//        }
//        System.gc();
    }

    //压缩图片
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        return inSampleSize;
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

}

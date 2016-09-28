package com.yzy.imgpagertransform;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.yzy.imgpagertransform.sdlv.Menu;
import com.yzy.imgpagertransform.sdlv.MenuItem;
import com.yzy.imgpagertransform.sdlv.SlideAndDragListView;
import com.yzy.imgpagertransform.utils.PhotoUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ManageImageActivity extends AppCompatActivity implements SlideAndDragListView.OnListItemLongClickListener,
        SlideAndDragListView.OnDragListener, SlideAndDragListView.OnSlideListener,
        SlideAndDragListView.OnListItemClickListener, SlideAndDragListView.OnMenuItemClickListener,
        SlideAndDragListView.OnItemDeleteListener{
    private static final String TAG = "New_ManageImageActivity";

    private List<Menu> mMenuList;
    private ArrayList<String> array;
    private List<String> picUrlList;
    private SlideAndDragListView<String> mListView;

    //弹框显示选择拍照还是图库
    PopupWindow selectPopWindow;

    private Toolbar toolbar;

    boolean isFromCamera = false;// 区分拍照旋转
    private int degree = 0;

    //图片保存路径
    private String filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_image);

        initData();
        initView();
        initMenu();
        initUiandListener();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Button btn_confirm = (Button) toolbar.findViewById(R.id.btn_confirm);
        Button btn_add = (Button) toolbar.findViewById(R.id.btn_add);
        setSupportActionBar(toolbar);


        //点击确认按钮事件
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"------点击了确定按钮");
                ArrayList<String> newPicUrlList = new ArrayList<String>();
                for (int i = 0;i<picUrlList.size();i++){
                    newPicUrlList.add(i,picUrlList.get(i));
                }
                Log.e("","-----ManageImageActivty   newPicUrlList == "+newPicUrlList.toString());

                Intent intent = new Intent();
                intent.setClass(ManageImageActivity.this,MainActivity.class);
                intent.putExtra(Constants.EXTRAS_NEW_PICLIST,newPicUrlList);
                setResult(Constants.REQUESTCODE_MANAGE_IMAGE,intent);
                finish();
                //startActivity(intent);
            }
        });
        //点击添加按钮事件
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"------点击了添加按钮");
                selectPopWindow = new SelectPopuWindow(ManageImageActivity.this, null);

            }
        });
    }

    private void initUiandListener() {
        mListView = (SlideAndDragListView) findViewById(R.id.lv_edit);
        mListView.setMenu(mMenuList);
        mListView.setAdapter(mAdapter);
        mListView.setOnListItemLongClickListener(this);
        mListView.setOnDragListener(this, picUrlList);
        mListView.setOnListItemClickListener(this);
        mListView.setOnSlideListener(this);
        mListView.setOnMenuItemClickListener(this);
        mListView.setOnItemDeleteListener(this);
        mListView.setDivider(new ColorDrawable(Color.GRAY));
        mListView.setDividerHeight(1);
    }

    private void initMenu() {
        mMenuList = new ArrayList<>(1);
        Menu menu0 = new Menu(false,true,0);
        menu0.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.slv_item_bg_btn2_width))
                .setBackground(new ColorDrawable(Color.RED))
                .setText("删除")
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .setTextColor(Color.WHITE)
                .setTextSize(10)
                .build());
        mMenuList.add(menu0);

    }

    private void initData() {

        picUrlList = new ArrayList<String>();
        //获取传过来的图片路径
        Intent intent = getIntent();
        picUrlList = intent.getStringArrayListExtra(Constants.EXTRAS_PICLIST);
        Log.e(TAG,"------- picUrlList == "+picUrlList.toString());
    }

    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return picUrlList.size();
        }

        @Override
        public Object getItem(int position) {
            return picUrlList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CustomViewHolder cvh = null;
            if (convertView == null) {
                cvh = new CustomViewHolder();
                convertView = LayoutInflater.from(ManageImageActivity.this).inflate(R.layout.list_item_handle_right, null);
                cvh.imageView = (ImageView) convertView.findViewById(R.id.drag_handle);

                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            String picUrl = picUrlList.get(position);
            Log.e("NewManageImage","---- ManageImageAdapter picUrl["+position+"] == "+picUrl);

            BitmapFactory.Options loadoptions = new BitmapFactory.Options();
            loadoptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picUrl,loadoptions);
            int imgHeight = loadoptions.outHeight;
            int imgWidth = loadoptions.outWidth;
            String imgType = loadoptions.outMimeType;
            Log.e("NewManageImage","---- ManageImageAdapter imgwidth == "+imgWidth+"   --imgheight =="+imgHeight);
            Bitmap loadBitmap = decodeSampledBitmapFromFile(picUrl,100,imgHeight);
            cvh.imageView.setImageBitmap(loadBitmap);
            return convertView;
        }

        class CustomViewHolder {
            public ImageView imageView;
        }

    };

    @Override
    public void onDragViewStart(int position) {
        // Toast.makeText(New_ManageImageActivity.this, "onDragViewStart   position--->" + position, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onDragViewStart   " + position);
    }

    @Override
    public void onDragViewMoving(int position) {
        Log.i(TAG, "onDragViewMoving   " + position);
    }

    @Override
    public void onDragViewDown(int position) {
        // Toast.makeText(New_ManageImageActivity.this, "onDragViewDown   position--->" + position, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onDragViewDown   " + position);
    }

    @Override
    public void onItemDelete(View view, int position) {
        picUrlList.remove(position - mListView.getHeaderViewsCount());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(View v, int position) {
        //Toast.makeText(New_ManageImageActivity.this, "onItemClick   position--->" + position, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onListItemClick   " + position);
    }

    @Override
    public void onListItemLongClick(View view, int position) {
        //Toast.makeText(New_ManageImageActivity.this, "onItemLongClick   position--->" + position, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onListItemLongClick   " + position);
    }

    @Override
    public int onMenuItemClick(View v, int itemPosition, int buttonPosition, int direction) {
        Log.i(TAG, "onMenuItemClick   " + itemPosition + "   " + buttonPosition + "   " + direction);
        int viewType = mAdapter.getItemViewType(itemPosition);
        switch (viewType) {
            case 0:
                return clickMenuBtn0(buttonPosition, direction);
            case 1:
                return clickMenuBtn1(buttonPosition, direction);
            default:
                return Menu.ITEM_NOTHING;
        }
    }
    private int clickMenuBtn0(int buttonPosition, int direction) {
        switch (direction) {
            case MenuItem.DIRECTION_LEFT:
                switch (buttonPosition) {
                    case 0:
                        return Menu.ITEM_SCROLL_BACK;
                }
                break;
            case MenuItem.DIRECTION_RIGHT:
                switch (buttonPosition) {
                    case 0:
                        return Menu.ITEM_DELETE_FROM_BOTTOM_TO_TOP;
                    case 1:
                        return Menu.ITEM_NOTHING;
                    case 2:
                        return Menu.ITEM_SCROLL_BACK;
                }
        }
        return Menu.ITEM_NOTHING;
    }

    private int clickMenuBtn1(int buttonPosition, int direction) {
        switch (direction) {
            case MenuItem.DIRECTION_LEFT:
                switch (buttonPosition) {
                    case 0:
                        return Menu.ITEM_SCROLL_BACK;
                }
                break;
            case MenuItem.DIRECTION_RIGHT:
                switch (buttonPosition) {
                    case 0:
                        return Menu.ITEM_DELETE_FROM_BOTTOM_TO_TOP;
                    case 1:
                        return Menu.ITEM_SCROLL_BACK;
                }
        }
        return Menu.ITEM_NOTHING;
    }
    @Override
    public void onSlideOpen(View view, View parentView, int position, int direction) {
        // Toast.makeText(New_ManageImageActivity.this, "onSlideOpen   position--->" + position + "  direction--->" + direction, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onSlideOpen   " + position);
    }

    @Override
    public void onSlideClose(View view, View parentView, int position, int direction) {
        // Toast.makeText(New_ManageImageActivity.this, "onSlideClose   position--->" + position + "  direction--->" + direction, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onSlideClose   " + position);
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

    public class SelectPopuWindow extends PopupWindow {
        private Context context;

        public SelectPopuWindow(Context mContext, View parent) {

            this.context = mContext;
            View view = View
                    .inflate(mContext, R.layout.item_popupwindows, null);
            view.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.fade_ins));
            LinearLayout ll_popup = (LinearLayout) view
                    .findViewById(R.id.ll_popup);
            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.push_bottom_in_2));

            setWidth(ViewGroup.LayoutParams.FILL_PARENT);
            setHeight(ViewGroup.LayoutParams.FILL_PARENT);
            setBackgroundDrawable(new BitmapDrawable());
            setFocusable(true);
            setOutsideTouchable(true);
            setContentView(view);
//			showAsDropDown(Per_CenterActivity.this, 0, 0);

            showAtLocation(mListView, Gravity.CENTER, 0, 0);
            update();

            final Button layout_photo = (Button) view
                    .findViewById(R.id.item_popupwindows_camera);
            final Button layout_choose = (Button) view
                    .findViewById(R.id.item_popupwindows_Photo);
            Button layout_close = (Button) view
                    .findViewById(R.id.item_popupwindows_cancel);

            /**
             * 拍照
             */
            layout_photo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Log.e("","-----点击拍照");
                    // TODO Auto-generated method stub
                    //						layout_choose.setBackgroundColor(getResources().getColor(
                    //								R.color.base_color_text_white));
                    //						layout_photo.setBackgroundDrawable(getResources().getDrawable(
                    //								R.drawable.pop_bg_press));
                    File dir = new File(Constants.MyAvatarDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    // 原图
                    File file = new File(dir, new SimpleDateFormat("yyMMddHHmmss")
                            .format(new Date())+".jpg");
                    filePath = file.getAbsolutePath();// 获取相片的保存路径
                    Log.e(TAG,"----- filePath == "+filePath+"------");
                    Uri imageUri = Uri.fromFile(file);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent,
                            Constants.REQUESTCODE_UPLOADAVATAR_CAMERA);
                }
            });
            layout_choose.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    Log.e("","-----点击相册");
                    //						layout_photo.setBackgroundColor(getResources().getColor(
                    //								R.color.base_color_text_white));
                    //						layout_choose.setBackgroundDrawable(getResources().getDrawable(
                    //								R.drawable.pop_bg_press));
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent,
                            Constants.REQUESTCODE_UPLOADAVATAR_LOCATION);
                }
            });
            layout_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUESTCODE_UPLOADAVATAR_CAMERA:// 拍照修改头像
                if (resultCode == RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Log.e(TAG,"---- onActivitResult()  sd不可用----");
                        //showToast("SD不可用");
                        return;
                    }
                    isFromCamera = true;
                    File file = new File(filePath);
                    degree = PhotoUtil.readPictureDegree(file.getAbsolutePath());
                    Log.i("life", "拍照后的角度：" + degree);
                    int dw=getWindowManager().getDefaultDisplay().getWidth();
                    startImageAction(Uri.fromFile(file),400, 200, Constants.REQUESTCODE_UPLOADAVATAR_CROP, true);
                }
                break;
            case Constants.REQUESTCODE_UPLOADAVATAR_LOCATION:// 本地修改头像
                if (selectPopWindow != null) {
                    selectPopWindow.dismiss();
                }
                Uri uri = null;
                if (data == null) {
                    return;
                }
                if (resultCode == RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Log.e(TAG,"---- onActivitResult()  sd不可用----");
                        //showToast("SD不可用");
                        return;
                    }
                    isFromCamera = false;
                    uri = data.getData();
                    int dw=getWindowManager().getDefaultDisplay().getWidth();
                    Log.e(TAG,"----- dw == "+dw);
                    startImageAction(uri, 400, 200, Constants.REQUESTCODE_UPLOADAVATAR_CROP, true);
                } else {
                    Log.e(TAG,"---- onActivitResult()  照片获取失败---");
                    // showToast("照片获取失败");
                }

                break;
            case Constants.REQUESTCODE_UPLOADAVATAR_CROP:// 裁剪头像返回
                Log.e(TAG,"----- onActivityResult 裁剪图片 --");
                if (selectPopWindow != null) {
                    selectPopWindow.dismiss();
                }
                if (data == null) {
                    // Toast.makeText(this, "取消选择", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"----- onActivityResult 裁剪图片 data null--");
                    return;
                } else {
                    Log.e(TAG,"----- onActivityResult 裁剪图片 data saveCropAvator--");
                    saveCropAvator(data);
                    //添加图片
                    mListView.setOnDragListener(this,picUrlList);

                }
                // 初始化文件路径
                filePath = "";
                // 上传头像
                // uploadAvatar();
                break;

            default:
                break;

        }
    }
    /**
     * 保存裁剪的头像
     *
     * @param data
     */
    private void saveCropAvator(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            Log.i(TAG, "--- saveCropAvatar - bitmap = " + bitmap +"  ----");
            if (bitmap != null) {
                //bitmap = PhotoUtil.toRoundCorner(bitmap, 10);
                if (isFromCamera && degree != 0) {
                    bitmap = PhotoUtil.rotaingImageView(degree, bitmap);
                }
                //logineduserIcon.setImageBitmap(bitmap);
                // 保存图片
                String filename1 = new SimpleDateFormat("yyMMddHHmmss")
                        .format(new Date())+".jpg";
                String path = Constants.CropDir + File.separator + filename1;
                PhotoUtil.saveBitmap(Constants.CropDir, filename1,
                        bitmap, true);
                //保存sharedPreferences
                //picUrllist.add(path);
                picUrlList.add(path);
                //saveApkEnableArray(MainActivity.this,picUrllist);
                // 上传头像
                if (bitmap != null && bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
    }

    /**
     * @Title: startImageAction
     * @return void
     * @throws
     */
    private void startImageAction(Uri uri, int outputX, int outputY,
                                  int requestCode, boolean isCrop) {
        Intent intent = null;
        if (isCrop) {
            intent = new Intent("com.android.camera.action.CROP");
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        }



        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 2);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("circleCrop", false);

        //intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
    }

}

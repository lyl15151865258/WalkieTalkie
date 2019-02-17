package jp.co.shiratsuki.walkietalkie.activity.loginregister;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kevin.crop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.appmain.CropActivity;
import jp.co.shiratsuki.walkietalkie.activity.base.BaseActivity;
import jp.co.shiratsuki.walkietalkie.bean.NormalResult;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.constant.Constants;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.interfaces.OnPictureSelectedListener;
import jp.co.shiratsuki.walkietalkie.network.ExceptionHandle;
import jp.co.shiratsuki.walkietalkie.network.NetClient;
import jp.co.shiratsuki.walkietalkie.network.NetworkSubscriber;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.BitmapUtils;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.NetworkUtil;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 选择头像页面
 * Created at 2018/8/4 0004 15:58
 *
 * @author LiYuliang
 * @version 1.0
 */

public class ChooseHeadPortraitActivity extends BaseActivity {

    private Context mContext;
    private String userId;
    private ImageView ivUserIcon;
    /**
     * 相册选图标记
     */
    private static final int GALLERY_REQUEST_CODE = 0;
    /**
     * 相机拍照标记
     */
    private static final int CAMERA_REQUEST_CODE = 1;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
    /**
     * 拍照临时图片
     */
    private String mTempPhotoPath;
    /**
     * 剪切后图像文件
     */
    private Uri mDestinationUri;
    private OnPictureSelectedListener mOnPictureSelectedListener;
    /**
     * 标记是否已经上传头像
     */
    private boolean isUploadedIcon = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_head_portrait);
        mContext = this;
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, "设置头像", R.drawable.back_white, R.drawable.icon_finish, onClickListener);
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        userId = user.getUser_id();
        ivUserIcon = findViewById(R.id.ivUserIcon);
        findViewById(R.id.btnTakePhoto).setOnClickListener(onClickListener);
        findViewById(R.id.btnSelectFromAlbum).setOnClickListener(onClickListener);
        mDestinationUri = Uri.fromFile(new File(getExternalFilesDir("Icons"), "cropImage.jpeg"));
        mTempPhotoPath = Environment.getExternalStorageDirectory() + File.separator + "photo.jpeg";
        setOnPictureSelectedListener(onPictureSelectedListener);
    }

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                if (isUploadedIcon) {
                    ActivityController.finishActivity(this);
                } else {
                    showToast("请上传头像后退出");
                }
                break;
            case R.id.iv_right:
                if (isUploadedIcon) {
                    ActivityController.finishActivity(this);
                } else {
                    showToast("请上传头像");
                }
                break;
            case R.id.btnTakePhoto:
                takePhoto();
                break;
            case R.id.btnSelectFromAlbum:
                pickFromGallery();
                break;
            default:
                break;
        }
    };

    /**
     * 图片裁剪完成的监听
     */
    private OnPictureSelectedListener onPictureSelectedListener = (fileUri, bitmap) -> {
        String filePath = fileUri.getEncodedPath();
        String imagePath = Uri.decode(filePath);
        String time = (new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)).format(new Date());
        //以“.webp”格式作为图片扩展名
        String type = "webp";
        //将本软件的包路径+文件名拼接成图片绝对路径
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        String newFile = getExternalFilesDir("Icons") + "/" + "_" + user.getUser_id() + time + "." + type;
        BitmapUtils.compressPicture(imagePath, newFile);
        uploadUserIcon(new File(newFile));
    };

    /**
     * 上传或更新头像
     *
     * @param file 头像文件
     */
    private void uploadUserIcon(File file) {
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), userId);
        // 创建 RequestBody，用于封装构建RequestBody
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        // MultipartBody.Part  和后端约定好Key，这里的partName是用image
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploadfile", file.getName(), requestFile);
        // 执行请求
        Observable<NormalResult> normalResultObservable = NetClient.getInstances(NetClient.BASE_URL_PROJECT).getNjMeterApi().uploadUserIcon(description, body);
        normalResultObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkSubscriber<NormalResult>(mContext, getClass().getSimpleName()) {

            @Override
            public void onStart() {
                super.onStart();
                //接下来可以检查网络连接等操作
                if (!NetworkUtil.isNetworkAvailable(mContext)) {
                    showToast("当前网络不可用，请检查网络");
                    if (!isUnsubscribed()) {
                        unsubscribe();
                    }
                } else {
                    showLoadingDialog(mContext, "更新中", true);
                }
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                cancelDialog();
                showToast(responseThrowable.message);
            }

            @Override
            public void onNext(NormalResult normalResult) {
                cancelDialog();
                if (normalResult == null) {
                    showToast("头像更新失败");
                } else {
                    String result = normalResult.getResult();
                    String photoPath = ("http://" + NetWork.SERVER_HOST_MAIN + ":" + NetWork.SERVER_PORT_MAIN + "/" + normalResult.getMessage()).replace("\\", "/");
                    // 更新存储的User对象
                    User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
                    user.setIcon_url(normalResult.getMessage());
                    SPHelper.save("User", GsonUtils.convertJSON(user));
                    switch (result) {
                        case Constants.SUCCESS:
                            showToast("头像更新成功");
                            showUserIcon(photoPath);
                            isUploadedIcon = true;
                            break;
                        case Constants.FAIL:
                            showToast("服务器保存异常，更新失败");
                            break;
                        default:
                            showToast("未知错误，头像更新失败");
                            break;
                    }
                }
            }
        });
    }

    /**
     * 加载头像
     *
     * @param photoPath 头像路径
     */
    private void showUserIcon(String photoPath) {
        LogUtils.d(NetClient.TAG_POST, "图片路径：" + photoPath);
        SPHelper.save("userIconPath", photoPath);
        if (photoPath != null) {
            RequestOptions options = new RequestOptions()
                    .error(R.drawable.photo_user)
                    .placeholder(R.drawable.photo_user)
                    .dontAnimate();
            Glide.with(this).load(photoPath).apply(options).into(ivUserIcon);
        }
    }

    /**
     * 设置图片选择的回调监听
     *
     * @param l 监听器
     */
    public void setOnPictureSelectedListener(OnPictureSelectedListener l) {
        this.mOnPictureSelectedListener = l;
    }

    /**
     * 打开相机拍照
     */
    private void takePhoto() {
        // Permission was added in API Level 16
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    getString(R.string.permission_write_storage_rationale),
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
        } else {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //下面这句指定调用相机拍照后的照片存储的路径
            takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mTempPhotoPath)));
            startActivityForResult(takeIntent, CAMERA_REQUEST_CODE);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }

    /**
     * 从相册选择照片
     */
    private void pickFromGallery() {
        // Permission was added in API Level 16
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
            // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(pickIntent, GALLERY_REQUEST_CODE);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    // 调用相机拍照
                    File temp = new File(mTempPhotoPath);
                    startCropActivity(Uri.fromFile(temp));
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    break;
                case GALLERY_REQUEST_CODE:
                    // 直接从相册获取
                    startCropActivity(data.getData());
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    break;
                case UCrop.REQUEST_CROP:
                    // 裁剪图片结果
                    handleCropResult(data);
                    break;
                case UCrop.RESULT_ERROR:
                    // 裁剪图片错误
                    handleCropError(data);
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri 图片路径
     */
    public void startCropActivity(Uri uri) {
        UCrop.of(uri, mDestinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(512, 512)
                .withTargetActivity(CropActivity.class)
                .start(this);
    }

    /**
     * 处理剪切成功的返回值
     *
     * @param result 返回值
     */
    private void handleCropResult(Intent result) {
        deleteTempPhotoFile();
        final Uri resultUri = UCrop.getOutput(result);
        if (null != resultUri && null != mOnPictureSelectedListener) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mOnPictureSelectedListener.onPictureSelected(resultUri, bitmap);
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        } else {
            showToast("无法剪切选择图片");
        }
    }

    /**
     * 处理剪切失败的返回值
     *
     * @param result 返回值
     */
    private void handleCropError(Intent result) {
        deleteTempPhotoFile();
        Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            showToast(cropError.getMessage());
        } else {
            showToast("无法剪切选择图片");
        }
    }

    /**
     * 删除拍照临时文件
     */
    private void deleteTempPhotoFile() {
        File tempFile = new File(mTempPhotoPath);
        if (tempFile.exists() && tempFile.isFile()) {
            boolean deleteResult = tempFile.delete();
            if (deleteResult) {
                LogUtils.d("文件删除成功");
            }
        }
    }

    /**
     * 请求权限
     * 如果权限被拒绝过，则提示用户需要权限
     */
    protected void requestPermission(String permission, String rationale, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(permission)) {
                showAlertDialog(getString(R.string.permission_title_rationale), rationale,
                        (dialog, which) -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{permission}, requestCode);
                            }
                        }, getString(R.string.label_ok));
            } else {
                requestPermissions(new String[]{permission}, requestCode);
            }
        }
    }

    protected void showAlertDialog(String title, String message, DialogInterface.OnClickListener onPositiveButtonClickListener, String positiveText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, onPositiveButtonClickListener);
        builder.show();
    }
}

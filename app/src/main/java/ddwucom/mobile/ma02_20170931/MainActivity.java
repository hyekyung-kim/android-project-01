package ddwucom.mobile.ma02_20170931;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "MainActivity";
    private Boolean isPermission = true;
    /*DATA*/
    final static int PERMISSION_REQ_CODE = 300;       // TODO: 외부저장소의 공용폴더에 저장할 때 사용할 것
    private final static int REQUEST_TAKE_THUMBNAIL = 100;  // 저화질 이미지 사용 요청
    private static final int REQUEST_TAKE_PHOTO = 200;  // 원본 이미지 사용 요청
    private File photoFile;

    /*UI*/
    private ImageView mImageView;
    private String mCurrentPhotoPath;
    String imageFileName;
    Uri photoURI;
    String type = "image/*";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        Log.i(TAG, getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());

    }

    //첫 화면: 버튼
    public void onClick(View v){
        switch(v.getId()){
            // 내서재 버튼 클릭시 읽고있는 책 목록 확인
            case R.id.btn_mybook: //내 서재
                //TODO: 읽고 있는 책/위시 직접추가 - 추가시 카메라로 책 표지등 추가 가능하도록
                //TODO: 독서 기록
                Intent intent_mybook = new Intent(this, ReadingMain.class);
                startActivity(intent_mybook);
                break;
            case R.id.btn_library:
                //TODO: 현재 위치 기반 도서관 정보
                Intent intent_library = new Intent(this, LibLocMain.class);
                startActivity(intent_library);
                break;

            case R.id.btn_search:
                /*TODO: 현재 책 검색, 상세 정보 확인까지 완료
                 *  상세 정보 화면에 이미지 추가, 읽기/위시추가 버튼 클릭시 추가되도록 구현*/
                Intent intent_search = new Intent(this, SearchMain.class);
                startActivity(intent_search);
                break;

            case R.id.btn_camera:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQ_CODE);
                }
                tedPermission();
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if(isPermission)   dispatchTakePictureIntent();
                else Toast.makeText(v.getContext(), getResources().getString(R.string.permission_camera), Toast.LENGTH_LONG).show();

                break;
            case R.id.btn_share:
                createInstagramIntent(type, imageFileName);
                break;
        }
    }
    /*원본 사진 요청*/
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 요청을 처리할 수 있는 카메라 앱이 있을 경우
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 사진을 저장할 파일 생성
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // 파일을 정상 생성하였을 경우
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "ddwucom.mobile.fileprovider",    // 다른 앱에서 내 앱의 파일을 접근하기 위한 권한명 지정
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Log.d("timeStamp", timeStamp);
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);  // TODO: 외부저장소의 공용폴더에 저장할 때 사용할 것
        Log.d("imageFileName", imageFileName);

        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("storageDir", storageDir.toString());
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.d("image", image.toString());

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.i(TAG, "Created file path: " + mCurrentPhotoPath);
        return image;
    }
    private void createInstagramIntent(String type, String mediaPath) {

        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);

        // Set the MIME type
        share.setType(type);

        // Create the URI from the media
        File media = new File(mCurrentPhotoPath);
        //Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", media);

        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, photoURI);
        // Log.d("Share path: ", uri.toString());

        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"));
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "외부저장소 쓰기 권한 획득!", Toast.LENGTH_SHORT).show();
                    dispatchTakePictureIntent();
                } else {
                    finish();
                    Toast.makeText(this, "외부저장소 쓰기 권한 없음", Toast.LENGTH_SHORT).show();
                }

        }
    }
    /**
     *  권한 설정
     */
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                isPermission = false;

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_gallery))
                .setDeniedMessage(getResources().getString(R.string.permission_camera))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }
}

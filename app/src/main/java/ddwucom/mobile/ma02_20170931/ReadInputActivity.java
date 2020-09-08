package ddwucom.mobile.ma02_20170931;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class ReadInputActivity extends AppCompatActivity {
    private static final String TAG = "ReadInputActivity";

    final static int PERMISSION_REQ_CODE = 300;       // TODO: 외부저장소의 공용폴더에 저장할 때 사용할 것

    private Boolean isPermission = true;

    String imgType;

    EditText etTitle;
    EditText etAuthor;
    EditText etPublisher;
    Button gallery;
    Button camera;
    private String mCurrentPhotoPath;

    private File photoFile;


    ReadingDBHelper helper;

    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_read);

        /* 입력받을 텍스트 */
        etTitle = findViewById(R.id.add_title);
        etAuthor = findViewById(R.id.add_author);
        etPublisher = findViewById(R.id.add_publisher);

        helper = new ReadingDBHelper(this);

        /* 이미지 뷰 관련 */
        imageView = findViewById(R.id.add_image);

        /* 버튼 관련 */
        gallery = findViewById(R.id.btn_gallery);
        camera = findViewById(R.id.btn_camera);


        gallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                tedPermission();
                if(isPermission) goToAlbum();
                else Toast.makeText(view.getContext(), getResources().getString(R.string.permission_gallery), Toast.LENGTH_LONG).show();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(ReadInputActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ReadInputActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQ_CODE);
                }
                tedPermission();
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if(isPermission)   takePhoto();
                else Toast.makeText(view.getContext(), getResources().getString(R.string.permission_camera), Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     *  앨범에서 이미지 가져오기
     */
    private void goToAlbum() {
        imgType = String.valueOf(1);
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
        Log.d("gallery to sta", intent.toString());
    }

    /**
     *  카메라에서 이미지 가져오기
     */
    private void takePhoto() {
        imgType = String.valueOf(2);
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
                Uri photoURI = FileProvider.getUriForFile(this,
                        "ddwucom.mobile.fileprovider",    // 다른 앱에서 내 앱의 파일을 접근하기 위한 권한명 지정
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, PICK_FROM_CAMERA);
            }
        } else {
            Toast.makeText(this, "카메라 앱이 없습니다.", Toast.LENGTH_SHORT);
        }
    }



    /*현재 시간을 사용한 파일명으로 앱전용의 외부저장소에 파일 정보 생성*/
    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Log.d("timeStamp", timeStamp);
        String imageFileName = "JPEG_" + timeStamp + "_";
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

    /* 받아온 이미지를 이미지뷰에 보여주기 */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("gallery0", intent.toString());

        if (requestCode == PICK_FROM_ALBUM) {
            Uri photoUri = intent.getData();

            Cursor cursor = null;
            Log.d("gallery1", photoUri.toString());
            try {
                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경
                 */
                String[] proj = { MediaStore.Images.Media.DATA };

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                photoFile = new File(cursor.getString(column_index));
                Log.d("gallery2", photoFile.toString());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            setImage(requestCode);
        }else if(requestCode == PICK_FROM_CAMERA){
            setImage(requestCode);
        }else {
            Toast.makeText(ReadInputActivity.this, "이미지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /* 추가/취소 버튼 클릭시 */
    public void onClick(View v){
        String title = etTitle.getText().toString();
        String author = etAuthor.getText().toString();
        String publisher = etPublisher.getText().toString();

        // 현재시간을 msec 으로 구한다.
        long now = System.currentTimeMillis();
        // 현재시간을 date 변수에 저장한다.
        Date date = new Date(now);
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
        // nowDate 변수에 값을 저장한다.
        String formatDate = sdfNow.format(date);

        switch(v.getId()){
            case R.id.add_button:
                if(title.length() == 0) {
                Toast.makeText(this, "제목을 입력하세요", Toast.LENGTH_SHORT).show();
            }else if(author.length() == 0) {
                Toast.makeText(this, "저자를 입력하세요", Toast.LENGTH_SHORT).show();
            }else if(publisher.length() == 0) {
                Toast.makeText(this, "출판사를 입력하세요", Toast.LENGTH_SHORT).show();
            }else {
                    SQLiteDatabase db = helper.getWritableDatabase();
                    ContentValues value = new ContentValues();

                    value.put(ReadingDBHelper.COL_TITLE, title);
                    value.put(ReadingDBHelper.COL_AUTHOR, author);
                    value.put(ReadingDBHelper.COL_PUBLISHER, publisher);
                    value.put(ReadingDBHelper.COL_IMGTYPE, imgType);    // 갤러리:1 카메라:2
                    value.put(ReadingDBHelper.COL_IMGURL, String.valueOf(photoFile)); // 저장 경로

                    value.put(ReadingDBHelper.COL_STARTDAY, formatDate);

                    long count = db.insert(ReadingDBHelper.TABLE_NAME, null, value);

                    if (count > 0) {
                        setResult(RESULT_OK, null);
                        helper.close();
                        Toast.makeText(this, "입력한 도서를 내 서재에 추가했습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "도서 추가 실패", Toast.LENGTH_SHORT).show();
                        helper.close();
                        finish();
                    }
                }
                break;

            case R.id.add_cancel:
                setResult(RESULT_CANCELED);

                finish();
                break;
        }
    }


    /* 카메라 사진 회전 */
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void setImage(int requestCode) {

        ImageView imageView = findViewById(R.id.add_image);
        Log.d("gallery3", imageView.toString());
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
        Log.d(TAG, "setImage : " + photoFile.getAbsolutePath());
        Bitmap bitmap;

        if(requestCode == PICK_FROM_CAMERA){
            bitmap = rotateImage(originalBm, 90);
        }else{  // PICK_FROM_ALBUM
            bitmap = originalBm;
        }

        imageView.setImageBitmap(bitmap);

        /**
         *  tempFile 사용 후 null 처리를 해줘야 합니다.
         *  (resultCode != RESULT_OK) 일 때 tempFile 을 삭제하기 때문에
         *  기존에 데이터가 남아 있게 되면 원치 않은 삭제가 이뤄집니다.
         */
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
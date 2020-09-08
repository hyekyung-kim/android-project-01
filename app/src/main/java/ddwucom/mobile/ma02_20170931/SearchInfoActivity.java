package ddwucom.mobile.ma02_20170931;


import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SearchInfoActivity extends AppCompatActivity {
    final int SEARCH_TO_READ = 100;

    TextView tvTitle;
    TextView tvAuthor;
    TextView tvPublisher;
    TextView tvDescription;
    ImageView imgBook;
    String imgUrl;
    String imgType;

    String title;
    String author;
    String publisher;

    ReadingDBHelper rHelper;
    WishDBHelper wHelper;
    NaverBookDTO bookDto;

    ImageFileManager imageFileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_info);

        rHelper = new ReadingDBHelper(this);
        wHelper = new WishDBHelper(this);

        tvTitle= findViewById(R.id.info_title);
        tvAuthor = findViewById(R.id.info_author);
        tvPublisher = findViewById(R.id.info_publisher);
        tvDescription = findViewById(R.id.info_description);
        imgBook = findViewById(R.id.book_img);

        Intent intent = getIntent();
        bookDto = (NaverBookDTO) intent.getSerializableExtra("NaverBookDTO");
        Bitmap img = intent.getParcelableExtra("img");

        imgUrl = intent.getStringExtra("url");

        tvTitle.setText(bookDto.getTitle());
        tvAuthor.setText(bookDto.getAuthor());
        tvPublisher.setText(bookDto.getPublisher());
        tvDescription.setText(bookDto.getDescription());
        tvDescription.setFocusable(false);
        tvDescription.setClickable(false);

        imgBook.setImageBitmap(img);

        title = tvTitle.getText().toString();
        author = tvAuthor.getText().toString();
        publisher = tvPublisher.getText().toString();
        imgType = String.valueOf(0);
    }

    public void onClick(View v) {
        long now = System.currentTimeMillis();
        // 현재시간을 date 변수에 저장한다.
        Date date = new Date(now);
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
        // nowDate 변수에 값을 저장한다.
        String formatDate = sdfNow.format(date);


        switch(v.getId()) {
            case R.id.btn_reading:
                /* 검색한 책을 내 서재에 추가 */
                SQLiteDatabase rDB = rHelper.getWritableDatabase();
                ContentValues rValue = new ContentValues();
                //                   value.put(ReadingDBHelper.COL_IMG, path);
                rValue.put(ReadingDBHelper.COL_TITLE, title);
                rValue.put(ReadingDBHelper.COL_AUTHOR, author);
                rValue.put(ReadingDBHelper.COL_PUBLISHER, publisher);
                rValue.put(ReadingDBHelper.COL_IMGTYPE, imgType);
                rValue.put(ReadingDBHelper.COL_IMGURL, imgUrl);
                rValue.put(ReadingDBHelper.COL_STARTDAY, formatDate);

                long count = rDB.insert(ReadingDBHelper.TABLE_NAME, null, rValue);

                if(count > 0){
                    setResult(RESULT_OK, null);
                    rHelper.close();
                    Toast.makeText(this, "선택한 도서를 내 서재에 추가했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "도서 추가 실패", Toast.LENGTH_SHORT).show();
                    rHelper.close();
                    finish();
                }
                rHelper.close();
                break;
            case R.id.btn_add_wish:
                /* 검색한 책을 위시 리스트에 추가 */
                SQLiteDatabase wDB = wHelper.getWritableDatabase();
                ContentValues wValue = new ContentValues();
                //                   value.put(ReadingDBHelper.COL_IMG, path);
                wValue.put(WishDBHelper.COL_TITLE, title);
                wValue.put(WishDBHelper.COL_AUTHOR, author);
                wValue.put(WishDBHelper.COL_PUBLISHER, publisher);
                wValue.put(WishDBHelper.COL_IMGTYPE, imgType);
                wValue.put(WishDBHelper.COL_IMGURL, imgUrl);
                wValue.put(WishDBHelper.COL_STARTDAY, formatDate);

                long w_count = wDB.insert(WishDBHelper.TABLE_NAME, null, wValue);

                if(w_count > 0){
                    setResult(RESULT_OK, null);
                    wHelper.close();
                    Toast.makeText(this, "선택한 도서를 위시리스트에 추가했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "도서 추가 실패", Toast.LENGTH_SHORT).show();
                    wHelper.close();
                    finish();
                }
                wHelper.close();
                break;
        }
        finish();

    }
}

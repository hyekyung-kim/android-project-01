package ddwucom.mobile.ma02_20170931;


import android.app.AlertDialog;
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

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WishInfoActivity extends AppCompatActivity {
    final int WISH_TO_READ = 100;

    TextView tvTitle;
    TextView tvAuthor;
    TextView tvPublisher;
    ImageView imgBook;

    String title;
    String author;
    String publisher;
    String imgType;
    String imgUrl;
    String startDay;
    String position;

    ReadingDBHelper readingDBHelper;
    WishDBHelper wishDBHelper;
    BookDTO bookDto;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_info);

        readingDBHelper = new ReadingDBHelper(this);
        wishDBHelper = new WishDBHelper(this);

        tvTitle= findViewById(R.id.info_title);
        tvAuthor = findViewById(R.id.info_author);
        tvPublisher = findViewById(R.id.info_publisher);
        imgBook = findViewById(R.id.book_img);

        Intent intent = getIntent();
        bookDto = (BookDTO) intent.getSerializableExtra("wishDTO");
        Bitmap img = intent.getParcelableExtra("img");
        title = bookDto.getTitle();
        author = bookDto.getAuthor();
        publisher = bookDto.getPublisher();
        imgType = bookDto.getImgType();
        imgUrl = bookDto.getImgUrl();
        startDay = bookDto.getStartDay();

        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvPublisher.setText(publisher);
        imgBook.setImageBitmap(img);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //alertDialog.dismiss();
    }




    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_wish:
                /* 읽는 중이던 책에 대한 독서 기록 */

                long now = System.currentTimeMillis();
                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
                // nowDate 변수에 값을 저장한다.
                String formatDate = sdfNow.format(date);


                /* 위시리스트의 책을 내 서재로 */

                SQLiteDatabase readingDB = readingDBHelper.getWritableDatabase();
                ContentValues readingValue = new ContentValues();
                //                   value.put(ReadingDBHelper.COL_IMG, path);
                readingValue.put(ReadingDBHelper.COL_TITLE, title);
                readingValue.put(ReadingDBHelper.COL_AUTHOR, author);
                readingValue.put(ReadingDBHelper.COL_PUBLISHER, publisher);
                readingValue.put(ReadingDBHelper.COL_IMGTYPE, imgType);
                readingValue.put(ReadingDBHelper.COL_IMGURL, imgUrl);
                readingValue.put(ReadingDBHelper.COL_STARTDAY, startDay);

                long count = readingDB.insert(ReadingDBHelper.TABLE_NAME, null, readingValue);

                if (count > 0) {
                    setResult(RESULT_OK, null);
                    readingDBHelper.close();
                    Toast.makeText(this, "위시리스트에 있는 책 " + title + "을(를) 읽습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "도서 추가 실패", Toast.LENGTH_SHORT).show();
                    readingDBHelper.close();
                }
                readingDBHelper.close();

                SQLiteDatabase wishDB = wishDBHelper.getWritableDatabase();
                String whereClause = WishDBHelper.COL_ID + "=?" ;
                String[] whereArgs = new String[] { String.valueOf(bookDto.get_id()) };
                wishDB.delete(WishDBHelper.TABLE_NAME, whereClause, whereArgs);
                wishDBHelper.close();

                //                        새로운 DB 내용으로 리스트뷰 갱신
//                readAllBooks();
//                wishAdapter.notifyDataSetChanged();
                finish();
                break;
            case R.id.btn_cancel:
                finish();
                break;
        }
        finish();

    }


}

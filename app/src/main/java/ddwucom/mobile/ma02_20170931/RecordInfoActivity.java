package ddwucom.mobile.ma02_20170931;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordInfoActivity extends AppCompatActivity {
    final int READ_TO_RECORD = 100;

    EditText etTitle;
    EditText etAuthor;
    EditText etPublisher;
    EditText etParagraph;
    EditText etContext;
    ImageView imgBook;

    String title;
    String author;
    String publisher;
    String imgType;
    String imgUrl;
    String paragraph;
    String context;
    String startDay;
    String endDay;

    RecordDBHelper recordHelper;
    BookDTO bookDto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_info);
        recordHelper = new RecordDBHelper(this);

        etTitle= findViewById(R.id.title);
        etAuthor = findViewById(R.id.author);
        etPublisher = findViewById(R.id.publisher);
        etParagraph = findViewById(R.id.record_paragraph);
        etContext = findViewById(R.id.record_context);
        imgBook = findViewById(R.id.book_img);


        Intent intent = getIntent();
        bookDto = (BookDTO) intent.getSerializableExtra("recordDTO");
        Bitmap img = intent.getParcelableExtra("img");
        title = bookDto.getTitle();
        author = bookDto.getAuthor();
        publisher = bookDto.getPublisher();
        imgType = bookDto.getImgType();
        imgUrl = bookDto.getImgUrl();
        paragraph = bookDto.getParagraph();
        context = bookDto.getContext();


        etTitle.setText(title);
        etAuthor.setText(author);
        etPublisher.setText(publisher);
        etParagraph.setText(paragraph);
        etContext.setText(context);
        imgBook.setImageBitmap(img);

        // tvDescription.setText(bookDto.getDescription());


        // imgBook.setImageBitmap(img);

    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_update:
                long now = System.currentTimeMillis();
                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
                // nowDate 변수에 값을 저장한다.
                String formatDate = sdfNow.format(date);

                /* 읽는 중이던 책에 대한 독서 기록 */
                SQLiteDatabase recordDB = recordHelper.getWritableDatabase();
                ContentValues recordValue = new ContentValues();
                //                   value.put(ReadingDBHelper.COL_IMG, path);
                recordValue.put(RecordDBHelper.COL_TITLE, title);
                recordValue.put(RecordDBHelper.COL_AUTHOR, author);
                recordValue.put(RecordDBHelper.COL_PUBLISHER, publisher);
                recordValue.put(RecordDBHelper.COL_PARAGRAPH, paragraph);
                recordValue.put(RecordDBHelper.COL_IMGTYPE, imgType);
                recordValue.put(RecordDBHelper.COL_IMGURL, imgUrl);
                recordValue.put(RecordDBHelper.COL_CONTEXT, context);
                recordValue.put(RecordDBHelper.COL_STARTDAY, startDay);
                recordValue.put(RecordDBHelper.COL_ENDDAY, formatDate);

                String whereClause = RecordDBHelper.COL_ID + "=?";
                String[] whereArgs = new String[] { String.valueOf(bookDto.get_id()) };
                long count = recordDB.update(RecordDBHelper.TABLE_NAME, recordValue, whereClause, whereArgs);

                if(count > 0){
                    setResult(RESULT_OK, null);
                    recordHelper.close();
                    Toast.makeText(this, "수정 완료", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "수정 실패", Toast.LENGTH_SHORT).show();
                    recordHelper.close();
                    finish();
                }
                recordHelper.close();
                break;
            case R.id.btn_cancel:
                finish();
                break;
        }
        finish();

    }


}

package ddwucom.mobile.ma02_20170931;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordAddActivity extends AppCompatActivity {
    EditText etTitle;
    EditText etAuthor;
    EditText etPublisher;
    EditText etParagraph;
    EditText etContext;
    ImageView imgBook;

    String position;

    String title;
    String author;
    String publisher;
    String imgType;
    String imgUrl;
    String paragraph;
    String context;
    String startDay;

    RecordDBHelper recordHelper;
    ImageFileManager imageFileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_input);
        //DELETE 관련 코드
        recordHelper = new RecordDBHelper(this);
        imageFileManager = new ImageFileManager(this);

        etTitle= findViewById(R.id.title);
        etAuthor = findViewById(R.id.author);
        etPublisher = findViewById(R.id.publisher);
        etParagraph = findViewById(R.id.record_paragraph);
        etContext = findViewById(R.id.record_context);
        imgBook = findViewById(R.id.book_img);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        author = intent.getStringExtra("author");
        publisher = intent.getStringExtra("publisher");
        startDay = intent.getStringExtra("startDay");
        imgType = intent.getStringExtra("imgType");
        imgUrl = intent.getStringExtra("imgUrl");
        position = intent.getStringExtra("position");

        Bitmap img = imageFileManager.getSavedBitmapFromInternal(imgUrl, Integer.parseInt(imgType));
        etTitle.setText(title);
        etAuthor.setText(author);
        etPublisher.setText(publisher);
        imgBook.setImageBitmap(img);

    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_record:
                title = etTitle.getText().toString();
                author = etAuthor.getText().toString();
                publisher = etPublisher.getText().toString();
                paragraph = etParagraph.getText().toString();
                context = etContext.getText().toString();

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

               recordValue.put(RecordDBHelper.COL_TITLE, title);
               recordValue.put(RecordDBHelper.COL_AUTHOR, author);
               recordValue.put(RecordDBHelper.COL_PUBLISHER, publisher);
               recordValue.put(RecordDBHelper.COL_IMGTYPE, imgType);
               recordValue.put(RecordDBHelper.COL_IMGURL, imgUrl);
               recordValue.put(RecordDBHelper.COL_PARAGRAPH, paragraph);
               recordValue.put(RecordDBHelper.COL_CONTEXT, context);
               recordValue.put(RecordDBHelper.COL_STARTDAY, startDay);
               recordValue.put(RecordDBHelper.COL_ENDDAY, formatDate);

               long count = recordDB.insert(RecordDBHelper.TABLE_NAME, null, recordValue);

               if (count > 0) {
                   setResult(RESULT_OK, null);
                   recordHelper.close();
                   Toast.makeText(this, "독서 기록 완료", Toast.LENGTH_SHORT).show();
                   finish();
               } else {
                   Toast.makeText(this, "독서 기록 실패", Toast.LENGTH_SHORT).show();
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

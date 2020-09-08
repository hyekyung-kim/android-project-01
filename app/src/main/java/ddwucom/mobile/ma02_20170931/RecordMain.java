package ddwucom.mobile.ma02_20170931;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RecordMain extends AppCompatActivity {
    final int RECORD_INFO_CODE = 100;
    final int REQ_CODE = 200;
    private ArrayList<BookDTO> bookList;
    private RecordAdapter recordAdapter;
    ListView listView;

    RecordDBHelper helper;
    ImageFileManager imgManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        /* 커스텀 어댑터 */
        listView = findViewById(R.id.recordView);
        bookList = new ArrayList<>();
        helper = new RecordDBHelper(this);
        recordAdapter = new RecordAdapter(this, R.layout.list_record, bookList);
        listView.setAdapter(recordAdapter);

        /* 리스트 항목 롱클릭 리스너-삭제 */
        listView.setOnItemLongClickListener(deleteItemLongClickListener);
        listView.setOnItemClickListener(showInfoClickListener);
    }
    AdapterView.OnItemClickListener showInfoClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    BookDTO dto = bookList.get(position);

                    ImageView imgView = (ImageView)view.findViewById(R.id.img_book);
                    Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();

                    Intent intent = new Intent(RecordMain.this, RecordInfoActivity.class);
                    intent.putExtra("img", bitmap);
                    intent.putExtra("recordDTO", bookList.get(position));
                    startActivityForResult(intent, RECORD_INFO_CODE);
                }
            };
    public void onClick(View v){
        switch(v.getId()){
            case R.id.reading_title:
                Intent reading = new Intent(this, ReadingMain.class);
                startActivity(reading);
                break;
            case R.id.wish_title:
                Intent wish = new Intent(this, WishMain.class);
                startActivity(wish);
                break;
        }
    }

    public void onMenuItemClick(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_reading:
                /* 추가-addActivity 호출 */
                Intent reading_intent = new Intent(this, ReadInputActivity.class);
                startActivityForResult(reading_intent, REQ_CODE);
                break;
            case R.id.menu_wish:
                /* 추가-wish 호출 */
                Intent wish_intent = new Intent(this, WishMain.class);
                startActivityForResult(wish_intent, REQ_CODE);
                break;

        }

    }

    /* 데이터베이스 갱신 */
    protected void onResume() {
        super.onResume();
        readAllBooks();
        recordAdapter.notifyDataSetChanged();
    }

    /* 데이터베이스 읽어오기 */
    private void readAllBooks(){
        bookList.clear();

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + RecordDBHelper.TABLE_NAME, null);

        while(cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex(RecordDBHelper.COL_ID));
            String title = cursor.getString(cursor.getColumnIndex(RecordDBHelper.COL_TITLE));
            String author = cursor.getString(cursor.getColumnIndex(RecordDBHelper.COL_AUTHOR));
            String publisher = cursor.getString(cursor.getColumnIndex(RecordDBHelper.COL_PUBLISHER));
            String imgType = cursor.getString(cursor.getColumnIndex(RecordDBHelper.COL_IMGTYPE));
            String imgUrl = cursor.getString(cursor.getColumnIndex(RecordDBHelper.COL_IMGURL));
            String paragraph = cursor.getString(cursor.getColumnIndex(RecordDBHelper.COL_PARAGRAPH));
            String context = cursor.getString(cursor.getColumnIndex(RecordDBHelper.COL_CONTEXT));
            String startDay = cursor.getString(cursor.getColumnIndex(RecordDBHelper.COL_STARTDAY));
            String endDay = cursor.getString(cursor.getColumnIndex(RecordDBHelper.COL_ENDDAY));

            // Activity에 따라 변경-record: 이미지, 제목, 저자, 출판사, 독서기간, 수정버튼
            bookList.add(new BookDTO(id, title, author, publisher, imgType, imgUrl, paragraph, context, startDay, endDay));
        }
        cursor.close();
        helper.close();
    }

    /* 리스트 항목 롱클릭시-삭제 */
    AdapterView.OnItemLongClickListener deleteItemLongClickListener =
            new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                    //도서 삭제 확인 대화상자
                    final int position = pos;

                    AlertDialog.Builder builder = new AlertDialog.Builder(RecordMain.this);
                    builder.setTitle("도서 삭제");
                    builder.setMessage(bookList.get(position).getTitle() + "에 대한 기록을 삭제하시겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //                        DB 삭제 수행
                            SQLiteDatabase db = helper.getWritableDatabase();
                            String whereClause = RecordDBHelper.COL_ID + "=?";
                            String[] whereArgs = new String[] { String.valueOf(bookList.get(position).get_id()) };
                            db.delete(RecordDBHelper.TABLE_NAME, whereClause, whereArgs);
                            helper.close();

                            //                        새로운 DB 내용으로 리스트뷰 갱신
                            readAllBooks();
                            recordAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("취소", null);
                    builder.show();
                    return true;
                }
            };

}
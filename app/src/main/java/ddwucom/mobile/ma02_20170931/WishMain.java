package ddwucom.mobile.ma02_20170931;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class WishMain extends AppCompatActivity {
    final int WISH_INFO_CODE = 100;
    final int REQ_CODE = 200;
    private ArrayList<BookDTO> bookList;
    private WishAdapter wishAdapter;
    ListView listView;

    WishDBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);

        /* 커스텀 어댑터 */
        listView = findViewById(R.id.wishView);
        bookList = new ArrayList<>();
        helper = new WishDBHelper(this);
        wishAdapter = new WishAdapter(this, R.layout.list_wish, bookList);
        listView.setAdapter(wishAdapter);

        /* 리스트 항목 롱클릭 리스너-삭제 */
        listView.setOnItemLongClickListener(deleteItemLongClickListener);
        listView.setOnItemClickListener(showInfoClickListener);
    }
    public void onClick(View v){
        switch(v.getId()){
            case R.id.reading_title:
                Intent reading = new Intent(this, ReadingMain.class);
                startActivity(reading);
                break;
            case R.id.record_title:
                Intent record = new Intent(this, RecordMain.class);
                startActivity(record);
                break;
        }
    }

    /* 메뉴 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.add_book, menu);
        return true;
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
                Intent wish_intent = new Intent(this, WishInputActivity.class);
                startActivityForResult(wish_intent, REQ_CODE);
                break;
        }

    }
    AdapterView.OnItemClickListener showInfoClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // NaverBookDTO dto = resultList.get(position);

                    ImageView imgView = (ImageView)view.findViewById(R.id.img_book);
                    Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();

                    Intent intent = new Intent(WishMain.this, WishInfoActivity.class);

                    intent.putExtra("wishDTO", bookList.get(position));
                    intent.putExtra("img", bitmap);
                    startActivityForResult(intent, WISH_INFO_CODE);
                }
            };
    /* 데이터베이스 갱신 */
    protected void onResume() {
        super.onResume();
        readAllBooks();
        wishAdapter.notifyDataSetChanged();
    }

    /* 데이터베이스 읽어오기 */
    private void readAllBooks(){
        bookList.clear();

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + WishDBHelper.TABLE_NAME, null);

        while(cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex(WishDBHelper.COL_ID));

            String title = cursor.getString(cursor.getColumnIndex(WishDBHelper.COL_TITLE));
            String author = cursor.getString(cursor.getColumnIndex(WishDBHelper.COL_AUTHOR));
            String publisher = cursor.getString(cursor.getColumnIndex(WishDBHelper.COL_PUBLISHER));
            String imgType = cursor.getString(cursor.getColumnIndex(WishDBHelper.COL_IMGTYPE));
            String imgUrl = cursor.getString(cursor.getColumnIndex(WishDBHelper.COL_IMGURL));
            String start = cursor.getString(cursor.getColumnIndex(WishDBHelper.COL_STARTDAY));

            //Activity에 따라 변경-wish: 이미지, 제목, 저자, 출판사, 읽기버튼
            bookList.add(new BookDTO(id, title, author, publisher, imgType, imgUrl, start));
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(WishMain.this);
                    builder.setTitle("도서 삭제");
                    builder.setMessage("위시 리스트의 도서 " + bookList.get(position).getTitle() + "을(를) 삭제하시겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //                        DB 삭제 수행
                            SQLiteDatabase db = helper.getWritableDatabase();
                            String whereClause = WishDBHelper.COL_ID + "=?" ;
                            String[] whereArgs = new String[] { String.valueOf(bookList.get(position).get_id()) };
                            db.delete(WishDBHelper.TABLE_NAME, whereClause, whereArgs);
                            helper.close();

                            //                        새로운 DB 내용으로 리스트뷰 갱신
                            readAllBooks();
                            wishAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("취소", null);
                    builder.show();
                    return true;
                }
            };

}



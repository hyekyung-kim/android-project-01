package ddwucom.mobile.ma02_20170931;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WishDBHelper extends SQLiteOpenHelper {
    final static String TAG = "WishDBHelper";
    final static String DB_NAME = "wishBooks.db";
    public final static String TABLE_NAME = "wish_table";
    public final static String COL_ID = "_id";
    public final static String COL_IMGTYPE = "imgType";
    public final static String COL_IMGURL = "imgUrl";
    public final static String COL_TITLE = "title";
    public final static String COL_AUTHOR = "author";
    public final static String COL_PUBLISHER = "publisher";
    public final static String COL_STARTDAY = "startDay";


    public WishDBHelper(Context context){
        super(context, DB_NAME, null, 1);
    }
    /* IMGTYPE
     *   0: 검색으로 저장한 이미지
     *   1: 갤러리 저장 이미지
     *   2: 카메라 저장 이미지  --> 구분할 필요 있나,,?
     * */
    @Override
    public void onCreate(SQLiteDatabase db){
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " integer primary key autoincrement, "
                + COL_TITLE + " TEXT, " + COL_AUTHOR + " TEXT, " + COL_PUBLISHER + " TEXT, "
                + COL_IMGTYPE + " TEXT, " + COL_IMGURL + " TEXT, " + COL_STARTDAY + " TEXT)";

        Log.d(TAG, sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}

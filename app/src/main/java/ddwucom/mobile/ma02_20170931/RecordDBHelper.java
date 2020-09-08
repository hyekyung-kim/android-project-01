package ddwucom.mobile.ma02_20170931;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RecordDBHelper extends SQLiteOpenHelper {
    final static String TAG = "RecordDBHelper";
    final static String DB_NAME = "recordBooks.db";
    public final static String TABLE_NAME = "record_table";
    public final static String COL_ID = "_id";
    public final static String COL_IMGTYPE = "imgType";
    public final static String COL_IMGURL = "imgUrl";
    public final static String COL_TITLE = "title";
    public final static String COL_AUTHOR = "author";
    public final static String COL_PUBLISHER = "publisher";
    public final static String COL_PARAGRAPH = "paragraph";
    public final static String COL_CONTEXT = "context";
    public final static String COL_STARTDAY = "startDay";
    public final static String COL_ENDDAY = "endDay";


    public RecordDBHelper(Context context){
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " integer primary key autoincrement, "
                + COL_TITLE + " TEXT, " + COL_AUTHOR + " TEXT, " + COL_PUBLISHER + " TEXT, "+ COL_IMGTYPE + " TEXT, " + COL_IMGURL + " TEXT, "
                + COL_PARAGRAPH + " TEXT, " + COL_CONTEXT + " TEXT, " + COL_STARTDAY + " TEXT, " + COL_ENDDAY + " TEXT)";
// COL_IMG + " BLOB, " +

//        SQLiteStatement s = db.compileStatement("CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " integer primary key autoincrement, " +
//                COL_IMG + " TEXT, " + COL_TITLE + " TEXT, " + COL_AUTHOR + " TEXT, " + COL_PUBLISHER + " TEXT, "
//                + COL_STARTDAY + " TEXT)");
//
//
//        s.bindBlob()
//        SQLiteStatement stmt = db.compileStatement(sql);
//        stmt.execute();


        Log.d(TAG, sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}

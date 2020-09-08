package ddwucom.mobile.ma02_20170931;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReadingInfoActivity extends AppCompatActivity {
    final int READ_TO_RECORD = 100;

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
    ImageFileManager imageFileManager;
    BookDTO bookDto;

    private TextView mText;
    private Button mPickDate;
    private Button mPickTime;

    //년,월,일,시,분
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;

    //Dialog
    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    String time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_info);

        readingDBHelper = new ReadingDBHelper(this);
        imageFileManager = new ImageFileManager(this);

        tvTitle= findViewById(R.id.info_title);
        tvAuthor = findViewById(R.id.info_author);
        tvPublisher = findViewById(R.id.info_publisher);
        imgBook = findViewById(R.id.book_img);

        Intent intent = getIntent();
        bookDto = (BookDTO) intent.getSerializableExtra("readingDTO");
        title = bookDto.getTitle();
        author = bookDto.getAuthor();
        publisher = bookDto.getPublisher();
        imgType = bookDto.getImgType();
        imgUrl = bookDto.getImgUrl();
        startDay = bookDto.getStartDay();

        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvPublisher.setText(publisher);

        Bitmap bitmap = imageFileManager.getSavedBitmapFromInternal(imgUrl, Integer.parseInt(imgType));
        imgBook.setImageBitmap(bitmap);

        //View 참조
        mText = (TextView)findViewById(R.id.date);
        mPickDate = (Button)findViewById(R.id.btn_calendar);
        mPickTime = (Button)findViewById(R.id.btn_time);

        //현재 날짜,시간 가져오기
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        //텍스트뷰 초기화
        updateDisplay();

        //날짜설정 이벤트
        final DatePickerDialog mDialog = new DatePickerDialog(this,
                android.R.style.Theme_DeviceDefault_Light_Dialog, mDateSetListener, mYear, mMonth, mDay);
        mPickDate.setOnClickListener(new View.OnClickListener() { // 달력
            @Override
            public void onClick(View v) {
                mDialog.show();
            }
        });

        //시간설정 이벤트
        final TimePickerDialog tDialog = new TimePickerDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog,
                            mTimeSetListener, 0, 0, false);

        mPickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tDialog.show();
            }
        });

    }

    //DatePicker 리스너
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };
    //TimePicker 리스너
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener(){
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinute = minute;
                    updateDisplay();
                }
            };

    //텍스트뷰 갱신
    private void updateDisplay(){
        time = String.format("%d/%d/%d %d:%d", mYear, mMonth+1, mDay, mHour, mMinute);
        mText.setText(time);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
            case TIME_DIALOG_ID :
                return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, false);
        }

        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //alertDialog.dismiss();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_record: // 기록
                // reading에서 정보 삭제
                SQLiteDatabase readingDB = readingDBHelper.getWritableDatabase();
                String whereClause = readingDBHelper.COL_ID + "=?" ;
                String[] whereArgs = new String[] { String.valueOf(bookDto.get_id()) };
                readingDB.delete(readingDBHelper.TABLE_NAME, whereClause, whereArgs);
                readingDBHelper.close();

                // record로 정보 이동
                ImageView imgView = findViewById(R.id.book_img);
                Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();

                Intent intent = new Intent(ReadingInfoActivity.this, RecordAddActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("author", author);
                intent.putExtra("publisher", publisher);
                intent.putExtra("imgType", imgType);
                intent.putExtra("imgUrl", imgUrl);
                intent.putExtra("startDay", startDay);
                intent.putExtra("position", position);

                startActivityForResult(intent, READ_TO_RECORD);
                break;
            case R.id.btn_cancel: // 취소
                finish();
                break;
            case R.id.btn_alarm: // 도서 반납 알림
                Calendar current_calendar = Calendar.getInstance();

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.YEAR, mYear);
                calendar.set(Calendar.MONTH, mMonth);
                calendar.set(Calendar.DATE, mDay);
                calendar.set(Calendar.HOUR_OF_DAY, mHour);
                calendar.set(Calendar.MINUTE, mMinute);
                calendar.set(Calendar.SECOND, 0);

                Date currentDateTime = calendar.getTime();
                if (current_calendar.after(calendar)) {
                    Toast.makeText(getApplicationContext(), "반납일이 현재 시간 이전입니다.\n다시한번 확인해주세요.", Toast.LENGTH_SHORT).show();
                } else{

                    String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
                    Toast.makeText(getApplicationContext(), date_text + "으로 반납 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                    //  Preference에 설정한 값 저장
                    SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                    editor.putLong("nextNotifyTime", (long)calendar.getTimeInMillis());
                    editor.apply();

                    diaryNotification(calendar);
                }

                break;
        }
        finish();

    }

    /* 알람 관련 설정 */
    void diaryNotification(Calendar calendar)
    {

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("title", title);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);



        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }


        // 부팅 후 실행되는 리시버 사용가능하게 설정
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }
}


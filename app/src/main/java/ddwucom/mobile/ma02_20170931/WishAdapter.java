package ddwucom.mobile.ma02_20170931;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class WishAdapter extends BaseAdapter {

    public static final String TAG = "WishAdapter";

    private Context context;
    private int layout;
    private ArrayList<BookDTO> bookDataList;
    private LayoutInflater inflater;
    private ImageFileManager imageFileManager = null;

    public WishAdapter(Context context, int layout, ArrayList<BookDTO> bookDataList){
        this.context = context;
        this.layout = layout;
        this.bookDataList = bookDataList;
        this.imageFileManager = new ImageFileManager(context);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount(){
        return bookDataList.size();
    }

    @Override
    public Object getItem(int pos){
        return bookDataList.get(pos);
    }

    @Override
    public long getItemId(int pos){
        return bookDataList.get(pos).get_id();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup viewGroup){
        final int position = pos;
        WishViewHolder viewHolder = null;
        View view = convertView;

        if(convertView == null){
            view = inflater.inflate(layout, viewGroup, false);
            viewHolder = new WishViewHolder();

            viewHolder.tvTitle = view.findViewById(R.id.title);
            viewHolder.tvAuthor =  view.findViewById(R.id.author);
            viewHolder.tvPublisher = view.findViewById(R.id.publisher);
            viewHolder.imgView = view.findViewById(R.id.img_book);
            viewHolder.tvStartDay = view.findViewById(R.id.date);
            view.setTag(viewHolder);
        } else {
            viewHolder = (WishViewHolder)view.getTag();
        }

        BookDTO dto = bookDataList.get(position);

        viewHolder.tvTitle.setText(dto.getTitle());
        viewHolder.tvAuthor.setText(dto.getAuthor());
        viewHolder.tvPublisher.setText(dto.getPublisher());
        viewHolder.tvStartDay.setText(dto.getStartDay());


//        dto 에 기록한 이미지 주소를 사용하여 이미지 파일을 읽어오기 수행
        Bitmap savedBitmap = imageFileManager.getSavedBitmapFromInternal(dto.getImgUrl(), Integer.parseInt(dto.getImgType()));
        Log.d("imgUrl", dto.getImgUrl());

//        파일에서 이미지 파일을 읽어온 결과에 따라 파일 이미지 사용 또는 네트워크 다운로드 수행
        if (savedBitmap != null) {
            viewHolder.imgView.setImageBitmap(savedBitmap);
            Log.d(TAG,  "Image loading from file");
        } else {
            viewHolder.imgView.setImageResource(R.mipmap.ic_launcher);  // 이미지를 다운받기 전엔 기본 이미지로 설정
            GetImageAsyncTask task = new GetImageAsyncTask(viewHolder);
            task.execute(dto.getImgUrl());
            Log.d(TAG,  "Image loading from network");
        }
        return view;
    }

    class WishViewHolder{
        ImageView imgView;
        TextView tvTitle;
        TextView tvAuthor;
        TextView tvPublisher;
        TextView tvStartDay;
    }

    /* 책 이미지를 다운로드 후 내부저장소에 파일로 저장하고 이미지뷰에 표시하는 AsyncTask */
    class GetImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

        WishViewHolder viewHolder;
        String imageAddress;

        public GetImageAsyncTask(WishViewHolder holder) {
            viewHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            imageAddress = params[0];
            Log.i(TAG, imageAddress);
            Bitmap result = downloadImage(imageAddress);
            return result;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {

            /*작성할 부분*/
            /*네트워크에서 다운 받은 이미지 파일을 ImageFileManager 를 사용하여
             내부저장소에 저장 다운받은 bitmap 을 이미지뷰에 지정*/

            /*네트워크를 통해 bitmap 을 정상적으로 받아왔을 경우 수행
             * 서버로부터 정상적으로 이미지 다운로드를 못했을 경우 null 이 반환되므로 기본 설정 이미지가 계속 유지됨*/
            if (bitmap != null) {
                // 다운로드한 bitmap 을 내부저장소에 저장
                imageFileManager.saveBitmapToInternal(bitmap, imageAddress);
                // 다운로드한 bitmap 을 이미지뷰에 표시
                viewHolder.imgView.setImageBitmap(bitmap);
            }
        }



        /* 이미지를 다운로드하기 위한 네트워크 관련 메소드 */

        /* 네트워크 환경 조사 */
        private boolean isOnline() {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }

        /* 주소를 전달받아 bitmap 다운로드 후 반환 */
        private Bitmap downloadImage(String address) {
            HttpURLConnection conn = null;
            InputStream stream = null;
            Bitmap result = null;

            try {
                URL url = new URL(address);
                conn = (HttpURLConnection)url.openConnection();
                stream = getNetworkConnection(conn);
                result = readStreamToBitmap(stream);
                if (stream != null) stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            return result;
        }


        /* URLConnection 을 전달받아 연결정보 설정 후 연결, 연결 후 수신한 InputStream 반환 */
        private InputStream getNetworkConnection(HttpURLConnection conn) throws Exception {
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + conn.getResponseCode());
            }

            return conn.getInputStream();
        }


        /* InputStream을 전달받아 비트맵으로 변환 후 반환 */
        private Bitmap readStreamToBitmap(InputStream stream) {
            return BitmapFactory.decodeStream(stream);
        }

    }
}

package ddwucom.mobile.ma02_20170931;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SearchMain extends AppCompatActivity {

    final int SEARCH_INFO_CODE = 100;

    public static final String TAG = "SearchMain";

    EditText etTarget;
    ListView lvList;
    String apiAddress;

    String query;

    SearchAdapter adapter;
    ArrayList<NaverBookDTO> resultList;
    BookXmlParser parser;
    ImageFileManager imgManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etTarget = findViewById(R.id.etTarget);
        lvList = findViewById(R.id.lvList);

        resultList = new ArrayList();
        adapter = new SearchAdapter(this, R.layout.list_search, resultList);
        lvList.setAdapter(adapter);

        apiAddress = getResources().getString(R.string.naverBook_api_url);
        parser = new BookXmlParser();
        imgManager = new ImageFileManager(this);

        //lvList.setOnItemLongClickListener(saveImageLongClickListener);

        /*
        * 리스트 뷰 클릭시 책에 대한 상세 정보 제공
        * 읽기/위시 추가 버튼
        * */
        lvList.setOnItemClickListener(showInfoClickListener);

    }
    AdapterView.OnItemClickListener showInfoClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    /* 리스트뷰에 보여지고 클릭된 이미지는 내부저장소에 있을 것임 */
                    NaverBookDTO dto = resultList.get(position);

                    String imgUrl = dto.getImageLink();
                    String imgName = imgManager.getFileNameFromUrl(imgUrl);

                            // TODO: 받아온 이미지 찾아서 intent로 넘기기
                    ImageView imgView = (ImageView)view.findViewById(R.id.ivImage);
                    Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();

                    Intent intent = new Intent(SearchMain.this, SearchInfoActivity.class);
                    intent.putExtra("NaverBookDTO", resultList.get(position));
                    intent.putExtra("url", imgUrl);
                    intent.putExtra("imgName", imgName);
                    intent.putExtra("img", bitmap);
                    startActivityForResult(intent, SEARCH_INFO_CODE);
                }
            };

    /* TODO: 이미지 저장 말고 다른 기능으로 바꾸기*/
    AdapterView.OnItemLongClickListener saveImageLongClickListener =
            new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    /* 작성할 부분 */
                    /*롱클릭한 항목의 이미지 주소를 가져와 내부 메모리에 지정한 이미지 파일을 외부저장소로 이동
                     * ImageFileManager 의 이동 기능 사용
                     * 이동을 성공할 경우 파일 명, 실패했을 경우 null 을 반환하므로 해당 값에 따라 Toast 출력*/
                    NaverBookDTO dto = resultList.get(position);

                    if (dto.getImageFileName() != null) {       // 외부저장소에 저장한 파일명이 기록되어 있을 경우
                        Toast.makeText(SearchMain.this, "Already moved!", Toast.LENGTH_SHORT).show();
                    } else {
                        String savedName = imgManager.moveStorage(dto.getImageLink());
                        Log.i(TAG, "Saved file name: " + savedName);
                        if (savedName != null) {
                            dto.setImageFileName(savedName);    // 외부저장소에 저장한 파일명을 dto 에 저장
                            Toast.makeText(SearchMain.this, savedName + " is saved to Ext.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SearchMain.this, "Save failure!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    return true;
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imgManager.clearSaveFilesOnInternal();
    }


    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnLibSearch:
                query = etTarget.getText().toString();
                try {
                    new NaverAsyncTask().execute(apiAddress + URLEncoder.encode(query, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    class NaverAsyncTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(SearchMain.this, "Wait", "Downloading...");
        }


        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            String result = downloadContents(address);
            if (result == null) return "Error!";
            return result;
        }


        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, result);

            resultList = parser.parse(result);      // 파싱 수행

            adapter.setList(resultList);    // Adapter 에 파싱 결과를 담고 있는 ArrayList 를 설정
            adapter.notifyDataSetChanged();

            progressDlg.dismiss();
        }
        /* 네트워크 관련 메소드 */

        /* 네트워크 환경 조사 */
        private boolean isOnline() {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }


        /* URLConnection 을 전달받아 연결정보 설정 후 연결, 연결 후 수신한 InputStream 반환
         * 네이버용을 수정 - ClientID, ClientSeceret 추가 strings.xml 에서 읽어옴*/
        private InputStream getNetworkConnection(HttpURLConnection conn) throws Exception {

            // 클라이언트 아이디 및 시크릿 그리고 요청 URL 선언
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("X-Naver-Client-Id", getResources().getString(R.string.naverBook_id));
            conn.setRequestProperty("X-Naver-Client-Secret", getResources().getString(R.string.naverBook_secret));

            if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + conn.getResponseCode());
            }

            return conn.getInputStream();
        }


        /* InputStream을 전달받아 문자열로 변환 후 반환 */
        protected String readStreamToString(InputStream stream){
            StringBuilder result = new StringBuilder();

            try {
                InputStreamReader inputStreamReader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String readLine = bufferedReader.readLine();

                while (readLine != null) {
                    result.append(readLine + "\n");
                    readLine = bufferedReader.readLine();
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }


        /* 주소(address)에 접속하여 문자열 데이터를 수신한 후 반환 */
        protected String downloadContents(String address) {
            HttpURLConnection conn = null;
            InputStream stream = null;
            String result = null;

            try {
                URL url = new URL(address);
                conn = (HttpURLConnection)url.openConnection();
                stream = getNetworkConnection(conn);
                result = readStreamToString(stream);
                if (stream != null) stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            return result;
        }

    }

}

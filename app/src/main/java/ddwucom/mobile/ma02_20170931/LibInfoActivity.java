package ddwucom.mobile.ma02_20170931;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LibInfoActivity extends AppCompatActivity {

    TextView tvName;
    TextView tvPhone;
    TextView tvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lib_info);

        Intent intent = getIntent();

        tvName = findViewById(R.id.name);
        tvPhone = findViewById(R.id.phone);
        tvAddress = findViewById(R.id.address);

        tvName.setText(intent.getStringExtra("name"));
        tvPhone.setText(intent.getStringExtra("phone"));
        tvAddress.setText(intent.getStringExtra("address"));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                finish();
                break;
            case R.id.btn_cancel:
                finish();
                break;
        }
    }
}

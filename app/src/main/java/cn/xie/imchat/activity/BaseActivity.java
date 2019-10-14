package cn.xie.imchat.activity;

import androidx.appcompat.app.AppCompatActivity;
import cn.xie.imchat.R;

import android.os.Bundle;
import android.view.Window;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }
}

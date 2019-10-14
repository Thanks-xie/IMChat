package cn.xie.imchat.activity;

import androidx.appcompat.app.AppCompatActivity;
import cn.xie.imchat.R;
import cn.xie.imchat.utils.Util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends BaseActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this;
        handler.postDelayed(task, 3000);
    }

    private Handler handler = new Handler(){};

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if (Util.getLoginStatic(context)){
                Intent intent = new Intent(context,MainActivity.class);
                startActivity(intent);
            }else {
                Intent intent = new Intent(context,LoginActivity.class);
                startActivity(intent);
            }
            finish();
            handler.removeCallbacks(task);

        }
    };
}

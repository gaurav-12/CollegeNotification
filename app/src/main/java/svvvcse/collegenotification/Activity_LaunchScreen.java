package svvvcse.collegenotification;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class Activity_LaunchScreen extends AppCompatActivity {

    private static final int SPLASH_DELAY_TIME = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Activity_LaunchScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_DELAY_TIME);
    }
}

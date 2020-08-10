package com.dnpa.finalproject.depressionsafetytracking.Splash;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.dnpa.finalproject.depressionsafetytracking.Login.LoginActivity;
import com.dnpa.finalproject.depressionsafetytracking.R;

public class SplashScreenView extends AppCompatActivity {

    private final int SPLASH_DURATION = 2500;   //SPLASH Duration (MS)
    Animation topAnim;
    ImageView image;
    TextView logo, slogan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_activity);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.animation);
        image = findViewById(R.id.imageView);
        logo = findViewById(R.id.textView);
        slogan = findViewById(R.id.textView2);
        image.setAnimation(topAnim);
        logo.setAnimation(topAnim);
        slogan.setAnimation(topAnim);

        new Handler().postDelayed(new Runnable(){
            public void run(){
                Intent intent = new Intent(SplashScreenView.this, LoginActivity.class);
                // Attach all the elements those you want to animate in design
                Pair[]pairs=new Pair[2];
                pairs[0]=new Pair<View, String>(image,"logo_image");
                pairs[1]=new Pair<View, String>(logo,"logo_text");
                //wrap the call in API level 21 or higher
                if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(SplashScreenView.this,pairs);
                    startActivity(intent,options.toBundle());
                }
            };
        }, SPLASH_DURATION);
    }
}

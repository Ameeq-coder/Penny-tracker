package com.example.pennytrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.pennytrack.Activites.Signup;
import com.example.pennytrack.Activites.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class onboard extends AppCompatActivity {


    ViewPager viewPager;
    LinearLayout mdotlayout;
    Button backbtn,nextbtn,skipbtn;
    TextView[] dots;
    ViewPagerAdapter viewPagerAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);
        backbtn=findViewById(R.id.back);
        nextbtn=findViewById(R.id.next);
        skipbtn=findViewById(R.id.skip);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getitem(0)>0){
                    viewPager.setCurrentItem(getitem(-1),true);
                }

            }
        });
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getitem(0)<2){
                    viewPager.setCurrentItem(getitem(1),true);
                }else {
                    Intent intent=new Intent(onboard.this, Signup.class);
                    startActivity(intent);
                    finish();
                }

            }
        });
        skipbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(onboard.this,Signup.class);
                startActivity(intent);
//                finish();

            }
        });
        viewPager=findViewById(R.id.slidepage);
        mdotlayout=findViewById(R.id.sliderindicator);
        viewPagerAdapter=new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        setUpIndicator(0);
        viewPager.addOnPageChangeListener(viewListner);
    }

    public void setUpIndicator(int postion){
        dots=new TextView[3];
        mdotlayout.removeAllViews();
        for (int i=0;i<dots.length;i++){
            dots[i]=new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.inactive,getApplicationContext().getTheme()));
            mdotlayout.addView(dots[i]);
        }
        dots[postion].setTextColor(getResources().getColor(R.color.active,getApplicationContext().getTheme()));
    }
    ViewPager.OnPageChangeListener viewListner=new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }
        @Override
        public void onPageSelected(int position) {
            setUpIndicator(position);
            if (position>0){
                backbtn.setVisibility(View.VISIBLE);
            }else {
                backbtn.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    private int getitem(int i){

        return viewPager.getCurrentItem() +i;
    }


    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent= new Intent(this, TabLayout.class);
            startActivity(intent);
        }else{
            //No User is Logged in
        }
    }


}
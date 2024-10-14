package com.example.pennytrack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ViewPagerAdapter extends PagerAdapter {
    Context context;

    int images[] = {
            R.drawable.first,
            R.drawable.second,
            R.drawable.third
    };

    int heading[] = {
            R.string.heading_one,
            R.string.heading_two,
            R.string.heading_three
    };

    int description[] = {
            R.string.desc_one,
            R.string.desc_two,
            R.string.desc_three
    };

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return heading.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout, container, false);

        // Find the ImageView and TextViews
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView slideImage = view.findViewById(R.id.onboardingicon);
        TextView slidetext = view.findViewById(R.id.texttitle);
        TextView slidedescription = view.findViewById(R.id.desctitle);

        // Set the image, heading, and description for the current position
        slideImage.setImageResource(images[position]);
        slidetext.setText(heading[position]);
        slidedescription.setText(description[position]);

        // Add the view to the container
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }
}
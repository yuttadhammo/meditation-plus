package org.sirimangalo.meditationplus;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by noah on 10/16/14.
 */
public class ImageAdapter extends BaseAdapter {
    private final String[] smilies;
    private Context context;

    public ImageAdapter(Context c) {
        context = c;
        smilies = c.getResources().getStringArray(R.array.smily_files);
    }

    public int getCount() {
        return smilies.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(60, 60));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(context.getResources().getIdentifier(smilies[position], "drawable", context.getPackageName()));
        return imageView;
    }

}

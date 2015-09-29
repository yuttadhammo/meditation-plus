/*
    This file is part of MeditationPlus.

    MeditationPlus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MeditationPlus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MeditationPlus.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.sirimangalo.meditationplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by noah on 10/15/14.
 */
public class Utils {
    public static String time2Ago(int then) {
        long nowL = System.currentTimeMillis()/1000;
        int now = (int) nowL;

        int ela = now - then;

        String time = "";
        if (ela < 5)
            time = "now";
        else if(ela < 60)
            time = ela + "s ago";
        else if(ela < 60*60)
            time = (int) Math.floor(ela/60) + "m";
        else if(ela < 60*60*24)
            time = (int) Math.floor(ela/60/60) + "h";
        else
            time = (int) Math.floor(ela/60/60/24) + "d";
        return time;
    }

    public static SpannableString replaceSmilies(Context context, String message, int alpha) {
        String[] tags = context.getResources().getStringArray(R.array.smily_tags);
        String[] files = context.getResources().getStringArray(R.array.smily_files);

        SpannableString span = new SpannableString(message);

        for(int i = 0; i < tags.length; i++){

            int id = -1;

            int index = message.indexOf(tags[i]);
            while (index >= 0) {
                if(id == -1)
                    id = context.getResources().getIdentifier(files[i],"drawable",context.getPackageName());

                Drawable d = context.getResources().getDrawable(id);
                d.setBounds(0, 0, 42,42);
                d.setAlpha(alpha);
                ImageSpan image = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                span.setSpan(image, index, index+tags[i].length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                index = message.indexOf(tags[i], index + tags[i].length());
            }

        }

        return span;
    }

    public static void openHTM(Context context) {
        String url = "http://htm.sirimangalo.org/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    public static String makeRedGreen(int percent, boolean dark) {
        String green = "";

        String red = "";

        int max = 255;
        int min = 200;
        if(dark) {
            max = 100;
            min = 0;
        }

        String maxColor = Integer.toHexString(max);
        String blue = Integer.toHexString(min);

        if(percent > 50) { // becoming green
            red = Integer.toHexString(max - ((percent-50)*(max-min)/50));
            green = maxColor;

        }
        else { // becoming red
            green = Integer.toHexString(min + ((percent)*(max-min)/50));
            red = maxColor;
        }
        return (red.length() == 1?"0":"") + red + (green.length() == 1?"0":"") + green + (blue.length() == 1?"0":"") + blue;
    }

    public static SpannableString createProfileSpan(final ActivityMain context, int start, int end, final String username, SpannableString span) {
        span.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan click = new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                context.showProfile(username);
            }

        };
        span.setSpan(click, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        span.setSpan(new UnderlineSpan() {
            public void updateDrawState(TextPaint tp) {
                tp.setUnderlineText(false);
            }
        }, start, end, 0);
        return span;
    }

    public static String getMD5Hash (String message) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            byte[] array = (md.digest(message.getBytes("CP1252")));

            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray
                        & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

/*

max 255, min 200

0: red 255, green 200,  blue 200
50: red 255, green 255,  blue 200
100: red 200, green 255,  blue 200 varColor: 255 - 55


 */
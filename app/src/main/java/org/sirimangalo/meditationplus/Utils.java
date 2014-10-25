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
import android.net.Uri;

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
            time = (int) Math.floor(ela/60) + "m ago";
        else if(ela < 60*60*24)
            time = (int) Math.floor(ela/60/60) + "h ago";
        else if(ela < 60*60*24*7)
            time = (int) Math.floor(ela/60/60/24) + "d ago";
        else {
            Date date = new Date(then*1000);
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            time = df.format(new Date(0));
        }
        return time;
    }

    public static String replaceSmilies(Context context, String message) {
        String[] tags = context.getResources().getStringArray(R.array.smily_tags);
        String[] files = context.getResources().getStringArray(R.array.smily_files);

        for(int i = 0; i < tags.length; i++){
            message = message.replace(tags[i],"<img src=\""+files[i]+"\">");
        }

        return message;
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
}

/*

max 255, min 200

0: red 255, green 200,  blue 200
50: red 255, green 255,  blue 200
100: red 200, green 255,  blue 200 varColor: 255 - 55


 */
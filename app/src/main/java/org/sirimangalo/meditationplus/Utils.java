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

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by noah on 10/15/14.
 */
public class Utils {
    public static String time2Ago(int then) {
        int now = (int) Math.round(new Date().getTime()/1000);

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
}

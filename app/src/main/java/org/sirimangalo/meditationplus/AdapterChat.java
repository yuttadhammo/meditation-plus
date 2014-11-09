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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noah on 10/15/14.
 */
public class AdapterChat extends ArrayAdapter<JSONObject> {


    private final List<JSONObject> values;
    private final ActivityMain context;
    private final ArrayList<String> users;
    private final String loggedUser;

    private String TAG = "AdapterChat";

    public AdapterChat(ActivityMain _context, int resource, List<JSONObject> items) {
        super(_context, resource, items);
        this.values = items;

        users = new ArrayList<String>();

        for(JSONObject i : items) {
            try{
                users.add(i.getString("username"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        context = _context;
        loggedUser = PreferenceManager.getDefaultSharedPreferences(context).getString("username",null);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_chat, parent, false);
        } else {
            rowView = convertView;
        }

        JSONObject p = values.get(position);
        try {
            int then = Integer.parseInt(p.getString("time"));
            long nowL = System.currentTimeMillis()/1000;
            int now = (int) nowL;

            int ela = now - then;
            int day = 60*60*24;
            ela = ela > day ? day : ela;
            int intColor = 255 - Math.round((float)ela*255/day);
            intColor = intColor > 100 ? intColor : 100;
            String hexTransparency = Integer.toHexString(intColor);
            hexTransparency = hexTransparency.length() > 1 ? hexTransparency : "0"+hexTransparency;
            String hexColor = "#"+hexTransparency+"000000";
            int transparency = Color.parseColor(hexColor);

            TextView time = (TextView) rowView.findViewById(R.id.time);
            if (time != null) {
                String ts = null;
                ts = Utils.time2Ago(then);
                time.setText(ts);
                time.setTextColor(transparency);
            }

            TextView mess = (TextView) rowView.findViewById(R.id.message);
            if (mess != null) {

                final String username = p.getString("username");

                String messageString = p.getString("message");

                messageString = Html.fromHtml(messageString).toString();

                SpannableString messageSpan = Utils.replaceSmilies(context, messageString, intColor);

                if(messageString.contains("@"+loggedUser)) {
                    int index = messageString.indexOf("@"+loggedUser);
                    messageSpan.setSpan(new StyleSpan(Typeface.BOLD), index, index+loggedUser.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    messageSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#" + hexTransparency + "990000")), index, index+loggedUser.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                for(String user : users) {
                    if(!user.equals(loggedUser) && messageString.contains("@"+user)) {
                        int index = messageString.indexOf("@" + user);
                        messageSpan = Utils.createProfileSpan(context, index, index+user.length()+1, user, messageSpan);
                        messageSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#" + hexTransparency + "009900")), index, index+user.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                Spannable userSpan = Utils.createProfileSpan(context, 0, username.length(), username, new SpannableString(username+": "));

                if(p.getString("me").equals("true"))
                    userSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#" + hexTransparency + "0000FF")), 0, userSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                else
                    userSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#" + hexTransparency + "000000")), 0, userSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


                CharSequence full = TextUtils.concat(userSpan,messageSpan);

                mess.setTextColor(transparency);
                mess.setText(full);
                mess.setMovementMethod(LinkMovementMethod.getInstance());

            }

            mess.setTag(p.getString("cid"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;

    }

}

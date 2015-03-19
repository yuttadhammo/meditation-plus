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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noah on 10/15/14.
 */

public class AdapterMed extends ArrayAdapter<JSONObject> {


    private final List<JSONObject> values;
    private final ActivityMain context;
    private final Handler postHandler;

    private double MAX_AGE = 60*60*6; // 3 hours * 2

    private String TAG = "AdapterMed";

    public AdapterMed(ActivityMain _context, int resource, List<JSONObject> items, Handler _postHandler) {
        super(_context, resource, items);
        this.values = items;
        context = _context;
        postHandler = _postHandler;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_med, parent, false);
        } else {
            rowView = convertView;
        }

        JSONObject p = values.get(position);

        TextView walk = (TextView) rowView.findViewById(R.id.one_walking);
        TextView sit = (TextView) rowView.findViewById(R.id.one_sitting);
        ImageView status = (ImageView) rowView.findViewById(R.id.one_status);
        TextView name = (TextView) rowView.findViewById(R.id.one_med);
        ImageView flag = (ImageView) rowView.findViewById(R.id.one_flag);

        View anuView = rowView.findViewById(R.id.anumodana_shell);
        TextView anuText = (TextView) rowView.findViewById(R.id.anumodana);

        try {
            String wo = p.getString("walking");
            String so = p.getString("sitting");
            int wi = Integer.parseInt(wo);
            int si = Integer.parseInt(so);
            int ti = Integer.parseInt(p.getString("start"));
            int ei = Integer.parseInt(p.getString("end"));

            long nowL = System.currentTimeMillis()/1000;

            int now = (int) nowL;

            boolean finished = false;


            String ws = "0";
            String ss = "0";

            if(ei > now) {

                float secs = now - ti;

                if (secs > wi * 60 || wi == 0) { //walking done
                    float ssecs = (int) (secs - (wi * 60));
                    if (ssecs < si * 60) // still sitting
                        ss = Integer.toString((int) Math.floor(si - ssecs / 60));
                    status.setImageResource(R.drawable.sitting_icon);
                }
                else { // still walking
                    ws = Integer.toString((int) Math.floor(wi - secs / 60));
                    ss = so;
                    status.setImageResource(R.drawable.walking_icon);
                }

                ws += "/" + wo;
                ss += "/" + so;
            }
            else {
                ws = wo;
                ss = so;

                double age = 1-(now - ei)/(MAX_AGE*1.5);

                String ageColor = Integer.toHexString((int) (255*age));

                if(ageColor.length() == 1)
                    ageColor = "0"+ageColor;

                int alpha = Color.parseColor("#"+ageColor+"000000");

                walk.setTextColor(alpha);
                sit.setTextColor(alpha);
                name.setTextColor(alpha);
                status.setAlpha((float) age);
                flag.setAlpha((float) age);
                // anuView.setAlpha((float)age);
                // anuText.setAlpha((float)age);

            }



            walk.setText(ws);
            sit.setText(ss);

            if(p.has("country")) {
                int id = context.getResources().getIdentifier("flag_"+p.getString("country").toLowerCase(),"drawable",context.getPackageName());
                flag.setImageResource(id);
                flag.setVisibility(View.VISIBLE);
            }

            final String username = p.getString("username");
            final String edit = p.getString("can_edit");
            name.setText(username);

            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.showProfile(username);
                }
            });

            String type = p.getString("type");
            if("love".equals(type))
                status.setImageResource(R.drawable.love_icon);

            String anu = p.getString("anumodana");
            if(!anu.equals("0"))
                anuText.setText(anu);

            boolean isMe = p.getString("me").equals("true");

            if(p.getString("anu_me").equals("1") || isMe) {
                anuText.setTextColor(0xFF00BB00);
                anuText.setTypeface(null, Typeface.BOLD);
            }
            else {
                final String sid = p.getString("sid");

                anuView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "anu clicked");
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        String loggedUsername = prefs.getString("username", "");
                        String loginToken = prefs.getString("login_token", "");
                        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                        nvp.add(new BasicNameValuePair("form_id", "anumed_" + sid));
                        nvp.add(new BasicNameValuePair("login_token", loginToken));
                        nvp.add(new BasicNameValuePair("submit", "Refresh"));
                        nvp.add(new BasicNameValuePair("username", loggedUsername));
                        nvp.add(new BasicNameValuePair("source", "android"));
                        PostTaskRunner postTask = new PostTaskRunner(postHandler, context);
                        postTask.doPostTask(nvp);

                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowView;
    }
}


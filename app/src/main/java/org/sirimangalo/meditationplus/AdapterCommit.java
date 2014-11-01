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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by noah on 10/15/14.
 */
public class AdapterCommit extends ArrayAdapter<JSONObject> {


    private final List<JSONObject> values;
    private final ActivityMain context;
    private final String loggedUser;

    private String[] dow = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
    private String TAG = "AdapterCommit";

    public AdapterCommit(ActivityMain _context, int resource, List<JSONObject> items, String _loggedUser) {
        super(_context, resource, items);
        this.values = items;
        context = _context;
        loggedUser = _loggedUser;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_item_commit, parent, false);

        final View shell = rowView.findViewById(R.id.detail_shell);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean visible = shell.getVisibility() ==  View.VISIBLE;

                shell.setVisibility( visible ? View.GONE : View.VISIBLE);

                context.setCommitVisible(position, !visible);

            }
        });

        final JSONObject p = values.get(position);

        TextView title = (TextView) rowView.findViewById(R.id.title);
        TextView descV = (TextView) rowView.findViewById(R.id.desc);
        TextView defV = (TextView) rowView.findViewById(R.id.def);
        TextView usersV = (TextView) rowView.findViewById(R.id.users);
        TextView youV = (TextView) rowView.findViewById(R.id.you);

        try {

            if(p.getBoolean("open"))
                shell.setVisibility(View.VISIBLE);

            title.setText(p.getString("title"));
            descV.setText(p.getString("description"));

            String length = p.getString("length");
            String time = p.getString("time");
            final String cid = p.getString("cid");

            String def = "";

            boolean repeat = false;

            if(length.indexOf(":") > 0) {
                repeat = true;
                String[] lengtha = length.split(":");
                def += lengtha[0]+" minutes walking and "+ lengtha[1] + " minutes sitting";
            }
            else
                def += length+" minutes total meditation";

            String period = p.getString("period");

            String day = p.getString("day");

            if(period.equals("daily")) {
                if(repeat)
                    def += " every day";
                else
                    def += " per day";
            }
            else if(period.equals("weekly")){
                if(repeat)
                    def += " every "+ dow[Integer.parseInt(day)];
                else
                    def += " per week";
            }
            else if(period.equals("monthly")){
                if(repeat)
                    def += " on the "+ day +(day.substring(day.length()-1).equals("1") ?"st":(day.substring(day.length()-1).equals("2")?"nd":(day.substring(day.length()-1).equals("3")?"rd":"th")))+" day of the month";
                else
                    def += " per month";
            }
            else if(period.equals("yearly")){
                if(repeat)
                    def += " on the "+ day +(day.substring(day.length()-1).equals("1") ?"st":(day.substring(day.length()-1).equals("2")?"nd":(day.substring(day.length()-1).equals("3")?"rd":"th")))+" day of the year";
                else
                    def += " per year";
            }

            if(!time.equals("any")) {
                Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                utc.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
                utc.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));

                Calendar here = Calendar.getInstance();
                here.setTimeInMillis(utc.getTimeInMillis());

                int hours = here.get(Calendar.HOUR_OF_DAY);
                int minutes = here.get(Calendar.MINUTE);

                def += " at "+(time.length() == 4?"0":"")+time.replace(":","")+"h UTC <i>("+(hours > 12 ? hours-12:hours)+":"+((minutes<10?"0":"")+minutes)+" "+(hours>11 && hours<24?"PM":"AM")+" your time)</i>";
            }


            defV.setText(Html.fromHtml(def));

            JSONObject usersJ = p.getJSONObject("users");

            ArrayList<String> committedArray = new ArrayList<String>();

            // collect into array

            for(int i = 0; i < usersJ.names().length(); i++) {
                try {
                    String j = usersJ.names().getString(i);
                    String k = j;
//                    if(j.equals(p.getString("creator")))
//                        k = "["+j+"]";
                    committedArray.add(k);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String text = context.getString(R.string.committed)+" ";

            // add spans

            int committed = -1;

            int pos = text.length(); // start after "Committed: "

            text += TextUtils.join(", ", committedArray);
            Spannable span = new SpannableString(text);

            span.setSpan(new StyleSpan(Typeface.BOLD), 0, pos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // bold the "Online: "

            for(int i = 0; i < committedArray.size(); i++) {
                try {

                    final String oneCom = committedArray.get(i);
                    String userCom = usersJ.getString(oneCom);
                    //String userCom = usersJ.getString(oneCom.replace("[", "").replace("]", ""));

                    //if(oneCom.replace("[","").replace("]","").equals(loggedUser))
                    if(oneCom.equals(loggedUser))
                        committed = Integer.parseInt(userCom);

                    int end = pos+oneCom.length();

                    ClickableSpan clickable = new ClickableSpan() {

                        @Override
                        public void onClick(View widget) {
                            context.showProfile(oneCom);
                        }

                    };
                    span.setSpan(clickable, pos, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    span.setSpan(new UnderlineSpan() {
                        public void updateDrawState(TextPaint tp) {
                            tp.setUnderlineText(false);
                        }
                    }, pos, end, 0);

                    String color = Utils.makeRedGreen(Integer.parseInt(userCom), true);

                    span.setSpan(new ForegroundColorSpan(Color.parseColor("#FF"+color)), pos, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    pos += oneCom.length() + 2;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            usersV.setText(span);
            usersV.setMovementMethod(new LinkMovementMethod());

            if(loggedUser != null && loggedUser.length() > 0) {
                LinearLayout bl = (LinearLayout) rowView.findViewById(R.id.commit_buttons);
                if(!usersJ.has(loggedUser)) {
                    Button commitB = new Button(context);
                    commitB.setId(R.id.commit_button);
                    commitB.setText(R.string.commit);
                    commitB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                            nvp.add(new BasicNameValuePair("full_update", "true"));

                            context.doSubmit("commitform_"+cid,nvp, true);
                        }
                    });
                    bl.addView(commitB);
                }
                else {
                    Button commitB = new Button(context);
                    commitB.setId(R.id.uncommit_button);
                    commitB.setText(R.string.uncommit);
                    commitB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                            nvp.add(new BasicNameValuePair("full_update", "true"));

                            context.doSubmit("uncommitform_"+cid,nvp, true);
                        }
                    });
                    bl.addView(commitB);
                }

                if(loggedUser.equals(p.getString("creator")) || context.isAdmin) {
                    Button commitB2 = new Button(context);
                    commitB2.setId(R.id.edit_commit_button);
                    commitB2.setText(R.string.edit);
                    commitB2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(context,ActivityCommit.class);
                            i.putExtra("commitment",p.toString());
                            context.startActivity(i);
                        }
                    });
                    bl.addView(commitB2);

                    Button commitB3 = new Button(context);
                    commitB3.setId(R.id.uncommit_button);
                    commitB3.setText(R.string.delete);
                    commitB3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                            nvp.add(new BasicNameValuePair("full_update", "true"));

                            context.doSubmit("delcommitform_"+cid,nvp, true);
                        }
                    });
                    bl.addView(commitB3);
                }

            }

            if(committed > -1) {
                int color = Color.parseColor("#FF"+Utils.makeRedGreen(committed, false));
                rowView.setBackgroundColor(color);
            }

            if(committed != -1) {
                youV.setText(String.format(context.getString(R.string.you_commit_x), committed));
                youV.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowView;
    }
}


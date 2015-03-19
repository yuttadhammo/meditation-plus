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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noah on 10/15/14.
 */
public class AdapterSchedule extends ArrayAdapter<JSONObject> {


    private final List<JSONObject> values;
    private final Activity context;

    private String TAG = "AdapterSchedule";

    private boolean isAdmin;

    public AdapterSchedule(Activity _context, List<JSONObject> items, boolean _isAdmin) {
        super(_context, R.layout.list_item_schedule, items);
        values = items;

        isAdmin = _isAdmin;

        context = _context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_schedule, parent, false);
        } else {
            rowView = convertView;
        }

        JSONObject p = values.get(position);
        try {
            final String title = p.getString("title");
            final String desc = p.getString("description");
            final String time = p.getString("time");
            final String sid = p.getString("id");

            final TextView timeView = (TextView) rowView.findViewById(R.id.time);
            timeView.setText(time+"h");

            final TextView titleView = (TextView) rowView.findViewById(R.id.title);
            titleView.setText(title);

            final TextView descView = (TextView) rowView.findViewById(R.id.desc);
            if (descView != null) {
                descView.setText(desc);
                Linkify.addLinks(descView,Linkify.ALL);
                descView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if(isAdmin){
                Button editB = (Button) rowView.findViewById(R.id.edit);
                editB.setVisibility(View.VISIBLE);
                editB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ActivityScheduleEdit.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        intent.putExtra("title", title);
                        intent.putExtra("desc", desc);
                        intent.putExtra("time", time);
                        intent.putExtra("sid", sid);
                        context.startActivityForResult(intent, 1);
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;

    }

}

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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by noah on 10/15/14.
 */

public class MedAdapter extends ArrayAdapter<JSONObject> {


    private final List<JSONObject> values;
    private final MainActivity context;

    private String TAG = "MedAdapter";

    public MedAdapter(MainActivity _context, int resource, List<JSONObject> items) {
        super(_context, resource, items);
        this.values = items;
        context = _context;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.med_list_item, parent, false);

        JSONObject p = values.get(position);

        TextView walk = (TextView) rowView.findViewById(R.id.one_walking);
        TextView sit = (TextView) rowView.findViewById(R.id.one_sitting);
        TextView name = (TextView) rowView.findViewById(R.id.one_med);
        ImageView flag = (ImageView) rowView.findViewById(R.id.one_flag);
        LinearLayout shell = (LinearLayout) rowView.findViewById(R.id.name_shell);

        try {
            String wo = p.getString("walking");
            String so = p.getString("sitting");
            int wi = Integer.parseInt(wo);
            int si = Integer.parseInt(so);
            int ti = Integer.parseInt(p.getString("start"));

            long nowL = System.currentTimeMillis()/1000;

            int now = (int) nowL;
            int secs = now - ti;

            String ws = "0";
            String ss = "0";

            if(secs > wi * 60) { //walking done
                int ssecs = secs - (wi * 60);
                if(ssecs < si * 60) // still sitting
                    ss = Integer.toString(Math.round(si - ssecs/60));
            }
            else { // still walking
                ws = Integer.toString(Math.round(wi - secs/60));
                ss = so;
            }

            ws += "/"+wo;
            ss += "/"+so;

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

            shell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.showProfile(username);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowView;
    }
}


package org.sirimangalo.meditationplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by noah on 10/15/14.
 */

public class MedAdapter extends ArrayAdapter<JSONObject> {


    private final List<JSONObject> values;
    private final Context context;

    public MedAdapter(Context _context, int resource, List<JSONObject> items) {
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

        try {
            String wo = p.getString("walking");
            String so = p.getString("sitting");
            int wi = Integer.parseInt(wo);
            int si = Integer.parseInt(so);
            int ti = Integer.parseInt(p.getString("start"));

            int now = Math.round(new Date().getTime()/1000);
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
            name.setText(p.getString("username"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowView;
    }
}


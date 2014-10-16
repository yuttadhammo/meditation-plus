package org.sirimangalo.meditationplus;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by noah on 10/15/14.
 */
public class ChatAdapter extends ArrayAdapter<JSONObject> {


    private final List<JSONObject> values;
    private final Context context;

    private String TAG = "ChatAdapter";

    public ChatAdapter(Context _context, int resource, List<JSONObject> items) {
        super(_context, resource, items);
        this.values = items;
        context = _context;
    }

    private Html.ImageGetter imgGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            int id;
            id = context.getResources().getIdentifier(source,"drawable",context.getPackageName());

            Drawable d = context.getResources().getDrawable(id);
            d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
            return d;
        }
    };

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.chat_list_item, parent, false);

        JSONObject p = values.get(position);
        try {
            int then = Integer.parseInt(p.getString("time"));
            int now = Math.round(new Date().getTime()/1000);

            int ela = now - then;
            int day = 60*60*24;
            ela = ela > day ? day : ela;
            int intColor = 255 - Math.round(ela*255/day);
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

                String message = Utils.replaceSmilies(context, p.getString("message"));

                String text = "<b>"+(p.getString("me").equals("true")?"<font color=\"blue\">":"")+p.getString("username")+(p.getString("me").equals("true")?"</font>":"")+"</b>: "+message;
                Spanned html = Html.fromHtml(text,imgGetter,null);

                mess.setText(html);
                mess.setTextColor(transparency);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;

    }

}

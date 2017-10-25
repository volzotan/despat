package de.volzo.despat;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;
import de.volzo.despat.web.ServerConnector;

public class ConfigActivity extends AppCompatActivity {

    public static final String TAG = ConfigActivity.class.getSimpleName();

    ConfigListAdapter configListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // MAC address
        // device name
        // free space internal
        // free space SD-card
        // battery internal
        // battery external

        // upload telemetry data
        // folder
        // server address


        final ListView lv = (ListView) findViewById(R.id.listView);

        List<ConfigItem> configItems = new ArrayList<ConfigItem>();

        configListAdapter = new ConfigListAdapter(this, configItems);
        lv.setAdapter(configListAdapter);

        Despat despat = ((Despat) getApplicationContext());
        SystemController systemController = despat.getSystemController();

        configItems.add(new ConfigItem("unique device identifier", "usually the MAC address", Config.getUniqueDeviceId(this), false));
        configItems.add(new ConfigItem("device name", "e.g. \"Red House\"", Config.getDeviceName(this), false));
        configItems.add(new ConfigItem("free space", "free space available on the internal memory", Float.toString(Util.getFreeSpaceOnDevice(Config.IMAGE_FOLDER)), false));
        configItems.add(new ConfigItem("free space SD-card", "free space available on the SD-card", "unavailable", false));
        configItems.add(new ConfigItem("battery internal", "", Float.toString(systemController.getBatteryLevel()), false));
        configItems.add(new ConfigItem("battery external", "", "unavailable", false));

        configItems.add(new ConfigItem("upload data", "send gathered data directly to the server", "1", true));

    }
}


class ConfigListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;

    List<ConfigItem> configItems;

    public ConfigListAdapter(Activity activity, List<ConfigItem> configItems) {
        this.activity = activity;
        this.configItems = configItems;
    }

    @Override
    public int getCount() {
        return configItems.size();
    }

    @Override
    public Object getItem(int location) {
        return configItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.listviewitem_config, null);

        ConfigItem c = configItems.get(position);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView description = (TextView) convertView.findViewById(R.id.description);
        TextView value = (TextView) convertView.findViewById(R.id.value);
        CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);

        title.setText(c.getTitle());
        description.setText(c.getDescription());

        if (!c.isValueIsBool()) {
            value.setText(c.getValue());

            checkbox.setVisibility(View.INVISIBLE);
        } else {
            checkbox.setVisibility(View.VISIBLE);
            try {
                int val = Integer.parseInt(c.getValue());

                if (val == 0) {
//                    checkbox.setActivated(false);
                    checkbox.setChecked(false);
                } else if (val == 1) {
                    checkbox.setChecked(true);
                } else {
                    throw new IllegalArgumentException("value not in boolean range");
                }

            } catch (Exception e) {
                Log.w("ConfigListAdapter", "parsing bool value failed", e);
                checkbox.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }

}

class ConfigItem {
    private String title;
    private String description;
    private String value;
    private boolean valueIsBool;

    private boolean setBackgroundRed;

    public ConfigItem(String title, String description, String value, boolean valueIsBoolean) {
        this.title = title;
        this.description = description;
        this.value = value;
        this.valueIsBool = valueIsBoolean;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isValueIsBool() {
        return valueIsBool;
    }

    public void setValueIsBool(boolean valueIsBool) {
        this.valueIsBool = valueIsBool;
    }
}
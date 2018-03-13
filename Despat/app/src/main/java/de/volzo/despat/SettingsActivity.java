package de.volzo.despat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = SettingsActivity.class.getSimpleName();

    Activity activity;

    ConfigListAdapter configListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.activity = this;

        // device name

        // shutter interval
        // folder

        // upload telemetry data
        // server address
        // heartbeat interval
        // upload interval

        // MAC address
        // free space internal
        // free space SD-card
        // battery internal
        // battery external

        // TODO: do sanity checks on Config variables

        final ListView lv = (ListView) findViewById(R.id.listView);
        lv.setOnItemClickListener(this);

        List<ConfigItem> configItems = new ArrayList<ConfigItem>();

        configListAdapter = new ConfigListAdapter(this, configItems);
        lv.setAdapter(configListAdapter);

        Despat despat = ((Despat) getApplicationContext());
        SystemController systemController = despat.getSystemController();

        configItems.add(new ConfigItem(Config.KEY_DEVICENAME, "device name", "human readable name, e.g. \"Red House\"", Config.getDeviceName(this), false));

        ConfigItem ci2 = new ConfigItem(Config.KEY_SHUTTER_INTERVAL, "shutter interval", "take image every X milliseconds", Config.getShutterInterval(activity), false);
        ci2.setValidationText(Config.sanityCheckShutterInterval(activity));
        configItems.add(ci2);

        ConfigItem ci3 = new ConfigItem(Config.KEY_SERVER_ADDRESS, "server address", "servpat URL", Config.getServerAddress(activity), false);
        ci3.setValidationText(Config.sanityCheckServerAddress(activity));
        configItems.add(ci3);

        configItems.add(new ConfigItem(null, "unique device identifier", "usually the MAC address", Config.getUniqueDeviceId(this), false));
        configItems.add(new ConfigItem(null, "free space", "free space available on the internal memory", Integer.toString(Math.round(Util.getFreeSpaceOnDeviceInMb(Config.getImageFolder(activity)))) + " MB", false));
        configItems.add(new ConfigItem(null, "free space SD-card", "free space available on the SD-card", "unavailable", false));
        configItems.add(new ConfigItem(null, "battery internal", "", Integer.toString(Math.round(systemController.getBatteryLevel())) + "%", false));
        configItems.add(new ConfigItem(null, "battery external", "", "unavailable", false));

        configItems.add(new ConfigItem(null, "upload data", "send gathered data directly to the server", "1", true));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, ((ConfigItem) configListAdapter.getItem(position)).getTitle());

        ConfigItem ci = (ConfigItem) configListAdapter.getItem(position);
        if (ci.getKey() != null) {
            createDialog(ci);
        }
    }

    private void createDialog(ConfigItem ci) {
        final String key = ci.getKey();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(ci.getTitle());
        builder.setMessage(ci.getDescription());

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_textinput, null);

        builder.setView(view);

        final TextView tv = ((TextView) view.findViewById(R.id.edittext));
        tv.setText(ci.getValue());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String value = tv.getText().toString();

                switch(key) {
                    case Config.KEY_DEVICENAME:
                        Config.setDeviceName(activity, value);
                        break;
                    case Config.KEY_SHUTTER_INTERVAL:
                        try {
                            Config.setShutterInterval(activity, Long.parseLong(value));
                        } catch (NumberFormatException e) {
                            Toast.makeText(activity, "not a number", Toast.LENGTH_SHORT);
                        }
                        break;
                    case Config.KEY_IMAGE_FOLDER:
                        Config.setImageFolder(activity, value);
                        break;
                    case Config.KEY_HEARTBEAT_INTERVAL:
                        Config.setHeartbeatInterval(activity, value);
                        break;
                    case Config.KEY_PHONE_HOME:
                        // TODO
                        break;
                    case Config.KEY_SERVER_ADDRESS:
                        Config.setServerAddress(activity, value);
                        break;
                    default:
                        Log.e(TAG, "unknown Config key");
                }
                // restart activity
                activity.recreate();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
        TextView validation = (TextView) convertView.findViewById(R.id.validation);

        title.setText(c.getTitle());
        description.setText(c.getDescription());

        if (!c.isValueIsBool()) {
            value.setText(c.getValue());

            value.setVisibility(View.VISIBLE);
            checkbox.setVisibility(View.INVISIBLE);
        } else {
            value.setVisibility(View.INVISIBLE);
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

        if (c.getValidationText() == null || c.getValidationText().length() <= 0) {
            validation.setVisibility(View.GONE);
        } else {
            validation.setText(c.getValidationText());
            validation.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

}

class ConfigItem {
    private String key;
    private String title;
    private String description;
    private String value;
    private boolean valueIsBool;

    private String validationText;

    public ConfigItem() {}

    public ConfigItem(String key, String title, String description, String value, boolean valueIsBoolean) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.value = value;
        this.valueIsBool = valueIsBoolean;
    }

    public ConfigItem(String key, String title, String description, int value, boolean valueIsBoolean) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.value = Integer.toString(value);
        this.valueIsBool = valueIsBoolean;
    }

    public ConfigItem(String key, String title, String description, long value, boolean valueIsBoolean) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.value = Long.toString(value);
        this.valueIsBool = valueIsBoolean;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getValidationText() {
        return validationText;
    }

    public void setValidationText(String validationText) {
        this.validationText = validationText;
    }
}
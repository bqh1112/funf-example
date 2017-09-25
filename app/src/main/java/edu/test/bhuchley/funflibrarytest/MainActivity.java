package edu.test.bhuchley.funflibrarytest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.builtin.SimpleLocationProbe;
import edu.mit.media.funf.probe.builtin.WifiProbe;
import edu.mit.media.funf.storage.NameValueDatabaseHelper;

public class MainActivity extends AppCompatActivity implements Probe.DataListener {

    public static final String PIPELINE_NAME = "default";
    private FunfManager funfManager;
    private BasicPipeline pipeline;
    private WifiProbe wifiProbe;
    private SimpleLocationProbe locationProbe;
    private CheckBox enabledCheckbox;
    private Button archiveButton, scanNowButton;
    private TextView dataCountView;
    private Handler handler;
    private ServiceConnection funfManagerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("xxx", "onServiceConnected called");
            funfManager = ((FunfManager.LocalBinder) service).getManager();

            Gson gson = funfManager.getGson();
            wifiProbe = gson.fromJson(new JsonObject(), WifiProbe.class);
            locationProbe = gson.fromJson(new JsonObject(), SimpleLocationProbe.class);
            pipeline = (BasicPipeline) funfManager.getRegisteredPipeline(PIPELINE_NAME);
            wifiProbe.registerPassiveListener(MainActivity.this);
            locationProbe.registerPassiveListener(MainActivity.this);

            // This checkbox enables or disables the pipeline
            enabledCheckbox.setChecked(pipeline.isEnabled());
            enabledCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (funfManager != null) {
                        if (isChecked) {
                            funfManager.enablePipeline(PIPELINE_NAME);
                            pipeline = (BasicPipeline) funfManager.getRegisteredPipeline(PIPELINE_NAME);
                        } else {
                            funfManager.disablePipeline(PIPELINE_NAME);
                        }
                    }
                }
            });
            Log.d("xxx", "onServiceConnected completed");

            // Set UI ready to use, by enabling buttons
            enabledCheckbox.setEnabled(true);
            archiveButton.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            funfManager = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Used to make interface changes on main thread
        handler = new Handler();

        Log.d("xxx", "on start of onCreaate");
        enabledCheckbox = (CheckBox) findViewById(R.id.enabledCheckbox);
        enabledCheckbox.setEnabled(false);

        // Runs an archive if pipeline is enabled
        archiveButton = (Button) findViewById(R.id.archiveButton);
        archiveButton.setEnabled(false);
        archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pipeline.isEnabled()) {
                    pipeline.onRun(BasicPipeline.ACTION_ARCHIVE, null);

                    // Wait 1 second for archive to finish, then refresh the UI
                    // (Note: this is kind of a hack since archiving is seamless and there are no messages when it occurs)
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "Archived!", Toast.LENGTH_SHORT).show();
                            //updateScanCount();
                        }
                    }, 1000L);
                } else {
                    Toast.makeText(getBaseContext(), "Pipeline is not enabled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Log.d("xxx", "Calling bindService");

        ComponentName retVal = startService(new Intent(this, FunfManager.class));
        if (retVal == null) {
            Log.d("xxx", "retVal = null");
        }
        // Bind to the service, to create the connection with FunfManager
        boolean result = bindService(new Intent(this, FunfManager.class), funfManagerConn, BIND_AUTO_CREATE);
        if (result) {
            Log.d("xxx", "bindService returned true");
        } else {
            Log.w("xxx", "bindService returned false");
        }
    }

    @Override
    public void onDataReceived(IJsonObject probeConfig, IJsonObject data) {

    }

    @Override
    public void onDataCompleted(IJsonObject probeConfig, JsonElement checkpoint) {
        // Re-register to keep listening after probe completes.
        wifiProbe.registerPassiveListener(this);
        locationProbe.registerPassiveListener(this);
    }
}
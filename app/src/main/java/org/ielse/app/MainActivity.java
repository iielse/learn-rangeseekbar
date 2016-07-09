package org.ielse.app;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.ielse.widget.RangeSeekBar;

public class MainActivity extends AppCompatActivity {

    private TextView t1, t2, t3, t4;
    private RangeSeekBar rsb1, rsb2, rsb3, rsb4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        t1 = (TextView) findViewById(R.id.t_1);
        t2 = (TextView) findViewById(R.id.t_2);
        t3 = (TextView) findViewById(R.id.t_3);
        t4 = (TextView) findViewById(R.id.t_4);

        rsb1 = (RangeSeekBar) findViewById(R.id.rsb_1);
        rsb2 = (RangeSeekBar) findViewById(R.id.rsb_2);
        findViewById(R.id.b_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float[] results2 = rsb2.getCurrentRange();
                t2.setText((int) results2[0] + " - " + (int) results2[1]);
            }
        });

        rsb3 = (RangeSeekBar) findViewById(R.id.rsb_3);

        rsb4 = (RangeSeekBar) findViewById(R.id.rsb_4);
        rsb4.setRules(0, 100, 20, 1);
        rsb4.setValue(15, 66);

        rsb1.setOnRangeChangedListener(new RangeSeekBar.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max) {
                t1.setText(min + " - " + max);
            }
        });

        final String[] level = new String[]{"miss", "bad", "good", "excellent", "perfect"};

        RangeSeekBar.OnRangeChangedListener callback = new RangeSeekBar.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max) {
                switch (view.getId()) {
                    case R.id.rsb_3:
                        t3.setText(level[(int) min] + " - " + level[(int) max]);
                        break;
                    case R.id.rsb_4:
                        t4.setText((int) min + " - " + (int) max);
                        break;
                }
            }
        };

        rsb3.setOnRangeChangedListener(callback);
        rsb4.setOnRangeChangedListener(callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

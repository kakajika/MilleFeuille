package com.labo.kaji.millefeuille.sample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.labo.kaji.millefeuille.ArcLayoutManager;
import com.labo.kaji.millefeuille.CardStackView;
import com.labo.kaji.millefeuille.R;
import com.labo.kaji.millefeuille.SlideStackLayoutManager;

/**
 * @author kakajika
 */
public class SampleActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardStackView cardStackView = (CardStackView) findViewById(R.id.view);
                cardStackView.addCard();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_layout_arc: {
                CardStackView cardStackView = (CardStackView) findViewById(R.id.view);
                cardStackView.setLayoutManager(new ArcLayoutManager());
                return true;
            }
            case R.id.action_layout_slide: {
                CardStackView cardStackView = (CardStackView) findViewById(R.id.view);
                cardStackView.setLayoutManager(new SlideStackLayoutManager());
                return true;
            }
            case R.id.action_layout_linear: {
                CardStackView cardStackView = (CardStackView) findViewById(R.id.view);
                cardStackView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}

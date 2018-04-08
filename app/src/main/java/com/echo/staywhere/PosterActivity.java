package com.echo.staywhere;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class PosterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster);

        Intent i = getIntent();
        String addr1 = i.getStringExtra("addr");
        String addr2 = i.getStringExtra("addr_detail");

        TextView tvAddr1 = findViewById(R.id.addr_1);
        TextView tvAddr2 = findViewById(R.id.addr_2);
        tvAddr1.setText(addr1);
        tvAddr2.setText(addr2);

        Toast.makeText(this,R.string.screenshot_hint, Toast.LENGTH_LONG).show();
    }
}

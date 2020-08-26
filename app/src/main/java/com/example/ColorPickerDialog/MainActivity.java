package com.example.ColorPickerDialog;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ColorPickerLibrary.ColorPickerDialog;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private int qrcode_color = 0xffff0000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog ColorPickerDialog = new ColorPickerDialog(MainActivity.this, qrcode_color, true, new ColorPickerDialog.OnColorPickerListener() {
                    @Override
                    public void onCancel(ColorPickerDialog dialog) {

                    }

                    @Override
                    public void onOk(ColorPickerDialog dialog, String color) {
                        qrcode_color = Color.parseColor(color);
                        button.setBackgroundColor(Color.parseColor(color));
                    }
                });
                ColorPickerDialog.show();
            }
        });
    }
}

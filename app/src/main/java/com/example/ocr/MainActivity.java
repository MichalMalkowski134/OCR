package com.example.ocr;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.ocr.databinding.ActivityMainBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    Button capture, copy;
    TextView viewData;
    private static final int CAMERA_CODE = 100;
    Bitmap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        capture = findViewById(R.id.button_capture);
        copy = findViewById(R.id.button_copy);
        viewData = findViewById(R.id.text_data);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, CAMERA_CODE);
        }

        capture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);

                try
                {
                    URL url = new URL("http://proz.pythonanywhere.com/getDescription");
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    httpCon.setDoOutput(true);
                    httpCon.setRequestMethod("POST");

                    httpCon.setRequestProperty("Content-Type", "application/json");
                    httpCon.setRequestProperty("Accept", "application/json");

                    String jsonInputString = "{Apap}";
                    try(OutputStream os = httpCon.getOutputStream())
                    {
                        byte[] in = jsonInputString.getBytes("utf-8");
                        os.write(in, 0, in.length);
                    }
                    httpCon.getResponseMessage();
                    httpCon.disconnect();
                }
                catch(Exception e)
                {
                }
            }
        });

        copy.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                String s = viewData.getText().toString();
                copyText(s);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult r = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                Uri u = r.getUri();
                try
                {
                    map = MediaStore.Images.Media.getBitmap(this.getContentResolver(), u);
                    getImageText(map);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getImageText(Bitmap bitmap)
    {
        TextRecognizer rec = new TextRecognizer.Builder(this).build();
        if(!rec.isOperational())
        {
            Toast.makeText(MainActivity.this, "Błąd!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Frame f = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlock = rec.detect(f);
            StringBuilder str = new StringBuilder();
            for(int i=0; i<textBlock.size(); i++)
            {
                TextBlock tb = textBlock.valueAt(i);
                str.append(tb.getValue());
                str.append("\n");
            }
            viewData.setText(str.toString());
            capture.setText("Wybierz kolejne zdjęcie");
            copy.setVisibility(View.VISIBLE);
        }
    }

    private void copyText(String str)
    {
        ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Skopiowano!", str);
        clipBoard.setPrimaryClip(clipData);
        Toast.makeText(MainActivity.this, "Skopiowano!", Toast.LENGTH_SHORT).show();
    }
}
package com.cubeqw.clicker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    EditText editText;
    Button button;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitleColor(Color.BLACK);
        editText=findViewById(R.id.link);
        button=findViewById(R.id.go);
        progressBar=findViewById(R.id.progress);
    }
    public void onClickGo(View v){
        button.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        cut_url(editText.getText().toString());
    }
    public void cut_url(final String s){
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://clck.ru/--?url="+s;
            StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            button.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            if(response.charAt(0)!='<'){
                                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(MainActivity .this, R.style.SheetDialog);
                                View sheetView = MainActivity.this.getLayoutInflater().inflate(R.layout.url_dialog, null);
                                mBottomSheetDialog.setContentView(sheetView);
                                mBottomSheetDialog.show();
                                LinearLayout copy = sheetView.findViewById(R.id.copy);
                                LinearLayout share = sheetView.findViewById(R.id.share);
                                TextView message = sheetView.findViewById(R.id.message);
                                ImageView qr=sheetView.findViewById(R.id.img_result_qr);
                                message.setText(response);
                                QRCodeWriter writer = new QRCodeWriter();
                                try {
                                    BitMatrix bitMatrix = writer.encode(s, BarcodeFormat.QR_CODE, 512, 512);
                                    int width = bitMatrix.getWidth();
                                    int height = bitMatrix.getHeight();
                                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                                    for (int x = 0; x < width; x++) {
                                        for (int y = 0; y < height; y++) {
                                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                                        }
                                    }
                                    qr.setImageBitmap(bmp);} catch (WriterException ex) {
                                    ex.printStackTrace();
                                }
                                share.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent=new Intent(Intent.ACTION_SEND);
                                        intent.setType("text/plain");
                                        intent.putExtra(Intent.EXTRA_TEXT,response);
                                        startActivity(Intent.createChooser(intent, "Поделится"));
                                    }
                                });

                                copy.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("", response);
                                        clipboard.setPrimaryClip(clip);
                                        mBottomSheetDialog.cancel();
                                    }
                                });
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    return params;
                }
            };
            queue.add(getRequest);
        }}

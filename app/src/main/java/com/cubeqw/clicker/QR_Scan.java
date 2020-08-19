package com.cubeqw.clicker;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.Result;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QR_Scan extends Activity {
    private CodeScanner mCodeScanner;
    SharedPreferences sPref;
    String json;
    Gson gson;
    List<Link> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r__scan);
        json = load("history");
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
        if (json.length() > 0) {
            Type type = new TypeToken<List<Link>>() {
            }.getType();
            list = gson.fromJson(json, type);
        }
        if (ContextCompat.checkSelfPermission(QR_Scan.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(QR_Scan.this, new String[]{Manifest.permission.CAMERA}, 123);
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(QR_Scan.this, R.style.SheetDialog);
                        View sheetView = QR_Scan.this.getLayoutInflater().inflate(R.layout.qr_dialog, null);
                        mBottomSheetDialog.setContentView(sheetView);
                        mBottomSheetDialog.show();
                        Date date = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, hh:mm");
                        Link link = new Link("true", result.getBarcodeFormat().name(), result.toString(), format.format(date));
                        list.add(link);
                        json = gson.toJson(list);
                        save("history", json);
                        TextView result_view = sheetView.findViewById(R.id.result);
                        result_view.setText(result.toString());
                        LinearLayout copy = sheetView.findViewById(R.id.copy);
                        LinearLayout search = sheetView.findViewById(R.id.search);
                        copy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TEXT, result.toString());
                                startActivity(Intent.createChooser(intent, "Поделиться"));
                                mBottomSheetDialog.cancel();
                            }
                        });
                        search.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.toString()));
                                    startActivity(browserIntent);
                                } catch (ActivityNotFoundException e) {
                                    String sh = "https://yandex.ru/search/?&text=" + result.toString();
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sh));
                                    startActivity(browserIntent);
                                }
                            }
                        });
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                startScanning();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCodeScanner != null) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        if (mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }

    void delete() {
        sPref = getSharedPreferences("sPref", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.clear();
        ed.commit();
    }

    void save(String key, String value) {
        sPref = getSharedPreferences("sPref", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(key, value);
        ed.commit();

    }

    String load(String s) {
        sPref = getSharedPreferences("sPref", MODE_PRIVATE);
        return sPref.getString(s, "");
    }
}
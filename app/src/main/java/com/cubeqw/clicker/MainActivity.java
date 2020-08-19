package com.cubeqw.clicker;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    EditText editText;
    LinearLayout button;
    ProgressBar progressBar;
    String title, favicon;
    TextView title_view;
    ImageView fav;
    ImageView clear;
    SharedPreferences sPref;
    String json;
    Gson gson;
    List<Link> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitleColor(Color.BLACK);
        editText = findViewById(R.id.link);
        json = load("history");
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
        if (json.length() > 0) {
            Type type = new TypeToken<List<Link>>() {
            }.getType();
            list = gson.fromJson(json, type);
        }
        button = findViewById(R.id.go);
        progressBar = findViewById(R.id.progress);
        clear = findViewById(R.id.clear);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editText.getText().toString().equals("")) {
                    clear.setVisibility(View.GONE);
                } else {
                    clear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editText.getText().toString().equals("")) {
                    clear.setVisibility(View.GONE);
                } else {
                    clear.setVisibility(View.VISIBLE);
                }
            }
        });
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
                            if(response.charAt(0)!='<') {
                                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.SheetDialog);
                                View sheetView = MainActivity.this.getLayoutInflater().inflate(R.layout.url_dialog, null);
                                mBottomSheetDialog.setContentView(sheetView);
                                mBottomSheetDialog.show();
                                title_view = sheetView.findViewById(R.id.title);
                                title_view.setText(title);
                                title_view.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                        Date date = new Date();
                                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, hh:mm");
                                        Link link = new Link("false", title, response, format.format(date));
                                        list.add(link);
                                        json = gson.toJson(list);
                                        save("history", json);
                                    }

                                    @Override
                                    public void afterTextChanged(Editable editable) {

                                    }
                                });
                                fav = sheetView.findViewById(R.id.favicon);
                                LinearLayout copy = sheetView.findViewById(R.id.copy);
                                LinearLayout share = sheetView.findViewById(R.id.share);
                                TextView message = sheetView.findViewById(R.id.message);
                                ImageView qr = sheetView.findViewById(R.id.img_result_qr);
                                message.setText(response);
                                Get_URL_Title content = new Get_URL_Title();
                                content.execute(s);
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
                                        startActivity(Intent.createChooser(intent, "Поделиться"));
                                        mBottomSheetDialog.cancel();
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
                                mBottomSheetDialog.show();
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
    }

    public void onClickQR(View view) {
        Intent intent = new Intent(MainActivity.this, QR_Scan.class);
        startActivity(intent);
    }

    public void onClickClear(View view) {
        editText.setText("");
        clear.setVisibility(View.GONE);
    }

    class Get_URL_Title extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... params) {
            favicon = "";
            title = "";
            if (!params[0].startsWith("http")) {
                params[0] = "https://" + params[0];
            }
            Log.d("Awsdasd", params[0]);
            Document doc = null;
            try {
                doc = Jsoup.connect(params[0]).get();

            } catch (IOException e) {
                e.printStackTrace();
            }
            title = "";
            favicon = "";
            if (doc != null) {
                title = doc.title();
                Element element = doc.head().select("link[href~=.*\\.(ico|png)]").first();
                if (element == null) {

                    element = doc.head().select("meta[itemprop=image]").first();
                    if (element != null) {
                        favicon = element.attr("content");
                    }
                } else {
                    favicon = element.attr("href");
                }
                Log.d("DSFaSDa", favicon);

                try {
                    if (!favicon.startsWith("http")) {
                        if (!(favicon.startsWith("//"))) {
                            favicon = params[0] + favicon;
                        } else {
                            favicon = "https:" + favicon;
                        }
                    }
                } catch (StringIndexOutOfBoundsException e) {
                }
            }
            Log.d("DSFaSDa", favicon);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            fav.setImageDrawable(null);
            title_view.setText("");
            title_view.setText(title);
            try {
                Picasso.get().load(favicon).into(fav);
            } catch (IllegalArgumentException e) {
            }
        }
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

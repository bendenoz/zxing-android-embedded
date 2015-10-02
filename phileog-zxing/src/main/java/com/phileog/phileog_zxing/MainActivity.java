package com.phileog.phileog_zxing;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * This sample performs continuous scanning, displaying the barcode and source image whenever
 * a barcode is scanned.
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private CompoundBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastCode = "";

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                barcodeView.setStatusText(result.getText());
                if (! result.getText().equals(lastCode)) {
                    lastCode = result.getText();
                    String[] codes = result.getText().split(":");
                    if (codes[0].equals("PHG") && codes[1].equals("FATAG")) {
                        // we have a Phileog QR Code !!
                        new backendUpdate().execute("http://www.dev.phileog.com/agorasv2/badge/scan/" + codes[2]);
                    }
                    //Added preview of scanned barcode
                    ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
                    imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
                }
            }

        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        barcodeView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
        beepManager.updatePrefs();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
        beepManager.close();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
        lastCode = "";
    }

    public void url_test(View view) {
        Log.v("badge","Test URL");
        String code = "PHG:FATAG:0001";
        String[] codes = code.split(":");
        if (codes[0].equals("PHG") && codes[1].equals("FATAG")) {
            new backendUpdate().execute("http://www.dev.phileog.com/agorasv2/badge/scan/" + codes[2]);
        }
        // beepManager.playBeepSoundAndVibrate();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /* http://developer.android.com/training/basics/network-ops/connecting.html */
    private class backendUpdate extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                return GetURL(urls[0]);
            } catch(IOException e) {
                Log.v("badge", "doInBackground error");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            // showDialog("Downloaded " + result + " bytes");
            // FIXME: BEEP GOOD / BAD
            Log.v("badge", "onPostExecute: " + b);
            if (b) beepManager.playBeepSoundAndVibrate();
            super.onPostExecute(b);
        }
    }

    private Boolean GetURL(String myurl) throws IOException {
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int response = conn.getResponseCode();
            Log.v("badge", "Resp code: " + response);
            return (response == 200); // keep it simple...
        } finally {
            // Log.v("badge", "GetURL error");
        }
    }

}

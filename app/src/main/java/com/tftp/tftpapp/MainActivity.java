package com.tftp.tftpapp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findview();


        //WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        //String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
    private void findview(){
        Button btnRun = (Button)findViewById(R.id.btnRun);
        Button btnSD = (Button)findViewById(R.id.btnSD);
        btnSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile(Environment.getDataDirectory().getPath());
            }
        });
        Button btnStorge = (Button)findViewById(R.id.btnStorge);
        btnStorge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile(Environment.getExternalStorageDirectory().getPath());
            }
        });
    }

    public void selectFile(String startPath){
        new ChooserDialog().with(this)
                .withStartFile(startPath)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                    }
                })
                .build()
                .show();
    }

    public boolean checkWifi(){
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if(wifiManager != null)
        {
            if(wifiManager.isWifiEnabled())
            {
                WifiInfo connectedWifiInfo = wifiManager.getConnectionInfo();
                if(connectedWifiInfo != null)
                {
                    if(connectedWifiInfo.getBSSID() != null)
                    {
                        return true;
                    }

                }

            }

        }
        return false;
    }
}

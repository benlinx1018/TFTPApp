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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;
import com.obsez.android.lib.filechooser.internals.FileUtil;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {
    private String STORGE_PATH;
    private String SD_PATH;
    private TextView txtWifi, txtFileName;
    private EditText editPort;
    private File selectedFile, serverFolder;
    private TFTPServer tftpServer;

    private boolean isRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SD_PATH = GetSDPath();// or "/storage/sdcard1"
        STORGE_PATH = "/storage/sdcard0";
        findview();


        setPort(1069);

        setDefaultFile();
        setSelectFile(selectedFile);


        //檢查Wifi 取得IP
        checkWifi();


    }

    private void run() {

        CreateServerFolder();
        try {
            if (selectedFile.exists()) {
                FileUtils.copyFileToDirectory(selectedFile, serverFolder);
            } else {
                Toast.makeText(MainActivity.this, "Not Select valid file", Toast.LENGTH_SHORT).show();
            }
            tftpServer = new TFTPServer(serverFolder, serverFolder, getPort(), TFTPServer.ServerMode.GET_AND_PUT);

            tftpServer.setSocketTimeout(600000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        tftpServer.run();
    }

    private void stop() {

        try {
            tftpServer.finalize();
            FileUtils.forceDelete(serverFolder);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    private void CreateServerFolder() {
        serverFolder = new File(Environment.getExternalStorageDirectory() + "/tftp");
        try {
            if (serverFolder.exists())
                FileUtils.forceDelete(serverFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!serverFolder.mkdir()) {
            Toast.makeText(MainActivity.this, "Can not Creat ServerFolder", Toast.LENGTH_SHORT).show();
        }

    }

    private void setDefaultFile() {
        try {
            InputStream is = getAssets().open("unicorn.5511mp1.cala-d3.pc15.1609.1-9.36.2012.cpr");
            selectedFile = new File(Environment.getExternalStorageDirectory(), "Unicorn.5511mp1.CALA-D3.PC15.1609.1-9.36.2012.cpr");
            FileUtils.copyToFile(is, selectedFile);
            setSelectFile(selectedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String GetSDPath() {
        File sdPath = null;
        if (Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            sdPath = Environment.getExternalStorageDirectory();
            return sdPath.getPath();
        }
        return Environment.getExternalStorageDirectory().getPath();
    }

    private void findview() {
        editPort = (EditText) findViewById(R.id.edtPort);
        txtFileName = (TextView) findViewById(R.id.txtFileName);
        txtWifi = (TextView) findViewById(R.id.txtWifi);
        final Button btnRun = (Button) findViewById(R.id.btnRun);
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRun) {
                    stop();
                    btnRun.setText("RUN TFTP");
                    isRun = false;
                } else {
                    run();
                    btnRun.setText("STOP TFTP");
                    isRun = true;
                }
            }
        });
        Button btnSD = (Button) findViewById(R.id.btnSD);
        btnSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile(SD_PATH);
            }
        });
        Button btnStorge = (Button) findViewById(R.id.btnStorage);
        btnStorge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile(STORGE_PATH);

            }
        });
    }

    private void setSelectFile(File file) {
        selectedFile = file;
        txtFileName.setText(selectedFile.getName());
    }

    public void selectFile(String startPath) {
        new ChooserDialog().with(this)
                .withStartFile(startPath)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        setSelectFile(pathFile);
                    }
                })
                .build()
                .show();
    }

    public boolean checkWifi() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            if (wifiManager.isWifiEnabled()) {
                WifiInfo connectedWifiInfo = wifiManager.getConnectionInfo();
                if (connectedWifiInfo != null) {
                    if (connectedWifiInfo.getBSSID() != null) {
                        String s = Formatter.formatIpAddress(connectedWifiInfo.getIpAddress());
                        txtWifi.setText("Wifi Address:" + s);
                        return true;
                    }

                }

            }

        }
        return false;
    }


    private void setPort(int port) {
        editPort.setText(String.valueOf(port), TextView.BufferType.EDITABLE);
    }

    private int getPort() {
        return Integer.parseInt(editPort.getText().toString());
    }


}

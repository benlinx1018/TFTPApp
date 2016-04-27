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
        //取得預設檔案:Unicorn.5511mp1.CALA-D3.PC15.1609.1-9.36.2012.cpr
        LoadDefaultFile();


        //檢查Wifi 取得IP
        checkWifi();




    }

    private void run() {
        //建立檢查ServerFolder
        serverFolder = CreateServerFolder();
        try {
            tftpServer = new TFTPServer(serverFolder, serverFolder, getPort(), TFTPServer.ServerMode.GET_AND_PUT);

            tftpServer.setSocketTimeout(600000);
            copyFile(selectedFile, new File(serverFolder,selectedFile.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        tftpServer.run();
    }
    private void stop()  {

        try {
            tftpServer.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        serverFolder.deleteOnExit();
    }

    private File CreateServerFolder() {
        File direct = new File(Environment.getExternalStorageDirectory() + "/tftp");

        if (!direct.exists()) {
           if(!direct.mkdir())
           {
               Toast.makeText(MainActivity.this, "Can not Creat ServerFolder", Toast.LENGTH_SHORT).show();
           }

        }
        return direct;
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

                } else {
                    run();
                    btnRun.setText("STOP TFTP");

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
                        selectedFile = pathFile;
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

    private void LoadDefaultFile() {
        try {
            File defaultFile = File.createTempFile("Unicorn.5511mp1.CALA-D3.PC15.1609.1-9.36.2012", ".cpr");


            InputStream is = getAssets().open("unicorn.5511mp1.cala-d3.pc15.1609.1-9.36.2012.cpr");
            copyInputStreamToFile(is, defaultFile);


            setSelectFile(defaultFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void setPort(int port) {
        editPort.setText(String.valueOf(port), TextView.BufferType.EDITABLE);
    }

    private int getPort() {
        return Integer.parseInt(editPort.getText().toString());
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

}

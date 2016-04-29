package com.tftp.tftpapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.AbstractVariable;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


public class SNMPActivity extends AppCompatActivity {
    private static final String TAG = "SNMP CLIENT";
    private static String ipAddress = "192.168.100.1";
    private static final String port = "161";

    public static final String READ_COMMUNITY = "public";
    public static final String WRITE_COMMUNITY = "private";
    private String FILE_NAME = "Unicorn.5511mp1.CALA-D3.PC15.1609.1-9.36.2012.cpr";
    private String tftpIP;
    private ProgressBar mSpinner;
    private TextView txtLog;
    private StringBuffer logResult;
    private TFTPTask tftpTask;
    private boolean isSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snmp);

        Intent intent = getIntent();
        tftpIP = intent.getStringExtra("IP");


        findView();
    }

    private void findView() {
        mSpinner = (ProgressBar) findViewById(R.id.progressBar);
        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSend) {
                    isSend = true;
                    executeAsyncTask(new TFTPTask());
                } else {
                    Toast.makeText(v.getContext(), "Task is Running!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        txtLog = (TextView) findViewById(R.id.txtLog);
    }

    private void sendSnmpRequest(Snmp snmp,CommunityTarget target,OID oid, AbstractVariable variable, int pduType) throws Exception {
        // Create TransportMapping and Listen

        log("Create the PDU");
        // create the PDU
        PDU pdu = new PDU();
        log("OID:"+oid.toString()+"\nVal:"+variable.toString());
        pdu.add(new VariableBinding(oid, variable));
        pdu.setType(pduType);
        log("Sending Request to Agent...");
        ResponseEvent rspEvt = snmp.send(pdu, target);


        if (rspEvt != null) {
            PDU response = rspEvt.getResponse();
            if (response != null) {


                if (response.getErrorIndex() == PDU.noError && response.getErrorStatus() == PDU.noError) {
                    VariableBinding vb = (VariableBinding) response.getVariableBindings().firstElement();
                    Variable var = vb.getVariable();
                    if (var.equals(variable)) {//比较返回值和设置值

                        log("SET SUCCESS !! NEW VAL:" + var.toString());
                    } else {
                        log("SET FAIL !! RESPONSE VAL:"+var.toString());
                    }
                } else {

                    log("Error:" + response.getErrorStatusText());

                }
            } else {

                log("Error: Response PDU is null ");
            }
        } else {

            log("Error: Agent Timeout... ");
        }
        log("\n");
    }

    // AsyncTask to do job in background
    private class TFTPTask extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            logResult = new StringBuffer();
            mSpinner.setVisibility(View.VISIBLE);
        }



        @Override
        protected Void doInBackground(Void... params) {
            try {

                log("start send snmp to -address: " + ipAddress + "/" + port);
                log("Create Target Address object");
                CommunityTarget target = new CommunityTarget();
                target.setCommunity(new OctetString(WRITE_COMMUNITY));
                target.setVersion(SnmpConstants.version2c);
                target.setAddress(new UdpAddress(ipAddress + "/" + port));
                target.setRetries(2);
                target.setTimeout(3000);

                Snmp snmp  = new Snmp(new DefaultUdpTransportMapping());

                log("open snmp listen!\n");
                snmp.listen();


                log("SET OID_BCM_cdPvtMibEnableKeyValue OCTETSTRING !@#$*&^");
                //log("1.3.6.1.4.1.4413.2.99.1.1.1.2.1.2.1");
                sendSnmpRequest(snmp,target,new OID("1.3.6.1.4.1.4413.2.99.1.1.1.2.1.2.1"), new OctetString("!@#$*&^"), PDU.SET);


                log("SET OID_v2FwControlImageNumber INTERGER 2");
                //log("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.1");
                sendSnmpRequest(snmp,target,new OID("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.1"), new Integer32(2), PDU.SET);


                log("SET OID_v2FwDloadTftpServer IPADDRESS "+tftpIP);
                //log("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.2");
                sendSnmpRequest(snmp,target,new OID("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.2"), new IpAddress(tftpIP), PDU.SET);


                log("SET OID_v2FwDloadTftpPath OCTETSTRING "+FILE_NAME);
                // log("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.3");
                sendSnmpRequest(snmp,target,new OID("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.3"), new OctetString(FILE_NAME), PDU.SET);

                log("SET OID_v2FwDloadNow INTERGER 1");
                //log("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.6");
                sendSnmpRequest(snmp,target,new OID("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.6"), new Integer32(1), PDU.SET);

                snmp.close();
            } catch (Exception e) {
                logErr("Error sending snmp request - Error: " + e.getMessage(),e);
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            isSend = false;
            txtLog.setText(logResult);
            mSpinner.setVisibility(View.GONE);
        }




    }
    private void log(String msg){
        Log.d(TAG, msg);
        logResult.append(msg+"\n");
    }
    private void logErr(String msg,Exception ex){
        Log.e(TAG, msg,ex);
        logResult.append(msg+"\n");
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public  static  < T >  void executeAsyncTask ( AsyncTask < T ,  ?,  ?> asyncTask , T ...  params )  {
        if ( Build. VERSION . SDK_INT >=  Build . VERSION_CODES . HONEYCOMB )
            asyncTask.executeOnExecutor ( AsyncTask.THREAD_POOL_EXECUTOR ,  params );
        else
            asyncTask.execute ( params );
    }
}

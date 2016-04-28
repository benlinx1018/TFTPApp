package com.tftp.tftpapp;

import android.content.Intent;
import android.os.AsyncTask;
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
    private static String ipAddress = "192.168.2.1";
    private static final String port = "161";

    private static final String OIDVALUE = "1.3.6.1.4.1.0";
    private static final int SNMP_VERSION = SnmpConstants.version2c;
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
                    tftpTask = new TFTPTask();
                    tftpTask.execute();
                } else {
                    Toast.makeText(getBaseContext(), "Task is Running!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        txtLog = (TextView) findViewById(R.id.txtLog);
    }

    private void sendSnmpRequest(OID oid, AbstractVariable variable, int pduType) throws Exception {
        // Create TransportMapping and Listen


        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        Log.d(TAG, "Create Target Address object");
        logResult.append("Create Target Address object\n");
        // Create Target Address object
        CommunityTarget comtarget = new CommunityTarget();
        comtarget.setCommunity(new OctetString(WRITE_COMMUNITY));
        comtarget.setVersion(SNMP_VERSION);

        Log.d(TAG, "-address: " + ipAddress + "/" + port);
        logResult.append("-address: " + ipAddress + "/" + port + "\n");

        comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
        comtarget.setRetries(2);
        comtarget.setTimeout(1000);

        Log.d(TAG, "Prepare PDU");
        logResult.append("Prepare PDU\n");
        // create the PDU
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid, variable));
        pdu.setType(pduType);


        Snmp snmp = new Snmp(transport);
        Log.d(TAG, "Sending Request to Agent...");
        logResult.append("Sending Request to Agent...\n");

        // send the PDU
        ResponseEvent rspEvt = snmp.send(pdu, comtarget);


        if (rspEvt != null) {
            PDU response = rspEvt.getResponse();
            if (response != null) {

                String errorStatusText = response.getErrorStatusText();
                if (response.getErrorIndex() == PDU.noError && response.getErrorStatus() == PDU.noError) {
                    VariableBinding vb = (VariableBinding) response.getVariableBindings().firstElement();
                    Variable var = vb.getVariable();
                    if (var.equals(variable)) {//比较返回值和设置值
                        System.out.println("SET SUCCESS! \n NEW VAL:" + var.toString()+"\n");
                    } else {
                        System.out.println("SET FAIL! \n");
                    }
                } else {
                    String errMSG ="Error:" + response.getErrorStatusText()+"\n";
                    Log.d(TAG,errMSG);
                    logResult.append(errMSG);

                }
            } else {
                Log.d(TAG, "Error: Response PDU is null \n");
                logResult.append("Error: Response PDU is null \n");

            }
        } else {
            Log.d(TAG, "Error: Agent Timeout... \n");
            logResult.append("Error: Agent Timeout... \n");

        }
        snmp.close();
    }

    // AsyncTask to do job in background
    private class TFTPTask extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            logResult = new StringBuffer();
            mSpinner.setVisibility(View.VISIBLE);
        }

        ;

        @Override
        protected Void doInBackground(Void... params) {
            try {

                // SET OID_BCM_cdPvtMibEnableKeyValue OCTETSTRING !@#$*&^
                //1.3.6.1.4.1.4413.2.99.1.1.1.2.1.2.1
                sendSnmpRequest(new OID("1.3.6.1.4.1.4413.2.99.1.1.1.2.1.2.1"), new OctetString("!@#$*&^"), PDU.SET);
                Thread.sleep(1000);


                // SET OID_v2FwControlImageNumber INTERGER 2
                //1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.1
                sendSnmpRequest(new OID("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.1"), new Integer32(2), PDU.SET);
                Thread.sleep(1000);


                //SET OID_v2FwDloadTftpServer IPADDRESS 192.168.100.3
                //1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.2
                sendSnmpRequest(new OID("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.2"), new IpAddress(tftpIP), PDU.SET);
                Thread.sleep(1000);


                //SET OID_v2FwDloadTftpPath OCTETSTRING UBC1302-U10C111-VCM-B0-4MB-EUTDC-PC15.cpr
                //1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.3
                sendSnmpRequest(new OID("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.3"), new OctetString(FILE_NAME), PDU.SET);
                Thread.sleep(1000);

                //SET OID_v2FwDloadNow INTERGER 1
                //1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.6
                sendSnmpRequest(new OID("1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.6"), new Integer32(1), PDU.SET);
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.e(TAG,
                        "Error sending snmp request - Error: " + e.getMessage(), e);
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            isSend = false;
            txtLog.setText(logResult);
            mSpinner.setVisibility(View.GONE);
        }

        ;

    }

    ;
}

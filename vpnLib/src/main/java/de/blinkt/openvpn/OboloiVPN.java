package de.blinkt.openvpn;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.RemoteException;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;

public class OboloiVPN extends Activity {
    private Activity activity;
    private OnVPNStatusChangeListener listener;
    private OpenVPNThread vpnThread = new OpenVPNThread();
    private OpenVPNService vpnService = new OpenVPNService();
    private String ovpnFileContent;
    private String expireAt;
    boolean vpnStart = false;

    public OboloiVPN(Activity activity) {
        this.activity = activity;
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
    }

    public void setOnVPNStatusChangeListener() {
        this.listener = listener;
    }

    public void launchVPN(String ovpnFileContent,String expireAt) {
        this.ovpnFileContent = ovpnFileContent;
        this.expireAt = expireAt;
        OpenVPNService.expireAt = expireAt;
        if (!vpnStart) {
                Intent intent = VpnService.prepare(activity);

                if (intent != null) {
                    startActivityForResult(intent, 1);
                    //connecting status
                }else{
                    if(listener != null) listener.onProfileLoaded(true);
                }
        }
    }

    public void init(){
        if(vpnStart){
            stopVpn();
        }else{
            startVpn();
        }
    }

    private boolean stopVpn() {
        try {
            vpnThread.stop();
            //disconnected status
            vpnStart = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void startVpn() {
        try {

            OpenVpnApi.startVpn(activity, ovpnFileContent, "Canada", "vpn", "vpn");

            //connecting status
            vpnStart = true;

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void isServiceRunning() {
        setStatus(vpnService.getStatus());
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration.equals(null)) duration = "00:00:00";
                if (lastPacketReceive.equals(null)) lastPacketReceive = "0";
                if (byteIn.equals(null)) byteIn = " ";
                if (byteOut.equals(null)) byteOut = " ";
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    public void setStatus(String connectionState) {
        if (connectionState!= null) {
            switch (connectionState) {
                case "DISCONNECTED":
                    vpnStart = false;
                    vpnService.setDefaultStatus();
                    break;
                case "CONNECTED":
                    vpnStart = true;// it will use after restart this activity

                    break;
            }
            if (listener != null) listener.onVPNStatusChanged(connectionState);
        }else{
            Log.e("Null connection status" , "Failed to get con status");
        }

    }

    public void updateConnectionStatus(String duration, String lastPacketReceive, String byteIn, String byteOut) {
        //binding.durationTv.setText("Duration: " + duration);
        //binding.lastPacketReceiveTv.setText("Packet Received: " + lastPacketReceive + " second ago");
        //binding.byteInTv.setText("Bytes In: " + byteIn);
        //binding.byteOutTv.setText("Bytes Out: " + byteOut);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if(listener != null) listener.onProfileLoaded(true);
        } else {
            if(listener != null) listener.onProfileLoaded(false);
        }
    }
}

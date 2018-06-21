package com.example.hana.imageservicemobileapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ImageServiceService extends Service {

    private BroadcastReceiver receiver;

    public ImageServiceService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flag, int startId) {
        Toast.makeText(this,"Service starting...", Toast.LENGTH_LONG).show();

        final IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        //get the different network states
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            // Starting the Transfer
                            startTransfer();
                        }
                    }
                }
            }
        };
        // Registers the receiver so that your service will listen for broadcasts
        this.registerReceiver(this.receiver, filter);
        return START_STICKY;
    }

    public void startTransfer() {
        Toast.makeText(this, "Wi-Fi Connection Found", Toast.LENGTH_SHORT).show();

        File dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        if (dcim == null) {
            return;
        }
        final File[] pics = dcim.listFiles();
        if (pics != null) {
            // Sets the progress bar
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationChannel channel;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel("default", "Progress bar", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Progress bar for image transfer");
                notificationManager.createNotificationChannel(channel);
            }
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            builder.setContentTitle("Picture Transfer Status");
            builder.setContentText("Transfer in progress");
            builder.setDefaults(0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Create tcp connection
                    TcpClient tcpClient = new TcpClient();
                    // Counter for the number of images already sent
                    int filesIndex = 0;
                    String imageSizeAndName;

                    for (File pic : pics) {

                        try {
                            //sends the message to the server
                            FileInputStream fis = new FileInputStream(pic);
                            Bitmap bm = BitmapFactory.decodeStream(fis);
                            byte[] imgbyte = getBytesFromBitmap(bm);
                            imageSizeAndName = String.valueOf(imgbyte.length) + " " + pic.getName();
                            // Send the image size and name
                            tcpClient.SendBytes(imageSizeAndName.getBytes());
                            // In order to sync the write and read to and from the socket
                            Thread.sleep(100);
                            // Send the image itself
                            tcpClient.SendBytes(imgbyte);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Sets the progress bar
                        filesIndex++;
                        builder.setProgress(pics.length, filesIndex, false);
                        notificationManager.notify(1, builder.build());
                    }
                    // Sets the progress bar
                    builder.setContentText("Transfer complete!");
                    builder.setProgress(0, 0, false);
                    notificationManager.notify(1, builder.build());
                    tcpClient.CloseTcpClient();
                }
            }).start();
        }
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    public void onDestroy() {
        Toast.makeText(this,"Service stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}

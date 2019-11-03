package com.example.sharedpreference.Clock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.sharedpreference.MainActivity;
import com.example.sharedpreference.R;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("com.g.android.RING")){
            Log.d("aaa", "onReceive: ");
            Intent intent2 = new Intent(context, MainActivity.class);
//            int noteId = intent.getIntExtra("noteId",1);
//            NoteBody noteBody = NotesLab.get(context).queryNote(noteId);
            //发送通知
            PendingIntent pi = PendingIntent.getActivity(context,0,intent2,0);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                //只在Android O之上需要渠道
                NotificationChannel notificationChannel = new NotificationChannel("channel1","clockchannel",NotificationManager.IMPORTANCE_HIGH);
                //如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道，通知才能正常弹出
                manager.createNotificationChannel(notificationChannel);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"channel1")
                    .setContentTitle("1")
                    .setContentText("2")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE);
            Notification notification = builder.build();
            manager.notify(1,notification);

//            //发送一条清空闹铃图标的广播
//            NotesLab.get(context).updateFlag(noteId,0);
//            Intent intent1 = new Intent("com.g.android.NoColor");
//            intent1.putExtra("noteId",noteId);
//            context.sendBroadcast(intent1);
        }
    }
}

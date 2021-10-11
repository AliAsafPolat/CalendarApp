package com.example.takvimuygulamasi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

// Alarm çalma eventlerini dinleyen broadcastReceiver classıdır.
public class HatirlatmaAlarmReceiver extends BroadcastReceiver {

    public static Ringtone ringtone;
    SharedPreferences sharedPreferences;
    public static final String VARSAYILAN_AYARLAR = "Varsayilan_Ayarlar";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Alarm çalacağı zaman ilgili etkinliğin adını alır ve bildirim olarak gösterir.
        String etkinlik_adi = intent.getStringExtra("etkinlik_adi");

        // Varsayılan ayarlardan ringtone kaydetmiştik. Bu kaydedilen ringtone u shared preferences vasıtasıyla alıyoruz.
        sharedPreferences = context.getSharedPreferences(VARSAYILAN_AYARLAR, Context.MODE_PRIVATE);
        String ringtone_str = sharedPreferences.getString("ringtone",null);

        // Eğer kaydedilen bir ayar yok ise bunun kontrolünü yapıyoruz.
        if(ringtone_str != null){
            Uri ringtone_uri = Uri.parse(ringtone_str);
            ringtone = RingtoneManager.getRingtone(context, ringtone_uri);
        }else{
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null)
            {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(context, alarmUri);
        }

        // Alarm çalınır.
        ringtone.play();
        Toast.makeText(context, "Alarm Vakti Geldi!", Toast.LENGTH_LONG).show();

        // Alarm çalarken bir dialog gösterilir. Bunun için kendi yazdığım dialog classı çağırılır ve parametre olarak etkinlik adı verilir.
        Intent mIntent = new Intent(context,DialogBoxAlarmDurdurma.class);
        mIntent.putExtra("etkinlik_adi",etkinlik_adi);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
    }

    // Alarm çalışıyorken gelen dialog ekranından alarmı kapatabilmek için her yerden erişilebilir bir alarmı kapat fonksiyonu tanımladım.
    public static void AlarmKapat(){
        if(ringtone!=null)
            ringtone.stop();
    }
}

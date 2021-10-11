package com.example.takvimuygulamasi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// Alarm çalma vakti geldiğinde alarm ile birlikte alert dialog şeklinde bir mesaj gösterilmektedir. Bu class mesajın gösterilmesi için kullanılmaktadır.
public class DialogBoxAlarmDurdurma extends Activity {

    Button btn_tamam;
    TextView hatirlatma_yazisi;
    String etkinlik_adi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_box_alarm_durdurma);

        hatirlatma_yazisi = findViewById(R.id.textViewAlarmDurdurmaYazisi);

        btn_tamam = findViewById(R.id.buttonAlarmDurdur);

        btn_tamam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //HatirlatmaAlarmReceiver.AlarmKapat();
                finish();
            }
        });

        // Hangi etkinliğin alarmı çalındıysa o etkinliğin adı ekranda bilgi olarak gösterilmeli.
        Intent i = getIntent();
        if(i.getExtras()!=null){
            etkinlik_adi = i.getStringExtra("etkinlik_adi");
            hatirlatma_yazisi.setText(etkinlik_adi+ " adlı etkinlik için hatırlatmanız var!");
        }

    }

    @Override
    protected void onDestroy() {
        // Tamam butonuna tıklandığında veya ekranın diğer kısımlarına basıldığında çalan alarmı durdurması için static bir method olan alarm kapat çalıştırılır.
        HatirlatmaAlarmReceiver.AlarmKapat();
        super.onDestroy();
    }
}

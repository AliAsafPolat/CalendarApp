package com.example.takvimuygulamasi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AyarlarEkrani extends AppCompatActivity {
    TextView textViewVarsayilanRingTone;
    TextView textViewVarsayilanHatirlatmaZamani;
    TextView textViewVarsayilanHatirlatmaSikligi;
    TextView textViewVarsayilanHatirlatmaSaati;

    Switch switch_dark_light;
    Button btn_kaydet;
    Button btn_hatirlatma_saati;

    Spinner spinner_ringtone;
    Spinner spinner_hatirlatma_zamani;
    Spinner spinner_hatirlatma_sikligi;
    ConstraintLayout ayarlar_layout;

    ArrayAdapter<CharSequence> adapter_hatirlatma_sikligi;
    ArrayAdapter<CharSequence> adapter_hatirlatma_zamani;
    ArrayAdapter<String> adapter_ringtone;

    SharedPreferences sharedPreferences;

    int varsayilan_saat=-1,varsayilan_dakika=-1;
    ArrayList<String> keyler;

    public static final String VARSAYILAN_AYARLAR = "Varsayilan_Ayarlar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar_ekrani);

        switch_dark_light = findViewById(R.id.switchDarkLightMode);
        btn_kaydet = findViewById(R.id.buttonVarsayilanAyarlarKaydet);
        spinner_hatirlatma_sikligi = findViewById(R.id.spinnerVarsayilanHatirlatmaSikligi);
        spinner_hatirlatma_zamani = findViewById(R.id.spinnerVarsayilanHatirlatmaZamani);
        spinner_ringtone = findViewById(R.id.spinnerVarsayilanRingtone);
        btn_hatirlatma_saati = findViewById(R.id.buttonVarsayilanHatirlatmaSaati);
        textViewVarsayilanRingTone = findViewById(R.id.textViewVarsayilanRingTone);
        textViewVarsayilanHatirlatmaZamani = findViewById(R.id.textViewVarsayilanHatirlatmaZamani);
        textViewVarsayilanHatirlatmaSikligi = findViewById(R.id.textViewVarsayilanHatirlatmaSikligi);
        textViewVarsayilanHatirlatmaSaati = findViewById(R.id.textViewVarsayilanHatirlatmaSaati);
        ayarlar_layout = findViewById(R.id.ayarlarEkraniLayout);


        // Spinnerlar??n adapterlerini ayarlar.
        adapter_hatirlatma_sikligi = ArrayAdapter.createFromResource(this,
                R.array.TekrarEtmeSikligi, android.R.layout.simple_spinner_item);
        adapter_hatirlatma_sikligi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_hatirlatma_sikligi.setAdapter(adapter_hatirlatma_sikligi);

        adapter_hatirlatma_zamani = ArrayAdapter.createFromResource(this,
                R.array.VarsayilanHatirlatmaZamani, android.R.layout.simple_spinner_item);
        adapter_hatirlatma_zamani.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_hatirlatma_zamani.setAdapter(adapter_hatirlatma_zamani);

        // Kaydedilen ayarlara eri??mek i??in kullan??rl??r.
        sharedPreferences = getSharedPreferences(VARSAYILAN_AYARLAR,Context.MODE_PRIVATE);

        // Kaydedilen ringtone uri bilgisini al??r. Daha ??nceden bir kay??t yap??lm???? m?? kontrol??n?? yapmak i??in kullan??lacakt??r.
        String _ringtone = sharedPreferences.getString("ringtone",null);
        // Ekran??n tema bilgisini tutar.
        Boolean _dark_light = sharedPreferences.getBoolean("dark_light",false);
        // Varsay??lan zaman bilgileri tutulur.
        int _varsayilan_dakika = sharedPreferences.getInt("varsayilan_dakika",-1);
        int _varsayilan_saat = sharedPreferences.getInt("varsayilan_saat",-1);

        // ??al??nacak ringtonelar??n??n key de??erleri ve uri de??erleri listelenmek ??zere maplenir.
        final Map<String,Uri> ringler = BildirimSesleriniGetir();

        // Spinnerda g??stermek i??in key de??erleri al??n??r. Spinnerda se??ilecek olan key de??erine g??re uri atamas?? yap??lacakt??r.
        keyler = new ArrayList<String>();

        for ( String key : ringler.keySet() ) {
            keyler.add(key);
        }

        // Zil sesleri i??in sistemden al??nan adlar kullan??l??r.
        adapter_ringtone = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, keyler);
        adapter_ringtone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_ringtone.setAdapter(adapter_ringtone);

        // E??er daha ??nceden kay??t yap??lmam???? ise null de??eri d??necektir. (Sistem ilk kez ??al????t??????nda.)
        if(_ringtone != null){
            if(_dark_light)
                dark_moda_gec(true);
            else
                light_moda_gec(true);

            switch_dark_light.setChecked(_dark_light);

            if(_varsayilan_dakika != -1 && _varsayilan_saat != -1){
                btn_hatirlatma_saati.setText(String.format("%02d:%02d", _varsayilan_saat, _varsayilan_dakika));
            }
        }else{
            // Daha ??nceden kay??t yap??lmam????sa ayarlar ilk defa yap??l??yor demektir. ??lk ekran?? light olarak se??elim.
            light_moda_gec(true);
        }


        btn_hatirlatma_saati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int saat = c.get(Calendar.HOUR_OF_DAY);
                int dakika = c.get(Calendar.MINUTE);

                // Hat??rlatma zaman??n?? almak i??in TimePicker kullan??l??r.
                TimePickerDialog timePickerDialog = new TimePickerDialog(AyarlarEkrani.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                btn_hatirlatma_saati.setText(String.format("%02d:%02d", hourOfDay, minute));
                                varsayilan_dakika = minute;
                                varsayilan_saat = hourOfDay;
                            }
                        }, saat, dakika, false);
                timePickerDialog.show();
            }
        });



        btn_kaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hatirlatma_sikligi = spinner_hatirlatma_sikligi.getSelectedItem().toString();
                String hatirlatma_zamani = spinner_hatirlatma_zamani.getSelectedItem().toString();
                Boolean dark_light = switch_dark_light.isChecked();

                // Daha ??nceden <String,Uri>  ??eklinde maplenen listeden se??ilen item(String) 'e kar????l??k gelen Uri de??eri al??n??r.
                Uri alarm_tonu = ringler.get(spinner_ringtone.getSelectedItem().toString());

                // Ayarlar??n kaydedilmesi sharedpreferences ile yap??lacakt??r.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // Se??ilen ringtone ad??n?? koy.
                editor.putString("ringtone_key",spinner_ringtone.getSelectedItem().toString());
                // Se??ilen ringtone urisini koy.
                editor.putString("ringtone",alarm_tonu.toString());

                // Ekrandaki di??er bilgileri kaydet...
                editor.putString("hatirlatma_sikligi",hatirlatma_sikligi);
                editor.putString("hatirlatma_zamani",hatirlatma_zamani);
                editor.putBoolean("dark_light",dark_light);

                // Spinnerlar?? se??ilen ayarlar?? g??stermek ??zere se??mek i??in, se??ili itemlar??n pozisyonlar??n?? tut.
                editor.putInt("ringtone_key_indis",spinner_ringtone.getSelectedItemPosition());
                editor.putInt("hatirlatma_sikligi_indis",spinner_hatirlatma_sikligi.getSelectedItemPosition());
                editor.putInt("hatirlatma_zamani_indis",spinner_hatirlatma_zamani.getSelectedItemPosition());

                // E??er zaman se??ilmi?? ise zaman?? koy.
                if(varsayilan_dakika!=-1 && varsayilan_saat!=-1){
                    editor.putInt("varsayilan_dakika",varsayilan_dakika);
                    editor.putInt("varsayilan_saat",varsayilan_saat);
                }
                // De??i??iklikleri uygula ve ????k.
                editor.commit();
                Toast.makeText(AyarlarEkrani.this,"Ayarlar??n??z Kaydedildi.",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        switch_dark_light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Se??ilmi?? ise dark, se??ilmemi?? ise light olarak ayarlar...
                if(isChecked){
                    buttonView.setText("Dark");
                    dark_moda_gec(false);
                }
                else{
                    light_moda_gec(false);
                    buttonView.setText("Light");
                }
            }
        });
    }

    // Temay?? dark moda ge??irir. T??m widgetlar??n ve layoutlar??n renkleri de??i??tirilir.
    // Parametre olarak ald?????? de??er animasyonlu ge??i?? i??in kullan??l??r. E??er takvim ekran??ndan buraya ge??ilmi?? ise animasyonlu ge??i?? yapmas??na gerek yoktur
    // fakat dark/light switch e t??kland??ysa animasyonlu ge??i?? yapacakt??r. Bunun bilgisi laz??m...
    public void dark_moda_gec(boolean baslangic_mi){

        ColorDrawable[] color = {new ColorDrawable(Color.WHITE), new ColorDrawable(Color.BLACK)};
        TransitionDrawable trans = new TransitionDrawable(color);

        if (baslangic_mi)
            ayarlar_layout.setBackgroundColor(Color.BLACK);
        else{
            ayarlar_layout.setBackground(trans);
            trans.startTransition(1000);
        }

        switch_dark_light.setTextColor(Color.WHITE);
        btn_kaydet.setTextColor(Color.WHITE);
        btn_kaydet.setBackgroundColor(Color.DKGRAY);
        btn_hatirlatma_saati.setTextColor(Color.WHITE);
        btn_hatirlatma_saati.setBackgroundColor(Color.DKGRAY);
        textViewVarsayilanRingTone.setTextColor(Color.WHITE);
        textViewVarsayilanHatirlatmaZamani.setTextColor(Color.WHITE);
        textViewVarsayilanHatirlatmaSikligi.setTextColor(Color.WHITE);
        textViewVarsayilanHatirlatmaSaati.setTextColor(Color.WHITE);


        spinner_hatirlatma_sikligi.getBackground().setColorFilter(getResources().getColor(R.color.colorDarkGray), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<CharSequence> adapter_hatirlatma_sikligi = ArrayAdapter.createFromResource(this,
                R.array.TekrarEtmeSikligi, R.layout.spinner_dark_goruntu);
        adapter_hatirlatma_sikligi.setDropDownViewResource(R.layout.spinner_dark_goruntu);
        spinner_hatirlatma_sikligi.setAdapter(adapter_hatirlatma_sikligi);


        spinner_hatirlatma_zamani.getBackground().setColorFilter(getResources().getColor(R.color.colorDarkGray), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<CharSequence> adapter_hatirlatma_zamani = ArrayAdapter.createFromResource(this,
                R.array.VarsayilanHatirlatmaZamani, R.layout.spinner_dark_goruntu);
        adapter_hatirlatma_zamani.setDropDownViewResource(R.layout.spinner_dark_goruntu);
        spinner_hatirlatma_zamani.setAdapter(adapter_hatirlatma_zamani);


        spinner_ringtone.getBackground().setColorFilter(getResources().getColor(R.color.colorDarkGray), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<String> adapter_ringtone = new ArrayAdapter<String>(this,R.layout.spinner_dark_goruntu, keyler);
        adapter_ringtone.setDropDownViewResource(R.layout.spinner_dark_goruntu);
        spinner_ringtone.setAdapter(adapter_ringtone);

        // Spinnerlar??n adapterleri de??i??ti??i i??in daha ??nceden se??ili olan itemlar??n atanmas??n??n burada yap??lmas?? gerekli.
        int hatirlatma_sikligi_indis = sharedPreferences.getInt("hatirlatma_sikligi_indis",0);
        int hatirlatma_zamani_indis = sharedPreferences.getInt("hatirlatma_zamani_indis",0);
        int ringtone_key_indis = sharedPreferences.getInt("ringtone_key_indis",0);
        String _ringtone = sharedPreferences.getString("ringtone",null);
        // Daha ??nceden kay??t yap??lm???? ise bu se??imlerin atanmas?? gerekir.
        if(_ringtone != null){
            spinner_ringtone.setSelection(ringtone_key_indis);
            spinner_hatirlatma_zamani.setSelection(hatirlatma_zamani_indis);
            spinner_hatirlatma_sikligi.setSelection(hatirlatma_sikligi_indis);
        }


    }

    public void light_moda_gec(boolean baslangic_mi){

        ColorDrawable[] color = {new ColorDrawable(Color.BLACK), new ColorDrawable(Color.WHITE)};
        TransitionDrawable trans = new TransitionDrawable(color);

        if(baslangic_mi)
            ayarlar_layout.setBackgroundColor(Color.WHITE);
        else{
            ayarlar_layout.setBackground(trans);
            trans.startTransition(1000);
        }

        switch_dark_light.setTextColor(Color.BLACK);
        btn_kaydet.setTextColor(Color.BLACK);
        btn_kaydet.setBackgroundColor(getResources().getColor(R.color.colorWhiteDarker));
        btn_hatirlatma_saati.setTextColor(Color.BLACK);
        btn_hatirlatma_saati.setBackgroundColor(getResources().getColor(R.color.colorWhiteDarker));
        textViewVarsayilanRingTone.setTextColor(Color.BLACK);
        textViewVarsayilanHatirlatmaZamani.setTextColor(Color.BLACK);
        textViewVarsayilanHatirlatmaSikligi.setTextColor(Color.BLACK);
        textViewVarsayilanHatirlatmaSaati.setTextColor(Color.BLACK);

        spinner_hatirlatma_sikligi.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        adapter_hatirlatma_sikligi = ArrayAdapter.createFromResource(this,
                R.array.TekrarEtmeSikligi, R.layout.spinner_light_goruntu);
        adapter_hatirlatma_sikligi.setDropDownViewResource(R.layout.spinner_light_goruntu);
        spinner_hatirlatma_sikligi.setAdapter(adapter_hatirlatma_sikligi);

        spinner_hatirlatma_zamani.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        adapter_hatirlatma_zamani = ArrayAdapter.createFromResource(this,
                R.array.VarsayilanHatirlatmaZamani, R.layout.spinner_light_goruntu);
        adapter_hatirlatma_zamani.setDropDownViewResource(R.layout.spinner_light_goruntu);
        spinner_hatirlatma_zamani.setAdapter(adapter_hatirlatma_zamani);

        spinner_ringtone.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        adapter_ringtone = new ArrayAdapter<String>(this,R.layout.spinner_light_goruntu, keyler);
        adapter_ringtone.setDropDownViewResource(R.layout.spinner_light_goruntu);
        spinner_ringtone.setAdapter(adapter_ringtone);


        // Spinnerlar??n adapterleri de??i??ti??i i??in se??ili olan itemlar??n atanmas??n??n burada yap??lmas?? gerekli.
        int hatirlatma_sikligi_indis = sharedPreferences.getInt("hatirlatma_sikligi_indis",0);
        int hatirlatma_zamani_indis = sharedPreferences.getInt("hatirlatma_zamani_indis",0);
        int ringtone_key_indis = sharedPreferences.getInt("ringtone_key_indis",0);
        String _ringtone = sharedPreferences.getString("ringtone",null);
        // Daha ??nceden kay??t yap??lm???? ise bu se??imlerin atanmas?? gerekir.
        if(_ringtone != null){
            spinner_ringtone.setSelection(ringtone_key_indis);
            spinner_hatirlatma_zamani.setSelection(hatirlatma_zamani_indis);
            spinner_hatirlatma_sikligi.setSelection(hatirlatma_sikligi_indis);
        }

    }

    // Sistemdeki ringtonelar?? al??r ve bir map halinde d??nd??r??r.
    public Map<String, Uri> BildirimSesleriniGetir() {
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_RINGTONE);
        Cursor cursor = manager.getCursor();

        Map<String, Uri> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String notificationUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            Uri uri = Uri.parse(notificationUri);
            list.put(notificationTitle, uri);
        }
        return list;
    }


}

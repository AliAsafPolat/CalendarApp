package com.example.takvimuygulamasi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class Hatirlatma_Zamanlari_Ekrani extends AppCompatActivity {
    RecyclerView recyclerViewHatirlatmalar;
    HatirlatmaAdapter hatirlatmaAdapter;
    ArrayList<String> hatirlatma_saatleri;
    ArrayList<String> hatirlatma_tarihleri;
    LinearLayoutManager linearLayoutManager;

    String etkinlik_baslangic_tarihi;
    String etkinlik_bitis_tarihi;
    String etkinlik_adi;

    int alarm_ay,alarm_yil,alarm_gun,alarm_saat,alarm_dakika;

    SQLite_Veri_Erisimi sqLiteVeriErisimi;

    Button btn_yeni_hatirlatma_zamani;
    Button btn_geri_don;

    SharedPreferences sharedPreferences;
    public static final String VARSAYILAN_AYARLAR = "Varsayilan_Ayarlar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hatirlatma__zamanlari__ekrani);

        sqLiteVeriErisimi = new SQLite_Veri_Erisimi(Hatirlatma_Zamanlari_Ekrani.this);
        sqLiteVeriErisimi.baglanti_ac();

        sharedPreferences = getSharedPreferences(VARSAYILAN_AYARLAR,Context.MODE_PRIVATE);

        hatirlatma_tarihleri = new ArrayList<String>();
        hatirlatma_saatleri = new ArrayList<String>();
        Intent i = getIntent();

        // Bu ekran a????l??rken etkinli??in ad??n??, do??ru zamana hat??rlatma ayarlamak i??in ba??lang???? ve biti?? zamanlar??n?? almaktad??r.
        if(i.getExtras()!=null){
            etkinlik_adi = i.getStringExtra("etkinlik_adi");
            etkinlik_baslangic_tarihi = i.getStringExtra("etkinlik_baslangic_tarihi");
            etkinlik_bitis_tarihi = i.getStringExtra("etkinlik_bitis_tarihi");

            String tarih[] = i.getStringArrayExtra("hatirlatma_tarihleri");
            Collections.addAll(hatirlatma_tarihleri,tarih);
            String saat[]= i.getStringArrayExtra("hatirlatma_saatleri");
            Collections.addAll(hatirlatma_saatleri,saat);
        }else{
            Log.i("takvim","Bilgi al??nmad??..."+etkinlik_adi);
        }

        // Hat??rlatmalar??n listelenmesi i??in bir recycler view kullan??lm????t??r.
        hatirlatmaAdapter = new HatirlatmaAdapter(Hatirlatma_Zamanlari_Ekrani.this,hatirlatma_tarihleri,hatirlatma_saatleri);
        recyclerViewHatirlatmalar =findViewById(R.id.recyclarView_hatirlatmalar);
        recyclerViewHatirlatmalar.setAdapter(hatirlatmaAdapter);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewHatirlatmalar.setLayoutManager(linearLayoutManager);

        btn_yeni_hatirlatma_zamani = findViewById(R.id.button_yeni_hatirlatma);
        btn_geri_don = findViewById(R.id.buttonEtkinlikEkraninaDon);
        hatirlatmaAdapter.notifyDataSetChanged();

        hatirlatmaAdapter.setOnItemClickListener(new HatirlatmaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Hat??rlama eklemek ve g??r??nt??lemek i??in kendi olu??turdu??um dialog box yap??s?? kullan??l??r.
                dialog_ac(position);
            }

            @Override
            public void onDeleteClick(final int position) {
                hatirlatma_sil(position);
            }
        });


        btn_yeni_hatirlatma_zamani.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hat??rlama eklemek ve g??r??nt??lemek i??in kendi olu??turdu??um dialog box yap??s?? kullan??l??r.
                dialog_ac(-1);
            }
        });

        // Hat??rlatma ekran??ndan ????karken bilgileri ge??irip sonland??r.
        btn_geri_don.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra("hatirlatma_tarihleri",hatirlatma_tarihleri);
                i.putExtra("hatirlatma_saatleri",hatirlatma_saatleri);
                setResult(Activity.RESULT_OK,i);
                finish();
            }
        });

    }

    public void hatirlatma_sil(final int position){

        // Etkinlik silme i??lemi ger??ekle??irken kullan??c??ya bilgi versin ve onay istesin...
        new android.app.AlertDialog.Builder(Hatirlatma_Zamanlari_Ekrani.this)
                .setTitle("Hat??rlatma Sil")
                .setMessage("Bu hat??rlatmay?? ger??ekten silmek istiyor musunuz?")

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String tarih = hatirlatma_tarihleri.get(position);
                        String saat = hatirlatma_saatleri.get(position);

                        // Alarm olu??turulurken hat??rlatma saatinin veritaban??nda tutulan primary key i pending intent kodu olarak verilmi??ti
                        // silinen alarm??n iptal edilmesi i??in bu kodun veritaban??ndan ??ekilmesi gerekir. Sonras??nda ilgili anahtar fonksiyona verilir.
                        int reqCode = sqLiteVeriErisimi.hatirlatma_id_getir(etkinlik_adi, etkinlik_baslangic_tarihi,tarih,saat);
                        cancelAlarm(null,reqCode);

                        Toast.makeText(Hatirlatma_Zamanlari_Ekrani.this,"Hat??rlatma Silindi.",Toast.LENGTH_SHORT).show();

                        // Recycler viewde g??ncellemeler yap??lmal??d??r.
                        hatirlatma_saatleri.remove(position);
                        hatirlatma_tarihleri.remove(position);
                        hatirlatmaAdapter.notifyItemRemoved(position);
                        // Veritaban??nda g??ncellemeler yap??lmal??d??r.
                        sqLiteVeriErisimi.hatirlatma_sil(etkinlik_adi, etkinlik_baslangic_tarihi,tarih,saat);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Hat??rlama eklemek i??in kendi olu??turdu??um dialog box yap??s??n?? a??ar. Parametre olarak pozisyon bilgisi al??r. (G??r??nt??leme yap??l??yorsa pozisyon yeni ekleme yap??l??yorsa -1)
    // Varsay??lan ayarlar?? okur ve ??nc??l de??erleri bunlara g??re ayarlar.
    public void dialog_ac(final int pos){

        final AlertDialog.Builder builder = new AlertDialog.Builder(Hatirlatma_Zamanlari_Ekrani.this);
        // Layout olarak kendi tasar??m??m eklendi.
        View mView = getLayoutInflater().inflate(R.layout.alert_dialog_hatirlatma_girisi,null);

        // Layout ??zerindeki widgetlara eri??ildi.
        final Button _hatirlatma_tarihi = mView.findViewById(R.id.buttonHatirlatmaTarihiGiris);
        final Button _hatirlatma_saati = mView.findViewById(R.id.buttonHatirlatmaSaatiGiris);
        final Button _ekle = mView.findViewById(R.id.buttonHatirlatmaZamaniEkle);
        final Spinner _hatirlatma_sikligi = mView.findViewById(R.id.spinnerHatirlatmaTekrar);

        ArrayAdapter<CharSequence> adapter_tekrar_sikligi = ArrayAdapter.createFromResource(this,
                R.array.TekrarEtmeSikligi, android.R.layout.simple_spinner_item);
        adapter_tekrar_sikligi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _hatirlatma_sikligi.setAdapter(adapter_tekrar_sikligi);

        // Varsay??lan ayarlardaki hat??rlatma s??kl?????? de??eri g??z??ks??n...
        int hatirlatma_sikligi_indis = sharedPreferences.getInt("hatirlatma_sikligi_indis",0);
        _hatirlatma_sikligi.setSelection(hatirlatma_sikligi_indis);

        // Pozisyonun -1 olmamas?? demek var olan bir hat??rlatma listeleniyor demektir.
        if (pos!=-1){
            // Kurulu olan hat??rlatman??n bilgileri g??sterilir.
            String tarih = hatirlatma_tarihleri.get(pos);
            String saat= hatirlatma_saatleri.get(pos);
            _hatirlatma_saati.setText(saat);
            _hatirlatma_tarihi.setText(tarih);
        }else{
            // Yeni alarm kurulacak ise parametrelerin d??zenlenmesi gerekir. Bunun i??in daha ??nceden kaydetti??imiz varsay??lan ayarlar ??ekilip g??sterilir.
            // Varsay??lan saat bilgileri ??ekilir ve ayarlan??r.
            alarm_dakika = sharedPreferences.getInt("varsayilan_dakika",0);
            alarm_saat = sharedPreferences.getInt("varsayilan_saat",0);
            _hatirlatma_saati.setText(String.format("%02d:%02d", alarm_saat, alarm_dakika));

            // Varsay??lan hat??rlatma zaman?? bilgisi ??ekilir. Bu bilgi 1 g??n ??nce, 3 g??n ??nce ve 1 hafta ??nce olmak ??zere 3 de??er alabilir.
            String hatirlatma_zamani = sharedPreferences.getString("hatirlatma_zamani",null);
            if(hatirlatma_zamani != null){
                // Etkinli??in ba??lang???? tarihi g??n,ay,y??l ??eklinde elde edilir.
                String _baslangic[] = etkinlik_baslangic_tarihi.split("/",3);
                int _gun = Integer.parseInt(_baslangic[0]);
                int _ay = Integer.parseInt(_baslangic[1]);
                int _yil = Integer.parseInt(_baslangic[2]);

                Calendar cal  = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH,_gun);
                cal.set(Calendar.MONTH,_ay-1);
                cal.set(Calendar.YEAR,_yil);

                // Varsay??lan hat??rlatma zaman?? etkinli??in ba??lang???? tarihlerine g??re ayarlan??p aray??zde g??sterilir.
                if(hatirlatma_zamani.compareTo("1 G??n ??nce")==0){
                    cal.add(Calendar.DATE, -1);
                    SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy");
                    String result = s.format(new Date(cal.getTimeInMillis()));
                    String _result[] = result.split("/",3);

                    alarm_yil = Integer.parseInt(_result[2]);
                    alarm_ay = Integer.parseInt(_result[1])-1;
                    alarm_gun = Integer.parseInt(_result[0]);

                    // Tarih k??sm??nda aylar 0. indisten ba??lad?????? i??in g??sterimde bir  fazla olmak durumunda...
                    _hatirlatma_tarihi.setText(String.format("%02d/%02d/%d", alarm_gun, alarm_ay+1,alarm_yil));

                }else if(hatirlatma_zamani.compareTo("3 G??n ??nce")==0){
                    cal.add(Calendar.DATE, -3);

                    SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy");
                    String result = s.format(new Date(cal.getTimeInMillis()));
                    String _result[] = result.split("/",3);

                    alarm_yil = Integer.parseInt(_result[2]);
                    alarm_ay = Integer.parseInt(_result[1])-1;
                    alarm_gun = Integer.parseInt(_result[0]);

                    // Tarih k??sm??nda aylar 0. indisten ba??lad?????? i??in g??sterimde bir  fazla olmak durumunda...
                    _hatirlatma_tarihi.setText(String.format("%02d/%02d/%d", alarm_gun, alarm_ay+1,alarm_yil));
                }else if(hatirlatma_zamani.compareTo("1 Hafta ??nce")==0){
                    cal.add(Calendar.DATE, -7);

                    SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy");
                    String result = s.format(new Date(cal.getTimeInMillis()));
                    String _result[] = result.split("/",3);

                    alarm_yil = Integer.parseInt(_result[2]);
                    alarm_ay = Integer.parseInt(_result[1])-1;
                    alarm_gun = Integer.parseInt(_result[0]);

                    // Tarih k??sm??nda aylar 0. indisten ba??lad?????? i??in g??sterimde bir  fazla olmak durumunda...
                    _hatirlatma_tarihi.setText(String.format("%02d/%02d/%d", alarm_gun, alarm_ay+1,alarm_yil));
                }
            }
        }
        // Diyalog g??r??nt??s?? ayarlan??r.
        builder.setView(mView);
        final AlertDialog dialog = builder.create();

        _hatirlatma_tarihi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int yil = c.get(Calendar.YEAR);
                int ay = c.get(Calendar.MONTH);
                int gun = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(Hatirlatma_Zamanlari_Ekrani.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                _hatirlatma_tarihi.setText(String.format("%02d/%02d/%d", dayOfMonth, monthOfYear+1,year));
                                alarm_ay = monthOfYear;
                                alarm_gun = dayOfMonth;
                                alarm_yil = year;
                            }
                        }, yil, ay, gun);
                datePickerDialog.show();
                if(_hatirlatma_saati.getText().toString().compareTo("Saat")==0){
                    _hatirlatma_saati.setText("12:00");
                    alarm_dakika = 0;
                    alarm_saat = 12;
                }

            }
        });

        // Hat??rlatma saati se??mek ??zere Time Picker kullan??l??r.
        _hatirlatma_saati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int saat = c.get(Calendar.HOUR_OF_DAY);
                int dakika = c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(Hatirlatma_Zamanlari_Ekrani.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                _hatirlatma_saati.setText(String.format("%02d:%02d", hourOfDay, minute));
                                alarm_dakika = minute;
                                alarm_saat = hourOfDay;

                            }
                        }, saat, dakika, false);
                timePickerDialog.show();
            }
        });

        // Alarm??n eklendi??i yerdir.
        _ekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (_hatirlatma_saati.getText().toString().compareTo("Saat") != 0 && _hatirlatma_tarihi.getText().toString().compareTo("Tarih") != 0) {
                    // Butonlar??n default adlar?? de??i??tirilmi?? ise eklenme yap??lm???? demektir.

                    Calendar calNow = Calendar.getInstance();
                    Calendar calSet = (Calendar) calNow.clone();

                    calSet.set(Calendar.YEAR,alarm_yil);
                    calSet.set(Calendar.MONTH,alarm_ay);
                    calSet.set(Calendar.DAY_OF_MONTH,alarm_gun);
                    calSet.set(Calendar.HOUR_OF_DAY, alarm_saat);
                    calSet.set(Calendar.MINUTE, alarm_dakika);
                    calSet.set(Calendar.SECOND, 0);
                    calSet.set(Calendar.MILLISECOND, 0);

                    // Hat??rlatma tarihi bilgisini tutar...
                    String ht_t = _hatirlatma_tarihi.getText().toString();

                    // E??er biti?? tarihinden sonra ekleme yapmaya ??al??????rsa hata al??nacakt??r...
                    if(once_mi(etkinlik_bitis_tarihi,ht_t)){
                        Toast.makeText(Hatirlatma_Zamanlari_Ekrani.this, "Biti?? tarihinden sonra hat??rlatma yap??lamaz! ("+etkinlik_bitis_tarihi+")", Toast.LENGTH_SHORT).show();
                    }
                    // E??er haz??rlanan zaman ??uandan sonras?? i??in ise d??zg??n olarak ekleme yap??l??r...
                    else if(calSet.compareTo(calNow) >= 0){

                        // E??er daha ??nceden olu??turulan bir hat??rlatmay?? de??i??tirip eklediyse ??ncesinin silinmesi gerekir...
                        if(pos!=-1){
                            int _reqCode = sqLiteVeriErisimi.hatirlatma_id_getir(etkinlik_adi, etkinlik_baslangic_tarihi,hatirlatma_tarihleri.get(pos),hatirlatma_saatleri.get(pos));
                            cancelAlarm(null,_reqCode);
                            sqLiteVeriErisimi.hatirlatma_sil(etkinlik_adi, etkinlik_baslangic_tarihi,hatirlatma_tarihleri.get(pos),hatirlatma_saatleri.get(pos));
                            // ??nceki hatirlatman??n silinmesi gerekir.
                            hatirlatma_saatleri.remove(pos);
                            hatirlatma_tarihleri.remove(pos);
                            hatirlatmaAdapter.notifyItemRemoved(pos);
                        }

                        hatirlatma_tarihleri.add(_hatirlatma_tarihi.getText().toString());
                        hatirlatma_saatleri.add(_hatirlatma_saati.getText().toString());
                        hatirlatmaAdapter.notifyDataSetChanged();

                        // Eklenen hat??rlatma veritaban??nda ve recycler view ??zerinde g??ncellenir.
                        sqLiteVeriErisimi.hatirlatma_tarihi_ekle(etkinlik_adi, etkinlik_baslangic_tarihi, _hatirlatma_tarihi.getText().toString(), _hatirlatma_saati.getText().toString());

                        // Pending intent request kodu olarak hat??rlatman??n veritaban?? ??zerindeki primary keyleri kullan??l??r.
                        int reqCode = sqLiteVeriErisimi.hatirlatma_id_getir(etkinlik_adi, etkinlik_baslangic_tarihi,_hatirlatma_tarihi.getText().toString(),_hatirlatma_saati.getText().toString());

                        // Tekrar s??kl??klar??na g??re alarmlar tekrarl?? ??ekilde kurulur... Tekrar yok ise -1 verilir.
                        String tekrar_siklik = _hatirlatma_sikligi.getSelectedItem().toString();
                        int tekrar = -1;
                        if(tekrar_siklik.compareTo("Her G??n")==0){
                            tekrar = 1000*60*60*24;
                        }else if(tekrar_siklik.compareTo("Haftada Bir")==0){
                            tekrar = 1000*60*60*24*7;
                        }
                        else if (tekrar_siklik.compareTo("Ayda Bir")==0){
                            tekrar = 1000*60*60*24*30;
                        }else if(tekrar_siklik.compareTo("Bir Kez")==0){
                            tekrar = -1;
                        }
                        // Alarm kurulmak ??zere fonksiyona verilir.
                        setAlarm(calSet,reqCode,tekrar,etkinlik_adi);
                        // A????lan diyalog kapat??l??r.
                        dialog.dismiss();

                        // Kullan??c?? bilgilendirilir.
                        Toast.makeText(Hatirlatma_Zamanlari_Ekrani.this, "Hat??rlama Ayarland??.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(Hatirlatma_Zamanlari_Ekrani.this,"Ge??mi?? Zamana Ekleme Yap??lamaz!",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Hatirlatma_Zamanlari_Ekrani.this, "Zaman Se??ilmedi!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Olu??turulan diyalog g??sterilir.
        dialog.show();
    }

    // Sorgulanan , tarih parametresinden ??nce mi diye kontrol edilir.
    public boolean once_mi(String sorgulanan, String tarih){
        String sorgu_dizi[]=sorgulanan.split("/");
        String tarih_dizi[]=tarih.split("/");

        int sorgu_yil = Integer.valueOf(sorgu_dizi[2]);
        int tarih_yil = Integer.valueOf(tarih_dizi[2]);

        int sorgu_ay = Integer.valueOf(sorgu_dizi[1]);
        int tarih_ay = Integer.valueOf(tarih_dizi[1]);

        int sorgu_gun = Integer.valueOf(sorgu_dizi[0]);
        int tarih_gun = Integer.valueOf(tarih_dizi[0]);


        if(sorgu_yil>tarih_yil)
            return false;
        else if(sorgu_yil<tarih_yil)
            return true;
        else if(sorgu_ay>tarih_ay)
            return false;
        else if(sorgu_ay<tarih_ay)
            return true;
        else if(sorgu_gun>tarih_gun)
            return false;
        else if(sorgu_gun<tarih_gun)
            return true;
        else
            return false;

    }

    // Verilen parametreler g??re alarm kurulur.
    private void setAlarm(Calendar alarmCalender,int ReqCode,int tekrar,String etkinlik_adi){
        Intent intent = new Intent(getBaseContext(), HatirlatmaAlarmReceiver.class);
        intent.putExtra("etkinlik_adi",etkinlik_adi);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), ReqCode, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        // E??er -1 ise tekrar yok demektir. Bu bir kez ger??ekle??en alarmlar i??in yap??lm???? bir kontrold??r...
        if(tekrar == -1){
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCalender.getTimeInMillis(), pendingIntent);
        }else{
            // Belirli aral??klarla tekrar eden bir alarm yap??ld??.
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,alarmCalender.getTimeInMillis(),tekrar,pendingIntent);
        }
    }

    // Kurulan alarm pending intent requestCode a g??re iptal edilir.
    private void cancelAlarm(Calendar alarmCalender,int ReqCode){
        Toast.makeText(getApplicationContext(),"Alarm ??ptal Edildi!",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getBaseContext(), HatirlatmaAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(),  ReqCode, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        sqLiteVeriErisimi.baglanti_kapat();
        super.onDestroy();
    }
}

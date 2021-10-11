package com.example.takvimuygulamasi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

// Etkinlik eklenirken açılan ekranın kod kısmıdır.

public class EtkinlikEklemeEkrani extends AppCompatActivity {
    ConstraintLayout etkinlik_layout;

    TextView textView_etkinlik_tipi;
    TextView textView_etkinlik_adi;
    TextView textView_etkinlik_detayi;
    TextView textView_baslangic_tarihi;
    TextView textView_bitis_tarihi;

    EditText etkinlik_adi;
    EditText etkinlik_detayi;
    EditText etkinlik_adresi;

    Button btn_baslangic_tarihi;
    Button btn_bitis_tarihi;
    Button btn_hatirlatma_zamanlari;
    Button btn_adres_ekle;
    Button btn_adres_goster;
    Button btn_etkinlik_ekle;
    Spinner etkinlik_tipi;

    String gelen_etkinlik_tarihi;
    SQLite_Veri_Erisimi sqLiteVeriErisimi;

    double latitude =0;
    double longitude=0;

    double gelen_latitude;
    double gelen_longitude;
    boolean gelen_var_mi =false;

    final static int HATIRLATMA_CODE = 103;
    final static int KONUM_AL = 107;
    final static int KONUM_GOSTER=108;

    // Varsayılan ayarları uygulamak için kullanılan değişkenler.
    SharedPreferences sharedPreferences;
    public static final String VARSAYILAN_AYARLAR = "Varsayilan_Ayarlar";
    boolean dark_light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etkinlik_ekleme_ekrani);

        // Veritabanını kullanmak için bir objesini oluşturup bağlantısını açarız.
        sqLiteVeriErisimi = new SQLite_Veri_Erisimi(EtkinlikEklemeEkrani.this);
        sqLiteVeriErisimi.baglanti_ac();

        // Varsayılan ayarları ayarlamak için okuma yaparız.
        sharedPreferences = getSharedPreferences(VARSAYILAN_AYARLAR, Context.MODE_PRIVATE);

        textView_etkinlik_tipi = findViewById(R.id.textView_etkinlik_tipi_yazisi);
        textView_etkinlik_adi = findViewById(R.id.textView_etkinlik_adi_yazisi);
        textView_etkinlik_detayi = findViewById(R.id.textView_etkinlik_detayi_yazisi);
        textView_baslangic_tarihi = findViewById(R.id.textView_etkinlik_baslangic_tarihi);
        textView_bitis_tarihi = findViewById(R.id.textView_etkinlik_bitis_tarihi);

        etkinlik_tipi = findViewById(R.id.spinnerEtkinlikTipi);
        etkinlik_layout = findViewById(R.id.etkinlik_ekleme_layout);
        etkinlik_adi = findViewById(R.id.editText_etkinlik_adi);
        etkinlik_detayi = findViewById(R.id.editText_etkinlik_detayi);
        etkinlik_adresi = findViewById(R.id.editText_etkinlik_adresi);
        btn_baslangic_tarihi = findViewById(R.id.button_etkinlik_baslangic_tarihi);
        btn_bitis_tarihi = findViewById(R.id.button_etkinlik_bitis_tarihi);
        btn_hatirlatma_zamanlari = findViewById(R.id.button_etkinlik_hatirlatma_zamanlari);
        btn_etkinlik_ekle = findViewById(R.id.buttonEtkinlikEkle);


        // Spinner widgetlarına değerleri atandı.
        ArrayAdapter<CharSequence> adapter_etkinlik_tipi = ArrayAdapter.createFromResource(this,
                R.array.EtkinlikTipi, android.R.layout.simple_spinner_item);
        adapter_etkinlik_tipi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etkinlik_tipi.setAdapter(adapter_etkinlik_tipi);

        btn_adres_ekle = findViewById(R.id.buttonEtkinlikAdresiEkle);
        btn_adres_goster = findViewById(R.id.buttonAdresGoster);

        // Yeni bir etkinlik ekleme olacağı gibi var olan bir etkinlik görüntülenmek istenebilir.
        // Eğer görüntüleme kısmından çağırılmışsa burası çalışır.
        Intent i =getIntent();


        btn_adres_ekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent i = new Intent(EtkinlikEklemeEkrani.this,HaritaEkrani.class);

                startActivityForResult(i,KONUM_AL);
            }
        });
        if(i.getExtras()!=null){
            String etkinlik_adi = i.getStringExtra("etkinlik_adi");
            String etkinlik_detayi=i.getStringExtra("etkinlik_detayi");
            String baslangic_tarihi=i.getStringExtra("baslangic_tarihi");
            String bitis_tarihi=i.getStringExtra("bitis_tarihi");
            String adres=i.getStringExtra("adres");
            String etkinlik_turu=i.getStringExtra("etkinlik_turu");
            gelen_latitude = i.getDoubleExtra("adres_latitude",0);
            gelen_longitude = i.getDoubleExtra("adres_longitude",0);

            // İkisinden biri sıfırdan farklı ise demek ki gelen bir adres değeri var demektir. Değişkeni ayarlayalım.
            if(gelen_longitude!=0 || gelen_latitude!=0)
                gelen_var_mi = true;

            gelen_etkinlik_tarihi = baslangic_tarihi;

            // Widgetlerı ayarla.
            this.etkinlik_adi.setText(etkinlik_adi);
            this.etkinlik_detayi.setText(etkinlik_detayi);
            this.btn_baslangic_tarihi.setText(baslangic_tarihi);
            this.btn_bitis_tarihi.setText(bitis_tarihi);
            this.etkinlik_adresi.setText(adres);

            // Etkinlik turunu ayarla.
            if(etkinlik_turu.compareTo("Doğum Günü")==0)
                this.etkinlik_tipi.setSelection(0);
            else if(etkinlik_turu.compareTo("Toplantı")==0)
                this.etkinlik_tipi.setSelection(1);
            else if(etkinlik_turu.compareTo("Görev")==0)
                this.etkinlik_tipi.setSelection(2);
        }

        btn_adres_goster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EtkinlikEklemeEkrani.this,HaritaEkrani.class);
                // Eğer adres bilgisi var ise göster yok ise veya eklenip kaydedilmedi ise uyarı mesajı ver.
                if(gelen_var_mi){
                    i.putExtra("adres_latitude",gelen_latitude);
                    i.putExtra("adres_longitude",gelen_longitude);
                    startActivityForResult(i,KONUM_GOSTER);
                }else{
                    new AlertDialog.Builder(EtkinlikEklemeEkrani.this)
                            .setTitle("Adres Goster")
                            .setMessage("Bu etkinliği kaydetmemiş veya henüz adres girişi yapmamış olabilirsiniz. Adres girişi yapıp etkinliği kaydettiğinizden emin olunuz.")
                            .setPositiveButton(android.R.string.yes,null)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                }
            }
        });

        // Etkinlik başlangıç tarihini seç.
        btn_baslangic_tarihi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int yil = c.get(Calendar.YEAR);
                int ay = c.get(Calendar.MONTH);
                int gun = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(EtkinlikEklemeEkrani.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                btn_baslangic_tarihi.setText(String.format("%02d/%02d/%d", dayOfMonth, monthOfYear+1,year));
                            }
                        }, yil, ay, gun);
                datePickerDialog.show();
            }
        });

        // Etkinlik bitiş tarihini seç.
        btn_bitis_tarihi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int yil = c.get(Calendar.YEAR);
                int ay = c.get(Calendar.MONTH);
                int gun = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(EtkinlikEklemeEkrani.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                btn_bitis_tarihi.setText(String.format("%02d/%02d/%d", dayOfMonth, monthOfYear+1,year));
                            }
                        }, yil, ay, gun);
                datePickerDialog.show();
            }
        });

        // Etkinliğin birden fazla hatırlatma zamanı olabilir bunlar için yeni bir aktiviteye geç.
        btn_hatirlatma_zamanlari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _etkinlik_adi = etkinlik_adi.getText().toString();
                if(_etkinlik_adi.matches("")){
                    Toast.makeText(EtkinlikEklemeEkrani.this,"Başlık Boş Bırakılamaz!",Toast.LENGTH_SHORT).show();
                }else if(btn_baslangic_tarihi.getText().toString().compareTo("__ / __ / __")==0 || btn_bitis_tarihi.getText().toString().compareTo("__ / __ / __")==0){
                    Toast.makeText(EtkinlikEklemeEkrani.this,"Tarihleri Belirtiniz!",Toast.LENGTH_SHORT).show();
                }else {

                    // Hatırlatma zamanlarının doğru bir şekilde ayarlanması için yeni bir veri tabanı var bu sebeple
                    // gerekli olan bilgileri de bu aktiviteye gönderiyoruz.
                    Intent i = new Intent(EtkinlikEklemeEkrani.this, Hatirlatma_Zamanlari_Ekrani.class);
                    String _etkinlik_baslangic_tarihi = btn_baslangic_tarihi.getText().toString();
                    String _etkinlik_bitis_tarihi = btn_bitis_tarihi.getText().toString();

                    // Daha önceden kaydedilen bir etkinlik ise önceki hatırlatma tarihlerini de göstermesi lazım. Bu bilgileri de veriyoruz.
                    ArrayList<String> hatirlatma_saatleri = sqLiteVeriErisimi.hatirlatma_saatlerini_getir(_etkinlik_adi, _etkinlik_baslangic_tarihi);
                    ArrayList<String> hatirlatma_tarihleri = sqLiteVeriErisimi.hatirlatma_tarihlerini_getir(_etkinlik_adi, _etkinlik_baslangic_tarihi);

                    // Arraylisti geçirmek için listeye çevir.
                    Object[] liste_hatirlatma_saatleri = hatirlatma_saatleri.toArray();
                    String[] _liste_hatirlatma_saatleri = Arrays.copyOf(liste_hatirlatma_saatleri, liste_hatirlatma_saatleri.length, String[].class);

                    Object[] liste_hatirlatma_tarihleri = hatirlatma_tarihleri.toArray();
                    String[] _liste_hatirlatma_tarihleri = Arrays.copyOf(liste_hatirlatma_tarihleri, liste_hatirlatma_tarihleri.length, String[].class);

                    i.putExtra("etkinlik_baslangic_tarihi", _etkinlik_baslangic_tarihi);
                    i.putExtra("etkinlik_bitis_tarihi",_etkinlik_bitis_tarihi);
                    i.putExtra("etkinlik_adi", _etkinlik_adi);
                    i.putExtra("hatirlatma_tarihleri", _liste_hatirlatma_tarihleri);
                    i.putExtra("hatirlatma_saatleri", _liste_hatirlatma_saatleri);

                    startActivityForResult(i,HATIRLATMA_CODE);
                }
            }
        });

        // Oluşturulan veya düzenlenen etkinliği ekle.
        btn_etkinlik_ekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();

                String _etkinlik_adi = etkinlik_adi.getText().toString();
                String _etkinlik_detayi=etkinlik_detayi.getText().toString();
                String _baslangic_tarihi=btn_baslangic_tarihi.getText().toString();
                String _bitis_tarihi=btn_bitis_tarihi.getText().toString();
                //String _hatirlatma_sikligi=tekrar_sikligi.getSelectedItem().toString();
                String _adres=etkinlik_adresi.getText().toString();
                String _etkinlik_turu=etkinlik_tipi.getSelectedItem().toString();

                // Takvim ekranına bilgilerini yolla.
                i.putExtra("etkinlik_adi",_etkinlik_adi);
                i.putExtra("etkinlik_detayi",_etkinlik_detayi);
                i.putExtra("baslangic_tarihi",_baslangic_tarihi);
                i.putExtra("bitis_tarihi",_bitis_tarihi);
                i.putExtra("adres",_adres);
                i.putExtra("etkinlik_turu",_etkinlik_turu);

                // Bu aktivitede lokasyon aratmamış ve kayıtlı bir lokasyondan sorgulama yapılmış ise
                if(latitude==0&&gelen_var_mi){
                    i.putExtra("adres_latitude",gelen_latitude);
                    i.putExtra("adres_longitude",gelen_longitude);
                }
                // Bu aktiviteden yeniden sorgulama yapmış ise değerlerin güncellenmesi gerekir...
                else{
                    i.putExtra("adres_latitude",latitude);
                    i.putExtra("adres_longitude",longitude);
                }

                // Eklemenin yapılabilmesi için başlangıç ve bitiş vakitlerinin anlamlı bir şekilde verilmiş olması gerekir.
                // Etkinlik adının verilmiş olması gerekir. Bu bilgilerin kontrolü yapılır ve hepsi düzgün ise ekleme yapılıp aktivite sonlanır.

                final Calendar c = Calendar.getInstance();
                int yil = c.get(Calendar.YEAR);
                int ay = c.get(Calendar.MONTH);
                int gun = c.get(Calendar.DAY_OF_MONTH);

                String bugun = String.format("%02d/%02d/%d", gun, ay+1,yil);

                if(_etkinlik_adi.matches("")){
                    Toast.makeText(EtkinlikEklemeEkrani.this,"Başlık Boş Bırakılamaz!",Toast.LENGTH_SHORT).show();
                }
                else if(btn_baslangic_tarihi.getText().toString().compareTo("__ / __ / __")==0 || btn_bitis_tarihi.getText().toString().compareTo("__ / __ / __")==0){
                    Toast.makeText(EtkinlikEklemeEkrani.this,"Tarihleri Belirtiniz!",Toast.LENGTH_SHORT).show();
                }
                else if(!once_mi(btn_baslangic_tarihi.getText().toString(),btn_bitis_tarihi.getText().toString())){
                    Toast.makeText(EtkinlikEklemeEkrani.this,"Bitiş Tarihi Başlangıçtan Önce Olamaz!",Toast.LENGTH_SHORT).show();
                }
                else if (bugun.compareTo(btn_baslangic_tarihi.getText().toString())!=0&&!once_mi(bugun,btn_baslangic_tarihi.getText().toString())){
                    Toast.makeText(EtkinlikEklemeEkrani.this,"Geçmiş Tarihe Etkinlik Eklenemez!",Toast.LENGTH_SHORT).show();
                }
                else{
                    setResult(Activity.RESULT_OK,i);
                    finish();
                }
            }
        });

        // Kaydedilen ayarlardan kullanılacak temanın bilgisi alınır ve ilgili tema ayarlanır.
        dark_light = sharedPreferences.getBoolean("dark_light",false);
        if(dark_light)
            dark_moda_gec();
        else
            light_moda_gec();

    }

    // Sorgulanan , tarih parametresinden önce mi diye kontrol edilir.
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
            return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        sqLiteVeriErisimi = new SQLite_Veri_Erisimi(EtkinlikEklemeEkrani.this);
        sqLiteVeriErisimi.baglanti_ac();
        // Konum ekranını çağırınca gelen sonucu kaydet.
        if (requestCode == KONUM_AL) {
            if(resultCode== Activity.RESULT_OK){

                latitude = data.getDoubleExtra("latitude",0);
                longitude = data.getDoubleExtra("longitude",0);
                String adres = data.getStringExtra("acik_adres");
                etkinlik_adresi.setText(adres);
            }
            // Hatırlatmaları çağırınca gelen sonucu kaydet.
        }else if (requestCode == HATIRLATMA_CODE) {
            if(resultCode== Activity.RESULT_OK){
                Toast.makeText(EtkinlikEklemeEkrani.this,"Hatırlatmalar Güncellendi.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        //sqLiteVeriErisimi = new SQLite_Veri_Erisimi(this);
        sqLiteVeriErisimi.baglanti_ac();
        super.onResume();
    }

    @Override
    protected void onPause() {
        sqLiteVeriErisimi.baglanti_kapat();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        sqLiteVeriErisimi.baglanti_kapat();
        super.onDestroy();
    }


    // Temayı dark moda geçirir.
    public void dark_moda_gec(){

        etkinlik_layout.setBackgroundColor(Color.DKGRAY);

        textView_bitis_tarihi.setTextColor(Color.WHITE);
        textView_baslangic_tarihi.setTextColor(Color.WHITE);
        textView_etkinlik_detayi.setTextColor(Color.WHITE);
        textView_etkinlik_adi.setTextColor(Color.WHITE);
        textView_etkinlik_tipi.setTextColor(Color.WHITE);

        // EditText bilgileri.
        etkinlik_detayi.setTextColor(Color.WHITE);
        etkinlik_detayi.setBackgroundColor(Color.GRAY);
        etkinlik_adresi.setTextColor(Color.WHITE);
        etkinlik_adresi.setBackgroundColor(Color.GRAY);
        etkinlik_adi.setTextColor(Color.WHITE);
        etkinlik_adi.setBackgroundColor(Color.GRAY);

        etkinlik_tipi.getBackground().setColorFilter(getResources().getColor(R.color.colorWhiteDarker), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<CharSequence> adapter_etkinlik_tipi = ArrayAdapter.createFromResource(this,
                R.array.EtkinlikTipi, R.layout.spinner_dark_goruntu);
        adapter_etkinlik_tipi.setDropDownViewResource(R.layout.spinner_dark_goruntu);
        etkinlik_tipi.setAdapter(adapter_etkinlik_tipi);

        btn_adres_goster.setBackgroundColor(Color.GRAY);
        btn_adres_goster.setTextColor(Color.WHITE);
        btn_adres_ekle.setBackgroundColor(Color.GRAY);
        btn_adres_ekle.setTextColor(Color.WHITE);
        btn_hatirlatma_zamanlari.setBackgroundColor(Color.GRAY);
        btn_hatirlatma_zamanlari.setTextColor(Color.WHITE);
        btn_bitis_tarihi.setBackgroundColor(Color.GRAY);
        btn_bitis_tarihi.setTextColor(Color.WHITE);
        btn_baslangic_tarihi.setBackgroundColor(Color.GRAY);
        btn_baslangic_tarihi.setTextColor(Color.WHITE);
        btn_etkinlik_ekle.setBackgroundColor(Color.GRAY);
        btn_etkinlik_ekle.setTextColor(Color.WHITE);

    }

    // Temayı light moda geçirir.
    public void light_moda_gec(){

        etkinlik_layout.setBackgroundColor(Color.WHITE);

        textView_bitis_tarihi.setTextColor(Color.BLACK);
        textView_baslangic_tarihi.setTextColor(Color.BLACK);
        textView_etkinlik_detayi.setTextColor(Color.BLACK);
        textView_etkinlik_adi.setTextColor(Color.BLACK);
        textView_etkinlik_tipi.setTextColor(Color.BLACK);


        // Bunlar Edittext
        etkinlik_detayi.setTextColor(Color.BLACK);
        etkinlik_detayi.setBackgroundColor(getResources().getColor(R.color.colorWhiteDarker));
        etkinlik_adresi.setTextColor(Color.BLACK);
        etkinlik_adresi.setBackgroundColor(getResources().getColor(R.color.colorWhiteDarker));
        etkinlik_adi.setTextColor(Color.BLACK);
        etkinlik_adi.setBackgroundColor(getResources().getColor(R.color.colorWhiteDarker));

        etkinlik_tipi.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<CharSequence> adapter_etkinlik_tipi = ArrayAdapter.createFromResource(this,
                R.array.EtkinlikTipi, R.layout.spinner_light_goruntu);
        adapter_etkinlik_tipi.setDropDownViewResource(R.layout.spinner_light_goruntu);
        etkinlik_tipi.setAdapter(adapter_etkinlik_tipi);

        btn_adres_goster.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_adres_goster.setTextColor(Color.BLACK);
        btn_adres_ekle.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_adres_ekle.setTextColor(Color.BLACK);
        btn_hatirlatma_zamanlari.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_hatirlatma_zamanlari.setTextColor(Color.BLACK);
        btn_bitis_tarihi.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_bitis_tarihi.setTextColor(Color.BLACK);
        btn_baslangic_tarihi.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_baslangic_tarihi.setTextColor(Color.BLACK);
        btn_etkinlik_ekle.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_etkinlik_ekle.setTextColor(Color.BLACK);
    }

}

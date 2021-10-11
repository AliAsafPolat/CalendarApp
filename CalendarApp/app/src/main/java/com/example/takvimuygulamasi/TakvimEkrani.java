package com.example.takvimuygulamasi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

// Uygulamanın ana ekranı budur.
public class TakvimEkrani extends AppCompatActivity {
    CalendarView takvim;
    Button btn_yeni_etkinlik;
    TextView textView_info;
    ConstraintLayout takvim_layout;
    ConstraintLayout takvim_icteki_layout;

    RecyclerView recyclerView_etkinlikler;
    EtkinlikAdapter etkinlikAdapter;
    ArrayList<Etkinlik> gun_etkinlikleri;
    LinearLayoutManager layoutManager;

    SharedPreferences sharedPreferences;
    SQLite_Veri_Erisimi sqlite_baglanti;

    String tarih;
    Etkinlik son_silinen_etkinlik;
    Boolean dark_light;

    final int ICERIK_GOSTER_KOD = 101;
    final int YENI_ETKİNLİK_EKLE_KOD = 102;
    private static final int LOCATION_SERVICE_CODE = 105;
    public static final String VARSAYILAN_AYARLAR = "Varsayilan_Ayarlar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takvim_ekrani);

        // Uygulama başlatılırken konum izninin alınmasını istiyorum. Daha sonra adres bilgisi için kullanılacak.
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_SERVICE_CODE);

        // Veritabanına erişmek için bağlantı nesnesi oluştur.
        sqlite_baglanti = new SQLite_Veri_Erisimi(TakvimEkrani.this);
        sqlite_baglanti.baglanti_ac();

        btn_yeni_etkinlik = findViewById(R.id.buttonYeniEtkinlik);
        takvim = findViewById(R.id.calendarViewTakvim);
        textView_info = findViewById(R.id.textView_Etkinlikler);
        recyclerView_etkinlikler = findViewById(R.id.recyclarView_etkinlikler);
        takvim_layout = findViewById(R.id.takvimEkraniLayout);
        takvim_icteki_layout = findViewById(R.id.takvimEkraniConstLayoutIceri);

        // Varsayılan ayarlara erişmek için sharedpreferencesten yararlanılır.
        sharedPreferences = getSharedPreferences(VARSAYILAN_AYARLAR, Context.MODE_PRIVATE);

        // Eğer tema dark-light olarak ayarlanmış ise değiştirilir.
        dark_light = sharedPreferences.getBoolean("dark_light",false);
        if(dark_light)
            dark_moda_gec();
        else
            light_moda_gec();


        // Takvim açıldığında hangi gündeyse o gündeki görevlerin gözükmesi gereklidir.
        final Calendar c = Calendar.getInstance();
        int yil = c.get(Calendar.YEAR);
        int ay = c.get(Calendar.MONTH);
        int gun = c.get(Calendar.DAY_OF_MONTH);
        tarih = String.format("%02d/%02d/%d", gun, ay+1,yil);

        textView_info.setText(tarih+" Günü Etkinlikleri");
        gun_etkinlikleri = sqlite_baglanti.etkinlikleri_getir(tarih);
        etkinlikAdapter = adapter_olustur(gun_etkinlikleri);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_etkinlikler.setLayoutManager(layoutManager);
        recyclerView_etkinlikler.setAdapter(etkinlikAdapter);

        // Takvimde farklı günlere tıklandığında o günlerin etkinliklerinin listelenmesi gerekir. Adapter değiştir.
        takvim.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                tarih = String.format("%02d/%02d/%d", dayOfMonth, month+1,year);
                textView_info.setText(tarih+" Günü Etkinlikleri");

                gun_etkinlikleri = sqlite_baglanti.etkinlikleri_getir(tarih);
                etkinlikAdapter = adapter_olustur(gun_etkinlikleri);
                recyclerView_etkinlikler.setAdapter(etkinlikAdapter);
                etkinlikAdapter.notifyDataSetChanged();
            }
        });

        // Yeni etkinlik eklemek için etkinlik ekleme ekranına yönlendir...
        btn_yeni_etkinlik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent (TakvimEkrani.this,EtkinlikEklemeEkrani.class);
                startActivityForResult(i,YENI_ETKİNLİK_EKLE_KOD);
            }
        });

    }

    // Verilen listeye göre recycler view adapteri ve click eventleri tanımlayıp döndürür.
    public EtkinlikAdapter adapter_olustur(ArrayList<Etkinlik> liste){

        final EtkinlikAdapter yeni_adapter = new EtkinlikAdapter(this,liste,false);
        yeni_adapter.setOnItemClickListener(new EtkinlikAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                Etkinlik tiklanan_etkinlik = gun_etkinlikleri.get(position);

                Intent i = new Intent(TakvimEkrani.this,EtkinlikEklemeEkrani.class);
                i.putExtra("etkinlik_adi",tiklanan_etkinlik.getEtkinlik_adi());
                i.putExtra("etkinlik_detayi",tiklanan_etkinlik.getEtkinlik_detayi());
                i.putExtra("baslangic_tarihi",tiklanan_etkinlik.getBaslangic_tarihi());
                i.putExtra("bitis_tarihi",tiklanan_etkinlik.getBitis_tarihi());
                i.putExtra("adres",tiklanan_etkinlik.getAdres());
                i.putExtra("etkinlik_turu",tiklanan_etkinlik.getEtkinlik_turu());
                i.putExtra("hatirlatma_tarihi",tiklanan_etkinlik.getHatirlatma_tarihi());
                i.putExtra("hatirlatma_saati",tiklanan_etkinlik.getHatirlatma_saati());
                i.putExtra("adres_latitude",tiklanan_etkinlik.getAdres_latitude());
                i.putExtra("adres_longitude",tiklanan_etkinlik.getAdres_longitude());

                // Etkinliklere tıklandığı zaman güncelleme yapılabileceği için etkinlik silinir sonrasında etkinlik kaydedildiğinde yeniden eklenir.
                son_silinen_etkinlik = tiklanan_etkinlik;
                gun_etkinlikleri.remove(position);
                yeni_adapter.notifyDataSetChanged();
                sqlite_baglanti.etkinlik_sil(tiklanan_etkinlik);
                startActivityForResult(i,ICERIK_GOSTER_KOD);
            }

            // Etkinliği silmek için tanımlanan event.
            @Override
            public void onDeleteClick(final int position) {
                // Etkinlik silme işlemi gerçekleşirken kullanıcıya bilgi versin ve onay istesin...
                new AlertDialog.Builder(TakvimEkrani.this)
                        .setTitle("Etkinlik Sil")
                        .setMessage("Bu etkinliği gerçekten silmek istiyor musunuz?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Etkinlik silinecek_item = gun_etkinlikleri.get(position);
                                // Hem hatırlatmaları silsin hem de günleri silsin. İki ayrı veritabanı tablosu tutulmuştu...
                                sqlite_baglanti.etkinlik_sil(silinecek_item);
                                sqlite_baglanti.gun_hatirlatmalarini_sil(silinecek_item.getEtkinlik_adi(),silinecek_item.getBaslangic_tarihi());
                                gun_etkinlikleri.remove(position);
                                etkinlikAdapter.notifyItemRemoved(position);
                                // Kullanıcı bilgilendirilsin.
                                Toast.makeText(TakvimEkrani.this,"Etkinlik Silindi.",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }

            // Etkinliği sosyal medya uygulamaları üzerinden göndermek için yazılan event.
            @Override
            public void onSendClick(int position) {
                // Tıklanan etkinliği al.
                Etkinlik e = gun_etkinlikleri.get(position);

                // Etkinlik bilgisini mesaj olarak yaz.
                String mesaj = "Merhaba! '"+e.getEtkinlik_adi()+"' adında bir "+e.getEtkinlik_turu().toLowerCase()+" etkinliğimiz bulunuyor.\n";

                // Eğer adres bilgisi var ise onu da mesajın sonuna ekle.
                if(e.getAdres_longitude()!=0 || e.getAdres_longitude()!=0){
                    mesaj  = mesaj + "\nİşte adresimiz! \n\n" +"http://www.google.com/maps/place/" + e.getAdres_latitude()+ "," + e.getAdres_longitude();
                }

                // Etkinliği gönder.
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, mesaj);
                startActivity(Intent.createChooser(i, "Bunun ile paylaş"));
            }
        });
        // Oluşturulan adapteri döndür...
        return yeni_adapter;
    }

    // İzinleri kontrol eder.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(TakvimEkrani.this, permission)
                == PackageManager.PERMISSION_DENIED) {
            // Alınan izin bilgisi
            ActivityCompat.requestPermissions(TakvimEkrani.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Log.i("izin","Lokasyon izni bulunmaktadır.");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        if (requestCode == LOCATION_SERVICE_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(TakvimEkrani.this,
                        "Lokasyon Erişim İzni Verildi",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(TakvimEkrani.this,
                        "Lokasyon Erişim İzni Reddedildi",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }

    @Override
    protected void onResume() {
        sqlite_baglanti = new SQLite_Veri_Erisimi(TakvimEkrani.this);
        sqlite_baglanti.baglanti_ac();

        // Ayarlar ekranına bu ekrandan gidildiği için ayarlardaki işlem bitince temanın güncellenmesi gerekir.
        sharedPreferences = getSharedPreferences(VARSAYILAN_AYARLAR, Context.MODE_PRIVATE);

        dark_light = sharedPreferences.getBoolean("dark_light",false);
        if(dark_light)
            dark_moda_gec();
        else
            light_moda_gec();

        super.onResume();
    }

    @Override
    protected void onPause() {
        // Açılan veritabanı bağlantısını kapat.
        sqlite_baglanti.baglanti_kapat();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        sqlite_baglanti.baglanti_kapat();
        super.onDestroy();
    }

    // Etkinlik gösterme ve etkinlik ekleme şeklinde dönüşler olabilir.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        sqlite_baglanti = new SQLite_Veri_Erisimi(TakvimEkrani.this);
        sqlite_baglanti.baglanti_ac();

        // startActivityForResult() methodu çalıştırıldığında yeni etkinlik ekleme tuşuna basıldı ise.
        if (requestCode == YENI_ETKİNLİK_EKLE_KOD) {
            if(resultCode== Activity.RESULT_OK){
                String etkinlik_adi = data.getStringExtra("etkinlik_adi");
                String etkinlik_detayi=data.getStringExtra("etkinlik_detayi");
                String baslangic_tarihi=data.getStringExtra("baslangic_tarihi");
                String bitis_tarihi=data.getStringExtra("bitis_tarihi");
                String adres=data.getStringExtra("adres");
                String etkinlik_turu=data.getStringExtra("etkinlik_turu");
                double latitude = data.getDoubleExtra("adres_latitude",-1);
                double longitude = data.getDoubleExtra("adres_longitude",-1);

                ArrayList<String> hatirlatma_tarihleri = sqlite_baglanti.hatirlatma_tarihlerini_getir(etkinlik_adi,baslangic_tarihi);
                ArrayList<String> hatirlatma_saatleri = sqlite_baglanti.hatirlatma_saatlerini_getir(etkinlik_adi,baslangic_tarihi);

                // Dönen değerler ile etkinlik oluşturup veritabanına ekle.
                Etkinlik etkinlik = new Etkinlik(etkinlik_adi,etkinlik_detayi,baslangic_tarihi,bitis_tarihi,adres,etkinlik_turu,hatirlatma_tarihleri,hatirlatma_saatleri,latitude,longitude);
                sqlite_baglanti.etkinlik_ekle(etkinlik);

                // Kullanıcıyı bilgilendir.
                Toast.makeText(TakvimEkrani.this,"Etkinlik eklendi.",Toast.LENGTH_SHORT).show();

                // Takvimde aynı gün üzerinde duruyorsa ekleme animasyonlu olarak gözüksün...
                if(etkinlik.getBaslangic_tarihi().compareTo(tarih)==0) {
                    gun_etkinlikleri = sqlite_baglanti.etkinlikleri_getir(tarih);
                    etkinlikAdapter = adapter_olustur(gun_etkinlikleri);
                    recyclerView_etkinlikler.setAdapter(etkinlikAdapter);
                    etkinlikAdapter.notifyItemInserted(gun_etkinlikleri.size() - 1);
                }

            }else if(resultCode==Activity.RESULT_CANCELED){
                // Ekleme yapılmadan dönülmüştür. Kullanıcıyı bilgilendir.
                Toast.makeText(TakvimEkrani.this,"Ekleme yapılmadı!",Toast.LENGTH_SHORT).show();
            }

            // startActivityForResult() methodu çalıştırıldığında daha önceden eklenen item görüntülenmek istendi ise.
        } else if (requestCode == ICERIK_GOSTER_KOD) {
            if(resultCode== Activity.RESULT_OK){    // Sonucu istendiği gibi döndürürse.
                String etkinlik_adi = data.getStringExtra("etkinlik_adi");
                String etkinlik_detayi=data.getStringExtra("etkinlik_detayi");
                String baslangic_tarihi=data.getStringExtra("baslangic_tarihi");
                String bitis_tarihi=data.getStringExtra("bitis_tarihi");
                String adres=data.getStringExtra("adres");
                String etkinlik_turu=data.getStringExtra("etkinlik_turu");

                ArrayList<String> hatirlatma_tarihleri = sqlite_baglanti.hatirlatma_tarihlerini_getir(etkinlik_adi,baslangic_tarihi);
                ArrayList<String> hatirlatma_saatleri = sqlite_baglanti.hatirlatma_saatlerini_getir(etkinlik_adi,baslangic_tarihi);
                double latitude = data.getDoubleExtra("adres_latitude",0);
                double longitude = data.getDoubleExtra("adres_longitude",0);

                // Değişen bilgilere göre etkinliği yeniden ekle.
                Etkinlik etkinlik = new Etkinlik(etkinlik_adi,etkinlik_detayi,baslangic_tarihi,bitis_tarihi,adres,etkinlik_turu,hatirlatma_tarihleri,hatirlatma_saatleri,latitude,longitude);
                sqlite_baglanti.etkinlik_ekle(etkinlik);

                // Kullanıcıyı bilgilendir.
                Toast.makeText(TakvimEkrani.this,"Etkinlik düzenlendi.",Toast.LENGTH_SHORT).show();

                // Takvimde aynı gün üzerinde duruyorsa ekleme animasyonlu olarak gözüksün...
                if(etkinlik.getBaslangic_tarihi().compareTo(tarih)==0) {
                    gun_etkinlikleri = sqlite_baglanti.etkinlikleri_getir(tarih);
                    etkinlikAdapter = adapter_olustur(gun_etkinlikleri);
                    recyclerView_etkinlikler.setAdapter(etkinlikAdapter);
                    etkinlikAdapter.notifyItemInserted(gun_etkinlikleri.size() - 1);
                }

            }else if(resultCode==Activity.RESULT_CANCELED){
                //İçerik gösterildikten sonra kaydedilemeden dönerse silinen etkinliği geri eklesin.
                gun_etkinlikleri.add(son_silinen_etkinlik);
                sqlite_baglanti.etkinlik_ekle(son_silinen_etkinlik);
                etkinlikAdapter.notifyDataSetChanged();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Varsayılan ayarlar ekranına geçmek için...
            case R.id.varsayilan_ayarlar:
                Intent i = new Intent(TakvimEkrani.this,AyarlarEkrani.class);
                startActivity(i);
                return true;
            // Yaklaşan etkinlikler ekranına geçmek için...
            case R.id.yaklasan_etkinlikler:
                Intent in = new Intent(TakvimEkrani.this,YaklasanEtkinlikGostermeEkrani.class);
                startActivity(in);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Temayı dark mode a geçirir.
    public void dark_moda_gec(){

        takvim_layout.setBackgroundColor(Color.GRAY);
        takvim_icteki_layout.setBackgroundColor(Color.DKGRAY);

        textView_info.setTextColor(Color.WHITE);
        recyclerView_etkinlikler.setBackgroundColor(Color.GRAY);

    }
    // Temayı light moda geçirir.
    public void light_moda_gec(){

        takvim_layout.setBackgroundColor(Color.WHITE);
        takvim_icteki_layout.setBackgroundColor(Color.WHITE);

        recyclerView_etkinlikler.setBackgroundColor(Color.WHITE);
        textView_info.setTextColor(Color.BLACK);

    }

}

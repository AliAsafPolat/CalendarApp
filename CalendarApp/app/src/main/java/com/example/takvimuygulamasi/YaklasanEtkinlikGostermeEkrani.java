package com.example.takvimuygulamasi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

// Yaklaşan etkinliklerin gösterildiği ekranın kod kısmıdır.
public class YaklasanEtkinlikGostermeEkrani extends AppCompatActivity {

    ConstraintLayout etkinlik_layout;

    TextView textView_etkinlik_tarihi;
    Button btn_gunluk_gosterim;
    Button btn_haftalik_gosterim;
    Button btn_aylik_gosterim;
    Button btn_tamam;

    RecyclerView recyclerView_yaklasan_etkinlikler;
    EtkinlikAdapter etkinlikAdapter;
    ArrayList<Etkinlik> yaklasan_etkinlikler;
    LinearLayoutManager layoutManager;

    SQLite_Veri_Erisimi sqlite_baglanti;
    String bugun;

    // Varsayılan ayarlar erişmek için kullanılır.
    SharedPreferences sharedPreferences;
    public static final String VARSAYILAN_AYARLAR = "Varsayilan_Ayarlar";
    boolean dark_light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yaklasan_etkinlik_gosterme_ekrani);

        etkinlik_layout = findViewById(R.id.yaklasan_etkinlik_layout);
        textView_etkinlik_tarihi = findViewById(R.id.textViewYaklasanEtkinlikTarih);
        btn_gunluk_gosterim = findViewById(R.id.buttonGunlukGosterim);
        btn_haftalik_gosterim = findViewById(R.id.buttonHaftalikGosterim);
        btn_aylik_gosterim = findViewById(R.id.buttonAylikGosterim);
        btn_tamam = findViewById(R.id.buttonYaklasanEtkinliklerTamam);
        recyclerView_yaklasan_etkinlikler = findViewById(R.id.recyclerViewYaklasanEtkinlikler);

        yaklasan_etkinlikler = new ArrayList<>();

        // Yaklaşan etkinlikler için bir recycler view yapısı kullanıldı.
        etkinlikAdapter = new EtkinlikAdapter(this,yaklasan_etkinlikler,true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_yaklasan_etkinlikler.setLayoutManager(layoutManager);
        recyclerView_yaklasan_etkinlikler.setAdapter(etkinlikAdapter);

        // Veritabanına erişmek için obje oluşturuldu.
        sqlite_baglanti = new SQLite_Veri_Erisimi(YaklasanEtkinlikGostermeEkrani.this);
        sqlite_baglanti.baglanti_ac();

        // Bugünün tarihini alıp ekranda göster.
        final Calendar c = Calendar.getInstance();
        int yil = c.get(Calendar.YEAR);
        int ay = c.get(Calendar.MONTH);
        int gun = c.get(Calendar.DAY_OF_MONTH);

        bugun = String.format("%02d/%02d/%d", gun, ay+1,yil);

        textView_etkinlik_tarihi.setText("Güncel Tarih : " + bugun);


        // Varsayılan ayarları al ve temayı ona göre ayarla.
        sharedPreferences = getSharedPreferences(VARSAYILAN_AYARLAR, Context.MODE_PRIVATE);

        // Bugünun etkinliklerini gösterir.
        btn_gunluk_gosterim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yaklasan_etkinlikler = sqlite_baglanti.etkinlikleri_getir(bugun);
                etkinlikAdapter = adapter_olustur(yaklasan_etkinlikler);
                recyclerView_yaklasan_etkinlikler.setAdapter(etkinlikAdapter);
                Toast.makeText(YaklasanEtkinlikGostermeEkrani.this,"Gün içerisindeki etkinlikler listelendi.",Toast.LENGTH_SHORT).show();
            }

        });

        // Bu hafta içerisindeki etkinlikleri gösterir.
        btn_haftalik_gosterim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    yaklasan_etkinlikler = sqlite_baglanti.haftalik_etkinlikleri_getir(bugun);
                    etkinlikAdapter = adapter_olustur(yaklasan_etkinlikler);
                    recyclerView_yaklasan_etkinlikler.setAdapter(etkinlikAdapter);
                    Toast.makeText(YaklasanEtkinlikGostermeEkrani.this,"Hafta içerisindeki etkinlikler listelendi.",Toast.LENGTH_SHORT).show();

                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(YaklasanEtkinlikGostermeEkrani.this,"Beklenmeyen bir hata oluştu!",Toast.LENGTH_SHORT).show();
                }
            }

        });

        // Bu ay içerisindeki etkinlikleri gösterir.
        btn_aylik_gosterim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    yaklasan_etkinlikler = sqlite_baglanti.aylik_etkinlikleri_getir(bugun);
                    etkinlikAdapter = adapter_olustur(yaklasan_etkinlikler);
                    recyclerView_yaklasan_etkinlikler.setAdapter(etkinlikAdapter);
                    Toast.makeText(YaklasanEtkinlikGostermeEkrani.this,"Ay içerisindeki etkinlikler listelendi.",Toast.LENGTH_SHORT).show();

                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(YaklasanEtkinlikGostermeEkrani.this,"Beklenmeyen bir hata oluştu!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Aktiviteyi sonlandır.
        btn_tamam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Varsayılan ayarlara göre temayı düzenle.
        dark_light = sharedPreferences.getBoolean("dark_light",false);
        if(dark_light)
            dark_moda_gec();
        else
            light_moda_gec();
    }

    // Etkinlikleri listelemek için adapter oluşturur ve döndürür.
    public EtkinlikAdapter adapter_olustur(ArrayList<Etkinlik> etkinlik){
        EtkinlikAdapter res = new EtkinlikAdapter(YaklasanEtkinlikGostermeEkrani.this,etkinlik,true);

        res.setOnItemClickListener(new EtkinlikAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Yaklaşan etkinlik bilgilerini göstermek için kendi oluşturduğum dialog box ekranı kullanılır.
                dialog_ac(position);
            }

            @Override
            public void onDeleteClick(int position) {
                // Bu methodu yazmama gerek yok çünkü bu ekranda silme butonu gözükmeyecektir...
                }

            @Override
            public void onSendClick(int position) {
                // Bu methodu yazmama gerek yok çünkü bu ekranda gönderme butonu gözükmeyecektir...
            }
        });

        return res;
    }

    // Kendi oluşturduğum dialog box ekranı gösterir.
    public void dialog_ac(final int position){
        final AlertDialog.Builder builder = new AlertDialog.Builder(YaklasanEtkinlikGostermeEkrani.this);
        View mView = getLayoutInflater().inflate(R.layout.alert_dialog_etkinlik_listeleme,null);

        // Dialog box üzerindeki widgetlar tanımlanır.
        final TextView _etkinlik_adi = mView.findViewById(R.id.textView_etkinlik_adi_enter);
        final TextView _etkinlik_detayi = mView.findViewById(R.id.textView_etkinlik_detayi_enter);
        final TextView _etkinlik_adresi = mView.findViewById(R.id.textView_etkinlik_adresi_enter);
        final Button _btn_tamam = mView.findViewById(R.id.button_etkinlik_gosterme_tamam);

        // Tıklanan etkinlik bilgileri alınır.
        Etkinlik e = yaklasan_etkinlikler.get(position);
        String etkinlik_detayi_ = e.getEtkinlik_detayi();
        String etkinlik_adresi_ = e.getAdres();

        // Eğer detay ve adres kısımlarında bilgi girişi yapılmamış ise kullanıcıya bunun bilgisi verilir.
        if(etkinlik_detayi_==null || etkinlik_detayi_.matches("")){
            _etkinlik_detayi.setText("Detay belirtilmemiştir.");
        }else
            _etkinlik_detayi.setText(e.getEtkinlik_detayi());

        if( etkinlik_adresi_==null || etkinlik_adresi_.matches("")){
            _etkinlik_adresi.setText("Adres belirtilmemiştir.");
        }else
            _etkinlik_adresi.setText(e.getAdres());

        // Dialog üzerinde etkinlik adı ayarlanır.
        _etkinlik_adi.setText(e.getEtkinlik_adi());

        // Dialog görüntüsü atanır.
        builder.setView(mView);
        final AlertDialog dialog = builder.create();

        // Tamama tıklandığında dialog kapanır
        _btn_tamam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Dialog göster.
        dialog.show();

    }

    // Temayı dark moda geçirir.
    public void dark_moda_gec(){
        etkinlik_layout.setBackgroundColor(Color.DKGRAY);

        textView_etkinlik_tarihi.setTextColor(Color.WHITE);
        btn_tamam.setBackgroundColor(Color.GRAY);
        btn_tamam.setTextColor(Color.WHITE);
        btn_aylik_gosterim.setBackgroundColor(Color.GRAY);
        btn_aylik_gosterim.setTextColor(Color.WHITE);
        btn_haftalik_gosterim.setBackgroundColor(Color.GRAY);
        btn_haftalik_gosterim.setTextColor(Color.WHITE);
        btn_gunluk_gosterim.setBackgroundColor(Color.GRAY);
        btn_gunluk_gosterim.setTextColor(Color.WHITE);
    }

    // Temayı light moda geçirir.
    public void light_moda_gec(){
        etkinlik_layout.setBackgroundColor(Color.WHITE);

        textView_etkinlik_tarihi.setTextColor(Color.RED);
        btn_tamam.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_tamam.setTextColor(Color.BLACK);
        btn_aylik_gosterim.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_aylik_gosterim.setTextColor(Color.BLACK);
        btn_haftalik_gosterim.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_haftalik_gosterim.setTextColor(Color.BLACK);
        btn_gunluk_gosterim.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        btn_gunluk_gosterim.setTextColor(Color.BLACK);
    }
}

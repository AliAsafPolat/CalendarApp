package com.example.takvimuygulamasi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Veritabanı tablolarının oluşturulduğu classdır.
public class SQLite_Baglantisi extends SQLiteOpenHelper {

    public SQLite_Baglantisi(Context con){
        super(con,"etkinlikler",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table  etkinlikler (id integer primary key autoincrement not null,etkinlik_adi text not null, etkinlik_detayi text," +
                "baslangic_tarihi text,bitis_tarihi text, adres text,etkinlik_turu text,adres_latitude double,adres_longitude double)";
        db.execSQL(sql);

        String sql2 = "create table etkinlik_hatirlatma (id integer primary key autoincrement not null, etkinlik_adi text not null, baslangic_tarihi text,hatirlatma_tarihi text," +
                "hatirlatma_saati text)";
        db.execSQL(sql2);

        Log.i("takvim","Veritabanları oluşturuldu!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // upgrade işlemleri var ise yazılması gereken method...
    }


}

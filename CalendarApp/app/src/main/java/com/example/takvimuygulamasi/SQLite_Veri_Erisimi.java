package com.example.takvimuygulamasi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

// Veritabanına erişimin sağlandığı classdır.
public class SQLite_Veri_Erisimi {
    SQLite_Baglantisi baglantim;
    SQLiteDatabase db;

    public SQLite_Veri_Erisimi(Context c){
        baglantim = new SQLite_Baglantisi(c);
    }

    // Veritabanına erişmek için bağlantı açılır.
    public void baglanti_ac(){
        db = baglantim.getWritableDatabase();
    }

    // Açılan bağlantı kapatılır.
    public void baglanti_kapat(){
        baglantim.close();
        db.close();
    }

    // Veritabanına aldığı etkinliği ekler.
    public void etkinlik_ekle(Etkinlik e){
        ContentValues contentValues = new ContentValues();

        contentValues.put("etkinlik_adi",e.getEtkinlik_adi());
        contentValues.put("etkinlik_detayi",e.getEtkinlik_detayi());
        contentValues.put("baslangic_tarihi",e.getBaslangic_tarihi());
        contentValues.put("bitis_tarihi",e.getBitis_tarihi());
        contentValues.put("adres",e.getAdres());
        contentValues.put("etkinlik_turu",e.getEtkinlik_turu());
        contentValues.put("adres_latitude",e.getAdres_latitude());
        contentValues.put("adres_longitude",e.getAdres_longitude());

        db.insert("etkinlikler",null,contentValues);
    }
    // Aldığı etkinliği veritabanından siler.
    public void etkinlik_sil(Etkinlik e){
        String etkinlik_tarih = e.getBaslangic_tarihi();
        String etkinlik_adi = e.getEtkinlik_adi();

        db.delete("etkinlikler","baslangic_tarihi='"+etkinlik_tarih+"' and etkinlik_adi='"+etkinlik_adi+"'",null);

    }
    // Verilen tarih bilgisine göre veritabanındaki ilgili tarihte olan etkinlikleri liste halinde döndürür.
    public ArrayList<Etkinlik> etkinlikleri_getir(String date){

        String kolonlar[]= {"etkinlik_adi", "etkinlik_detayi","baslangic_tarihi","bitis_tarihi","adres","etkinlik_turu","adres_latitude","adres_longitude"};
        ArrayList<Etkinlik> liste = new ArrayList<Etkinlik>();
        Cursor cursor = db.query("etkinlikler",kolonlar,"baslangic_tarihi=?",new String []{date},null,null,null);
        cursor.moveToFirst();

        // Veritabanındaki ilk eleman bilgisini kaçırmamak için.
        if(cursor.isFirst()){
            String etkinlik_adi=cursor.getString(0);
            if(etkinlik_adi!=null){
                String etkinlik_detayi=cursor.getString(1);
                String baslangic_tarihi=cursor.getString(2);
                String bitis_tarihi=cursor.getString(3);
                String adres=cursor.getString(4);
                String etkinlik_turu=cursor.getString(5);
                double adres_latitude = cursor.getDouble(6);
                double adres_longitude = cursor.getDouble(7);

                // Burada hatırlatma tarihleri alınacak. Hatırlatma tarihleri ayrı bir tabloda tutulduğu için bunların da çekilmesi lazım.
                ArrayList<String> hatirlatma_tarihi =hatirlatma_tarihlerini_getir(etkinlik_adi,baslangic_tarihi);
                ArrayList<String> hatirlatma_saati = hatirlatma_saatlerini_getir(etkinlik_adi,baslangic_tarihi);

                Etkinlik e = new Etkinlik(etkinlik_adi,etkinlik_detayi,baslangic_tarihi,
                        bitis_tarihi,adres,etkinlik_turu,hatirlatma_tarihi,hatirlatma_saati,adres_latitude,adres_longitude);
                liste.add(e);
            }
        }
        // İlk elemandan sonra gelen elemanlar için.
        while(cursor.moveToNext()){
            String etkinlik_adi=cursor.getString(0);
            String etkinlik_detayi=cursor.getString(1);
            String baslangic_tarihi=cursor.getString(2);
            String bitis_tarihi=cursor.getString(3);
            String adres=cursor.getString(4);
            String etkinlik_turu=cursor.getString(5);
            double adres_latitude = cursor.getDouble(6);
            double adres_longitude = cursor.getDouble(7);

            // Burada hatırlatma tarihleri alınacak. Hatırlatma tarihleri ayrı bir tabloda tutulduğu için bunların da çekilmesi lazım.
            ArrayList<String> hatirlatma_tarihi =hatirlatma_tarihlerini_getir(etkinlik_adi,baslangic_tarihi);
            ArrayList<String> hatirlatma_saati = hatirlatma_saatlerini_getir(etkinlik_adi,baslangic_tarihi);
            Etkinlik e = new Etkinlik(etkinlik_adi,etkinlik_detayi,baslangic_tarihi,
                    bitis_tarihi,adres,etkinlik_turu,hatirlatma_tarihi,hatirlatma_saati,adres_latitude,adres_longitude);
            liste.add(e);
        }

        return liste;
    }

    // Veritabanında tutulan ilgili etkinliğin hatırlatma tarihlerini getirir.
    public ArrayList<String> hatirlatma_tarihlerini_getir(String etkinlik_adi,String etkinlik_tarihi){
        String kolonlar[]={"hatirlatma_tarihi"};
        ArrayList<String> liste = new ArrayList<String>();
        Cursor cursor = db.query("etkinlik_hatirlatma",kolonlar,"baslangic_tarihi=? and etkinlik_adi=?",new String []{etkinlik_tarihi,etkinlik_adi},null,null,null);
        cursor.moveToFirst();

        if(cursor.isFirst()){
            String tarih = cursor.getString(0);
            if(tarih!=null)
                liste.add(tarih);
        }
        while(cursor.moveToNext()){
            String tarih = cursor.getString(0);
            liste.add(tarih);
        }
        return liste;
    }

    // Veritabanında tutulan ilgili etkinliğin hatırlatma saatlerini getirir.
    public ArrayList<String> hatirlatma_saatlerini_getir(String etkinlik_adi, String etkinlik_tarihi){
        String kolonlar[]={"hatirlatma_saati"};
        ArrayList<String> liste = new ArrayList<String>();
        Cursor cursor = db.query("etkinlik_hatirlatma",kolonlar,"baslangic_tarihi=? and etkinlik_adi=?",new String []{etkinlik_tarihi,etkinlik_adi},null,null,null);
        cursor.moveToFirst();
        if(cursor.isFirst()){
            String tarih = cursor.getString(0);
            if(tarih!=null)
                liste.add(tarih);
        }
        while(cursor.moveToNext()) {
            String tarih = cursor.getString(0);
            liste.add(tarih);
        }
        return liste;
    }

    // Verilen parametrelere göre yeni bir hatırlatma tarihi ekler.
    public void hatirlatma_tarihi_ekle(String etkinlik_adi, String baslangic_tarihi, String hatirlatma_tarihi ,String hatirlatma_saati){
        ContentValues contentValues = new ContentValues();
        contentValues.put("etkinlik_adi",etkinlik_adi);
        contentValues.put("baslangic_tarihi",baslangic_tarihi);
        contentValues.put("hatirlatma_tarihi",hatirlatma_tarihi);
        contentValues.put("hatirlatma_saati",hatirlatma_saati);

        db.insert("etkinlik_hatirlatma",null,contentValues);
    }

    // Bilgisi verilen hatırlatmanın hatırlama id sini çeker. Pending intent ile alarm kurmak için gereklidir bu bilgi...
    public int hatirlatma_id_getir(String etkinlik_adi,String etkinlik_tarihi,String hatirlatma_tarih,String hatirlatma_saat){
        String kolonlar[]={"id"};
        Integer res = new Integer(0);
        Cursor cursor = db.query("etkinlik_hatirlatma",kolonlar,"baslangic_tarihi=? and etkinlik_adi=? and hatirlatma_tarihi=? and hatirlatma_saati=?",new String []{etkinlik_tarihi,etkinlik_adi,hatirlatma_tarih,hatirlatma_saat},null,null,null);
        cursor.moveToFirst();
        if(cursor.isFirst()){
            Integer id = cursor.getInt(0);
            if(id!=null)
                res = id;
        }
        return res;
    }

    // Verilen bilgilere göre hatırlatma siler.
    public void hatirlatma_sil(String etkinlik_adi,String etkinlik_tarihi,String hatirlatma_tarih,String hatirlatma_saat){

        db.delete("etkinlik_hatirlatma","baslangic_tarihi='"+etkinlik_tarihi+"' and etkinlik_adi='"+etkinlik_adi+"' and hatirlatma_tarihi='"+hatirlatma_tarih
                +"' and hatirlatma_saati='"+hatirlatma_saat+"'",null);
    }

    // Bir güne ayrılımış tüm hatırlatmaları siler.
    public void gun_hatirlatmalarini_sil(String etkinlik_adi,String etkinlik_tarihi){
        db.delete("etkinlik_hatirlatma","baslangic_tarihi='"+etkinlik_tarihi+"' and etkinlik_adi='"+etkinlik_adi+"'",null);
    }

    // Yaklaşan etkinlikleri göstermek için verilen tarihten sonra bir hafta içerisinde olan etkinlikleri döndürür.
    public ArrayList<Etkinlik> haftalik_etkinlikleri_getir(String tarih) throws ParseException {

        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");

        Calendar hafta_son = Calendar.getInstance();
        hafta_son.setTime(sdf.parse(tarih));
        Calendar gelen_tarih = Calendar.getInstance();
        gelen_tarih.setTime(sdf.parse(tarih));
        hafta_son.add(Calendar.DAY_OF_MONTH,7);

        ArrayList<Etkinlik> liste = new ArrayList<Etkinlik>();

            Cursor cursor = db.rawQuery("select * from etkinlikler",null);
            cursor.moveToFirst();

            if(cursor.isFirst()){
               String etkinlik_adi=cursor.getString(cursor.getColumnIndex("etkinlik_adi"));
                if(etkinlik_adi!=null){
                    String etkinlik_detayi=cursor.getString(cursor.getColumnIndex("etkinlik_detayi"));
                    String baslangic_tarihi=cursor.getString(cursor.getColumnIndex("baslangic_tarihi"));
                    String bitis_tarihi=cursor.getString(cursor.getColumnIndex("bitis_tarihi"));
                    String adres=cursor.getString(cursor.getColumnIndex("adres"));
                    String etkinlik_turu=cursor.getString(cursor.getColumnIndex("etkinlik_turu"));
                    double adres_latitude = cursor.getDouble(cursor.getColumnIndex("adres_latitude"));
                    double adres_longitude = cursor.getDouble(cursor.getColumnIndex("adres_longitude"));

                    ArrayList<String> hatirlatma_tarihi = hatirlatma_tarihlerini_getir(etkinlik_adi,baslangic_tarihi);
                    ArrayList<String> hatirlatma_saati = hatirlatma_saatlerini_getir(etkinlik_adi,baslangic_tarihi);
                    Etkinlik e = new Etkinlik(etkinlik_adi,etkinlik_detayi,baslangic_tarihi,
                            bitis_tarihi,adres,etkinlik_turu,hatirlatma_tarihi,hatirlatma_saati,adres_latitude,adres_longitude);

                    Calendar baslangic_ = Calendar.getInstance();
                    baslangic_.setTime(sdf.parse(baslangic_tarihi));
                    // Eğer bir hafta içerisinde gerçekleşiyor ise listeye ekle...
                    if(hafta_son.compareTo(baslangic_)>0&&gelen_tarih.compareTo(baslangic_)<=0){
                        liste.add(e);
                    }
                }
            }
            while(cursor.moveToNext()){
                String etkinlik_adi=cursor.getString(1);
                String etkinlik_detayi=cursor.getString(2);
                String baslangic_tarihi=cursor.getString(3);
                String bitis_tarihi=cursor.getString(4);
                String adres=cursor.getString(5);
                String etkinlik_turu=cursor.getString(6);
                double adres_latitude = cursor.getDouble(7);
                double adres_longitude = cursor.getDouble(8);

                ArrayList<String> hatirlatma_tarihi =hatirlatma_tarihlerini_getir(etkinlik_adi,baslangic_tarihi);
                ArrayList<String> hatirlatma_saati = hatirlatma_saatlerini_getir(etkinlik_adi,baslangic_tarihi);
                Etkinlik e = new Etkinlik(etkinlik_adi,etkinlik_detayi,baslangic_tarihi,
                        bitis_tarihi,adres,etkinlik_turu,hatirlatma_tarihi,hatirlatma_saati,adres_latitude,adres_longitude);
                Calendar baslangic_ = Calendar.getInstance();
                baslangic_.setTime(sdf.parse(baslangic_tarihi));

                // Eğer bir hafta içerisinde gerçekleşiyorsa ekle...
                if(hafta_son.compareTo(baslangic_)>0&&gelen_tarih.compareTo(baslangic_)<=0){
                    liste.add(e);
                }
            }
        return liste;
    }

    // Verilen tarihe göre bir ay içerisindeki etkinlikleri getirir.
    public ArrayList<Etkinlik> aylik_etkinlikleri_getir(String tarih) throws ParseException {

        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");

        Calendar hafta_son = Calendar.getInstance();
        hafta_son.setTime(sdf.parse(tarih));
        Calendar gelen_tarih = Calendar.getInstance();
        gelen_tarih.setTime(sdf.parse(tarih));
        hafta_son.add(Calendar.DAY_OF_MONTH,30);

        ArrayList<Etkinlik> liste = new ArrayList<Etkinlik>();

        Cursor cursor = db.rawQuery("select * from etkinlikler",null);
        cursor.moveToFirst();

        if(cursor.isFirst()){
            String etkinlik_adi=cursor.getString(cursor.getColumnIndex("etkinlik_adi"));
            if(etkinlik_adi!=null){
                String etkinlik_detayi=cursor.getString(cursor.getColumnIndex("etkinlik_detayi"));
                String baslangic_tarihi=cursor.getString(cursor.getColumnIndex("baslangic_tarihi"));
                String bitis_tarihi=cursor.getString(cursor.getColumnIndex("bitis_tarihi"));
                String adres=cursor.getString(cursor.getColumnIndex("adres"));
                String etkinlik_turu=cursor.getString(cursor.getColumnIndex("etkinlik_turu"));
                double adres_latitude = cursor.getDouble(cursor.getColumnIndex("adres_latitude"));
                double adres_longitude = cursor.getDouble(cursor.getColumnIndex("adres_longitude"));

                ArrayList<String> hatirlatma_tarihi = hatirlatma_tarihlerini_getir(etkinlik_adi,baslangic_tarihi);
                ArrayList<String> hatirlatma_saati = hatirlatma_saatlerini_getir(etkinlik_adi,baslangic_tarihi);
                Etkinlik e = new Etkinlik(etkinlik_adi,etkinlik_detayi,baslangic_tarihi,
                        bitis_tarihi,adres,etkinlik_turu,hatirlatma_tarihi,hatirlatma_saati,adres_latitude,adres_longitude);

                Calendar baslangic_ = Calendar.getInstance();
                baslangic_.setTime(sdf.parse(baslangic_tarihi));
                // Bir ay içerisinde ise ekle...
                if(hafta_son.compareTo(baslangic_)>0&&gelen_tarih.compareTo(baslangic_)<=0){
                    liste.add(e);
                }
            }
        }
        while(cursor.moveToNext()){
            String etkinlik_adi=cursor.getString(1);
            String etkinlik_detayi=cursor.getString(2);
            String baslangic_tarihi=cursor.getString(3);
            String bitis_tarihi=cursor.getString(4);
            String adres=cursor.getString(5);
            String etkinlik_turu=cursor.getString(6);
            double adres_latitude = cursor.getDouble(7);
            double adres_longitude = cursor.getDouble(8);

            ArrayList<String> hatirlatma_tarihi =hatirlatma_tarihlerini_getir(etkinlik_adi,baslangic_tarihi);
            ArrayList<String> hatirlatma_saati = hatirlatma_saatlerini_getir(etkinlik_adi,baslangic_tarihi);
            Etkinlik e = new Etkinlik(etkinlik_adi,etkinlik_detayi,baslangic_tarihi,
                    bitis_tarihi,adres,etkinlik_turu,hatirlatma_tarihi,hatirlatma_saati,adres_latitude,adres_longitude);
            Calendar baslangic_ = Calendar.getInstance();
            baslangic_.setTime(sdf.parse(baslangic_tarihi));
            // Bir ay içerisinde ise ekle..
            if(hafta_son.compareTo(baslangic_)>0&&gelen_tarih.compareTo(baslangic_)<=0){
                liste.add(e);
            }
        }
        return liste;
    }

}

package com.example.takvimuygulamasi;

import java.util.ArrayList;

// Etkinlik bilgilerinin tutulması için tanımlanan classdır.
public class Etkinlik {
    private String etkinlik_adi;
    private String etkinlik_detayi;
    private String baslangic_tarihi;
    private String bitis_tarihi;
    private String adres;
    private int etkinlik_icon;
    private String etkinlik_turu;
    private ArrayList<String> hatirlatma_tarihi;
    private ArrayList<String> hatirlatma_saati;
    private double adres_latitude;
    private double adres_longitude;


    public Etkinlik(String etkinlik_adi, String etkinlik_detayi, String baslangic_tarihi, String bitis_tarihi,
                     String adres, String etkinlik_turu, ArrayList<String> hatirlatma_tarihi, ArrayList<String> hatirlatma_saati,double adres_latitude,double adres_longitude) {
        this.etkinlik_adi = etkinlik_adi;
        this.etkinlik_detayi = etkinlik_detayi;
        this.baslangic_tarihi = baslangic_tarihi;
        this.bitis_tarihi = bitis_tarihi;
        //this.hatirlatma_sikligi = hatirlatma_sikligi;
        this.adres = adres;
        this.etkinlik_turu = etkinlik_turu;
        this.hatirlatma_tarihi = hatirlatma_tarihi;
        this.hatirlatma_saati = hatirlatma_saati;
        this.adres_latitude = adres_latitude;
        this.adres_longitude = adres_longitude;
        if(etkinlik_turu.compareTo("Doğum Günü")==0)
            etkinlik_icon = R.drawable.ic_dogum_gunu;
        else if(etkinlik_turu.compareTo("Görev")==0)
            etkinlik_icon = R.drawable.ic_gorev;
        else if(etkinlik_turu.compareTo("Toplantı")==0)
            etkinlik_icon = R.drawable.ic_toplanti;

    }

    public double getAdres_latitude() {
        return adres_latitude;
    }

    public void setAdres_latitude(double adres_latitude) {
        this.adres_latitude = adres_latitude;
    }

    public double getAdres_longitude() {
        return adres_longitude;
    }

    public void setAdres_longitude(double adres_longitude) {
        this.adres_longitude = adres_longitude;
    }

    public ArrayList<String> getHatirlatma_tarihi() {
        return hatirlatma_tarihi;
    }

    public void setHatirlatma_tarihi(ArrayList<String> hatirlatma_tarihi) {
        this.hatirlatma_tarihi = hatirlatma_tarihi;
    }

    public ArrayList<String> getHatirlatma_saati() {
        return hatirlatma_saati;
    }

    public void setHatirlatma_saati(ArrayList<String> hatirlatma_saati) {
        this.hatirlatma_saati = hatirlatma_saati;
    }

    public int getEtkinlik_icon() {
        return etkinlik_icon;
    }

    public void setEtkinlik_icon(int etkinlik_icon) {
        this.etkinlik_icon = etkinlik_icon;
    }

    public String getEtkinlik_turu() {
        return etkinlik_turu;
    }

    public void setEtkinlik_turu(String etkinlik_turu) {
        this.etkinlik_turu = etkinlik_turu;
    }

    public String getEtkinlik_adi() {
        return etkinlik_adi;
    }

    public void setEtkinlik_adi(String etkinlik_adi) {
        this.etkinlik_adi = etkinlik_adi;
    }

    public String getEtkinlik_detayi() {
        return etkinlik_detayi;
    }

    public void setEtkinlik_detayi(String etkinlik_detayi) {
        this.etkinlik_detayi = etkinlik_detayi;
    }

    public String getBaslangic_tarihi() {
        return baslangic_tarihi;
    }

    public void setBaslangic_tarihi(String baslangic_tarihi) {
        this.baslangic_tarihi = baslangic_tarihi;
    }

    public String getBitis_tarihi() {
        return bitis_tarihi;
    }

    public void setBitis_tarihi(String bitis_tarihi) {
        this.bitis_tarihi = bitis_tarihi;
    }


    public String getAdres() {
        return adres;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }
}

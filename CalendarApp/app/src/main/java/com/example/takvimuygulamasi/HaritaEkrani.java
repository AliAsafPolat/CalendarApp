package com.example.takvimuygulamasi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// Konum alma ve gösterme için tasarlanan ekranın kod kısmı.
public class HaritaEkrani extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private Marker marker;
    Button btn_konum_al;

    double gelen_latitude;
    double gelen_longitude;

    double latitude;
    double longitude;
    String adres;

    // Adres goruntuleniyor mu yoksa yeni adres mi ekleniyor bunun bilgisi tutulur.
    boolean goruntuleme_mi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harita_ekrani);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_konum_al = findViewById(R.id.buttonKonumAl);

        // Eğer adres bilgisi gelmiş ise görüntüleme yapılıyordur. Değişkene bu değer alınır.
        Intent i = getIntent();
        if(i.getExtras()!=null){
            gelen_longitude = i.getDoubleExtra("adres_longitude",0);
            gelen_latitude= i.getDoubleExtra("adres_latitude",0);
            goruntuleme_mi = true;
        }

        // Görüntüleme yapılıyorsa butona tıklandığında işlem tamamlansın ve çıkılsın.
        btn_konum_al.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(goruntuleme_mi){
                    setResult(Activity.RESULT_OK);
                    finish();
                }else{
                    // Değil ise izinler kontrol edilip adres bilgileri gönderilir. Create methodu sonunda güncel konum alınır. Seçim yapılmasa dahi
                    // bu bilgi gönderilir. Seçim yapılırsa (basılı tutup kaydırarak bir yer seçilirse) konum güncellenir ve seçilen konum gönderilir.
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("takvim", "İzinler Alınmadı...");
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    } else{
                        Intent i = new Intent();
                        i.putExtra("acik_adres",adres);
                        i.putExtra("latitude",latitude);
                        i.putExtra("longitude",longitude);
                        Toast.makeText(HaritaEkrani.this,"Konum Eklendi",Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK,i);
                        finish();
                    }

                }
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if(goruntuleme_mi){
                    LatLng koordinatlar = new LatLng(gelen_latitude, gelen_longitude);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(koordinatlar, 13.0f));
                    if (marker == null) {
                        marker = mMap.addMarker(new MarkerOptions().position(koordinatlar).draggable(true));
                    } else
                        marker.setPosition(koordinatlar);
                }else{
                    Location mCurrentLocation = locationResult.getLastLocation();
                    LatLng koordinatlar = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(koordinatlar, 13.0f));
                    if (marker == null) {
                        marker = mMap.addMarker(new MarkerOptions().position(koordinatlar).draggable(true));
                    } else
                        marker.setPosition(koordinatlar);
                }

            }
        };
        // İzinler kontrol edilsin ve görüntüleme değil ise güncel konum baz alınsın. Seçim olursa bu konum değişir.
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else if(!goruntuleme_mi)
            konumAl();

    }

    // Latitude ve longitude değerlerine göre adres bilgisi elde edilir.
    private String adresGetir(LatLng myCoordinates) {
        String acik_adres ="";
        Geocoder geocoder = new Geocoder(HaritaEkrani.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(myCoordinates.latitude, myCoordinates.longitude, 1);
            acik_adres = addresses.get(0).getAddressLine(0);
            Log.d("mylog", "Address: " + acik_adres);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return acik_adres;
    }

    // Son bilinen güncel konumu alır.
    private void konumAl() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        String provider = locationManager.getBestProvider(criteria, true);

        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(provider);

        // alınan son adresin durumuna bakılıyor... Zaman olarak çok geçmiş mi bunun kontrolü yapılır...
        if (location != null && (System.currentTimeMillis() - location.getTime()) <= 1000 * 2) {
            LatLng kordinatlar = new LatLng(location.getLatitude(), location.getLongitude());
            adres = adresGetir(kordinatlar);
            longitude = location.getLongitude();
            latitude = location.getLatitude();

        } else {
            @SuppressLint("RestrictedApi") LocationRequest
                    locationRequest = new LocationRequest();
            locationRequest.setNumUpdates(1);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    mLocationCallback, Looper.myLooper());

            LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
            adres = adresGetir(myCoordinates);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Görüntüleme yapılıyorsa seçilen kısma focus yapılıyor.
        if(goruntuleme_mi){
            btn_konum_al.setText("Tamam");
            LatLng _koordinatlar = new LatLng(gelen_latitude, gelen_longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(_koordinatlar, 13.0f));
            if (marker == null) {
                marker = mMap.addMarker(new MarkerOptions().position(_koordinatlar));
            } else
                marker.setPosition(_koordinatlar);
        Toast.makeText(HaritaEkrani.this,"Konum Gösteriliyor",Toast.LENGTH_SHORT).show();
        }

        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Konum seçilecekse...
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // TODO Auto-generated method stub
                // Here your code
                Toast.makeText(HaritaEkrani.this, "Konum Değiştiriliyor...",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // TODO Auto-generated method stub
                // Seçme işlemi gerçekleştiyse seçilen konuma göre bilgileri güncelle.
                LatLng position = marker.getPosition();
                latitude = position.latitude;
                longitude = position.longitude;
                adres = adresGetir(new LatLng(latitude,longitude));
                Toast.makeText(HaritaEkrani.this,"Konum güncellendi.",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

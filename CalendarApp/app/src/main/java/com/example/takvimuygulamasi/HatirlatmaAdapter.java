package com.example.takvimuygulamasi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// Hatırlatma zamanlarını göstermek için recycler view yapısı kullanılır. Adapter olarak bu class yazıldı.
public class HatirlatmaAdapter extends RecyclerView.Adapter<HatirlatmaAdapter.MyViewHolder> {
    ArrayList<String> hatirlatma_tarihleri;
    ArrayList<String> hatirlatma_saatleri;
    LayoutInflater layoutInflater;

    private OnItemClickListener mListener;

    public HatirlatmaAdapter(Context context, ArrayList<String> tarihler,ArrayList<String> saatler){
        this.hatirlatma_saatleri = saatler;
        this.hatirlatma_tarihleri = tarihler;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.hatirlatma_zamanlari_gosterme_duzeni,parent,false);
        MyViewHolder holder = new MyViewHolder(view,mListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String saat;
        String tarih;
        saat= hatirlatma_saatleri.get(position);
        tarih = hatirlatma_tarihleri.get(position);
        holder.hatirlatma_tarihi.setText(tarih);
        holder.hatirlatma_saati.setText(saat);
    }

    @Override
    public int getItemCount() {
        if(hatirlatma_saatleri != null)
            return hatirlatma_saatleri.size();
        else
            return 0;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(HatirlatmaAdapter.OnItemClickListener listener){
        mListener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView hatirlatma_tarihi;
        TextView hatirlatma_saati;
        ImageView silme;

        public MyViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);
            hatirlatma_saati = (TextView)itemView.findViewById(R.id.buttonHatirlatmaSaatiGiris);
            hatirlatma_tarihi = (TextView) itemView.findViewById(R.id.buttonHatirlatmaTarihiGiris);
            silme = (ImageView)itemView.findViewById(R.id.imageViewHatirlatmaSil);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION){ // Gerçekten bir pozisyonda mı şuan onu almalıyız.
                            listener.onItemClick(position);     // Itema basıldığı zaman bu event çalışsın.
                        }
                    }
                }
            });

            silme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION){ // Gerçekten bir pozisyonda mı şuan onu almalıyız.
                            listener.onDeleteClick(position);     // Itema basıldığı zaman bu event çalışsın.
                        }
                    }
                }
            });

        }
    }
}

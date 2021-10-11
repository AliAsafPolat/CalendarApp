package com.example.takvimuygulamasi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

// Takvim ekranında etkinliklerin gösterilmesi için bir recycler view kullanılmıştır bunun için tanımlanan adapter class ıdır.
// Yaklaşan etkinlikleri göstermek için de aynı adapter kullanılır fakat bu kez layout farklılık gösterir bunu sağlamak için yaklasan_mi parametresi alınır ve
// layoutlar bu parametreye göre oluşturulur.
public class EtkinlikAdapter extends RecyclerView.Adapter<EtkinlikAdapter.MyViewHolder>{

    ArrayList<Etkinlik> etkinlikler;
    LayoutInflater layoutInflater;
    private OnItemClickListener mListener;
    boolean yaklasan_mi;

    public  EtkinlikAdapter(Context con, ArrayList<Etkinlik> etkinlikler,boolean yaklasan_mi){
        layoutInflater = LayoutInflater.from(con);
        this.etkinlikler = etkinlikler;
        this.yaklasan_mi = yaklasan_mi;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        // Eğer yaklaşan etkinlikleri gösteriyorsak farklı bir layout kullanacağız. Gönderme ve silme eventleri olmayacak.
        if(yaklasan_mi){
            view= layoutInflater.inflate(R.layout.yaklasan_etkinlik_gosterme_duzeni,parent,false);
        }else
            view = layoutInflater.inflate(R.layout.etkinlik_gosterme_duzeni,parent,false);

        MyViewHolder holder = new MyViewHolder(view,mListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Etkinlik n;
        n = etkinlikler.get(position);
        holder.etkinlik_adi.setText(n.getEtkinlik_adi());
        holder.etkinlik_tarihi.setText(n.getBaslangic_tarihi());
        holder.etkinlik_icon.setImageResource(n.getEtkinlik_icon());

    }

    @Override
    public int getItemCount() {
        return etkinlikler.size();
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onSendClick(int position);
    }

    public void setOnItemClickListener(EtkinlikAdapter.OnItemClickListener listener){
        mListener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView etkinlik_tarihi;
        TextView etkinlik_adi;
        ImageView delete;
        ImageView etkinlik_icon;
        ImageView send;


        public MyViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);
            etkinlik_tarihi = (TextView) itemView.findViewById(R.id.textView_EtkinlikTarihi);
            etkinlik_adi = (TextView) itemView.findViewById(R.id.textView_EtkinlikAdi);
            etkinlik_icon = (ImageView) itemView.findViewById(R.id.imageView_EtkinlikIcon);
            if(!yaklasan_mi){
                delete = (ImageView)itemView.findViewById(R.id.imageViewDeleteIcon);
                send = (ImageView) itemView.findViewById(R.id.imageViewGonder);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(listener!=null){
                            int position = getAdapterPosition();
                            if(position!=RecyclerView.NO_POSITION){   // Gerçekten bir pozisyonda mı şuan onu almalıyız.
                                listener.onDeleteClick(position);     // Itema basıldığı zaman bu event çalışsın.
                            }
                        }
                    }
                });

                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(listener!=null){
                            int position = getAdapterPosition();
                            if(position!=RecyclerView.NO_POSITION){ // Gerçekten bir pozisyonda mı şuan onu almalıyız.
                                listener.onSendClick(position);     // Itema basıldığı zaman bu event çalışsın.
                            }
                        }
                    }
                });
            }
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


        }
    }
}

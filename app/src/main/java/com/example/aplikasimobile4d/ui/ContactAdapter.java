package com.example.aplikasimobile4d.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplikasimobile4d.R;
import com.example.aplikasimobile4d.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter RecyclerView untuk menampilkan daftar kontak.
 *
 * <p>M1: render baris sederhana (nama, umur, nomor, ikon favorit) dengan
 * {@code notifyDataSetChanged()}. DiffUtil yang lebih efisien menyusul di M4.</p>
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final List<Contact> items = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<Contact> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged(); // diganti DiffUtil di M4
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {

        private final TextView textNama;
        private final TextView textDetail;
        private final TextView textFavorit;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            textNama = itemView.findViewById(R.id.textNama);
            textDetail = itemView.findViewById(R.id.textDetail);
            textFavorit = itemView.findViewById(R.id.textFavorit);
        }

        void bind(Contact contact) {
            textNama.setText(contact.nama);

            // Gabungkan umur (int) + nomor (String) menjadi satu baris detail
            StringBuilder detail = new StringBuilder();
            detail.append(contact.umur).append(" thn");
            if (contact.nomorTelepon != null && !contact.nomorTelepon.isEmpty()) {
                detail.append(" • ").append(contact.nomorTelepon); // • pemisah
            }
            textDetail.setText(detail.toString());

            // boolean -> bintang penuh/kosong
            textFavorit.setText(contact.favorit ? "★" : "☆"); // ★ / ☆
        }
    }
}

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
 * Adapter RecyclerView untuk daftar kontak.
 *
 * <p>M3: setiap baris memancarkan tiga aksi melalui {@link OnItemActionListener}:
 * tap = ubah, tahan-lama = hapus, tap bintang = toggle favorit. Adapter tidak tahu cara
 * mengubah/menghapus data — itu urusan Activity + Repository (pemisahan tanggung jawab).</p>
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    /** Aksi yang bisa dilakukan pada satu baris kontak. */
    public interface OnItemActionListener {
        void onEdit(Contact contact);
        void onDelete(Contact contact);
        void onToggleFavorit(Contact contact);
    }

    private final List<Contact> items = new ArrayList<>();
    private final OnItemActionListener listener;

    public ContactAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

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
        Contact contact = items.get(position);
        holder.bind(contact);

        holder.itemView.setOnClickListener(v -> listener.onEdit(contact));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(contact);
            return true; // konsumsi event, jangan teruskan
        });
        holder.textFavorit.setOnClickListener(v -> listener.onToggleFavorit(contact));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {

        final TextView textNama;
        final TextView textDetail;
        final TextView textFavorit;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            textNama = itemView.findViewById(R.id.textNama);
            textDetail = itemView.findViewById(R.id.textDetail);
            textFavorit = itemView.findViewById(R.id.textFavorit);
        }

        void bind(Contact contact) {
            textNama.setText(contact.nama);

            StringBuilder detail = new StringBuilder();
            detail.append(contact.umur).append(" thn");
            if (contact.nomorTelepon != null && !contact.nomorTelepon.isEmpty()) {
                detail.append(" • ").append(contact.nomorTelepon);
            }
            textDetail.setText(detail.toString());

            textFavorit.setText(contact.favorit ? "★" : "☆");
        }
    }
}

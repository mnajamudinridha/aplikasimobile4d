package com.example.aplikasimobile4d.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplikasimobile4d.R;
import com.example.aplikasimobile4d.model.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /**
     * Mengganti isi daftar memakai {@link DiffUtil} agar hanya baris yang benar-benar berubah
     * yang digambar ulang (animasi halus, hemat). Bandingkan {@link #areItemsTheSame} pakai id,
     * {@link #areContentsTheSame} pakai field yang tampil.
     */
    public void setItems(List<Contact> newItems) {
        final List<Contact> oldList = new ArrayList<>(items);
        final List<Contact> newList = newItems == null ? new ArrayList<>() : newItems;

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                // Identitas baris = id (push key)
                return Objects.equals(oldList.get(oldPos).id, newList.get(newPos).id);
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Contact a = oldList.get(oldPos);
                Contact b = newList.get(newPos);
                return a.umur == b.umur
                        && a.favorit == b.favorit
                        && Objects.equals(a.nama, b.nama)
                        && Objects.equals(a.nomorTelepon, b.nomorTelepon);
            }
        });

        items.clear();
        items.addAll(newList);
        result.dispatchUpdatesToAdapter(this);
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

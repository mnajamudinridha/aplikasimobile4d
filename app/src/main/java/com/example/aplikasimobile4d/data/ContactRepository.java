package com.example.aplikasimobile4d.data;

import androidx.annotation.NonNull;

import com.example.aplikasimobile4d.model.Contact;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Satu-satunya kelas yang menyentuh Firebase Realtime Database.
 *
 * <p>UI (Activity/Adapter) tidak boleh memanggil Firebase langsung — semua lewat repository
 * ini. Tujuannya menjaga keterbacaan dan memudahkan penggantian backend (lihat
 * app/Docs/ARSITEKTUR.md). Pada milestone M1 baru tersedia operasi <b>READ realtime</b>;
 * Create/Update/Delete menyusul di M2–M3.</p>
 */
public class ContactRepository {

    /** Callback hasil pembacaan daftar kontak. */
    public interface ContactsCallback {
        void onData(List<Contact> contacts);
        void onError(Exception e);
    }

    /** Callback operasi tulis (create/update/delete). */
    public interface OnComplete {
        void onSuccess();
        void onError(Exception e);
    }

    /** Node koleksi semua kontak: /contacts */
    private final DatabaseReference contactsRef =
            FirebaseDatabase.getInstance().getReference("contacts");

    /** Disimpan agar bisa dilepas lagi di {@link #removeObserver()}. */
    private ValueEventListener listener;

    public ContactRepository() {
        // Jaga node /contacts tetap tersinkron walau tak ada listener aktif, sehingga
        // data terbaru tersedia di cache lokal untuk akses offline (NFR-2).
        contactsRef.keepSynced(true);
    }

    /**
     * Memasang listener <b>realtime</b>. {@code onDataChange} dipanggil sekarang (data awal)
     * dan setiap kali data di server berubah — inilah inti fitur realtime.
     */
    public void observeAll(final ContactsCallback callback) {
        if (listener != null) {
            return; // sudah memantau, hindari listener ganda
        }
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Contact> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Contact contact = child.getValue(Contact.class);
                    if (contact != null) {
                        contact.id = child.getKey(); // id = push key
                        list.add(contact);
                    }
                }
                // Urut nama A->Z di sisi klien (keputusan D-2; data kecil)
                Collections.sort(list, (a, b) -> safe(a.nama).compareToIgnoreCase(safe(b.nama)));
                callback.onData(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        };
        contactsRef.addValueEventListener(listener);
    }

    /**
     * CREATE — membuat kontak baru. Membuat <i>push key</i> unik lalu menulis seluruh objek
     * via {@code setValue()}. Tidak perlu mengabari daftar secara manual: listener realtime
     * di MainActivity otomatis menerima data baru.
     */
    public void create(Contact contact, final OnComplete callback) {
        String id = contactsRef.push().getKey(); // push key terurut waktu
        if (id == null) {
            callback.onError(new IllegalStateException("Gagal membuat id kontak"));
            return;
        }
        long now = System.currentTimeMillis();
        contact.createdAt = now;
        contact.updatedAt = now;
        contactsRef.child(id).setValue(contact)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * UPDATE — memperbarui kontak yang sudah ada. Memakai {@code updateChildren(toMap())}
     * (partial update) sehingga hanya field di map yang ditulis. {@code updatedAt} diperbarui;
     * pemanggil bertanggung jawab mempertahankan {@code createdAt} pada objek.
     */
    public void update(Contact contact, final OnComplete callback) {
        if (contact.id == null) {
            callback.onError(new IllegalStateException("id kontak null saat update"));
            return;
        }
        contact.updatedAt = System.currentTimeMillis();
        contactsRef.child(contact.id).updateChildren(contact.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** DELETE — menghapus satu kontak (hard delete, keputusan D-3). */
    public void delete(String id, final OnComplete callback) {
        contactsRef.child(id).removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Update field tunggal {@code favorit} (boolean) tanpa menyentuh field lain — contoh
     * partial update yang paling kecil. Fire-and-forget: perubahan tampil lewat listener realtime.
     */
    public void toggleFavorit(String id, boolean nilaiBaru) {
        Map<String, Object> patch = new HashMap<>();
        patch.put("favorit", nilaiBaru);
        patch.put("updatedAt", System.currentTimeMillis());
        contactsRef.child(id).updateChildren(patch);
    }

    /** Melepas listener realtime. Wajib dipanggil saat layar berhenti agar tidak bocor. */
    public void removeObserver() {
        if (listener != null) {
            contactsRef.removeEventListener(listener);
            listener = null;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

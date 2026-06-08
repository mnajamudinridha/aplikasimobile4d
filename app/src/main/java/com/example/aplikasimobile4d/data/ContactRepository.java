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
import java.util.List;

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

    /** Node koleksi semua kontak: /contacts */
    private final DatabaseReference contactsRef =
            FirebaseDatabase.getInstance().getReference("contacts");

    /** Disimpan agar bisa dilepas lagi di {@link #removeObserver()}. */
    private ValueEventListener listener;

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

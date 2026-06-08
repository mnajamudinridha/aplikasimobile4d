package com.example.aplikasimobile4d;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Kelas Application aplikasi Daftar Kontak Teman.
 *
 * <p>Tugas utamanya: mengaktifkan <b>disk persistence</b> Firebase Realtime Database
 * SATU KALI, sebelum referensi database mana pun dibuat. Dengan persistence aktif,
 * aplikasi tetap menampilkan data terakhir saat offline dan otomatis menyinkronkan
 * perubahan ketika koneksi kembali (lihat NFR-2 di app/Docs/PRD.md).</p>
 *
 * <p>Penting: {@code setPersistenceEnabled(true)} HARUS dipanggil sebelum pemanggilan
 * {@code FirebaseDatabase.getInstance().getReference(...)} pertama. Memanggilnya setelah
 * itu akan melempar exception. Karena {@code Application.onCreate()} berjalan paling awal
 * dalam siklus hidup aplikasi, di sinilah tempat paling aman.</p>
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Aktifkan cache lokal agar app dapat berjalan offline dan sinkron otomatis.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

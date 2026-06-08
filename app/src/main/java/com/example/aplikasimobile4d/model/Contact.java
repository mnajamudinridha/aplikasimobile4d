package com.example.aplikasimobile4d.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Model data satu kontak teman (POJO untuk Firebase Realtime Database).
 *
 * <p>Sengaja memakai tiga tipe data berbeda agar terlihat bagaimana masing-masing tipe
 * mengalir dari form ke database (lihat app/Docs/MODEL-DATA.md):</p>
 * <ul>
 *   <li>{@link #nama}, {@link #nomorTelepon} — <b>String</b></li>
 *   <li>{@link #umur} — <b>int</b></li>
 *   <li>{@link #favorit} — <b>boolean</b></li>
 * </ul>
 *
 * <p>Syarat POJO Firebase: WAJIB punya konstruktor kosong publik dan field/getter publik
 * agar (de)serialisasi otomatis bekerja saat {@code snapshot.getValue(Contact.class)}.</p>
 */
@IgnoreExtraProperties
public class Contact {

    /**
     * Id kontak = <i>push key</i> Firebase (nama node), BUKAN bagian isi node.
     * Karena itu ditandai {@link Exclude} agar tidak ikut ditulis ke dalam node.
     * Diisi manual dari {@code snapshot.getKey()} saat membaca.
     */
    @Exclude
    public String id;

    public String nama;          // String  — wajib diisi
    public String nomorTelepon;  // String  — opsional (disimpan String, jaga angka 0 di depan)
    public int umur;             // int     — default 0
    public boolean favorit;      // boolean — default false
    public long createdAt;       // epoch ms — waktu dibuat
    public long updatedAt;       // epoch ms — waktu terakhir diubah

    /** Konstruktor kosong WAJIB untuk getValue(Contact.class). */
    public Contact() {
    }

    public Contact(String nama, String nomorTelepon, int umur, boolean favorit) {
        this.nama = nama;
        this.nomorTelepon = nomorTelepon;
        this.umur = umur;
        this.favorit = favorit;
    }

    /**
     * Memetakan objek ke {@code Map} untuk <b>partial update</b> via
     * {@code DatabaseReference.updateChildren(...)}. Hanya key yang ada di map yang ditulis,
     * sehingga field lain di server tidak tersentuh.
     *
     * <p>Catatan desain: {@code createdAt} ikut disertakan agar nilainya stabil saat update;
     * pemanggil bertanggung jawab mempertahankan nilai aslinya.</p>
     */
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("nama", nama);
        result.put("nomorTelepon", nomorTelepon);
        result.put("umur", umur);
        result.put("favorit", favorit);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }
}

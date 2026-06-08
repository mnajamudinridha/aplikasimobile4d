# Arsitektur — Aplikasi Daftar Kontak Teman

Dokumen ini menjelaskan **arsitektur Android**, **setup Firebase**, **dependensi (versi
teruji)**, dan **pola kode CRUD** acuan. Lihat [`PRD.md`](./PRD.md) untuk produk dan
[`MODEL-DATA.md`](./MODEL-DATA.md) untuk struktur data.

---

## 1. Gambaran Arsitektur

Pola **MVVM ringan + Repository**. Tujuan: UI tidak pernah menyentuh Firebase langsung —
semua lewat `ContactRepository`. Ini membuat kode mudah dibaca, diuji, dan diganti backend-nya.

```
┌────────────────────────────────────────────────────────┐
│                       Lapisan UI                        │
│  MainActivity ── ContactAdapter (RecyclerView)          │
│  AddEditContactActivity (form tambah/ubah)              │
└───────────────┬────────────────────────────────────────┘
                │ panggil method + terima callback
┌───────────────▼────────────────────────────────────────┐
│                  ContactRepository                       │
│  satu-satunya pemegang DatabaseReference                 │
│  create() read() update() delete() toggleFavorit()      │
└───────────────┬────────────────────────────────────────┘
                │ Firebase Android SDK
┌───────────────▼────────────────────────────────────────┐
│            Firebase Realtime Database (cloud)            │
│                  node: /contacts                         │
└─────────────────────────────────────────────────────────┘

Model: Contact (POJO)  ←dipakai semua lapisan
```

**Aturan main:**
- `Contact` — data murni (POJO), tanpa logika Firebase.
- `ContactRepository` — **satu-satunya** kelas yang mengimpor & memakai `DatabaseReference`.
- `Activity` — hanya tampilan & interaksi; minta data ke repository, render hasilnya.

---

## 2. Struktur Paket

```
com.example.aplikasimobile4d
├── App.java                         // Application: aktifkan persistence sekali
├── model/
│   └── Contact.java                 // POJO (lihat MODEL-DATA.md)
├── data/
│   └── ContactRepository.java       // membungkus DatabaseReference (CRUD)
├── ui/
│   ├── MainActivity.java            // daftar kontak + FAB + cari
│   ├── ContactAdapter.java          // RecyclerView.Adapter (+ DiffUtil)
│   └── AddEditContactActivity.java  // form tambah/ubah
└── util/
    └── Validators.java              // validasi nama/umur/telepon (opsional)
```

> Pemisahan `model` / `data` / `ui` / `util` membuat tanggung jawab jelas — salah satu
> sasaran keterbacaan (NFR-5).

---

## 3. Setup Firebase (langkah M0)

1. Buat project di [Firebase Console](https://console.firebase.google.com/).
2. **Add app → Android**, isi package **`com.example.aplikasimobile4d`**.
3. Unduh **`google-services.json`** → letakkan di folder **`app/`**.
   *(File ini berisi kunci proyek — sebaiknya masuk `.gitignore`.)*
4. **Build → Realtime Database → Create Database**.
   - Pilih lokasi (mis. `asia-southeast1` / Singapore — terdekat).
   - Mulai dengan **Locked mode**, lalu ganti rules ke versi DEV di [`MODEL-DATA.md` §6.1](./MODEL-DATA.md).
5. Salin **Database URL** (mis. `https://<project>-default-rtdb.asia-southeast1.firebasedatabase.app`).

> ⚠️ `google-services.json` **belum** ada di repo ini. Tanpa file ini, aplikasi crash saat
> inisialisasi Firebase. Ini langkah wajib pertama sebelum menulis kode.

---

## 4. Dependensi (versi TERUJI untuk AGP 7.2.1 / compileSdk 32)

Build saat ini: **AGP 7.2.1**, **compileSdk/targetSdk 32**, **Java 1.8**, Gradle **Groovy DSL**.
Versi Firebase di bawah dipilih karena **kompatibel** dengan konfigurasi ini. Versi BoM yang
jauh lebih baru (33.x/34.x) menuntut AGP & compileSdk lebih tinggi — **jangan** dipakai
sebelum build di-upgrade.

### 4.1 `build.gradle` (root) — tambah plugin google-services

```groovy
plugins {
    id 'com.android.application' version '7.2.1' apply false
    id 'com.android.library'     version '7.2.1' apply false
    // TAMBAH:
    id 'com.google.gms.google-services' version '4.3.15' apply false
}
```

### 4.2 `app/build.gradle` — terapkan plugin & dependensi

```groovy
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'   // TAMBAH
}

dependencies {
    // AndroidX dipatok ke versi yang cocok compileSdk 32 + AGP 7.2.1 (lihat catatan di bawah)
    implementation 'androidx.appcompat:appcompat:1.4.2'
    implementation 'com.google.android.material:material:1.6.1'

    // TAMBAH — Firebase via BoM (kelola versi terpusat)
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-database'   // RTDB (versi dari BoM)

    // TAMBAH — daftar kontak
    implementation 'androidx.recyclerview:recyclerview:1.2.1'

    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

> **Kenapa BoM (Bill of Materials)?** BoM menyamakan versi seluruh pustaka Firebase, jadi
> `firebase-database` cukup ditulis tanpa nomor versi — BoM yang menentukan. Ini mencegah
> bentrok versi antar-modul Firebase.

> ⚠️ **Version skew (penting).** `compileSdk`, AGP, dan versi pustaka adalah satu kontrak
> tiga-arah. Versi terbaru `appcompat` (≥1.5) / `material` (≥1.7) menarik `androidx.core:core:1.16`
> yang **mewajibkan compileSdk 34 + AGP 8.6+** → build gagal di toolchain ini. Karena proyek
> dipatok ke **AGP 7.2.1 / compileSdk 32 / JDK 11** (JBR bawaan Android Studio), pustaka AndroidX
> juga dipatok ke versi seangkatan: `appcompat:1.4.2`, `material:1.6.1`, `test.ext:junit:1.1.5`,
> `espresso-core:3.5.1`. Firebase BoM 32.7.0 tetap aman (tidak menuntut compileSdk 34).
> Bila ingin pustaka terbaru, upgrade satu paket: AGP 8.6+ + Gradle 8.7+ + JDK 17 + compileSdk 34 + `namespace`.

### 4.3 `AndroidManifest.xml` — izin internet & Application

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<application
    android:name=".App"
    ... >
```

---

## 5. Inisialisasi Persistence (offline)

Aktifkan **sekali saja**, sebelum referensi DB apa pun dibuat — di `Application.onCreate()`:

```java
package com.example.aplikasimobile4d;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class App extends Application {
    @Override public void onCreate() {
        super.onCreate();
        // Cache lokal: app jalan offline & sinkron otomatis saat online (NFR-2).
        // Harus dipanggil SEKALI, sebelum getReference() pertama.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
```

> Memanggil `setPersistenceEnabled(true)` setelah ada referensi DB akan **crash**. Karena
> itu ditaruh paling awal di `Application`.

---

## 6. Pola CRUD di `ContactRepository`

Semua interaksi Firebase terpusat di sini. Pola callback memakai antarmuka sederhana agar
Activity tidak tahu detail Firebase.

```java
private final DatabaseReference contactsRef =
        FirebaseDatabase.getInstance().getReference("contacts");
```

### 6.1 CREATE — `push()` + `setValue()`

```java
public void create(Contact c, OnComplete cb) {
    String id = contactsRef.push().getKey();   // buat push key unik
    long now = System.currentTimeMillis();
    c.createdAt = now;
    c.updatedAt = now;
    contactsRef.child(id).setValue(c)
        .addOnSuccessListener(v -> cb.onSuccess())
        .addOnFailureListener(cb::onError);
}
```

### 6.2 READ — listener **realtime** (`addValueEventListener`)

```java
public void observeAll(OnContacts cb) {
    listener = new ValueEventListener() {
        @Override public void onDataChange(DataSnapshot snapshot) {
            List<Contact> list = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                Contact c = child.getValue(Contact.class);   // butuh konstruktor kosong
                if (c != null) {
                    c.id = child.getKey();                    // id = push key
                    list.add(c);
                }
            }
            // urut nama A->Z di sisi klien (D-2)
            Collections.sort(list, (a, b) ->
                a.nama.compareToIgnoreCase(b.nama));
            cb.onData(list);
        }
        @Override public void onCancelled(DatabaseError error) {
            cb.onError(error.toException());
        }
    };
    contactsRef.addValueEventListener(listener);   // dipanggil ulang tiap data berubah
}
```

> `addValueEventListener` = **realtime**: callback dipanggil sekarang (data awal) **dan**
> setiap kali ada perubahan di server. Inilah jantung fitur realtime (FR-2).

### 6.3 UPDATE — partial via `updateChildren()`

```java
public void update(Contact c, OnComplete cb) {
    c.updatedAt = System.currentTimeMillis();
    contactsRef.child(c.id).updateChildren(c.toMap())   // hanya field di map
        .addOnSuccessListener(v -> cb.onSuccess())
        .addOnFailureListener(cb::onError);
}
```

### 6.4 Update field tunggal — toggle `favorit` (boolean)

```java
public void toggleFavorit(String id, boolean nilaiBaru) {
    Map<String, Object> patch = new HashMap<>();
    patch.put("favorit", nilaiBaru);
    patch.put("updatedAt", System.currentTimeMillis());
    contactsRef.child(id).updateChildren(patch);   // tidak menyentuh field lain (FR-5)
}
```

### 6.5 DELETE — `removeValue()`

```java
public void delete(String id, OnComplete cb) {
    contactsRef.child(id).removeValue()
        .addOnSuccessListener(v -> cb.onSuccess())
        .addOnFailureListener(cb::onError);
}
```

### 6.6 Ringkasan pemetaan CRUD ↔ Firebase

| Operasi | Method Firebase | FR |
|---|---|---|
| Create | `push().getKey()` + `child(id).setValue(obj)` | FR-1 |
| Read (realtime) | `addValueEventListener` + `getChildren()` + `getValue(Contact.class)` | FR-2 |
| Update (partial) | `child(id).updateChildren(map)` | FR-3 |
| Update field tunggal | `child(id).updateChildren({"favorit": ...})` | FR-5 |
| Delete | `child(id).removeValue()` | FR-4 |

---

## 7. Lifecycle Listener (kritis)

Listener realtime **harus dilepas** saat layar tidak terlihat, jika tidak → kebocoran memori
& callback ganda (NFR-6).

```java
// MainActivity
@Override protected void onStart() {
    super.onStart();
    repo.observeAll(callback);     // pasang
}
@Override protected void onStop() {
    super.onStop();
    repo.removeObserver();         // contactsRef.removeEventListener(listener)
}
```

> Pola: **pasang di `onStart`, lepas di `onStop`**. Simpan referensi `listener` agar bisa
> dilepas dengan `removeEventListener(listener)`.

---

## 8. RecyclerView & DiffUtil

`ContactAdapter` menampilkan list. Pakai **DiffUtil** agar hanya item berubah yang
di-render ulang (animasi halus, hemat).

```java
DiffUtil.Callback diff = new DiffUtil.Callback() {
    @Override public boolean areItemsTheSame(int o, int n) {
        return lama.get(o).id.equals(baru.get(n).id);          // identitas = id
    }
    @Override public boolean areContentsTheSame(int o, int n) {
        return lama.get(o).equals(baru.get(n));                // isi sama?
    }
    // ...
};
```

---

## 9. Penanganan Thread & Error

- Callback Firebase berjalan di **main thread** → aman langsung update UI.
- Operasi tulis mengembalikan `Task` → pakai `addOnSuccessListener` / `addOnFailureListener`.
- Tampilkan kegagalan via `Toast`/`Snackbar` (pesan ramah, lihat FR-7).
- Saat offline, operasi **berhasil secara lokal** dulu lalu dikirim saat online (persistence) —
  jangan anggap "gagal" hanya karena offline.

---

## 10. Diagram Urutan — Tambah Kontak (realtime)

```
Pengguna     AddEditActivity   ContactRepository   RTDB        MainActivity(listener)
   │  isi+Simpan   │                 │              │                  │
   │──────────────>│  create(c,cb)   │              │                  │
   │               │────────────────>│ push+setValue│                  │
   │               │                 │─────────────>│                  │
   │               │                 │   onSuccess  │                  │
   │               │<────────────────│              │                  │
   │   tutup form  │                 │   data change│                  │
   │<──────────────│                 │              │─────────────────>│ onDataChange
   │               │                 │              │                  │ render ulang list
```

Perhatikan: form **tidak** perlu mengirim data balik ke daftar — daftar ter-update sendiri
lewat listener realtime begitu RTDB berubah. Inilah keuntungan model realtime.

---

*Detail langkah implementasi bertahap ada di [`ROADMAP.md`](./ROADMAP.md).*

# Aplikasi Daftar Kontak Teman 📇

Aplikasi Android **native** (bahasa **Java**) untuk mencatat daftar kontak teman dengan
operasi **CRUD** penuh dan sinkronisasi **realtime** ke **Firebase Realtime Database**.

Dibuat sebagai **proyek pembelajaran**: contoh bersih pola CRUD + realtime DB, sekaligus
menunjukkan tiga tipe data (**String, int, boolean**) mengalir utuh dari form → objek Java →
JSON → database.

> 📚 Dokumentasi lengkap (PRD, model data, arsitektur, roadmap) ada di [`app/Docs/`](./app/Docs/).

---

## ✨ Fitur

- ➕ **Tambah** kontak (nama, nomor telepon, umur, favorit)
- 📃 **Daftar realtime** — perubahan langsung tampil tanpa refresh
- ✏️ **Ubah** & 🗑️ **Hapus** (dengan dialog konfirmasi)
- ⭐ **Toggle favorit** cepat dari daftar
- 🔍 **Cari** nama + filter **hanya favorit**
- 📴 **Dukungan offline** (persistence + keepSynced) — sinkron otomatis saat online

## 🧱 Teknologi

| Aspek | Nilai |
|---|---|
| Bahasa | Java 1.8 |
| Backend | Firebase Realtime Database |
| Arsitektur | MVVM ringan + Repository |
| UI | RecyclerView + DiffUtil, Material Components |
| minSdk / target / compile | 24 / 32 / 32 |
| AGP / Gradle DSL | 7.2.1 / Groovy |
| Firebase BoM | 32.7.0 |

## 🗂️ Struktur Kode

```
app/src/main/java/com/example/aplikasimobile4d/
├── App.java                       # Application: aktifkan persistence offline
├── model/Contact.java             # POJO (String/int/boolean)
├── data/ContactRepository.java    # satu-satunya yang menyentuh Firebase (CRUD)
└── ui/
    ├── MainActivity.java          # daftar realtime + cari/filter + FAB
    ├── ContactAdapter.java        # RecyclerView + DiffUtil
    └── AddEditContactActivity.java# form tambah/ubah
```

## 🚀 Cara Menjalankan

1. **Clone** repo ini.
2. Buat project di [Firebase Console](https://console.firebase.google.com/), tambah app
   Android dengan package **`com.example.aplikasimobile4d`**.
3. Unduh **`google-services.json`** dan letakkan di folder **`app/`**.
   *(File ini berisi kunci proyek dan sudah di-`.gitignore` — tidak ikut di-commit.)*
4. Aktifkan **Realtime Database** (lokasi mis. `asia-southeast1`).
5. Pasang security rules dari [`database.rules.json`](./database.rules.json) di tab **Rules**.
6. Buka di Android Studio, **sync Gradle**, lalu **Run** ▶️.

> ⚠️ Tanpa `google-services.json`, sinkronisasi Gradle akan gagal — itu memang langkah wajib.

## 🔐 Security Rules

[`database.rules.json`](./database.rules.json) memvalidasi **tipe per field** di server
(`isString`/`isNumber`/`isBoolean`). Mode `.read/.write = true` **hanya untuk belajar** —
untuk produksi gunakan Firebase Auth + data per-pengguna (lihat `app/Docs/MODEL-DATA.md` §6.3).

## 🧭 Status Milestone

| M | Fokus | Status |
|---|---|---|
| M0 | Setup Firebase + dependensi | ✅ |
| M1 | Model + Repository | ✅ |
| M2 | Create + Read realtime | ✅ |
| M3 | Update + Delete + favorit | ✅ |
| M4 | Cari/filter + validasi + DiffUtil | ✅ |
| M5 | Offline + rules + perapihan | ✅ |

## 📖 Pemetaan CRUD ↔ Firebase

| Operasi | Method |
|---|---|
| Create | `push().getKey()` → `setValue(obj)` |
| Read (realtime) | `addValueEventListener` → `getValue(Contact.class)` |
| Update | `updateChildren(map)` |
| Delete | `removeValue()` |

---

*Proyek edukasi. Lihat [`app/Docs/`](./app/Docs/) untuk dokumentasi rancangan lengkap.*

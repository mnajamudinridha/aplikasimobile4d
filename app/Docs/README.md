# Dokumentasi — Aplikasi Daftar Kontak Teman

Aplikasi Android (native, **Java**) untuk mencatat **daftar kontak teman** dengan **CRUD**
penuh dan sinkronisasi **realtime** ke **Firebase Realtime Database**. Dibuat untuk
**pembelajaran**: contoh bersih pola CRUD + tiga tipe data (**String, int, boolean**) yang
mengalir utuh dari form → objek Java → JSON → database.

> Backend = **Firebase Realtime Database** (bukan Cloudflare).

---

## Indeks Dokumen

Baca berurutan:

1. **[PRD.md](./PRD.md)** — *Acuan utama.* Tujuan, persona, scope, functional requirements
   (FR-1..FR-8), NFR, user flow, UI/UX, validasi, risiko, keputusan terbuka.
2. **[MODEL-DATA.md](./MODEL-DATA.md)** — Entitas `Contact`: POJO Java, pohon RTDB + contoh
   JSON, validasi, security rules (DEV & arah produksi).
3. **[ARSITEKTUR.md](./ARSITEKTUR.md)** — Arsitektur Android (MVVM ringan + Repository),
   setup Firebase, dependensi versi teruji, dan **pola kode CRUD** acuan.
4. **[ROADMAP.md](./ROADMAP.md)** — Implementasi bertahap **M0–M5**, checklist, dan titik
   **🎯 LATIHAN** (kode yang ditulis sendiri oleh pembelajar).

---

## Ringkasan Cepat

| Aspek | Nilai |
|---|---|
| Package | `com.example.aplikasimobile4d` |
| Bahasa | Java 1.8 |
| Backend | Firebase Realtime Database |
| Entitas | `Contact` (nama=String, nomorTelepon=String, umur=int, favorit=boolean) |
| Operasi | Create / Read realtime / Update / Delete + toggle favorit |
| minSdk / target / compile | 24 / 32 / 32 |
| AGP / Gradle DSL | 7.2.1 / Groovy |
| Firebase BoM (teruji) | 32.7.0 |

---

## Memulai (ringkas)

> Detail langkah ada di [ARSITEKTUR.md §3–§4](./ARSITEKTUR.md) dan [ROADMAP.md → M0](./ROADMAP.md).

1. Buat project Firebase, tambah app Android `com.example.aplikasimobile4d`.
2. Unduh **`google-services.json`** sendiri → taruh di `app/` *(jangan commit; sudah di-gitignore)*.
3. Aktifkan Realtime Database, pasang rules DEV dari [MODEL-DATA.md §6.1](./MODEL-DATA.md).
4. Tambah plugin & dependensi Firebase (versi teruji di ARSITEKTUR §4).
5. Build & jalankan. Ikuti milestone M0 → M5.

---

## Status

- ✅ **PRD & dokumentasi selesai** (dokumen ini).
- ✅ **Implementasi kode selesai** — milestone M0–M5 (lihat [`README` root](../../README.md)).

---

## Pemetaan CRUD ↔ Firebase (contekan)

| Operasi | Method Firebase |
|---|---|
| **C**reate | `push().getKey()` → `child(id).setValue(obj)` |
| **R**ead (realtime) | `addValueEventListener` → `getChildren()` → `getValue(Contact.class)` |
| **U**pdate | `child(id).updateChildren(map)` |
| **D**elete | `child(id).removeValue()` |

---

*Dokumen ditulis dalam Bahasa Indonesia untuk keperluan pembelajaran.*

# PRD — Aplikasi Daftar Kontak Teman

> **Product Requirements Document**
> Aplikasi mobile Android (native, **Java**) untuk mencatat daftar kontak teman dengan
> operasi **CRUD** penuh dan sinkronisasi **realtime** ke **Firebase Realtime Database (RTDB)**.

| Item | Nilai |
|---|---|
| Nama aplikasi | Daftar Kontak Teman |
| Package | `com.example.aplikasimobile4d` |
| Platform | Android (native) |
| Bahasa | Java (Java 1.8) |
| Backend | Firebase Realtime Database |
| Tujuan | **Pembelajaran** pola CRUD + realtime DB yang bersih |
| Status dokumen | Draft v1 — *belum ada implementasi kode* |
| minSdk / targetSdk / compileSdk | 24 / 32 / 32 |

Dokumen ini adalah **acuan utama**. Detail teknis dipecah ke dokumen pendamping:

- [`MODEL-DATA.md`](./MODEL-DATA.md) — struktur data, POJO, pohon RTDB, security rules
- [`ARSITEKTUR.md`](./ARSITEKTUR.md) — arsitektur Android, setup Firebase, pola kode CRUD
- [`ROADMAP.md`](./ROADMAP.md) — milestone M0–M5, checklist, titik latihan
- [`README.md`](./README.md) — indeks & cara pakai dokumen

---

## 1. Ringkasan

Aplikasi **Daftar Kontak Teman** adalah aplikasi Android sederhana untuk menyimpan,
menampilkan, mengubah, dan menghapus daftar teman. Setiap kontak punya nama (teks),
umur (angka), dan penanda favorit (ya/tidak). Semua data disimpan di **Firebase Realtime
Database** sehingga setiap perubahan langsung tersinkron **realtime** ke seluruh perangkat
yang membuka aplikasi — tanpa perlu tombol "refresh".

Aplikasi ini punya **tujuan ganda**:
1. **Produk fungsional** — buku kontak teman yang benar-benar bisa dipakai.
2. **Bahan ajar** — contoh referensi pola CRUD Firebase RTDB di Android dengan Java yang
   rapi, mudah dibaca, dan menunjukkan tipe data **String, int, dan boolean** mengalir
   utuh dari form → objek Java → JSON → database.

---

## 2. Latar Belakang & Tujuan Pembelajaran

Banyak tutorial CRUD hanya menampilkan satu tipe data (teks). Aplikasi ini sengaja memakai
**tiga tipe data berbeda** dalam satu entitas agar pembelajar memahami bagaimana masing-masing
tipe diperlakukan di tiap lapisan.

**Tujuan pembelajaran (learning objectives):**

- **LO-1** — Memahami 4 operasi CRUD dan padanannya di Firebase RTDB:
  `push()/setValue()` (Create), `addValueEventListener` (Read realtime),
  `updateChildren()` (Update), `removeValue()` (Delete).
- **LO-2** — Membuat **POJO** Java yang kompatibel dengan (de)serialisasi otomatis RTDB
  (konstruktor kosong, field/getter publik).
- **LO-3** — Menangani tiga tipe input: **String** (`nama`), **int** (`umur`),
  **boolean** (`favorit`) — termasuk validasi dan konversi `String` → `int` dari form.
- **LO-4** — Menampilkan list dinamis dengan **RecyclerView + Adapter** yang ikut berubah
  saat data di server berubah (realtime).
- **LO-5** — Memahami **lifecycle listener** (pasang/lepas) agar tidak terjadi kebocoran
  memori atau callback ganda.
- **LO-6** — Mengonfigurasi **security rules** dan **persistence (offline)** dasar.

> Penekanan: kode harus **mudah dibaca** dan **konsisten**, bukan paling singkat atau
> paling "pintar". Ini referensi belajar.

---

## 3. Sasaran Pengguna (Persona)

| Persona | Deskripsi | Kebutuhan utama |
|---|---|---|
| **Pelajar Android** (utama) | Sedang belajar Java + Firebase, ingin contoh CRUD bersih | Kode jelas, pola standar, bisa ditiru |
| **Pengguna akhir** | Ingin mencatat kontak teman secara ringan | Tambah/ubah/hapus cepat, data tidak hilang |
| **Pengajar/Mentor** | Memakai app sebagai bahan demo di kelas | Alur mudah dijelaskan, satu entitas, tidak rumit |

---

## 4. Lingkup (Scope)

### 4.1 Termasuk (In Scope)

- Satu entitas tunggal: **Contact** (Kontak).
- CRUD penuh: tambah, lihat (realtime), ubah, hapus.
- Tiga tipe field: String (`nama`, `nomorTelepon`), int (`umur`), boolean (`favorit`).
- Daftar kontak dengan RecyclerView, urut berdasarkan nama (A→Z).
- Toggle cepat status **favorit** langsung dari daftar.
- Pencarian/filter sederhana di sisi klien (by nama & favorit).
- Status kosong (empty state) dan indikator loading.
- Dukungan **offline** dasar (persistence) dan sinkronisasi otomatis saat online kembali.

### 4.2 Tidak Termasuk (Out of Scope) — untuk versi belajar ini

- Autentikasi pengguna (login/registrasi). *Lihat keputusan D-1.*
- Foto/avatar kontak (upload Storage).
- Multi-pengguna dengan data terpisah per akun.
- Pagination / data berskala besar.
- Push notification.
- Sinkronisasi ke kalender/kontak telepon perangkat.

---

## 5. Entitas & Model Data (Ringkas)

Entitas tunggal **Contact**. Detail lengkap di [`MODEL-DATA.md`](./MODEL-DATA.md).

| Field | Tipe | Wajib | Contoh | Catatan |
|---|---|---|---|---|
| `id` | String | auto | `-NpQ1aB2c...` | push key dari Firebase |
| `nama` | **String** | ✅ | `"Budi"` | tidak boleh kosong |
| `nomorTelepon` | **String** | ❌ | `"081234..."` | String (jaga angka 0 di depan) |
| `umur` | **int** | ❌ | `21` | ≥ 0, default `0` |
| `favorit` | **boolean** | ❌ | `true` | default `false` |
| `createdAt` | long (epoch ms) | auto | `1717800000000` | waktu dibuat |
| `updatedAt` | long (epoch ms) | auto | `1717800500000` | waktu diubah |

> **Catatan tipe data**: nomor telepon **sengaja** disimpan sebagai `String`, bukan `int`,
> karena angka `0` di depan akan hilang dan nomor panjang bisa melebihi rentang `int`.
> Demo tipe **int** diwakili oleh `umur` yang memang murni angka. Ini poin belajar penting:
> *"angka yang ditampilkan" tidak selalu cocok disimpan sebagai tipe angka.*

---

## 6. Functional Requirements (FR)

Setiap FR memetakan ke operasi CRUD dan menunjukkan target pembelajaran.

### FR-1 — Tambah Kontak (Create)
Pengguna membuka form, mengisi `nama` (wajib), `nomorTelepon`, `umur`, dan `favorit`,
lalu menyimpan. Aplikasi membuat node baru via `push()` dan menulis data via `setValue()`.
- **Validasi**: `nama` tidak boleh kosong; `umur` harus angka ≥ 0 (atau kosong → 0).
- **Hasil**: kontak baru langsung muncul di daftar semua perangkat (realtime).

### FR-2 — Lihat Daftar Kontak (Read, realtime)
Saat layar utama dibuka, aplikasi memasang listener realtime ke node `contacts`.
Setiap perubahan di server (tambah/ubah/hapus) langsung memperbarui daftar.
- **Tampilan**: RecyclerView, item menampilkan nama, umur, nomor, ikon favorit (★).
- **Urutan**: berdasarkan `nama` A→Z (diurutkan di sisi klien).
- **Empty state**: jika belum ada kontak, tampilkan pesan "Belum ada kontak".

### FR-3 — Ubah Kontak (Update)
Pengguna memilih kontak → membuka form ter-isi → mengubah → menyimpan.
Aplikasi melakukan **partial update** via `updateChildren(Map)` (hanya field berubah +
`updatedAt`), bukan menimpa seluruh node.
- **Hasil**: perubahan tersinkron realtime.

### FR-4 — Hapus Kontak (Delete)
Pengguna menghapus kontak (mis. tahan-lama atau tombol hapus). Muncul **dialog konfirmasi**
sebelum benar-benar menghapus via `removeValue()`.
- **Catatan**: hard delete (tidak ada undo di MVP — *lihat D-4*).

### FR-5 — Toggle Favorit Cepat
Dari daftar, pengguna menekan ikon bintang untuk menandai/melepas favorit. Ini meng-update
**hanya** field `favorit` (boolean) tanpa membuka form penuh — demo update field tunggal.

### FR-6 — Cari & Filter
Pengguna mengetik di kotak cari → daftar tersaring berdasarkan `nama` (case-insensitive).
Tersedia filter "hanya favorit". Penyaringan dilakukan **di sisi klien** (data kecil).

### FR-7 — Status Loading & Kosong
Saat pertama memuat, tampilkan indikator loading. Setelah selesai: tampilkan data atau
empty state. Saat gagal (mis. permission/offline tanpa cache), tampilkan pesan error ramah.

### FR-8 — Dukungan Offline
Persistence diaktifkan sekali di `Application.onCreate()`. Saat offline, perubahan
disimpan lokal dan otomatis dikirim saat koneksi kembali.

---

## 7. Non-Functional Requirements (NFR)

| Kode | Kategori | Persyaratan |
|---|---|---|
| NFR-1 | Realtime | Perubahan tampil di perangkat lain **< 1 detik** pada koneksi normal |
| NFR-2 | Offline | Aplikasi tetap menampilkan data terakhir & menerima input saat offline |
| NFR-3 | Keamanan | Security rules dikonfigurasi; mode `true/true` **hanya** untuk dev (*lihat D-1*) |
| NFR-4 | Kompatibilitas | Berjalan di Android API 24+ (Android 7.0) |
| NFR-5 | Keterbacaan | Penamaan konsisten, satu kelas satu tanggung jawab, ada komentar pengajaran |
| NFR-6 | Stabilitas | Tidak ada kebocoran listener; callback dilepas di `onStop`/`onDestroy` |
| NFR-7 | Ukuran | APK debug wajar; hanya dependensi yang dipakai |

---

## 8. Alur Pengguna (User Flows)

**Tambah kontak**
```
Layar Utama (daftar) → tap FAB (+) → Form Tambah → isi nama/umur/telepon/favorit
→ tap Simpan → validasi OK → push ke RTDB → kembali ke daftar → kontak baru muncul (realtime)
```

**Ubah kontak**
```
Layar Utama → tap item → Form Ubah (ter-isi) → ubah field → Simpan
→ updateChildren ke RTDB → kembali → item ter-update (realtime)
```

**Hapus kontak**
```
Layar Utama → tahan-lama item (atau tombol hapus) → Dialog konfirmasi
→ "Hapus" → removeValue → item hilang dari daftar (realtime)
```

**Toggle favorit**
```
Layar Utama → tap ikon ★ pada item → update field favorit → ikon berubah (realtime)
```

---

## 9. UI / UX

Dua layar utama (sengaja minimal):

**A. MainActivity — Daftar Kontak**
- AppBar judul "Kontak Teman" + kotak cari + filter favorit.
- RecyclerView berisi kartu kontak: `nama`, `umur thn`, `nomorTelepon`, ikon ★.
- **FAB (+)** di kanan bawah → buka form tambah.
- Empty state & loading.

**B. AddEditContactActivity — Form Tambah/Ubah**
- Input `nama` (teks, wajib), `nomorTelepon` (telepon), `umur` (number), `favorit` (Switch/Checkbox).
- Tombol **Simpan**; mode "Tambah" vs "Ubah" ditentukan apakah ada `id` yang dikirim.
- Pesan error inline saat validasi gagal.

> Detail komponen & layout di [`ARSITEKTUR.md`](./ARSITEKTUR.md).

---

## 10. Arsitektur (Ringkas)

Pola **MVVM ringan + Repository**:

```
UI (MainActivity, AddEditContactActivity, ContactAdapter)
        ↓ memanggil
ContactRepository  ── membungkus ──>  DatabaseReference (Firebase RTDB)
        ↑ callback realtime
Model: Contact (POJO)
```

- `Contact` — model data (POJO).
- `ContactRepository` — satu-satunya tempat yang menyentuh `DatabaseReference`
  (create/read/update/delete). UI tidak memanggil Firebase langsung.
- Activity hanya mengurus tampilan & interaksi.

Detail penuh + pola kode CRUD ada di [`ARSITEKTUR.md`](./ARSITEKTUR.md).

---

## 11. Validasi & Edge Cases

| Kasus | Penanganan |
|---|---|
| `nama` kosong | Tolak simpan, tampilkan error "Nama wajib diisi" |
| `umur` bukan angka | Tolak / kosongkan → default `0` |
| `umur` negatif | Tolak, "Umur tidak valid" |
| `nomorTelepon` kosong | Boleh (opsional) |
| Hapus saat offline | Ditunda, dieksekusi saat online (persistence) |
| Data lama tanpa field baru | POJO memberi nilai default (mis. `favorit=false`) |
| Karakter spesial di nama | Disimpan apa adanya (String) |

---

## 12. Metrik Keberhasilan (untuk pembelajaran)

- ✅ Keempat operasi CRUD bekerja dan tersinkron realtime di **dua perangkat** sekaligus.
- ✅ Tiga tipe data (String/int/boolean) tersimpan & terbaca dengan tipe yang benar di RTDB.
- ✅ Listener dilepas dengan benar (tidak ada warning kebocoran / callback ganda).
- ✅ Aplikasi tetap menampilkan data saat offline lalu sinkron saat online.
- ✅ Pembelajar bisa menjelaskan padanan setiap operasi CRUD ↔ method Firebase.

---

## 13. Risiko & Mitigasi

| Risiko | Dampak | Mitigasi |
|---|---|---|
| Versi Firebase tidak cocok AGP 7.2.1 | Build gagal | Pakai versi teruji di `ARSITEKTUR.md` (BoM 32.7.0) |
| `google-services.json` belum ditambahkan | Crash saat init | Langkah setup wajib di M0, dokumen ARSITEKTUR |
| Listener tidak dilepas | Kebocoran/crash | Pasang di `onStart`, lepas di `onStop` |
| Rules `true/true` lolos ke produksi | Data publik | Tandai jelas "dev only", D-1 |
| Konversi `umur` gagal | Crash `NumberFormatException` | Validasi & `try/parse` aman sebelum simpan |

---

## 14. Keputusan Desain Terbuka (Open Decisions)

Default MVP dipilih, **belum final** — bisa diubah pengguna.

- **D-1 — Autentikasi**: MVP **tanpa Auth** (rules dev `.read/.write = true`).
  Produksi sebaiknya pakai Firebase Auth + data per-user (`contacts/{uid}`).
- **D-2 — Pengurutan**: diurutkan **di sisi klien** (data kecil). Alternatif: `orderByChild("nama")` + `.indexOn`.
- **D-3 — Hapus**: **hard delete** (`removeValue`). Alternatif: soft delete (field `deleted`).
- **D-4 — Undo**: **tidak ada** undo di MVP. Bisa ditambah Snackbar "Urungkan".
- **D-5 — Nomor telepon**: disimpan **String** (keputusan final, lihat §5).

---

## 15. Rencana Bertahap (Ringkas)

Implementasi dibagi milestone **M0–M5** (detail + checklist di [`ROADMAP.md`](./ROADMAP.md)):

| Milestone | Fokus |
|---|---|
| **M0** | Setup project + Firebase + dependensi |
| **M1** | Model `Contact` + Repository + koneksi RTDB (baca list kosong) |
| **M2** | Create + Read realtime (daftar menampilkan kontak) |
| **M3** | Update + Delete + toggle favorit |
| **M4** | Cari/filter + validasi + empty/loading state |
| **M5** | Offline persistence + security rules + perapihan |

---

## 16. Glosarium

- **CRUD** — Create, Read, Update, Delete.
- **RTDB** — Firebase Realtime Database, basis data JSON tersinkron realtime.
- **POJO** — Plain Old Java Object; objek Java biasa untuk memetakan data.
- **push key** — kunci unik terurut-waktu yang dihasilkan `DatabaseReference.push()`.
- **listener** — objek yang menerima notifikasi saat data berubah.
- **persistence** — penyimpanan cache lokal Firebase agar app jalan offline.
- **FAB** — Floating Action Button.

---

*Dokumen ini adalah PRD acuan. Lihat dokumen pendamping untuk detail teknis implementasi.*

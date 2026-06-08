# Roadmap Implementasi — Aplikasi Daftar Kontak Teman

Rencana implementasi bertahap (**M0–M5**). Setiap milestone punya: **tujuan**, **checklist
tugas**, **target belajar**, **kriteria selesai (acceptance)**, dan saran **commit**.
Beberapa bagian ditandai **🎯 LATIHAN** — tempat kamu menulis sendiri 5–10 baris kode inti
(bukan boilerplate) agar benar-benar paham.

Acuan: [`PRD.md`](./PRD.md) · [`MODEL-DATA.md`](./MODEL-DATA.md) · [`ARSITEKTUR.md`](./ARSITEKTUR.md)

---

## Ikhtisar Milestone

| M | Fokus | Hasil yang terlihat |
|---|---|---|
| **M0** | Setup project + Firebase + dependensi | App build & connect ke RTDB |
| **M1** | Model + Repository + baca list kosong | Daftar tampil (masih kosong), tanpa crash |
| **M2** | Create + Read realtime | Tambah kontak → langsung muncul di daftar |
| **M3** | Update + Delete + toggle favorit | Bisa ubah, hapus, dan tandai favorit |
| **M4** | Cari/filter + validasi + empty/loading | Form aman, daftar bisa disaring |
| **M5** | Offline + security rules + perapihan | Jalan offline, rules terpasang, kode rapi |

---

## M0 — Setup Project & Firebase

**Tujuan:** aplikasi terhubung ke Firebase RTDB dan bisa di-build.

**Checklist:**
- [ ] Buat project Firebase, tambah app Android (`com.example.aplikasimobile4d`).
- [ ] Unduh `google-services.json` → taruh di `app/`.
- [ ] Tambah `google-services.json` ke `.gitignore`.
- [ ] Aktifkan Realtime Database (lokasi `asia-southeast1`), set rules **DEV**.
- [ ] Tambah plugin & dependensi (lihat [`ARSITEKTUR.md` §4](./ARSITEKTUR.md)).
- [ ] Tambah izin `INTERNET` + `ACCESS_NETWORK_STATE` di manifest.
- [ ] Buat `App.java` (Application) + `setPersistenceEnabled(true)`, daftarkan di manifest.

**Target belajar:** LO-6 (konfigurasi project & persistence).
**Acceptance:** project **build sukses**; log menunjukkan koneksi RTDB tanpa error.
**Commit:** `chore(setup): integrasi Firebase RTDB + dependensi`

---

## M1 — Model & Repository (baca daftar kosong)

**Tujuan:** ada `Contact` (POJO) dan `ContactRepository`; layar utama memasang listener dan
menampilkan daftar (masih kosong) tanpa crash.

**Checklist:**
- [ ] Buat `model/Contact.java` (lihat [`MODEL-DATA.md` §2](./MODEL-DATA.md)).
- [ ] Buat `data/ContactRepository.java` dengan `observeAll()` + `removeObserver()`.
- [ ] `MainActivity` + RecyclerView + `ContactAdapter` kosong.
- [ ] Pasang listener di `onStart`, lepas di `onStop`.

**🎯 LATIHAN M1 — `Contact.toMap()`**
> Di `Contact.java`, tulis method `toMap()` (≈8 baris). **Keputusan yang kamu ambil:**
> field apa saja yang ikut dikirim saat update? Apakah `createdAt` boleh ikut (berisiko
> tertimpa) atau cukup `updatedAt`? Pertimbangkan: `updateChildren` hanya menyentuh key
> yang ada di map. Lihat kerangka di `MODEL-DATA.md §2`.

**Target belajar:** LO-2 (POJO), LO-5 (lifecycle listener).
**Acceptance:** app jalan, daftar kosong tampil, **tidak crash** saat buka/tutup berulang.
**Commit:** `feat(data): model Contact + repository observeAll`

---

## M2 — Create & Read Realtime

**Tujuan:** bisa menambah kontak lewat form, dan daftar ter-update **realtime**.

**Checklist:**
- [ ] `AddEditContactActivity` (form: nama, nomorTelepon, umur, favorit).
- [ ] FAB di `MainActivity` → buka form mode "Tambah".
- [ ] `ContactRepository.create()` (push + setValue).
- [ ] `ContactAdapter` render item (nama, umur, nomor, ikon ★).
- [ ] Verifikasi realtime: ubah data lewat Firebase Console → daftar ikut berubah.

**🎯 LATIHAN M2 — konversi & validasi input form**
> Di `AddEditContactActivity`, tulis logika ambil-input-form (≈10 baris): baca `nama`,
> `umur` (**String → int** yang aman), `favorit` (Switch → boolean). **Keputusan kamu:**
> jika `umur` kosong → `0` atau tolak? jika bukan angka → pesan apa? (lihat `MODEL-DATA.md §5`).

**Target belajar:** LO-1 (Create), LO-3 (tiga tipe data), LO-4 (RecyclerView realtime).
**Acceptance:** tambah kontak → **langsung** muncul di daftar (uji 2 perangkat/emulator).
**Commit:** `feat(crud): tambah kontak (Create) + daftar realtime (Read)`

---

## M3 — Update, Delete & Toggle Favorit

**Tujuan:** lengkapi CRUD — ubah, hapus, dan tandai favorit.

**Checklist:**
- [ ] Tap item → buka form mode "Ubah" (ter-isi lewat `id`).
- [ ] `ContactRepository.update()` (updateChildren partial).
- [ ] Tahan-lama / tombol hapus → **dialog konfirmasi** → `delete()` (removeValue).
- [ ] Tap ikon ★ di item → `toggleFavorit()` (update field boolean tunggal).

**Target belajar:** LO-1 (Update/Delete), LO-3 (update boolean).
**Acceptance:** ketiga operasi tersinkron realtime; hapus minta konfirmasi dulu.
**Commit:** `feat(crud): ubah, hapus, dan toggle favorit`

---

## M4 — Cari/Filter, Validasi & State

**Tujuan:** form aman dari input buruk; daftar bisa disaring; ada empty/loading state.

**Checklist:**
- [ ] Kotak cari (filter `nama`, case-insensitive) + filter "hanya favorit".
- [ ] Validasi simpan: `nama` wajib, `umur` valid (lihat `MODEL-DATA.md §5`).
- [ ] Empty state ("Belum ada kontak") + indikator loading awal.
- [ ] Pesan error ramah saat operasi gagal.

**🎯 LATIHAN M4a — logika filter di sisi klien**
> Tulis fungsi `filter(query, hanyaFavorit)` (≈8 baris) yang menyaring `List<Contact>`.
> **Keputusan kamu:** cocokkan `nama` saja atau juga `nomorTelepon`? case-sensitive?
> Bagaimana jika query kosong?

**🎯 LATIHAN M4b — `DiffUtil.areContentsTheSame`**
> Tentukan **kapan dua `Contact` dianggap "sama isinya"** (≈6 baris) agar RecyclerView
> hanya menggambar ulang yang berubah. **Keputusan kamu:** field mana yang dibandingkan?
> Apakah `updatedAt` cukup sebagai penanda perubahan, atau bandingkan field per field?

**Target belajar:** LO-3, LO-4, penanganan edge case (PRD §11).
**Acceptance:** input buruk ditolak dengan pesan jelas; cari/filter bekerja; ada empty/loading.
**Commit:** `feat(ux): cari/filter + validasi input + empty/loading state`

---

## M5 — Offline, Security Rules & Perapihan

**Tujuan:** aplikasi tahan offline, rules terpasang, dan kode dirapikan sebagai referensi.

**Checklist:**
- [ ] Uji offline: matikan internet → tambah/ubah → nyalakan → data tersinkron.
- [ ] Pasang **security rules** lengkap (validasi tipe per field) dari `MODEL-DATA.md §6.1`.
- [ ] Rapikan penamaan, tambah komentar pengajaran, hapus kode tak terpakai (NFR-5).
- [ ] Tulis `README` singkat cara menjalankan (butuh `google-services.json` sendiri).
- [ ] (Opsional) Snackbar "Urungkan" untuk hapus (D-4).

**Target belajar:** LO-6 (offline & rules), keterbacaan.
**Acceptance:** semua FR (FR-1..FR-8) terpenuhi; metrik PRD §12 tercapai.
**Commit:** `feat(reliability): offline persistence + security rules + perapihan`

---

## Ringkasan Titik Latihan (Learning Mode)

| Kode | Lokasi | Apa yang kamu putuskan |
|---|---|---|
| 🎯 M1 | `Contact.toMap()` | Field mana yang ikut saat partial update |
| 🎯 M2 | `AddEditContactActivity` | Aturan konversi & validasi `umur` (String→int) |
| 🎯 M4a | filter klien | Kriteria pencocokan & case-sensitivity |
| 🎯 M4b | `DiffUtil` | Definisi "dua kontak sama isinya" |

> Sisanya (boilerplate Activity, layout, wiring Firebase) akan disediakan/dibimbing. Empat
> titik di atas dipilih karena **ada keputusan desain bermakna** — bukan sekadar mengetik.

---

## Keputusan Desain Terbuka (rujuk PRD §14)

Konfirmasikan sebelum/selama implementasi bila ingin menyimpang dari default MVP:

- **D-1** Auth (default: tanpa Auth, rules DEV) — naikkan ke per-user saat produksi.
- **D-2** Pengurutan (default: klien) — bisa pindah ke `orderByChild` + index.
- **D-3** Hapus (default: hard delete) — bisa soft delete.
- **D-4** Undo (default: tidak ada) — bisa Snackbar urungkan.
- **D-5** Nomor telepon (final: String).

---

*Setelah M5, evaluasi: apakah perlu fitur lanjutan (foto, multi-user, Auth)? Itu di luar
scope versi belajar ini — buka PRD baru bila diperlukan.*

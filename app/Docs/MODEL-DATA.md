# Model Data — Aplikasi Daftar Kontak Teman

Dokumen ini merinci **struktur data** entitas `Contact`: bentuk POJO Java, pohon JSON di
Firebase Realtime Database, aturan validasi, dan security rules. Lihat [`PRD.md`](./PRD.md)
untuk konteks produk dan [`ARSITEKTUR.md`](./ARSITEKTUR.md) untuk pola kode CRUD.

---

## 1. Entitas `Contact`

Satu entitas tunggal. Inti pembelajaran: **satu objek, tiga tipe data** (String, int, boolean)
mengalir konsisten dari form → POJO Java → JSON → database.

| Field | Tipe Java | Tipe JSON/RTDB | Wajib | Default | Keterangan |
|---|---|---|---|---|---|
| `id` | `String` | string (key) | auto | — | **push key**, bukan disimpan di dalam node (lihat §4) |
| `nama` | `String` | string | ✅ | — | Nama teman; tidak boleh kosong |
| `nomorTelepon` | `String` | string | ❌ | `""` | Disimpan String (jaga `0` di depan) |
| `umur` | `int` | number | ❌ | `0` | ≥ 0 |
| `favorit` | `boolean` | boolean | ❌ | `false` | Penanda teman favorit |
| `createdAt` | `long` | number | auto | now | Epoch milidetik saat dibuat |
| `updatedAt` | `long` | number | auto | now | Epoch milidetik saat terakhir diubah |

> **Kenapa `nomorTelepon` String, bukan int?**
> Nomor seperti `081234567` akan kehilangan `0` di depan jika disimpan angka, dan nomor
> panjang melebihi rentang `int` (maks ±2,1 miliar). Tipe **int** murni diwakili `umur`.
> Ini contoh nyata: *tidak semua "angka" cocok jadi tipe angka.*

---

## 2. Rancangan POJO Java

POJO RTDB punya **syarat khusus** agar (de)serialisasi otomatis bekerja:

1. **Konstruktor kosong publik** wajib ada → dipakai `snapshot.getValue(Contact.class)`.
2. Field **publik** atau punya **getter** publik agar terbaca saat ditulis ke DB.
3. Field yang **tidak** ingin ikut disimpan ditandai `@Exclude` (mis. `id`, karena id =
   push key, bukan bagian isi node).
4. Sediakan `toMap()` untuk **partial update** (Update field tertentu saja).

Kerangka POJO (referensi — implementasi final di tahap kode):

```java
package com.example.aplikasimobile4d.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Contact {

    // id = push key. Tidak disimpan di dalam node → @Exclude.
    @Exclude
    public String id;

    public String  nama;          // String  — wajib
    public String  nomorTelepon;  // String  — opsional
    public int     umur;          // int     — default 0
    public boolean favorit;       // boolean — default false
    public long    createdAt;
    public long    updatedAt;

    // (1) Konstruktor kosong WAJIB untuk getValue(Contact.class)
    public Contact() { }

    public Contact(String nama, String nomorTelepon, int umur, boolean favorit) {
        this.nama = nama;
        this.nomorTelepon = nomorTelepon;
        this.umur = umur;
        this.favorit = favorit;
    }

    // (4) toMap() untuk partial update via updateChildren(...)
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("nama", nama);
        m.put("nomorTelepon", nomorTelepon);
        m.put("umur", umur);
        m.put("favorit", favorit);
        m.put("createdAt", createdAt);
        m.put("updatedAt", updatedAt);
        return m;
    }
}
```

> **Catatan belajar**: di RTDB, field `boolean` Java menjadi `true/false` JSON,
> `int`/`long` menjadi `number`, dan `String` menjadi `string`. Karena JSON hanya punya
> satu tipe angka, `int` dan `long` sama-sama jadi `number` — saat dibaca kembali, Firebase
> mengembalikannya ke tipe Java sesuai deklarasi field POJO.

---

## 3. Struktur Pohon RTDB

Struktur **datar (flat)** sesuai best practice RTDB — hindari nesting dalam.

```
aplikasimobile4d-rtdb/            (root database)
└── contacts/                      (koleksi semua kontak)
    ├── -NpQ1aB2c3D4e5F6/          (push key = id kontak)
    │   ├── nama:         "Budi Santoso"
    │   ├── nomorTelepon: "081234567890"
    │   ├── umur:         21
    │   ├── favorit:      true
    │   ├── createdAt:    1717800000000
    │   └── updatedAt:    1717800000000
    │
    └── -NpQ1xY9z8W7v6U5/
        ├── nama:         "Citra Dewi"
        ├── nomorTelepon: "085600001111"
        ├── umur:         20
        ├── favorit:      false
        ├── createdAt:    1717800100000
        └── updatedAt:    1717800100000
```

Contoh JSON ekuivalen (untuk impor/uji):

```json
{
  "contacts": {
    "-NpQ1aB2c3D4e5F6": {
      "nama": "Budi Santoso",
      "nomorTelepon": "081234567890",
      "umur": 21,
      "favorit": true,
      "createdAt": 1717800000000,
      "updatedAt": 1717800000000
    },
    "-NpQ1xY9z8W7v6U5": {
      "nama": "Citra Dewi",
      "nomorTelepon": "085600001111",
      "umur": 20,
      "favorit": false,
      "createdAt": 1717800100000,
      "updatedAt": 1717800100000
    }
  }
}
```

> `id` (push key) menjadi **nama node**, jadi tidak diduplikasi di dalam isi node
> (itulah alasan `@Exclude` pada field `id`). Saat membaca, id diambil dari `snapshot.getKey()`.

---

## 4. Aturan Penamaan & Konvensi

- Node koleksi: `contacts` (jamak, lowercase).
- Key kontak: **push key** otomatis (`-Nxxxx`) — terurut waktu, unik, aman dari tabrakan.
- Field: `camelCase` (`nomorTelepon`, `createdAt`) — sama persis dengan nama field POJO.
- Waktu: **epoch milidetik** (`long`), bukan string tanggal — mudah diurutkan & dibandingkan.

---

## 5. Validasi (di sisi Klien)

Dilakukan sebelum menulis ke RTDB (lihat FR-1/FR-3 di PRD):

| Field | Aturan klien |
|---|---|
| `nama` | `trim()` tidak kosong; panjang ≤ 60 |
| `umur` | parse aman dari String; `0 ≤ umur ≤ 150`; kosong → `0` |
| `nomorTelepon` | opsional; jika diisi, hanya digit/`+`/spasi; panjang ≤ 20 |
| `favorit` | dari Switch/Checkbox → langsung boolean |

Konversi `umur` (String form → int) harus aman:

```java
int umur = 0;
String raw = inputUmur.getText().toString().trim();
if (!raw.isEmpty()) {
    try {
        umur = Integer.parseInt(raw);
    } catch (NumberFormatException e) {
        // tampilkan error "Umur harus angka", batalkan simpan
    }
}
```

---

## 6. Security Rules (Validasi di sisi Server)

Rules RTDB memvalidasi **tipe & isi** di server — pertahanan terakhir walau klien bermasalah.
Ini juga menegaskan kembali ketiga tipe data secara eksplisit.

### 6.1 Mode Pengembangan (DEV) — *jangan dipakai produksi*

```json
{
  "rules": {
    "contacts": {
      ".read": true,
      ".write": true,
      "$contactId": {
        ".validate": "newData.hasChildren(['nama'])",
        "nama":         { ".validate": "newData.isString() && newData.val().length > 0 && newData.val().length <= 60" },
        "nomorTelepon": { ".validate": "newData.isString() && newData.val().length <= 20" },
        "umur":         { ".validate": "newData.isNumber() && newData.val() >= 0 && newData.val() <= 150" },
        "favorit":      { ".validate": "newData.isBoolean()" },
        "createdAt":    { ".validate": "newData.isNumber()" },
        "updatedAt":    { ".validate": "newData.isNumber()" },
        "$other":       { ".validate": false }
      }
    }
  }
}
```

> `.read/.write = true` artinya **siapa pun** bisa baca/tulis. Aman untuk belajar di proyek
> pribadi, **tidak** untuk publik. Lihat keputusan **D-1** di PRD.

### 6.2 Catatan tiga tipe data di rules

| Field | Rule kunci | Memastikan |
|---|---|---|
| `nama` | `newData.isString()` | wajib **teks** |
| `umur` | `newData.isNumber()` | wajib **angka** |
| `favorit` | `newData.isBoolean()` | wajib **boolean** |

Inilah inti pelajaran: tipe Java (`String/int/boolean`) **sejajar** dengan validasi server
(`isString/isNumber/isBoolean`). Klien jahat tidak bisa menulis `umur` berupa teks.

### 6.3 Arah Produksi (referensi, di luar scope MVP)

Dengan Firebase Auth, batasi per pengguna:

```json
{
  "rules": {
    "contacts": {
      "$uid": {
        ".read":  "auth != null && auth.uid === $uid",
        ".write": "auth != null && auth.uid === $uid"
      }
    }
  }
}
```

---

## 7. Indexing (Opsional)

Jika nanti pengurutan dipindah ke server (`orderByChild("nama")`), tambahkan index:

```json
"contacts": { ".indexOn": ["nama", "favorit"] }
```

Untuk MVP (data kecil, urut di klien) ini **belum** diperlukan — lihat keputusan **D-2**.

---

*Lihat [`ARSITEKTUR.md`](./ARSITEKTUR.md) untuk bagaimana model ini dibaca/ditulis lewat
`ContactRepository`.*

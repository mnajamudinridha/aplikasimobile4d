package com.example.aplikasimobile4d.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.aplikasimobile4d.R;
import com.example.aplikasimobile4d.data.ContactRepository;
import com.example.aplikasimobile4d.model.Contact;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Form tambah kontak (CREATE).
 *
 * <p>Inti pembelajaran M2: mengambil tiga tipe input dari form lalu menulisnya ke RTDB —
 * String ({@code nama}, {@code nomorTelepon}), int ({@code umur}, dikonversi aman dari String),
 * boolean ({@code favorit}, dari switch). Mode <i>edit</i> menyusul di M3.</p>
 *
 * <p>Setelah simpan berhasil, activity langsung {@code finish()}. Daftar di MainActivity
 * tidak perlu diberi tahu manual — listener realtime-nya yang menampilkan kontak baru.</p>
 */
public class AddEditContactActivity extends AppCompatActivity {

    private TextInputLayout layoutNama;
    private TextInputLayout layoutUmur;
    private TextInputEditText inputNama;
    private TextInputEditText inputTelepon;
    private TextInputEditText inputUmur;
    private SwitchCompat switchFavorit;

    private final ContactRepository repository = new ContactRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);
        setTitle(R.string.judul_tambah);

        layoutNama = findViewById(R.id.layoutNama);
        layoutUmur = findViewById(R.id.layoutUmur);
        inputNama = findViewById(R.id.inputNama);
        inputTelepon = findViewById(R.id.inputTelepon);
        inputUmur = findViewById(R.id.inputUmur);
        switchFavorit = findViewById(R.id.switchFavorit);

        Button buttonSimpan = findViewById(R.id.buttonSimpan);
        buttonSimpan.setOnClickListener(v -> simpan());
    }

    private void simpan() {
        // --- Field String wajib ---
        String nama = textOf(inputNama);
        if (TextUtils.isEmpty(nama)) {
            layoutNama.setError(getString(R.string.error_nama_kosong));
            return;
        }
        layoutNama.setError(null);

        // --- Field int: konversi String -> int yang aman (kosong dianggap 0) ---
        int umur = 0;
        String rawUmur = textOf(inputUmur);
        if (!TextUtils.isEmpty(rawUmur)) {
            try {
                umur = Integer.parseInt(rawUmur);
            } catch (NumberFormatException e) {
                layoutUmur.setError(getString(R.string.error_umur_invalid));
                return;
            }
        }
        layoutUmur.setError(null);

        // --- Field String opsional + boolean ---
        String telepon = textOf(inputTelepon);
        boolean favorit = switchFavorit.isChecked();

        Contact contact = new Contact(nama, telepon, umur, favorit);
        repository.create(contact, new ContactRepository.OnComplete() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddEditContactActivity.this,
                        R.string.pesan_tersimpan, Toast.LENGTH_SHORT).show();
                finish(); // daftar ter-update realtime, tak perlu kirim hasil
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AddEditContactActivity.this,
                        getString(R.string.error_simpan, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}

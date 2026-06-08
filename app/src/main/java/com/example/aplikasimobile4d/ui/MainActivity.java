package com.example.aplikasimobile4d.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplikasimobile4d.R;
import com.example.aplikasimobile4d.data.ContactRepository;
import com.example.aplikasimobile4d.model.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Layar utama: menampilkan daftar kontak secara <b>realtime</b>.
 *
 * <p>Pola lifecycle listener (penting, NFR-6): pasang listener di {@link #onStart()} dan
 * lepas di {@link #onStop()} supaya tidak terjadi kebocoran memori / callback ganda saat
 * layar tidak terlihat.</p>
 */
public class MainActivity extends AppCompatActivity {

    private ContactRepository repository;
    private ContactAdapter adapter;
    private TextView textEmpty;
    private View progress;

    /** Aksi per baris dari adapter: tap=ubah, tahan-lama=hapus, tap bintang=toggle favorit. */
    private final ContactAdapter.OnItemActionListener itemActionListener = new ContactAdapter.OnItemActionListener() {
        @Override
        public void onEdit(Contact contact) {
            bukaForm(contact);
        }

        @Override
        public void onDelete(Contact contact) {
            konfirmasiHapus(contact);
        }

        @Override
        public void onToggleFavorit(Contact contact) {
            repository.toggleFavorit(contact.id, !contact.favorit);
        }
    };

    /** Callback realtime dari repository. Berjalan di main thread → aman update UI langsung. */
    private final ContactRepository.ContactsCallback callback = new ContactRepository.ContactsCallback() {
        @Override
        public void onData(List<Contact> contacts) {
            progress.setVisibility(View.GONE);
            adapter.setItems(contacts);
            textEmpty.setText(R.string.empty_kontak);
            textEmpty.setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onError(Exception e) {
            progress.setVisibility(View.GONE);
            textEmpty.setText(getString(R.string.error_memuat, e.getMessage()));
            textEmpty.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textEmpty = findViewById(R.id.textEmpty);
        progress = findViewById(R.id.progress);
        repository = new ContactRepository();

        RecyclerView recycler = findViewById(R.id.recyclerContacts);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(itemActionListener);
        recycler.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> bukaForm(null)); // null = mode Tambah
    }

    /** Buka form. {@code contact == null} → mode Tambah; selain itu → mode Ubah (data ter-isi). */
    private void bukaForm(Contact contact) {
        Intent intent = new Intent(this, AddEditContactActivity.class);
        if (contact != null) {
            intent.putExtra(AddEditContactActivity.EXTRA_ID, contact.id);
            intent.putExtra(AddEditContactActivity.EXTRA_NAMA, contact.nama);
            intent.putExtra(AddEditContactActivity.EXTRA_TELEPON, contact.nomorTelepon);
            intent.putExtra(AddEditContactActivity.EXTRA_UMUR, contact.umur);
            intent.putExtra(AddEditContactActivity.EXTRA_FAVORIT, contact.favorit);
            intent.putExtra(AddEditContactActivity.EXTRA_CREATED_AT, contact.createdAt);
        }
        startActivity(intent);
    }

    /** Dialog konfirmasi sebelum menghapus (hard delete). */
    private void konfirmasiHapus(Contact contact) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.konfirmasi_hapus_judul)
                .setMessage(getString(R.string.konfirmasi_hapus_pesan, contact.nama))
                .setPositiveButton(R.string.aksi_hapus, (dialog, which) ->
                        repository.delete(contact.id, new ContactRepository.OnComplete() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(MainActivity.this,
                                        R.string.pesan_dihapus, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(MainActivity.this,
                                        getString(R.string.error_hapus, e.getMessage()),
                                        Toast.LENGTH_LONG).show();
                            }
                        }))
                .setNegativeButton(R.string.aksi_batal, null)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        progress.setVisibility(View.VISIBLE);
        repository.observeAll(callback); // pasang listener realtime
    }

    @Override
    protected void onStop() {
        super.onStop();
        repository.removeObserver(); // lepas listener
    }
}

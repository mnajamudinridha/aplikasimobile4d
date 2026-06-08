package com.example.aplikasimobile4d.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

        RecyclerView recycler = findViewById(R.id.recyclerContacts);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter();
        recycler.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditContactActivity.class)));

        repository = new ContactRepository();
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

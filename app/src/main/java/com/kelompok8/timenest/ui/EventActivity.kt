package com.kelompok8.timenest.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kelompok8.timenest.R

class EventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        // Tampilkan fragment di sini
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EventFragment())
            .commit()
    }
}
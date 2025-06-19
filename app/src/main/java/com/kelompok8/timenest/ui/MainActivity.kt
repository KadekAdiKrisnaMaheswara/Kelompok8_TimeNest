package com.kelompok8.timenest.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kelompok8.timenest.R
import com.kelompok8.timenest.ui.EventFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tampilkan EventFragment sebagai awal
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EventFragment())
            .commit()
    }
}
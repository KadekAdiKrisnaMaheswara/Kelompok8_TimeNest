package com.kelompok8.timenest.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kelompok8.timenest.R

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_dashboard)
    }
}

package com.kelompok8.timenest.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kelompok8.timenest.R
import com.android.volley.Request
import com.android.volley.Response
import com.kelompok8.timenest.ui.home.DashboardActivity
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                btnLogin.isEnabled = true
                return@setOnClickListener
            } else {
                val url = "http://10.0.2.2/timenest_api/login.php"

                val stringRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        try {
                            val jsonResponse = JSONObject(response)
                            val status = jsonResponse.getString("status")

                            if (status == "success") {
                                val userId = jsonResponse.getInt("user_id")

                                // Simpan user_id ke SharedPreferences
                                val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                                sharedPref.edit().putInt("user_id", userId).apply()

                                Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, DashboardActivity::class.java))
                                finish()
                            } else {
                                val message = jsonResponse.optString("message", "Login gagal")
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Format respons tidak valid", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }) {
                    override fun getParams(): Map<String, String> {
                        return mapOf(
                            "email" to email,
                            "password" to password
                        )
                    }
                }

                Volley.newRequestQueue(this).add(stringRequest)
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}

package com.kelompok8.timenest.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kelompok8.timenest.R
import com.android.volley.Response
import com.android.volley.Request

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val fullName = findViewById<EditText>(R.id.etFullName)
        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val name = fullName.text.toString().trim()
            val mail = email.text.toString().trim()
            val pass = password.text.toString().trim()
            val confirm = confirmPassword.text.toString().trim()

            if (name.isEmpty() || mail.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            } else if (pass != confirm) {
                Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
            } else {
                val url = "http://10.0.2.2/timenest_api/register.php"

                val stringRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        if (response == "success") {
                            Toast.makeText(this, "Registrasi Berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Gagal: $response", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }) {
                    override fun getParams(): Map<String, String> {
                        return mapOf(
                            "full_name" to name,
                            "email" to mail,
                            "password" to pass
                        )
                    }
                }

                Volley.newRequestQueue(this).add(stringRequest)
            }
        }
    }
}
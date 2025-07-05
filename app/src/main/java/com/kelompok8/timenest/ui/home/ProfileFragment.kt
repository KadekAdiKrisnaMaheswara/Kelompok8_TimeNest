package com.kelompok8.timenest.ui.home

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.kelompok8.timenest.R

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var etProfileName: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var tvProfileEmail: TextView

    private var imageUri: Uri? = null

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DAFTAR PHOTO PICKER
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    imageUri = uri
                    profileImage.setImageURI(uri)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile_fragment, container, false)

        profileImage = view.findViewById(R.id.profileImage)
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto)
        etProfileName = view.findViewById(R.id.etProfileName)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)
        btnLogout = view.findViewById(R.id.btnLogout)
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail)

        loadProfile()

        btnChangePhoto.setOnClickListener {
            openPhotoPicker()
        }

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logout Clicked", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun openPhotoPicker() {
        pickImageLauncher.launch("image/*")
    }

    private fun loadProfile() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        val savedName = sharedPref.getString("user_name", "")
        val savedEmail = sharedPref.getString("email", "kelompok8@gmail.com")
        val savedImageUri = sharedPref.getString("image_uri", null)

        etProfileName.setText(savedName)
        tvProfileEmail.text = savedEmail

        if (savedImageUri != null) {
            profileImage.setImageURI(Uri.parse(savedImageUri))
        }
    }

    private fun saveProfile() {
        val name = etProfileName.text.toString()

        // Simpan ke user_profile (boleh tetap ada kalau memang dipakai)
        val profilePref = requireActivity().getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        profilePref.edit().putString("full_name", name).apply()

        // ✅ Tambahan: simpan juga ke UserSession (biar tidak ke-replace saat login ulang)
        val sessionPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sessionPref.edit().putString("user_name", name).apply()

        // Simpan foto kalau ada
        if (imageUri != null) {
            sessionPref.edit().putString("image_uri", imageUri.toString()).apply()
        }

        Toast.makeText(requireContext(), "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()
    }

}
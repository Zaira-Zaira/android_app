package com.example.fil_rouge.user

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.fil_rouge.R
import com.example.fil_rouge.databinding.ActivityUserInfoBinding
import com.example.fil_rouge.network.Api
import com.example.fil_rouge.network.UserInfo
import com.example.fil_rouge.network.UserWebService
import com.example.fil_rouge.tasklist.Task
import com.example.fil_rouge.tasklist.UserInfoViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.modernstorage.permissions.RequestAccess
import com.google.modernstorage.permissions.StoragePermissions
import com.google.modernstorage.storage.AndroidFileSystem
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*

class UserInfoActivity : AppCompatActivity() {

private lateinit var binding: ActivityUserInfoBinding
    private val getPhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->

        binding.avatarView.load(bitmap)
        if (bitmap == null) return@registerForActivityResult
        lifecycleScope.launch {
            val response = Api.userWebService.updateAvatar(bitmap.toRequestBody());
            if (response.isSuccessful) {
                val userInfo = response.body()
                Log.i("Blqblq", userInfo.toString())
                binding.avatarView.load(userInfo?.avatar)
            } else {
                Log.e("Blqblq", response.message())
            }
        }
    }

    private val userViewModel: UserInfoViewModel by viewModels()
    private lateinit var photoUri: Uri
    private val fileSystem by lazy { AndroidFileSystem(this) };

    private val openCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { accepted ->
        if(accepted){
            galleryLauncher.launch("image/*")
        }
        else{
            Log.e("error", "error");
        }
    }

    private val requestCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accepted ->
            if (accepted) { // lancer l'action souhaité
                getPhoto.launch();
                photoUri = fileSystem.createMediaStoreUri(
                    filename = "picture-${UUID.randomUUID()}.jpg",
                    collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    directory = "Todo",
                )!!
                openCamera.launch(photoUri);
            }
            else{
                Log.e("error", "Camera denied")
            }
        }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction("Open Settings") {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                startActivity(intent)
            }
            .show()
    }


    val requestWriteAccess = registerForActivityResult(RequestAccess()) { accepted ->
        val camPermission = Manifest.permission.CAMERA
        val permissionStatus = checkSelfPermission(camPermission)
        val isAlreadyAccepted = permissionStatus == PackageManager.PERMISSION_GRANTED
        val isExplanationNeeded = shouldShowRequestPermissionRationale(camPermission)

        when {
            isAlreadyAccepted -> getPhoto.launch();
            isExplanationNeeded -> showMessage("Accepter la permission");
            else -> requestCamera.launch(camPermission)
        }
    }


    val requestReadAccess = registerForActivityResult(RequestAccess()) { hasAccess ->
        if (hasAccess) {
            galleryLauncher.launch("image/*")
        } else {
            Log.e("error", "Storage access denied")
        }
    }

    private fun launchCameraWithPermissions() {
        requestWriteAccess.launch(
            RequestAccess.Args(
                action = StoragePermissions.Action.READ_AND_WRITE,
                types = listOf(StoragePermissions.FileType.Image),
                createdBy = StoragePermissions.CreatedBy.Self
            )
        )
    }

    private fun openGallery() {
        requestReadAccess.launch(
            RequestAccess.Args(
                action = StoragePermissions.Action.READ,
                types = listOf(StoragePermissions.FileType.Image),
                createdBy = StoragePermissions.CreatedBy.AllApps
            )
        )
    }

    // register
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {uri->
        binding.avatarView.load(uri)
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            val response = Api.userWebService.updateAvatar(uri.toRequestBody());
            if (response.isSuccessful) {
                val userInfo = response.body()
                Log.i("Blqblq", userInfo.toString())
                binding.avatarView.load(userInfo?.avatar)
            } else {
                Log.e("Blqblq", response.message())
            }
        }
    }

    private fun Uri.toRequestBody(): MultipartBody.Part {
        val fileInputStream = contentResolver.openInputStream(this)!!
        val fileBody = fileInputStream.readBytes().toRequestBody()
        return MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "temp.jpeg",
            body = fileBody
        )
    }


    private fun Bitmap.toRequestBody(): MultipartBody.Part {
        val tmpFile = File.createTempFile("avatar", "jpeg")
        tmpFile.outputStream().use {
            this.compress(Bitmap.CompressFormat.JPEG, 100, it) // this est le bitmap dans ce contexte
        }
        return MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "temp.jpeg",
            body = tmpFile.readBytes().toRequestBody()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.takePictureButton.setOnClickListener{
            launchCameraWithPermissions();
        }
        binding.getAvatar.setOnClickListener{
            openGallery();
        }

        val firstname = findViewById<EditText>(R.id.editFirstname);
        val lastname = findViewById<EditText>(R.id.editLastname);
        val mail = findViewById<EditText>(R.id.editMail);
        val validBtn = findViewById<Button>(R.id.valid_userInfo);


            lifecycleScope.launch {
                val response = Api.userWebService.getInfo()
                if (response.isSuccessful) {
                    val userInfo = response.body()!!
                    firstname.setText(userInfo.firstName);
                    lastname.setText(userInfo.lastName);
                    mail.setText(userInfo.email);
                } else {
                    Log.e("Blqblq", response.message())
                }
        }

        validBtn.setOnClickListener{
            val newUserInfo =
                UserInfo(firstName = firstname.text.toString(), lastName = lastname.text.toString(), email = mail.text.toString(), avatar = "");
            lifecycleScope.launch {
                userViewModel.update(newUserInfo);
            }
            finish();
        }
    }

}
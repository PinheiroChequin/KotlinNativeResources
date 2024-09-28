package com.projeto.native_resource

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.projeto.native_resource.ui.theme.NativeresourceTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainViewModel: MainViewModel by viewModels()

        setContent {
            NativeresourceTheme {
                FormScreen(mainViewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun FormScreen(mainViewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imagePath by remember { mutableStateOf<String?>(null) }

    var registeredUsers by remember { mutableStateOf<List<String>>(emptyList()) }
    var location by remember { mutableStateOf<Location?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val context = LocalContext.current as Activity
    val dbHelper = FormDatabaseHelper(context)
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri?.let { uri ->
                imageBitmap = mainViewModel.uriToBitmap(context, uri)
                imagePath = uri.path
                Log.d("ImagePath", "Imagem salva em: $imagePath")
                Toast.makeText(context, "Imagem salva em: $imagePath", Toast.LENGTH_LONG).show()
            }
        }
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = mainViewModel.createImageFileUri(context)
            imageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
        }
    }

    val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            getLastKnownLocation(context, fusedLocationClient) { loc ->
                location = loc
            }
        } else {
            Toast.makeText(context, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comentário") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) -> {
                        val uri = mainViewModel.createImageFileUri(context)
                        imageUri = uri
                        cameraLauncher.launch(uri)
                    }
                    else -> {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tirar Foto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Imagem capturada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (imagePath != null) {
                    dbHelper.insertFormData(name, email, comment, imagePath!!)
                    registeredUsers = dbHelper.getAllFormData()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) -> {
                        getLastKnownLocation(context, fusedLocationClient) { loc ->
                            location = loc
                        }
                    }
                    else -> {
                        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obter Localização")
        }

        Spacer(modifier = Modifier.height(16.dp))
        location?.let {
            Text("Latitude: ${it.latitude}, Longitude: ${it.longitude}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Usuários Cadastrados:")
        Spacer(modifier = Modifier.height(8.dp))

        registeredUsers.forEach { user ->
            Text(user)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

fun getLastKnownLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLocationReceived(location)
                } else {
                    Toast.makeText(context, "Não foi possível obter a localização", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Log.e("LocationError", "Erro ao obter localização")
                Toast.makeText(context, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
            }
    } else {
        Log.e("PermissionError", "Permissão de localização não concedida")
    }
}

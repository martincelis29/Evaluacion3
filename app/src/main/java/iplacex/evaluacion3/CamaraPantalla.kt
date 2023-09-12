package iplacex.evaluacion3

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamaraUI(
    permisos: ActivityResultLauncher<Array<String>>,
    cameraController: LifecycleCameraController
) {
    val contexto = LocalContext.current
    val mainVM: MainVM = viewModel()

    val ubicacionObtenida = remember { mutableStateOf(false) }
    val dialogVisible = remember { mutableStateOf(false) } // Inicialmente oculto
    val nombreFoto = remember { mutableStateOf("") }
    val fotoTomada = remember { mutableStateOf<Uri?>(null) }

    permisos.launch(
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )
    Box {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PreviewView(it).apply {
                    controller = cameraController
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = { mainVM.PantallaActual.value = Pantalla.Form }
            ) {
                Text("Volver")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(38.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            FloatingActionButton(
                onClick = {
                    if (!ubicacionObtenida.value) {
                        mainVM.onPermisoUbicacionOk = {
                            ubicacionObtenida.value = true
                            conseguirUbicacion(contexto) {
                                mainVM.latitud.value = it.latitude
                                mainVM.longitud.value = it.longitude
                            }
                        }
                        permisos.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                            )
                        )
                    } else {
                        tomarFotografia(
                            cameraController,
                            crearImagen(contexto),
                            contexto
                        ) {
                            fotoTomada.value = it
                            dialogVisible.value = true
                        }
                    }
                },
                shape = CircleShape
            ) {

            }
        }
        if (dialogVisible.value) {
            Dialog(
                onDismissRequest = {
                    dialogVisible.value = false
                }
            ) {
                ElevatedCard(
                    //modifier = Modifier.height(65.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text("Ingrese un nombre para la foto:")
                        Spacer(modifier = Modifier.height(5.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = nombreFoto.value,
                            onValueChange = { nombreFoto.value = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (nombreFoto.value.isNotBlank() && fotoTomada.value != null) {
                                    val fotoConUbicacion = FotoConUbicacion(
                                        nombreFoto.value,
                                        fotoTomada.value!!,
                                        mainVM.latitud.value,
                                        mainVM.longitud.value
                                    )
                                    mainVM.fotosConUbicacion.add(fotoConUbicacion)
                                    dialogVisible.value = false
                                    mainVM.PantallaActual.value = Pantalla.Form
                                }
                            }
                        ) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}


fun tomarFotografia(
    cameraController: LifecycleCameraController,
    archivo: File,
    contexto: Context,
    imagenGuardada: (uri: Uri) -> Unit
) {
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(archivo).build()

    cameraController.takePicture(
        outputFileOptions,
        ContextCompat.getMainExecutor(contexto),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.also {
                    Log.v("tomarFotografia()::onImageSaved", "Foto guardada en ${it.toString()}")
                    imagenGuardada(it)
                }
            }

            override fun onError(e: ImageCaptureException) {
                Log.e("tomarFotografia()", "Error: ${e.message}")
            }
        }
    )
}

fun crearImagen(contexto: Context): File {
    val nombre = LocalDateTime.now().toString().replace(Regex("[T:.-]"), "").substring(0, 14)
    val directorio: File? = contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imagen = File(directorio, "IMG_${nombre}.jpg")
    return imagen
}

fun uri2imageBitmap(uri: Uri, contexto: Context) =
    BitmapFactory.decodeStream(
        contexto.contentResolver.openInputStream(uri)
    ).asImageBitmap()

class SinPermisoException(mensaje: String) : Exception(mensaje)

fun conseguirUbicacion(
    contexto: Context,
    onPermisoUbicacionOk: (location: Location) -> Unit
) {
    try {
        val servicio = LocationServices.getFusedLocationProviderClient(contexto)
        val tarea = servicio.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
        tarea.addOnSuccessListener { location ->
            if (location != null) {
                onPermisoUbicacionOk(location)
            } else {
                Log.v("conseguirUbicacion", "Ubicación nula")

            }
        }
    } catch (e: SecurityException) {
        throw SinPermisoException(e.message ?: "No tiene permisos para conseguir la ubicación")
    }
}
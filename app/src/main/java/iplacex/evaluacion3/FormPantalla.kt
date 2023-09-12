package iplacex.evaluacion3

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FormUI() {
    val contexto = LocalContext.current
    val mainVM: MainVM = viewModel()

    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    LazyColumn {
        items(mainVM.fotosConUbicacion) { fotoConUbicacion ->
            val uri = fotoConUbicacion.foto
            val latitud = fotoConUbicacion.latitud
            val longitud = fotoConUbicacion.longitud

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(5.dp))
                Text(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    text = fotoConUbicacion.nombre
                )
                Spacer(Modifier.height(5.dp))
                Card(
                    modifier = Modifier
                        .clickable {
                            selectedImageUri.value = uri
                        }
                        .size(200.dp)
                ) {
                    Image(
                        painter = BitmapPainter(uri2imageBitmap(uri, contexto)),
                        contentDescription = "Imagen Capturada",
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(90f),
                        contentScale = ContentScale.Crop,

                        )
                }
                Button(onClick = {
                    mainVM.onPermisoUbicacionOk = {
                        mainVM.latitud.value = latitud
                        mainVM.longitud.value = longitud
                    }
                    mainVM.PantallaActual.value = Pantalla.Mapa
                }) {
                    Text("Ver Ubicación")
                }
                Spacer(Modifier.height(25.dp))
            }
        }
    }

    if (selectedImageUri.value != null) {
        Dialog(
            onDismissRequest = {
                selectedImageUri.value = null
            },
            properties = DialogProperties(
                dismissOnClickOutside = true
            )
        ) {
            selectedImageUri.value?.let { uri ->
                Image(
                    painter = BitmapPainter(uri2imageBitmap(uri, contexto)),
                    contentDescription = "Imagen en pantalla completa",
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(90f),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.End
    ) {
        FloatingActionButton(
            onClick = {
                mainVM.PantallaActual.value = Pantalla.Camara
            },
        ) {
            Text("   Tomar Foto   ")
        }
    }
}
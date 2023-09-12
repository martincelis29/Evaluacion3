package iplacex.evaluacion3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapaUI() {
    val contexto = LocalContext.current
    val mainVM: MainVM = viewModel()

    Box {
        AndroidView(
            factory = {
                MapView(it).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    Configuration.getInstance().userAgentValue =
                        contexto.packageName
                    controller.setZoom(15.0)
                }
            },
            update = {
                it.overlays.removeIf { true }
                it.invalidate()

                it.controller.setZoom(18.0)
                val geoPoint = GeoPoint(mainVM.latitud.value, mainVM.longitud.value)
                it.controller.animateTo(geoPoint)

                val marcador = Marker(it)
                marcador.position = geoPoint
                marcador.setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_CENTER
                )
                it.overlays.add(marcador)
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
                .padding(28.dp)
                .padding(bottom = 40.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            ElevatedCard(
                modifier = Modifier.height(65.dp)
            ) {
                Text(
                    text = "Latitud: ${mainVM.latitud.value}\n" +
                            "Longitud: ${mainVM.longitud.value}",
                    modifier = Modifier
                        .padding(16.dp),
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

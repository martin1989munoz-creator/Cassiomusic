package com.example.casioplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casioplayer.util.BraveLauncher

@Composable
fun CasioMainScreen(viewModel: CasioViewModel) {
    val state by viewModel.uiState.collectAsState()
    val lcdColors = getLcdColors(state.currentTheme)
    val context = LocalContext.current

    var showSearchDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CasioChassisBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CasioWatchHeader(
                onInfoClick = { showInfoDialog = true },
                onThemeToggle = { viewModel.cycleTheme() },
                onBuzzerToggle = { viewModel.toggleHaptic() },
                hapticEnabled = state.hapticEnabled
            )

            LcdDisplayPanel(
                state = state,
                colors = lcdColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            CasioPhysicalKeypad(
                onPrev = {
                    if (state.selectedIndex > 0) {
                        viewModel.selectTrack(state.selectedIndex - 1)
                    }
                },
                onPlayPause = {
                    viewModel.playCurrentTrack()
                },
                onNext = {
                    if (state.selectedIndex < state.tracks.size - 1) {
                        viewModel.selectTrack(state.selectedIndex + 1)
                    }
                },
                onVolDown = { viewModel.adjustVolume(false) },
                onVolUp = { viewModel.adjustVolume(true) },
                onSystemToggle = { viewModel.dispatchMediaPlayPause() },
                onSearchClick = { showSearchDialog = true },
                onAddClick = { showAddDialog = true },
                onFavToggle = { viewModel.toggleFavoriteCurrent() },
                onDelete = { viewModel.deleteCurrentTrack() }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CASIO F-91W DIGITAL AUDIO CONTROLLER • 50M WATER RESIST",
                color = Color.Gray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }

    if (showSearchDialog) {
        SearchQueryDialog(
            onDismiss = { showSearchDialog = false },
            onConfirm = { query ->
                viewModel.searchAndLaunch(query)
                showSearchDialog = false
            }
        )
    }

    if (showAddDialog) {
        AddTrackDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, query, isFav, playlist ->
                viewModel.addTrack(title, query, isFav, playlist)
                showAddDialog = false
            }
        )
    }

    if (showInfoDialog) {
        BraveStatusInfoDialog(
            isBraveInstalled = state.isBraveInstalled,
            onDismiss = { showInfoDialog = false },
            onInstallBrave = {
                BraveLauncher.openBraveInPlayStore(context)
                showInfoDialog = false
            }
        )
    }
}

@Composable
fun CasioWatchHeader(
    onInfoClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onBuzzerToggle: () -> Unit,
    hapticEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "CASIO",
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "F-91W CHRONO-MEDIA",
                color = Color.Cyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .size(36.dp)
                    .background(CasioButtonColor, RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Palette, contentDescription = "Cambiar Color LCD", tint = Color.LightGray)
            }
            IconButton(
                onClick = onBuzzerToggle,
                modifier = Modifier
                    .size(36.dp)
                    .background(CasioButtonColor, RoundedCornerShape(4.dp))
            ) {
                Icon(
                    if (hapticEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = "Haptic/Buzzer",
                    tint = if (hapticEnabled) Color.Green else Color.DarkGray
                )
            }
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(CasioButtonColor, RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Info, contentDescription = "Diagnóstico Brave", tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun LcdDisplayPanel(
    state: CasioUiState,
    colors: LcdColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.background)
            .border(3.dp, CasioChassisBlack, RoundedCornerShape(8.dp))
            .border(1.dp, colors.onBackground.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = state.dayOfWeek,
                        color = colors.onBackground,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = state.dayOfMonth,
                        color = colors.onBackground,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.isBraveInstalled) "[BRAVE OK]" else "[NO BRAVE]",
                        color = colors.onBackground,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BAT:${state.batteryPercent}%",
                        color = colors.onBackground,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = state.timeFormatted,
                color = colors.onBackground,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 38.sp,
                letterSpacing = 3.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.onBackground.copy(alpha = 0.08f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = state.messageLine,
                    color = colors.onBackground,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "INDEX [${state.tracks.size}] - ${state.selectedFilterPlaylist ?: "ALL TRACKS"}:",
                color = colors.onBackground,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, colors.onBackground.copy(alpha = 0.3f))
                    .padding(4.dp)
            ) {
                if (state.tracks.isEmpty()) {
                    item {
                        Text(
                            text = "- NO TRACKS SAVED -\nPRESIONE [ADD] PARA CARGAR",
                            color = colors.onBackground.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    itemsIndexed(state.tracks) { index, track ->
                        val isSelected = index == state.selectedIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) colors.onBackground.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .padding(vertical = 3.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (isSelected) ">" else " "} ${track.title}",
                                color = colors.onBackground,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (track.isFavorite) {
                                Text(
                                    text = "★",
                                    color = colors.onBackground,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CasioPhysicalKeypad(
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onVolDown: () -> Unit,
    onVolUp: () -> Unit,
    onSystemToggle: () -> Unit,
    onSearchClick: () -> Unit,
    onAddClick: () -> Unit,
    onFavToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RetroButton(
                label = "|<< PREV",
                modifier = Modifier.weight(1f),
                onClick = onPrev
            )
            RetroButton(
                label = "► PLAY/BRV",
                modifier = Modifier.weight(1.3f),
                accentColor = CasioAccentBlue,
                onClick = onPlayPause
            )
            RetroButton(
                label = "NEXT >>|",
                modifier = Modifier.weight(1f),
                onClick = onNext
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RetroButton(
                label = "VOL -",
                modifier = Modifier.weight(1f),
                onClick = onVolDown
            )
            RetroButton(
                label = "SYS ||",
                modifier = Modifier.weight(1.3f),
                onClick = onSystemToggle
            )
            RetroButton(
                label = "VOL +",
                modifier = Modifier.weight(1f),
                onClick = onVolUp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RetroButton(
                label = "SEARCH",
                modifier = Modifier.weight(1f),
                onClick = onSearchClick
            )
            RetroButton(
                label = "ADD",
                modifier = Modifier.weight(1f),
                onClick = onAddClick
            )
            RetroButton(
                label = "★ FAV",
                modifier = Modifier.weight(1f),
                onClick = onFavToggle
            )
            RetroButton(
                label = "DEL",
                modifier = Modifier.weight(0.9f),
                accentColor = CasioAccentRed,
                onClick = onDelete
            )
        }
    }
}

@Composable
fun RetroButton(
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(accentColor ?: CasioButtonColor)
            .border(2.dp, CasioButtonBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SearchQueryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("BUSCAR EN BRAVE / YOUTUBE", fontFamily = FontFamily.Monospace) },
        text = {
            Column {
                Text(
                    "Introduce el artista o tema. Se abrirá de forma nativa en Brave.",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text("Término de búsqueda o URL") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }) {
                Text("EJECUTAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR")
            }
        }
    )
}

@Composable
fun AddTrackDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, query: String, isFav: Boolean, playlist: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var playlist by remember { mutableStateOf("DEFAULT") }
    var isFav by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AGREGAR PISTA LOCAL", fontFamily = FontFamily.Monospace) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de Canción / Artista") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Búsqueda YouTube o URL") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = playlist,
                    onValueChange = { playlist = it },
                    label = { Text("Playlist (Ej: ROCK, RETRO)") },
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(checked = isFav, onCheckedChange = { isFav = it })
                    Text("Marcar como Favorito", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, query, isFav, playlist) }) {
                Text("GUARDAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR")
            }
        }
    )
}

@Composable
fun BraveStatusInfoDialog(
    isBraveInstalled: Boolean,
    onDismiss: () -> Unit,
    onInstallBrave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBraveInstalled) "BRAVE DETECTADO" else "BRAVE NO INSTALADO",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isBraveInstalled) {
                        "Estado: Brave Browser está disponible como motor web.\n\n" +
                        "PANTALLA APAGADA:\n" +
                        "Para escuchar YouTube con la pantalla apagada:\n" +
                        "1. Abre Brave.\n" +
                        "2. Ve a Ajustes > Pantalla > Reproducción en segundo plano.\n" +
                        "3. Activa la opción y reinicia el navegador."
                    } else {
                        "Estado: Brave Browser no se encuentra instalado en este dispositivo."
                    },
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        confirmButton = {
            if (!isBraveInstalled) {
                Button(onClick = onInstallBrave) {
                    Text("INSTALAR BRAVE")
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("ENTENDIDO")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR")
            }
        }
    )
}
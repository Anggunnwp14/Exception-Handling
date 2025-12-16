package com.example.ukonnect2.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ukonnect2.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ==========================================================
// === DATA CLASS & ENUM ====================================
// ==========================================================

enum class AktivitasStatus { UPCOMING, COMPLETED, MISSED }

data class JadwalItem(
    val dayShort: String,
    val dateNumber: Int,
    val title: String,
    val timeString: String,
    val startTime: Date,
    val endTime: Date,
    val iconResId: Int,
    val status: AktivitasStatus = AktivitasStatus.UPCOMING
)

// ==========================================================
// === FUNGSI PEMBANTU ======================================
// ==========================================================

fun formatDateForJadwal(timestamp: Long): Pair<String, Int> {
    val date = Date(timestamp)
    val localeId = Locale.forLanguageTag("in-ID") // ✅ pengganti yang tidak deprecated
    val dayFormat = SimpleDateFormat("EEE", localeId)
    val dayShort = dayFormat.format(date).uppercase(Locale.ROOT).replace(".", "")
    val dateFormat = SimpleDateFormat("d", Locale.ROOT)
    return Pair(dayShort, dateFormat.format(date).toIntOrNull() ?: 0)
}

// ==========================================================
// === KOMPONEN UTAMA =======================================
// ==========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AktivitasScreen() {
    val context = LocalContext.current
    val orange = Color(0xFFFFA726)
    val redWarning = Color(0xFFFF5722)
    val greenSuccess = Color(0xFF4CAF50)

    var showDialog by remember { mutableStateOf(false) }
    var selectedJadwalIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var startTimeStr by remember { mutableStateOf<String?>(null) }
    var endTimeStr by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val sampleJadwal = remember { mutableStateListOf<JadwalItem>() }

    // ✅ Loop pengecekan status otomatis
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            val now = Date()
            sampleJadwal.forEachIndexed { i, item ->
                if (item.status == AktivitasStatus.UPCOMING && now.after(item.endTime)) {
                    sampleJadwal[i] = item.copy(status = AktivitasStatus.MISSED)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aktivitas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        selectedJadwalIndex = null
                        title = ""
                        selectedDate = null
                        startTimeStr = null
                        endTimeStr = null
                        showDialog = true
                    },
                    containerColor = orange,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AktivitasListContent(
                sampleJadwal,
                selectedTabIndex,
                { selectedTabIndex = it },
                { item, idx ->
                    selectedJadwalIndex = idx
                    title = item.title
                    selectedDate = item.startTime.time
                    val times = item.timeString.replace(" WIB", "").split(" - ")
                    startTimeStr = times.getOrNull(0)?.trim()
                    endTimeStr = times.getOrNull(1)?.trim()
                    showDialog = true
                },
                { sampleJadwal.removeAt(it) },
                { sampleJadwal[it] = sampleJadwal[it].copy(status = AktivitasStatus.COMPLETED) },
                Triple(orange, greenSuccess, redWarning)
            )
        }
    }

    // ==========================================================
    // === DIALOG INPUT / EDIT JADWAL ===========================
    // ==========================================================
    if (showDialog) {
        val isEditing = selectedJadwalIndex != null
        val calendarNow = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            context,
            { _, y, m, d ->
                calendarNow.set(y, m, d)
                selectedDate = calendarNow.timeInMillis
                startTimeStr = null; endTimeStr = null
            },
            calendarNow.get(Calendar.YEAR),
            calendarNow.get(Calendar.MONTH),
            calendarNow.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

        val timePickerStart = TimePickerDialog(
            context,
            { _, h, m ->
                val now = Calendar.getInstance()
                val selectedDay = Calendar.getInstance().apply { timeInMillis = selectedDate!! }
                val isToday = now.get(Calendar.YEAR) == selectedDay.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == selectedDay.get(Calendar.DAY_OF_YEAR)

                if (isToday) {
                    val currentHour = now.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = now.get(Calendar.MINUTE)
                    if (h < currentHour || (h == currentHour && m < currentMinute)) {
                        Toast.makeText(context, "Waktu mulai tidak bisa lampau", Toast.LENGTH_LONG).show()
                        return@TimePickerDialog
                    }
                }
                startTimeStr = String.format("%02d:%02d", h, m)
                endTimeStr = null
            },
            calendarNow.get(Calendar.HOUR_OF_DAY),
            calendarNow.get(Calendar.MINUTE),
            true
        )

        val timePickerEnd = TimePickerDialog(
            context,
            { _, h, m ->
                endTimeStr = String.format("%02d:%02d", h, m)
            },
            calendarNow.get(Calendar.HOUR_OF_DAY) + 1,
            0,
            true
        )

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (isEditing) "Ubah Jadwal" else "Jadwal Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama Aktivitas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { datePicker.show() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedDate?.let { val (d, n) = formatDateForJadwal(it); "$d, $n" } ?: "Pilih Tanggal",
                            color = orange
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { timePickerStart.show() },
                            modifier = Modifier.weight(1f),
                            enabled = selectedDate != null
                        ) {
                            Text(startTimeStr ?: "Mulai", color = if (selectedDate != null) orange else Color.Gray)
                        }
                        OutlinedButton(
                            onClick = { timePickerEnd.show() },
                            modifier = Modifier.weight(1f),
                            enabled = startTimeStr != null
                        ) {
                            Text(endTimeStr ?: "Selesai", color = if (startTimeStr != null) orange else Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedDate != null && startTimeStr != null && endTimeStr != null && title.isNotBlank()) {
                            val baseCal = Calendar.getInstance().apply { timeInMillis = selectedDate!! }
                            val startParts = startTimeStr!!.split(":")
                            val startCal = (baseCal.clone() as Calendar).apply {
                                set(Calendar.HOUR_OF_DAY, startParts[0].toInt())
                                set(Calendar.MINUTE, startParts[1].toInt())
                            }
                            val endParts = endTimeStr!!.split(":")
                            val endCal = (baseCal.clone() as Calendar).apply {
                                set(Calendar.HOUR_OF_DAY, endParts[0].toInt())
                                set(Calendar.MINUTE, endParts[1].toInt())
                            }
                            if (endCal.before(startCal)) endCal.add(Calendar.DAY_OF_YEAR, 1)

                            val now = Date()
                            val status = if (now.after(endCal.time)) AktivitasStatus.MISSED else AktivitasStatus.UPCOMING
                            val (d, n) = formatDateForJadwal(selectedDate!!)
                            val newItem = JadwalItem(d, n, title, "$startTimeStr - $endTimeStr WIB", startCal.time, endCal.time, R.drawable.ic_back, status)

                            if (isEditing) {
                                val finalStatus = if (sampleJadwal[selectedJadwalIndex!!].status == AktivitasStatus.COMPLETED)
                                    AktivitasStatus.COMPLETED else status
                                sampleJadwal[selectedJadwalIndex!!] = newItem.copy(status = finalStatus)
                            } else {
                                sampleJadwal.add(newItem)
                            }
                            selectedTabIndex = if (status == AktivitasStatus.UPCOMING) 0 else 1
                            showDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && endTimeStr != null,
                    colors = ButtonDefaults.buttonColors(containerColor = orange)
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}

// ==========================================================
// === LIST KONTEN ==========================================
// ==========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AktivitasListContent(
    sampleJadwal: List<JadwalItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onEdit: (JadwalItem, Int) -> Unit,
    onDelete: (Int) -> Unit,
    onComplete: (Int) -> Unit,
    colors: Triple<Color, Color, Color>
) {
    val (orange, green, red) = colors
    var searchText by remember { mutableStateOf("") }

    val filtered by remember(sampleJadwal, selectedTabIndex, searchText) {
        derivedStateOf {
            sampleJadwal.filter {
                (if (selectedTabIndex == 0) it.status == AktivitasStatus.UPCOMING else it.status != AktivitasStatus.UPCOMING) &&
                        it.title.contains(searchText, true)
            }.sortedBy { it.startTime }
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        TabRow(
            selectedTabIndex,
            indicator = {
                TabRowDefaults.SecondaryIndicator( // ✅ pengganti non-deprecated
                    Modifier.tabIndicatorOffset(it[selectedTabIndex]),
                    color = orange
                )
            }
        ) {
            listOf("Mendatang", "Riwayat").forEachIndexed { i, t ->
                Tab(
                    selected = selectedTabIndex == i,
                    onClick = { onTabSelected(i) },
                    text = { Text(t, color = if (selectedTabIndex == i) orange else Color.Gray) }
                )
            }
        }

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Cari...") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = orange,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(filtered) { _, item ->
                AktivitasItemCard(
                    item,
                    selectedTabIndex == 1,
                    green,
                    red,
                    { onEdit(item, sampleJadwal.indexOf(item)) },
                    { onDelete(sampleJadwal.indexOf(item)) },
                    { onComplete(sampleJadwal.indexOf(item)) }
                )
            }
        }
    }
}

@Composable
fun AktivitasItemCard(
    item: JadwalItem,
    isHistory: Boolean,
    green: Color,
    red: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
                Text(item.dayShort, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text("${item.dateNumber}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.timeString, fontSize = 12.sp, color = Color.Gray)
            }

            if (isHistory) {
                val (label, color) = if (item.status == AktivitasStatus.COMPLETED)
                    "Selesai" to green else "Terlewat" to red
                Surface(color = color.copy(0.1f), shape = RoundedCornerShape(50)) {
                    Text(
                        label,
                        color = color,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(6.dp, 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Selesai") },
                            onClick = { onComplete(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = green) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { onEdit(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus") },
                            onClick = { onDelete(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = red) }
                        )
                    }
                }
            }
        }
    }
}

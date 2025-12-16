package com.example.ukonnect2.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ukonnect2.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsensiScreen(onBack: () -> Unit) {
    var riwayatList by remember {
        mutableStateOf(
            listOf(
                AbsensiItem("Latihan Fisik Bersama", "Hadir tepat waktu"),
                AbsensiItem("Latihan Rutin Futsal", "Terlambat karena macet"),
                AbsensiItem("Meeting Anggota", "Tidak hadir, izin sakit"),
                AbsensiItem("Latihan Teknik Basket", "Datang tepat waktu")
            )
        )
    }

    var itemEdit by remember { mutableStateOf<AbsensiItem?>(null) }
    var showDialogEdit by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Absensi Kehadiran",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Kembali",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFB))
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            if (riwayatList.isEmpty()) {
                Text(
                    text = "Belum ada riwayat absensi.",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color.Gray
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    riwayatList.forEach { item ->
                        RiwayatCard(
                            item = item,
                            onDelete = { riwayatList = riwayatList - item },
                            onEdit = {
                                itemEdit = item
                                showDialogEdit = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialogEdit && itemEdit != null) {
        EditRiwayatDialog(
            item = itemEdit!!,
            onDismiss = { showDialogEdit = false },
            onSave = { newItem ->
                riwayatList = riwayatList.map { if (it == itemEdit) newItem else it }
                showDialogEdit = false
            }
        )
    }
}

// 🔹 Data Model
data class AbsensiItem(
    val kegiatan: String,
    val alasan: String
)

// 🔹 Card Riwayat
@Composable
fun RiwayatCard(
    item: AbsensiItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF4F4F4),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "Kegiatan",
                    tint = Color.Black,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.kegiatan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Alasan: ${item.alasan}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1976D2))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
            }
        }
    }
}

// 🔹 Dialog Edit
@Composable
fun EditRiwayatDialog(
    item: AbsensiItem,
    onDismiss: () -> Unit,
    onSave: (AbsensiItem) -> Unit
) {
    var kegiatan by remember { mutableStateOf(item.kegiatan) }
    var alasan by remember { mutableStateOf(item.alasan) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(AbsensiItem(kegiatan, alasan)) }) {
                Text("Simpan", color = Color(0xFF1976D2))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        },
        title = { Text("Edit Absensi", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = kegiatan,
                    onValueChange = { kegiatan = it },
                    label = { Text("Kegiatan") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = alasan,
                    onValueChange = { alasan = it },
                    label = { Text("Alasan") },
                    singleLine = true
                )
            }
        }
    )
}

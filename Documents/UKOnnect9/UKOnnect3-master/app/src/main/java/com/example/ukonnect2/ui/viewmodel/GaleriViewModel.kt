package com.example.ukonnect2.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.util.*

data class FotoItem(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val keterangan: String,
    val tanggal: String,
    val hari: String
)

class GaleriViewModel : ViewModel() {

    // Menyimpan semua foto dalam galeri
    var galeriList = mutableStateListOf<FotoItem>()
        private set

    // Menyimpan foto yang baru dipilih dari galeri
    var selectedImageUri = mutableStateOf<Uri?>(null)

    // Tambah foto baru
    fun addFoto(uri: Uri, keterangan: String, tanggal: String, hari: String) {
        val foto = FotoItem(
            uri = uri,
            keterangan = keterangan,
            tanggal = tanggal,
            hari = hari
        )
        galeriList.add(foto)
    }

    // Hapus foto berdasarkan ID
    fun deleteFoto(id: String) {
        galeriList.removeAll { it.id == id }
    }

    // Update data foto (edit)
    fun updateFoto(item: FotoItem, uri: Uri, keterangan: String, tanggal: String, hari: String) {
        val index = galeriList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            galeriList[index] = item.copy(uri = uri, keterangan = keterangan, tanggal = tanggal, hari = hari)
        }
    }

    // Bersihkan foto yang dipilih setelah disimpan
    fun clearSelectedImage() {
        selectedImageUri.value = null
    }
}

package com.example.ukonnect2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Import ViewModel
import com.example.ukonnect2.ui.screen.PeminjamanViewModel

// Import Schedulers
import com.example.ukonnect2.notifications.AbsensiScheduler
import com.example.ukonnect2.notifications.AktivitasReminderScheduler
import com.example.ukonnect2.notifications.GaleriReminderScheduler
import com.example.ukonnect2.notifications.LoanReminderScheduler

// Import Components & Screens
import com.example.ukonnect2.ui.component.BottomNavBar
import com.example.ukonnect2.ui.screen.*
import com.example.ukonnect2.ui.theme.UKOnnect2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- SCHEDULERS ---
        val loanScheduler = LoanReminderScheduler(applicationContext)
        val absensiScheduler = AbsensiScheduler(applicationContext)
        val aktivitasScheduler = AktivitasReminderScheduler(applicationContext)
        val galeriScheduler = GaleriReminderScheduler(applicationContext)

        // 1. ABSENSI (Masuk 21:40, Pulang 17:00)
        absensiScheduler.scheduleDailyReminders(
            targetMasukHour = 14,
            targetMasukMinute = 19,
            targetPulangHour = 17,
            targetPulangMinute = 0
        )

        // 2. PENGINGAT GALERI (Notif 21:50)
        galeriScheduler.scheduleDailyCheck(
            deadlineHour = 21,
            deadlineMinute = 50
        )

        // 3. AKTIVITAS (Notif 23:00)
        aktivitasScheduler.scheduleDailyAktivitasCheck(
            deadlineHour = 23,
            deadlineMinute = 0
        )
        // ------------------

        setContent {
            UKOnnect2Theme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {

                    // --- LOGIN SCREEN ---
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // --- MAIN APP FLOW (Bottom Navigation) ---
                    composable("main") {
                        val innerNav = rememberNavController()

                        // ViewModel untuk Peminjaman dideklarasikan di scope 'main'
                        // agar data tetap tersimpan saat pindah tab
                        val peminjamanVM: PeminjamanViewModel = viewModel()

                        Scaffold(
                            bottomBar = {
                                BottomNavBar(
                                    currentRoute = innerNav.currentBackStackEntryAsState().value?.destination?.route,
                                    onNavigate = { route ->
                                        innerNav.navigate(route) {
                                            popUpTo(innerNav.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onAbsenClick = {
                                        navController.navigate("qr_scanner")
                                    }
                                )
                            }
                        ) { innerPadding ->
                            NavHost(
                                navController = innerNav,
                                startDestination = "beranda",
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                // 1. BERANDA
                                composable("beranda") {
                                    MainScreen(
                                        peminjamanVM = peminjamanVM,
                                        onGoGaleri = { innerNav.navigate("galeri") },
                                        onGoAbsensi = { innerNav.navigate("absensi") },
                                        onGoProfil = { innerNav.navigate("profil") },
                                        onGoAktivitas = { innerNav.navigate("aktivitas") },
                                        onGoBeranda = { innerNav.navigate("beranda") } // Biasanya tidak perlu navigate ke diri sendiri, tapi oke
                                    )
                                }

                                // 2. AKTIVITAS
                                composable("aktivitas") { AktivitasScreen() }

                                // 3. GALERI (UPDATE: Tambahkan onBack)
                                composable("galeri") {
                                    GaleriScreen(
                                        onBack = {
                                            // Aksi tombol back: kembali ke layar sebelumnya
                                            innerNav.popBackStack()
                                        }
                                    )
                                }

                                // 4. PEMINJAMAN
                                composable("pinjam") {
                                    PinjamScreen(viewModel = peminjamanVM)
                                }

                                // 5. PROFIL
                                composable("profil") { ProfilScreen() }

                                // 6. ABSENSI LIST
                                composable("absensi") {
                                    AbsensiScreen(
                                        onBack = { innerNav.popBackStack() }
                                    )
                                }
                            }
                        }
                    }

                    // --- QR SCANNER (Fullscreen, di luar BottomNav) ---
                    composable("qr_scanner") {
                        QrScannerScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onQrCodeScanned = { qrValue ->
                                Toast.makeText(
                                    applicationContext,
                                    "Hasil Scan: $qrValue",
                                    Toast.LENGTH_LONG
                                ).show()

                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
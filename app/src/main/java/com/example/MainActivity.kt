package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CalculationSession
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.BpsBlue
import com.example.ui.theme.BpsDarkBlue
import com.example.ui.theme.BpsDanger
import com.example.ui.theme.BpsDangerLight
import com.example.ui.theme.BpsLightBlue
import com.example.ui.theme.BpsMediumBlue
import com.example.ui.theme.BpsPaleBlue
import com.example.ui.theme.BpsSuccess
import com.example.ui.theme.BpsSuccessLight
import com.example.ui.theme.BpsWarning
import com.example.ui.theme.BpsWarningLight
import com.example.ui.theme.MyApplicationTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Top-level currency formatter
fun formatRupiah(amount: Long): String {
    val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp " + format.format(amount)
}

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = !isDarkTheme },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: CalculatorViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session by viewModel.currentSession.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf(0) } // 0: Tenaga, 1: Keuangan, 2: Aset & Modal, 3: Ringkasan

    // State for Dialogs
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ============================================================
        // HEADER
        // ============================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(BpsDarkBlue, BpsMediumBlue)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧮", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kalkulator SE2026-L",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Alat Bantu Perhitungan · BPS",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { onThemeToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark Mode",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // TABS / NAVIGATION
        // ============================================================
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabData = listOf(
                    Triple(0, "👥", "Tenaga"),
                    Triple(1, "💰", "Keuangan"),
                    Triple(2, "🏢", "Aset"),
                    Triple(3, "📋", "Ringkasan")
                )

                tabData.forEach { (index, emoji, text) ->
                    val isSelected = activeTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) BpsBlue else Color.Transparent
                            )
                            .clickable { activeTab = index }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // CONTENT SECTIONS
        // ============================================================
        Box(modifier = Modifier.fillMaxWidth()) {
            when (activeTab) {
                0 -> TenagaKerjaScreen(viewModel = viewModel, session = session)
                1 -> KeuanganScreen(viewModel = viewModel, session = session)
                2 -> AsetModalScreen(viewModel = viewModel, session = session)
                3 -> RingkasanScreen(
                    viewModel = viewModel,
                    session = session,
                    onSaveClick = { showSaveDialog = true },
                    onLoadClick = { showLoadDialog = true },
                    onResetClick = { showResetDialog = true }
                )
            }
        }
    }

    // ============================================================
    // DIALOGS & SHEET IMPLEMENTATION
    // ============================================================
    val context = LocalContext.current

    if (showSaveDialog) {
        var tempTitle by remember { mutableStateOf(session.title.ifBlank { "Sesi " + SimpleDateFormat("dd-MM HH:mm", Locale.getDefault()).format(Date()) }) }

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Simpan Perhitungan") },
            text = {
                Column {
                    Text("Masukkan nama/judul perhitungan sensus untuk disimpan ke database lokal.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        label = { Text("Nama Responden / Sesi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempTitle.isNotBlank()) {
                            viewModel.saveSession(tempTitle) {
                                showSaveDialog = false
                                Toast.makeText(context, "✅ Data perhitungan disimpan.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BpsSuccess)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showLoadDialog) {
        val savedList by viewModel.savedSessions.collectAsStateWithLifecycle()

        AlertDialog(
            onDismissRequest = { showLoadDialog = false },
            title = { Text("Muat Sensus Terpilih") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    if (savedList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tidak ada data perhitungan tersimpan.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(savedList) { item ->
                                val dateFormatted = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.loadSession(item)
                                            showLoadDialog = false
                                            Toast.makeText(context, "📂 Data berhasil dimuat.", Toast.LENGTH_SHORT).show()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                            Text(dateFormatted, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            // Brief summary
                                            val summaryPendapatan = when (item.financeMode) {
                                                "harian" -> (item.pendBarangHarian + item.pendLainnyaHarian) * item.hariBukaHarian * item.bulanAktifHarian
                                                "bulanan" -> (item.pendBarangBulanan + item.pendLainnyaBulanan) * item.bulanAktifBulanan
                                                else -> item.pendBarangTahunan + item.pendLainnyaTahunan
                                            }
                                            Text(
                                                "Tenaga: ${item.pekerjaL + item.pekerjaP} | Pend: ${formatRupiah(summaryPendapatan)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BpsBlue
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteSession(item.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus data",
                                                tint = BpsDanger
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLoadDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Perhitungan") },
            text = { Text("Apakah Anda yakin ingin me-reset semua input data perhitungan sensus menjadi nol?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSession()
                        showResetDialog = false
                        Toast.makeText(context, "🔄 Perhitungan berhasil direset.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BpsDanger)
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ============================================================
// SECTION 1: TENAGA KERJA
// ============================================================
@Composable
fun TenagaKerjaScreen(
    viewModel: CalculatorViewModel,
    session: CalculationSession
) {
    val totalLP by viewModel.totalLP.collectAsStateWithLifecycle()
    val totalDT by viewModel.totalDT.collectAsStateWithLifecycle()
    val isConsistent by viewModel.isTenagaKerjaConsistent.collectAsStateWithLifecycle()

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👥", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kalkulator Tenaga Kerja",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BpsDarkBlue
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            NumberTextField(
                value = session.pekerjaL,
                onValueChange = { viewModel.updatePekerjaL(it) },
                label = "Pekerja Laki-laki"
            )
            Spacer(modifier = Modifier.height(12.dp))

            NumberTextField(
                value = session.pekerjaP,
                onValueChange = { viewModel.updatePekerjaP(it) },
                label = "Pekerja Perempuan"
            )
            Spacer(modifier = Modifier.height(12.dp))

            NumberTextField(
                value = session.dibayar,
                onValueChange = { viewModel.updateDibayar(it) },
                label = "Pekerja Dibayar"
            )
            Spacer(modifier = Modifier.height(12.dp))

            NumberTextField(
                value = session.tdkDibayar,
                onValueChange = { viewModel.updateTdkDibayar(it) },
                label = "Pekerja Tidak Dibayar"
            )
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total L+P", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("$totalLP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BpsBlue)
                }
                Column {
                    Text("Total D+T", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("$totalDT", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BpsBlue)
                }
            }

            if (!isConsistent) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = BpsDangerLight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("❌", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Jumlah tenaga kerja tidak konsisten.",
                            color = BpsDanger,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// SECTION 2: KEUANGAN
// ============================================================
@Composable
fun KeuanganScreen(
    viewModel: CalculatorViewModel,
    session: CalculationSession
) {
    val totalPendapatan by viewModel.totalPendapatanTahunan.collectAsStateWithLifecycle()
    val totalPengeluaran by viewModel.totalPengeluaranTahunan.collectAsStateWithLifecycle()
    val labaRugi by viewModel.labaRugi.collectAsStateWithLifecycle()
    val margin by viewModel.margin.collectAsStateWithLifecycle()
    val pendOnline by viewModel.pendOnline.collectAsStateWithLifecycle()
    val pendOffline by viewModel.pendOffline.collectAsStateWithLifecycle()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mode Selection Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💱", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Metode Input Keuangan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BpsDarkBlue
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("harian" to "Harian", "bulanan" to "Bulanan", "tahunan" to "Tahunan").forEach { (valStr, displayStr) ->
                        val isSelected = session.financeMode == valStr
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BpsPaleBlue.copy(alpha = 0.4f) else Color.Transparent)
                                .clickable { viewModel.updateFinanceMode(valStr) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateFinanceMode(valStr) },
                                colors = RadioButtonDefaults.colors(selectedColor = BpsBlue)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = displayStr,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) BpsBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Pendapatan Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📈", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pendapatan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BpsDarkBlue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                when (session.financeMode) {
                    "harian" -> {
                        RupiahTextField(
                            value = session.pendBarangHarian,
                            onValueChange = { viewModel.updatePendBarangHarian(it) },
                            label = "Pendapatan Barang/Jasa per Hari"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.pendLainnyaHarian,
                            onValueChange = { viewModel.updatePendLainnyaHarian(it) },
                            label = "Pendapatan Lainnya per Hari"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NumberTextField(
                            value = session.hariBukaHarian,
                            onValueChange = { viewModel.updateHariBukaHarian(it) },
                            label = "Hari Buka per Bulan"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NumberTextField(
                            value = session.bulanAktifHarian,
                            onValueChange = { viewModel.updateBulanAktifHarian(it) },
                            label = "Jumlah Bulan Aktif"
                        )
                    }
                    "bulanan" -> {
                        RupiahTextField(
                            value = session.pendBarangBulanan,
                            onValueChange = { viewModel.updatePendBarangBulanan(it) },
                            label = "Pendapatan Barang/Jasa per Bulan"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.pendLainnyaBulanan,
                            onValueChange = { viewModel.updatePendLainnyaBulanan(it) },
                            label = "Pendapatan Lainnya per Bulan"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NumberTextField(
                            value = session.bulanAktifBulanan,
                            onValueChange = { viewModel.updateBulanAktifBulanan(it) },
                            label = "Jumlah Bulan Aktif"
                        )
                    }
                    else -> {
                        RupiahTextField(
                            value = session.pendBarangTahunan,
                            onValueChange = { viewModel.updatePendBarangTahunan(it) },
                            label = "Pendapatan Barang/Jasa Tahunan"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.pendLainnyaTahunan,
                            onValueChange = { viewModel.updatePendLainnyaTahunan(it) },
                            label = "Pendapatan Lainnya Tahunan"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Total Pendapatan Tahunan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = formatRupiah(totalPendapatan),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BpsBlue
                )
            }
        }

        // Pengeluaran Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📉", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pengeluaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BpsDarkBlue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                when (session.financeMode) {
                    "harian" -> {
                        RupiahTextField(
                            value = session.upahHarian,
                            onValueChange = { viewModel.updateUpahHarian(it) },
                            label = "Upah/Gaji (Harian)"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.produksiHarian,
                            onValueChange = { viewModel.updateProduksiHarian(it) },
                            label = "Biaya Produksi (Harian)"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.daganganHarian,
                            onValueChange = { viewModel.updateDaganganHarian(it) },
                            label = "Pembelian Barang Dagangan (Harian)"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.operasionalHarian,
                            onValueChange = { viewModel.updateOperasionalHarian(it) },
                            label = "Operasional (Harian)"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.nonOpHarian,
                            onValueChange = { viewModel.updateNonOpHarian(it) },
                            label = "Non-operasional (Harian)"
                        )
                    }
                    "bulanan" -> {
                        RupiahTextField(
                            value = session.upahBulanan,
                            onValueChange = { viewModel.updateUpahBulanan(it) },
                            label = "Upah/Gaji (Bulanan)"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.produksiBulanan,
                            onValueChange = { viewModel.updateProduksiBulanan(it) },
                            label = "Biaya Produksi (Bulanan)"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.daganganBulanan,
                            onValueChange = { viewModel.updateDaganganBulanan(it) },
                            label = "Pembelian Barang Dagangan (Bulanan)"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.operasionalBulanan,
                            onValueChange = { viewModel.updateOperasionalBulanan(it) },
                            label = "Operasional (Bulanan)"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.nonOpBulanan,
                            onValueChange = { viewModel.updateNonOpBulanan(it) },
                            label = "Non-operasional (Bulanan)"
                        )
                    }
                    else -> {
                        RupiahTextField(
                            value = session.upahTahunan,
                            onValueChange = { viewModel.updateUpahTahunan(it) },
                            label = "Upah/Gaji Tahunan"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.produksiTahunan,
                            onValueChange = { viewModel.updateProduksiTahunan(it) },
                            label = "Biaya Produksi Tahunan"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.daganganTahunan,
                            onValueChange = { viewModel.updateDaganganTahunan(it) },
                            label = "Pembelian Barang Dagangan Tahunan"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.operasionalTahunan,
                            onValueChange = { viewModel.updateOperasionalTahunan(it) },
                            label = "Operasional Tahunan"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RupiahTextField(
                            value = session.nonOpTahunan,
                            onValueChange = { viewModel.updateNonOpTahunan(it) },
                            label = "Non-operasional Tahunan"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Total Pengeluaran Tahunan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = formatRupiah(totalPengeluaran),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BpsBlue
                )
            }
        }

        // Laba Rugi Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Laba/Rugi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BpsDarkBlue
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Laba/Rugi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            text = formatRupiah(labaRugi),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (labaRugi >= 0) BpsSuccess else BpsDanger
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Margin (%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%%", margin),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BpsBlue
                        )
                    }
                }

                if (labaRugi < 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BpsWarningLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Usaha mengalami kerugian.",
                                color = BpsWarning,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Ekonomi Digital Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌐", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ekonomi Digital",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BpsDarkBlue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                NumberTextField(
                    value = session.persenOnline,
                    onValueChange = { viewModel.updatePersenOnline(it.coerceIn(0, 100)) },
                    label = "Persentase Pendapatan Online (%)",
                    maxDigits = 3
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pendapatan Online", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(formatRupiah(pendOnline), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BpsBlue)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Pendapatan Offline", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(formatRupiah(pendOffline), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BpsBlue)
                    }
                }
            }
        }
    }
}

// ============================================================
// SECTION 3: ASET & MODAL
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsetModalScreen(
    viewModel: CalculatorViewModel,
    session: CalculationSession
) {
    val totalAset by viewModel.totalAset.collectAsStateWithLifecycle()
    val totalModalPercent by viewModel.totalModalPercent.collectAsStateWithLifecycle()
    val isModalConsistent by viewModel.isModalConsistent.collectAsStateWithLifecycle()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Aset Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏢", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aset Usaha",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BpsDarkBlue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.updateTidakTahuAset(!session.tidakTahuAset) }
                        .background(if (session.tidakTahuAset) BpsWarningLight.copy(alpha = 0.3f) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Checkbox(
                        checked = session.tidakTahuAset,
                        onCheckedChange = { viewModel.updateTidakTahuAset(it) },
                        colors = CheckboxDefaults.colors(checkedColor = BpsWarning)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Responden tidak tahu nilai aset",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (session.tidakTahuAset) BpsWarning else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (session.tidakTahuAset) {
                    // Show Range selection dropdown
                    val options = listOf(
                        "≤ Rp500 juta",
                        "Rp500 juta–Rp2 milar",
                        "Rp2 miliar–Rp10 miliar",
                        "Rp10 milar–Rp50 milar",
                        "> Rp50 milar"
                    )
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = session.rangeAset,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Range Aset") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        viewModel.updateRangeAset(opt)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Show full asset fields
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RupiahTextField(value = session.asetTanah, onValueChange = { viewModel.updateAsetTanah(it) }, label = "Tanah")
                        RupiahTextField(value = session.asetBangunan, onValueChange = { viewModel.updateAsetBangunan(it) }, label = "Bangunan")
                        RupiahTextField(value = session.asetMesin, onValueChange = { viewModel.updateAsetMesin(it) }, label = "Mesin")
                        RupiahTextField(value = session.asetKendaraan, onValueChange = { viewModel.updateAsetKendaraan(it) }, label = "Kendaraan")
                        RupiahTextField(value = session.asetPeralatan, onValueChange = { viewModel.updateAsetPeralatan(it) }, label = "Peralatan")
                        RupiahTextField(value = session.asetKas, onValueChange = { viewModel.updateAsetKas(it) }, label = "Kas")
                        RupiahTextField(value = session.asetPiutang, onValueChange = { viewModel.updateAsetPiutang(it) }, label = "Piutang")
                        RupiahTextField(value = session.asetPersediaan, onValueChange = { viewModel.updateAsetPersediaan(it) }, label = "Persediaan")
                        RupiahTextField(value = session.asetLainnya, onValueChange = { viewModel.updateAsetLainnya(it) }, label = "Aset Lainnya")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Total Aset Usaha", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(formatRupiah(totalAset), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = BpsBlue)
                }
            }
        }

        // Modal Composition Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Komposisi Modal (%)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BpsDarkBlue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberTextField(value = session.modPribadi, onValueChange = { viewModel.updateModPribadi(it.coerceIn(0, 100)) }, label = "Pribadi", maxDigits = 3)
                    NumberTextField(value = session.modNonProfit, onValueChange = { viewModel.updateModNonProfit(it.coerceIn(0, 100)) }, label = "Lembaga Non-Profit", maxDigits = 3)
                    NumberTextField(value = session.modKorporasi, onValueChange = { viewModel.updateModKorporasi(it.coerceIn(0, 100)) }, label = "Korporasi", maxDigits = 3)
                    NumberTextField(value = session.modPemerintah, onValueChange = { viewModel.updateModPemerintah(it.coerceIn(0, 100)) }, label = "Pemerintah", maxDigits = 3)
                    NumberTextField(value = session.modAsing, onValueChange = { viewModel.updateModAsing(it.coerceIn(0, 100)) }, label = "Asing", maxDigits = 3)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Komposisi Modal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("$totalModalPercent%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BpsBlue)
                    }
                }

                if (!isModalConsistent) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BpsDangerLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("❌", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Total komposisi modal harus tepat 100%.",
                                color = BpsDanger,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// SECTION 4: RINGKASAN & ACTIONS
// ============================================================
@Composable
fun RingkasanScreen(
    viewModel: CalculatorViewModel,
    session: CalculationSession,
    onSaveClick: () -> Unit,
    onLoadClick: () -> Unit,
    onResetClick: () -> Unit
) {
    val totalLP by viewModel.totalLP.collectAsStateWithLifecycle()
    val totalPendapatan by viewModel.totalPendapatanTahunan.collectAsStateWithLifecycle()
    val totalPengeluaran by viewModel.totalPengeluaranTahunan.collectAsStateWithLifecycle()
    val labaRugi by viewModel.labaRugi.collectAsStateWithLifecycle()
    val margin by viewModel.margin.collectAsStateWithLifecycle()
    val pendOnline by viewModel.pendOnline.collectAsStateWithLifecycle()
    val pendOffline by viewModel.pendOffline.collectAsStateWithLifecycle()
    val totalAset by viewModel.totalAset.collectAsStateWithLifecycle()
    val totalModalPercent by viewModel.totalModalPercent.collectAsStateWithLifecycle()

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📋", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ringkasan Sensus Ekonomi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BpsDarkBlue
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RingkasanRow(label = "Nama / Sesi Sensus", value = session.title.ifBlank { "-" })
                RingkasanRow(label = "Total Tenaga Kerja (L+P)", value = "$totalLP Orang")
                RingkasanRow(label = "Total Pendapatan Tahunan", value = formatRupiah(totalPendapatan))
                RingkasanRow(label = "Total Pengeluaran Tahunan", value = formatRupiah(totalPengeluaran))
                RingkasanRow(
                    label = "Estimasi Laba/Rugi",
                    value = formatRupiah(labaRugi),
                    valueColor = if (labaRugi >= 0) BpsSuccess else BpsDanger,
                    isBold = true
                )
                RingkasanRow(label = "Margin Keuntungan (%)", value = String.format(Locale.getDefault(), "%.1f%%", margin))
                RingkasanRow(label = "Pendapatan Online", value = formatRupiah(pendOnline))
                RingkasanRow(label = "Pendapatan Offline", value = formatRupiah(pendOffline))
                
                if (session.tidakTahuAset) {
                    RingkasanRow(label = "Range Nilai Aset", value = session.rangeAset.ifBlank { "-" })
                } else {
                    RingkasanRow(label = "Total Nilai Aset", value = formatRupiah(totalAset))
                }
                
                RingkasanRow(label = "Total Komposisi Modal", value = "$totalModalPercent%")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BpsSuccess),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onLoadClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BpsWarning),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Load", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Muat", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onResetClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BpsDanger),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RingkasanRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
    HorizontalDivider(color = Color.Gray.copy(alpha = 0.08f))
}

// ============================================================
// REUSABLE UI FIELDS
// ============================================================
@Composable
fun RupiahTextField(
    value: Long,
    onValueChange: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) {
        mutableStateOf(if (value == 0L) "" else NumberFormat.getNumberInstance(Locale("id", "ID")).format(value))
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { it.isDigit() }
            val parsed = digitsOnly.toLongOrNull() ?: 0L
            onValueChange(parsed)
            textValue = if (parsed == 0L) "" else NumberFormat.getNumberInstance(Locale("id", "ID")).format(parsed)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        leadingIcon = {
            Text(
                "Rp",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BpsBlue,
                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun NumberTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    maxDigits: Int = 10
) {
    var textValue by remember(value) {
        mutableStateOf(if (value == 0) "" else value.toString())
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { it.isDigit() }.take(maxDigits)
            val parsed = digitsOnly.toIntOrNull() ?: 0
            onValueChange(parsed)
            textValue = if (parsed == 0) "" else parsed.toString()
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}


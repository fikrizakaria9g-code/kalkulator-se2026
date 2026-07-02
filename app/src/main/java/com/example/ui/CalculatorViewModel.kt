package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CalculationSession
import com.example.data.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SessionRepository
    val savedSessions: StateFlow<List<CalculationSession>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SessionRepository(database.sessionDao())
        savedSessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private val _currentSession = MutableStateFlow(CalculationSession(title = "Sesi Baru"))
    val currentSession: StateFlow<CalculationSession> = _currentSession.asStateFlow()

    // Calculated derived values
    val totalLP: StateFlow<Int> = _currentSession.map { session ->
        session.pekerjaL + session.pekerjaP
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    val totalDT: StateFlow<Int> = _currentSession.map { session ->
        session.dibayar + session.tdkDibayar
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    val isTenagaKerjaConsistent: StateFlow<Boolean> = _currentSession.map { session ->
        (session.pekerjaL + session.pekerjaP) == (session.dibayar + session.tdkDibayar)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val totalPendapatanTahunan: StateFlow<Long> = _currentSession.map { s ->
        when (s.financeMode) {
            "harian" -> (s.pendBarangHarian + s.pendLainnyaHarian) * s.hariBukaHarian * s.bulanAktifHarian
            "bulanan" -> (s.pendBarangBulanan + s.pendLainnyaBulanan) * s.bulanAktifBulanan
            else -> s.pendBarangTahunan + s.pendLainnyaTahunan
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 204000000L)

    val totalPengeluaranTahunan: StateFlow<Long> = _currentSession.map { s ->
        val totalBase = s.upahHarian + s.produksiHarian + s.daganganHarian + s.operasionalHarian + s.nonOpHarian
        val totalBaseBulanan = s.upahBulanan + s.produksiBulanan + s.daganganBulanan + s.operasionalBulanan + s.nonOpBulanan
        val totalBaseTahunan = s.upahTahunan + s.produksiTahunan + s.daganganTahunan + s.operasionalTahunan + s.nonOpTahunan

        when (s.financeMode) {
            "harian" -> totalBase * s.hariBukaHarian * s.bulanAktifHarian
            "bulanan" -> totalBaseBulanan * s.bulanAktifBulanan
            else -> totalBaseTahunan
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 204000000L)

    val labaRugi: StateFlow<Long> = combine(totalPendapatanTahunan, totalPengeluaranTahunan) { pend, peng ->
        pend - peng
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val margin: StateFlow<Double> = combine(labaRugi, totalPendapatanTahunan) { laba, pend ->
        if (pend != 0L) (laba.toDouble() / pend * 100) else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pendOnline: StateFlow<Long> = combine(totalPendapatanTahunan, _currentSession) { pend, s ->
        (pend * s.persenOnline.coerceIn(0, 100)) / 100
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val pendOffline: StateFlow<Long> = combine(totalPendapatanTahunan, _currentSession) { pend, s ->
        (pend * (100 - s.persenOnline.coerceIn(0, 100))) / 100
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalAset: StateFlow<Long> = _currentSession.map { s ->
        if (s.tidakTahuAset) {
            0L
        } else {
            s.asetTanah + s.asetBangunan + s.asetMesin + s.asetKendaraan + s.asetPeralatan +
                    s.asetKas + s.asetPiutang + s.asetPersediaan + s.asetLainnya
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalModalPercent: StateFlow<Int> = _currentSession.map { s ->
        s.modPribadi + s.modNonProfit + s.modKorporasi + s.modPemerintah + s.modAsing
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    val isModalConsistent: StateFlow<Boolean> = totalModalPercent.map { total ->
        total == 100
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)


    // Individual setters (maintaining data structure integrity)
    fun updatePekerjaL(v: Int) {
        _currentSession.value = _currentSession.value.copy(pekerjaL = v)
    }

    fun updatePekerjaP(v: Int) {
        _currentSession.value = _currentSession.value.copy(pekerjaP = v)
    }

    fun updateDibayar(v: Int) {
        _currentSession.value = _currentSession.value.copy(dibayar = v)
    }

    fun updateTdkDibayar(v: Int) {
        _currentSession.value = _currentSession.value.copy(tdkDibayar = v)
    }

    fun updateFinanceMode(v: String) {
        _currentSession.value = _currentSession.value.copy(financeMode = v)
    }

    fun updatePersenOnline(v: Int) {
        _currentSession.value = _currentSession.value.copy(persenOnline = v)
    }

    // Pendapatan setters
    fun updatePendBarangHarian(v: Long) {
        _currentSession.value = _currentSession.value.copy(pendBarangHarian = v)
    }

    fun updatePendLainnyaHarian(v: Long) {
        _currentSession.value = _currentSession.value.copy(pendLainnyaHarian = v)
    }

    fun updateHariBukaHarian(v: Int) {
        _currentSession.value = _currentSession.value.copy(hariBukaHarian = v)
    }

    fun updateBulanAktifHarian(v: Int) {
        _currentSession.value = _currentSession.value.copy(bulanAktifHarian = v)
    }

    fun updatePendBarangBulanan(v: Long) {
        _currentSession.value = _currentSession.value.copy(pendBarangBulanan = v)
    }

    fun updatePendLainnyaBulanan(v: Long) {
        _currentSession.value = _currentSession.value.copy(pendLainnyaBulanan = v)
    }

    fun updateBulanAktifBulanan(v: Int) {
        _currentSession.value = _currentSession.value.copy(bulanAktifBulanan = v)
    }

    fun updatePendBarangTahunan(v: Long) {
        _currentSession.value = _currentSession.value.copy(pendBarangTahunan = v)
    }

    fun updatePendLainnyaTahunan(v: Long) {
        _currentSession.value = _currentSession.value.copy(pendLainnyaTahunan = v)
    }

    // Pengeluaran setters
    fun updateUpahHarian(v: Long) {
        _currentSession.value = _currentSession.value.copy(upahHarian = v)
    }

    fun updateProduksiHarian(v: Long) {
        _currentSession.value = _currentSession.value.copy(produksiHarian = v)
    }

    fun updateDaganganHarian(v: Long) {
        _currentSession.value = _currentSession.value.copy(daganganHarian = v)
    }

    fun updateOperasionalHarian(v: Long) {
        _currentSession.value = _currentSession.value.copy(operasionalHarian = v)
    }

    fun updateNonOpHarian(v: Long) {
        _currentSession.value = _currentSession.value.copy(nonOpHarian = v)
    }

    fun updateUpahBulanan(v: Long) {
        _currentSession.value = _currentSession.value.copy(upahBulanan = v)
    }

    fun updateProduksiBulanan(v: Long) {
        _currentSession.value = _currentSession.value.copy(produksiBulanan = v)
    }

    fun updateDaganganBulanan(v: Long) {
        _currentSession.value = _currentSession.value.copy(daganganBulanan = v)
    }

    fun updateOperasionalBulanan(v: Long) {
        _currentSession.value = _currentSession.value.copy(operasionalBulanan = v)
    }

    fun updateNonOpBulanan(v: Long) {
        _currentSession.value = _currentSession.value.copy(nonOpBulanan = v)
    }

    fun updateUpahTahunan(v: Long) {
        _currentSession.value = _currentSession.value.copy(upahTahunan = v)
    }

    fun updateProduksiTahunan(v: Long) {
        _currentSession.value = _currentSession.value.copy(produksiTahunan = v)
    }

    fun updateDaganganTahunan(v: Long) {
        _currentSession.value = _currentSession.value.copy(daganganTahunan = v)
    }

    fun updateOperasionalTahunan(v: Long) {
        _currentSession.value = _currentSession.value.copy(operasionalTahunan = v)
    }

    fun updateNonOpTahunan(v: Long) {
        _currentSession.value = _currentSession.value.copy(nonOpTahunan = v)
    }

    // Aset setters
    fun updateAsetTanah(v: Long) {
        _currentSession.value = _currentSession.value.copy(asetTanah = v)
    }

    fun updateAsetBangunan(v: Long) {
        _currentSession.value = _currentSession.value.copy(asetBangunan = v)
    }

    fun updateAsetMesin(v: Long) {
        _currentSession.value = _currentSession.value.copy(asetMesin = v)
    }

    fun updateAsetKendaraan(v: Long) {
        _currentSession.value = _currentSession.value.copy(asetKendaraan = v)
    }

    fun updateAsetPeralatan(v: Long) {
        _currentSession.value = _currentSession.value.copy(asetPeralatan = v)
    }

    fun updateAsetKas(v: Long) {
        _currentSession.value = _currentSession.value.copy(asetKas = v)
    }

    fun updateAsetPiutang(v: Long) {
        _currentSession.value = _currentSession.value.copy(asetPiutang = v)
    }

    fun updateAsetPersediaan(v: Long) {
        _currentSession.value = _currentSession.value.copy(asetPersediaan = v)
    }

    fun updateAsetLainnya(v: Long) {
        _currentSession.value = _currentSession.value.copy(asetLainnya = v)
    }

    fun updateTidakTahuAset(v: Boolean) {
        _currentSession.value = _currentSession.value.copy(tidakTahuAset = v)
    }

    fun updateRangeAset(v: String) {
        _currentSession.value = _currentSession.value.copy(rangeAset = v)
    }

    // Modal setters
    fun updateModPribadi(v: Int) {
        _currentSession.value = _currentSession.value.copy(modPribadi = v)
    }

    fun updateModNonProfit(v: Int) {
        _currentSession.value = _currentSession.value.copy(modNonProfit = v)
    }

    fun updateModKorporasi(v: Int) {
        _currentSession.value = _currentSession.value.copy(modKorporasi = v)
    }

    fun updateModPemerintah(v: Int) {
        _currentSession.value = _currentSession.value.copy(modPemerintah = v)
    }

    fun updateModAsing(v: Int) {
        _currentSession.value = _currentSession.value.copy(modAsing = v)
    }

    // Database Actions
    fun saveSession(title: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val toSave = _currentSession.value.copy(
                title = title,
                timestamp = System.currentTimeMillis()
            )
            repository.insert(toSave)
            onComplete()
        }
    }

    fun loadSession(session: CalculationSession) {
        _currentSession.value = session
    }

    fun deleteSession(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun resetSession() {
        _currentSession.value = CalculationSession(
            title = "Sesi Baru",
            pekerjaL = 0,
            pekerjaP = 0,
            dibayar = 0,
            tdkDibayar = 0,
            financeMode = "bulanan",
            persenOnline = 0,
            pendBarangHarian = 0L,
            pendLainnyaHarian = 0L,
            hariBukaHarian = 26,
            bulanAktifHarian = 12,
            pendBarangBulanan = 0L,
            pendLainnyaBulanan = 0L,
            bulanAktifBulanan = 12,
            pendBarangTahunan = 0L,
            pendLainnyaTahunan = 0L,
            upahHarian = 0L,
            produksiHarian = 0L,
            daganganHarian = 0L,
            operasionalHarian = 0L,
            nonOpHarian = 0L,
            upahBulanan = 0L,
            produksiBulanan = 0L,
            daganganBulanan = 0L,
            operasionalBulanan = 0L,
            nonOpBulanan = 0L,
            upahTahunan = 0L,
            produksiTahunan = 0L,
            daganganTahunan = 0L,
            operasionalTahunan = 0L,
            nonOpTahunan = 0L,
            asetTanah = 0L,
            asetBangunan = 0L,
            asetMesin = 0L,
            asetKendaraan = 0L,
            asetPeralatan = 0L,
            asetKas = 0L,
            asetPiutang = 0L,
            asetPersediaan = 0L,
            asetLainnya = 0L,
            tidakTahuAset = false,
            rangeAset = "",
            modPribadi = 0,
            modNonProfit = 0,
            modKorporasi = 0,
            modPemerintah = 0,
            modAsing = 0
        )
    }
}

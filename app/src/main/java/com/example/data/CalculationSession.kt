package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_sessions")
data class CalculationSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Tenaga Kerja
    val pekerjaL: Int = 5,
    val pekerjaP: Int = 3,
    val dibayar: Int = 8,
    val tdkDibayar: Int = 0,
    
    // Keuangan Mode
    val financeMode: String = "bulanan", // "harian", "bulanan", "tahunan"
    val persenOnline: Int = 30,
    
    // Pendapatan State
    val pendBarangHarian: Long = 0,
    val pendLainnyaHarian: Long = 0,
    val hariBukaHarian: Int = 26,
    val bulanAktifHarian: Int = 12,
    
    val pendBarangBulanan: Long = 15000000,
    val pendLainnyaBulanan: Long = 2000000,
    val bulanAktifBulanan: Int = 12,
    
    val pendBarangTahunan: Long = 204000000,
    val pendLainnyaTahunan: Long = 24000000,
    
    // Pengeluaran State
    val upahHarian: Long = 0,
    val produksiHarian: Long = 0,
    val daganganHarian: Long = 0,
    val operasionalHarian: Long = 0,
    val nonOpHarian: Long = 0,
    
    val upahBulanan: Long = 8000000,
    val produksiBulanan: Long = 4000000,
    val daganganBulanan: Long = 3000000,
    val operasionalBulanan: Long = 1500000,
    val nonOpBulanan: Long = 500000,
    
    val upahTahunan: Long = 96000000,
    val produksiTahunan: Long = 48000000,
    val daganganTahunan: Long = 36000000,
    val operasionalTahunan: Long = 18000000,
    val nonOpTahunan: Long = 6000000,
    
    // Aset
    val asetTanah: Long = 50000000,
    val asetBangunan: Long = 150000000,
    val asetMesin: Long = 80000000,
    val asetKendaraan: Long = 70000000,
    val asetPeralatan: Long = 25000000,
    val asetKas: Long = 15000000,
    val asetPiutang: Long = 5000000,
    val asetPersediaan: Long = 20000000,
    val asetLainnya: Long = 10000000,
    val tidakTahuAset: Boolean = false,
    val rangeAset: String = "",
    
    // Modal
    val modPribadi: Int = 70,
    val modNonProfit: Int = 0,
    val modKorporasi: Int = 20,
    val modPemerintah: Int = 5,
    val modAsing: Int = 5
)

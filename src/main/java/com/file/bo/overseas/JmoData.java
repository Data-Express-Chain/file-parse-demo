package com.file.bo.overseas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public class JmoData {
    @Data
    public static class KartuDigitalData {
        @JsonProperty("kartuDigital")
        private KartuDigital kartuDigital;
    }

    @Data
    public static class KartuDigital {
        @JsonProperty("statusKepesertaan")
        private String statusKepesertaan;

        @JsonProperty("segmenPeserta")
        private String segmenPeserta;

        @JsonProperty("Perusahaan Tempat Bekerja")
        private String perusahaanTempatBekerja;

        @JsonProperty("Upah Terakhir")
        private String upahTerakhir;

        @JsonProperty("Pembayaran Iuran Terakhir")
        private String pembayaranIuranTerakhir;

        @JsonProperty("Tanggal Usia Pensiun")
        private String tanggalUsiaPensiun;

        @JsonProperty("Masa luran Jaminan Pensiun")
        private String masaLaluJaminanPensiun;

        @JsonProperty("Tanggal Kepesertaan Awal Jaminan Kehilangan Pekerjaa")
        private String tanggalKepesertaanAwalJaminanKehilanganPekerjaa;

        @JsonProperty("Masa luran Jaminan Kehilangan Pekerjaan")
        private String masaLaluJaminanKehilanganPekerjaan;

        @JsonProperty("Program Yang Dikuti")
        private String programYangDikuti;
    }

    @Data
    public static class ProfileData {
        @JsonProperty("profile")

        private Profile profile;
    }

    @Data
    public static class Profile {
        @JsonProperty("name")
        private String name;

        @JsonProperty("program")
        private String program;

        @JsonProperty("KPT/Paspor")
        private String kptPaspor;

        @JsonProperty("normorHp")
        private String normorHp;
    }

}
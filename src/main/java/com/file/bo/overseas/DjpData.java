package com.file.bo.overseas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class DjpData {

    @Data
    public static class ProfileData {
        @JsonProperty("dataUtama")
        private DataUtama dataUtama;

        @JsonProperty("dataLainnya")
        private DataLainnya dataLainnya;

        @JsonProperty("anggotaKeluarga")
        private AnggotaKeluarga anggotaKeluarga;
    }

    @Data
    public static class DataUtama {
        @JsonProperty("NPWP15")
        private String npwp15;

        @JsonProperty("NITKU")
        private String nitku;

        @JsonProperty("NIK/NPWP16")
        private String nikNpwp16;

        @JsonProperty("Nama")
        private String nama;

        @JsonProperty("Tempat Lahir")
        private String tempatLahir;

        @JsonProperty("Tanggal Lahir")
        private String tanggalLahir;
    }

    @Data
    public static class DataLainnya {
        @JsonProperty("Alamat")
        private String alamat;

        @JsonProperty("Kebangsaan")
        private String kebangsaan;

        @JsonProperty("Handphone")
        private String handphone;

        @JsonProperty("Email")
        private String email;
    }

    @Data
    public static class AnggotaKeluarga {
        @JsonProperty("NO.KK")
        private String noKK;

        @JsonProperty("NIK")
        private String nik;

        @JsonProperty("NAMA")
        private String nama;

        @JsonProperty("TEMPATLAHIR")
        private String tempatLahir;

        @JsonProperty("TGLLAHIR")
        private String tglLahir;

        @JsonProperty("STATUSHUBKELUARGA")
        private String statusHubKeluarga;

        @JsonProperty("PEKERJAAN")
        private String pekerjaan;

        @JsonProperty("STATUS")
        private String status;
    }

    @Data
    public static class PaymentData {
        @JsonProperty("Pembayaran")
        private List<Pembayaran> pembayaran;
    }

    @Data
    public static class Pembayaran {
        @JsonProperty("TAHUN/MASAPAJAK")
        private String tahunMasapajak;

        @JsonProperty("TANGGALBAYAR")
        private String tanggalBayar;

        @JsonProperty("NTPN")
        private String ntpn;

        @JsonProperty("NOMINALBAYAR")
        private String nominalBayar;
    }

    @Data
    public static class DeclarationData {
        @JsonProperty("RiwayatPelaporan")
        private List<RiwayatPelaporan> riwayatPelaporan;
    }

    @Data
    public static class RiwayatPelaporan {
        @JsonProperty("JENIS SPT")
        private String jenisSpt;

        @JsonProperty("TAHUN/MASA PAJAK")
        private String tahunMasapajak;

        @JsonProperty("PEMBETULAN KE")
        private String pembetulanKe;

        @JsonProperty("STATUS")
        private String status;
    }
}
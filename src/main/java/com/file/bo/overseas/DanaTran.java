package com.file.bo.overseas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DanaTran {

    /**
     * 交易日期
     */
    @JsonProperty("Tanggal & Waktu")
    private String tanggalWaktu;

    /**
     * 交易类型
     */
    @JsonProperty("Transaksi")
    private String transaksi;

    /**
     * 支付方式
     */
    @JsonProperty("MetodePembayaran")
    private String metodePembayaran;

    /**
     * 金额
     */
    @JsonProperty("Jumlah")
    private String jumlah;
}

package com.file.bo.overseas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DanaData {

    /**
     * 姓名
     */
    @JsonProperty("name")
    private String name;

    /**
     * 账号
     */
    @JsonProperty("no")
    private String no;

    /**
     * 账单所属期
     */
    @JsonProperty("month")
    private String month;

    /**
     * 文档发布时间
     */
    @JsonProperty("diterbitkanPada")
    private String diterbitkanPada;

    /**
     * 交易明细
     */
    @JsonProperty("transactionList")
    private List<DanaTran> danaTrans;

    @Data
    public static class UserProfileData {
        @JsonProperty("profile")
        private DanaProfile profile;

        @Data
        public static class DanaProfile {

            /**
             * 姓名
             */
            @JsonProperty("name")
            private String name;

            /**
             * 手机号
             */
            @JsonProperty("no")
            private String no;

            /**
             * 邮箱地址
             */
            @JsonProperty("email")
            private String email;
        }
    }

    @Data
    public static class TransactionListData {
        @JsonProperty("transactionList")
        private List<DanaTran> transactionList;
    }

    @Data
    public static class DanaTran {

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
}


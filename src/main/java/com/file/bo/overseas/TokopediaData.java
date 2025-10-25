package com.file.bo.overseas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class TokopediaData {

    @Data
    public static class UserProfileData {
        @JsonProperty("user")
        private UserProfile userProfile;

        @Data
        public static class UserProfile {

            /**
             * 姓名
             */
            @JsonProperty("fullName")
            private String fullName;

            /**
             * 邮箱
             */
            @JsonProperty("email")
            private String email;

            /**
             * 手机号
             */
            @JsonProperty("phone")
            private String phone;

            /**
             * 性别
             */
            @JsonProperty("gender")
            private String gender;

            /**
             * 出生日期
             */
            @JsonProperty("birthday")
            private String birthday;

            /**
             * 钱包
             */
            @JsonProperty("wallet")
            private WalletData wallet = new WalletData();
        }

        @Data
        public static class WalletData {

            /**
             * 钱包余额
             */
            @JsonProperty("saldo")
            private String saldo = "";

            /**
             * 余额
             */
            @JsonProperty("gopay")
            private String gopay = "";
        }
    }

    @Data
    public static class AddressesData {
        @JsonProperty("addresses")
        private List<AddressData> addresses;

        @Data
        public static class AddressData {

            /**
             * 地址标签
             */
            @JsonProperty("label")
            private String label;

            /**
             * 收件人
             */
            @JsonProperty("recipient")
            private String recipient;

            /**
             * 手机号
             */
            @JsonProperty("phone")
            private String phone;

            /**
             * 收件地址
             */
            @JsonProperty("fullAddress")
            private String fullAddress;

            /**
             * 是否是默认地址
             */
            @JsonProperty("isMain")
            private Boolean isMain = false;

            /**
             * 是否已进行精准定位
             */
            @JsonProperty("isPinpoint")
            private Boolean isPinpoint = false;
        }
    }

    @Data
    public static class BankAccountsData {
        @JsonProperty("bankAccouts")
        private List<BankAccountData> bankAccounts;

        @Data
        public static class BankAccountData {

            /**
             * 银行名称
             */
            @JsonProperty("bankName")
            private String bankName;

            /**
             * 账号
             */
            @JsonProperty("accountNumber")
            private String accountNumber;

            /**
             * 账号持有人
             */
            @JsonProperty("accountHolder")
            private String accountHolder;
        }
    }

    @Data
    public static class OrderListData {
        @JsonProperty("orderList")
        private List<OrderData> orderList;

        @Data
        public static class OrderData {

            /**
             * 订单编号
             */
            @JsonProperty("id")
            private String id = "";

            /**
             * 订单日期
             */
            @JsonProperty("date")
            private String date;

            /**
             * 订单状态
             */
            @JsonProperty("status")
            private String status;

            /**
             * 商铺名称
             */
            @JsonProperty("shop")
            private String shop = "";

            /**
             * 商品名称
             */
            @JsonProperty("product")
            private String product;

            /**
             * 数量
             */
            @JsonProperty("qty")
            private String qty;

            /**
             * 商品价格
             */
            @JsonProperty("price")
            private String price = "";

            /**
             * 总价
             */
            @JsonProperty("total")
            private String total;
        }
    }
}

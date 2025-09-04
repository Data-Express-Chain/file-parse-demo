package com.file.bo.overseas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class GojekData {

    @Data
    public static class UserProfileData {
        @JsonProperty("userProfile")
        private UserProfile userProfile;

        @Data
        public static class UserProfile {

            /**
             * 昵称
             */
            @JsonProperty("name")
            private String name;

            /**
             * 邮箱
             */
            @JsonProperty("email")
            private String email;

            /**
             * 手机号
             */
            @JsonProperty("phoneNumber")
            private String phoneNumber;
        }
    }

    @Data
    public static class TransactionListData {

        @JsonProperty("transactions")
        private List<TransactionData> transactions;

        @Data
        public static class TransactionData {

            /**
             * 交易日期
             */
            @JsonProperty("date")
            private String date;

            /**
             * 交易明细
             */
            @JsonProperty("transactionDetails")
            private List<TransactionDetail> transactionDetails;
        }

        @Data
        public static class TransactionDetail {

            /**
             * 交易描述
             */
            @JsonProperty("description")
            private String description;

            /**
             * 交易金额
             */
            @JsonProperty("amount")
            private String amount;

            /**
             * 支付方式
             */
            @JsonProperty("method")
            private String method;

            /**
             * 补充字段
             */
            @JsonProperty("ext")
            private String ext;
        }
    }
}

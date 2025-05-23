package com.file.bo.overseas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class ShopeeData {

    @Data
    public static class CoinData {
        @JsonProperty("coinsAvailable")
        private String coinsAvailable;

        @JsonProperty("earningList")
        private List<Earning> earningList;

        @Data
        public static class Earning {
            @JsonProperty("coin")
            private String coin;

            @JsonProperty("dueTime")
            private String dueTime;
        }
    }

    @Data
    public static class BankAccountBriefData {
        @JsonProperty("bankAccountBrief")
        private List<BankAccountBrief> bankAccountBrief;

        @Data
        public static class BankAccountBrief {
            @JsonProperty("bankName")
            private String bankName;

            @JsonProperty("cardNo")
            private String cardNo;

            @JsonProperty("label")
            private String label;
        }
    }

    @Data
    public static class UserProfileData {
        @JsonProperty("userProfile")
        private UserProfile userProfile;

        @Data
        public static class UserProfile {
            @JsonProperty("name")
            private String name;

            @JsonProperty("bio")
            private String bio;

            @JsonProperty("email")
            private String email;

            @JsonProperty("phoneNumber")
            private String phoneNumber;

            @JsonProperty("gender")
            private String gender;

            @JsonProperty("dateOfBirth")
            private String dateOfBirth;
        }
    }


    @Data
    public static class PurchaseListData {
        @JsonProperty("purchaseList")
        private List<Purchase> purchaseList;

        @Data
        public static class Purchase {
            @JsonProperty("orderNumber")
            private String orderNumber;

            @JsonProperty("merchantName")
            private String merchantName;

            @JsonProperty("orderStatus")
            private String orderStatus;

            @JsonProperty("productName")
            private String productName;

            @JsonProperty("productSpec")
            private String productSpec;

            @JsonProperty("quantity")
            private String quantity;

            @JsonProperty("originalPrice")
            private String originalPrice;

            @JsonProperty("discountedPrice")
            private String discountedPrice;

            @JsonProperty("quantity_1")
            private String quantity_1;

            @JsonProperty("totalPrice")
            private String totalPrice;
        }
    }

    @Data
    public static class AddressListData {
        @JsonProperty("addressList")
        private List<Address> addressList;

        @Data
        public static class Address {
            @JsonProperty("name")
            private String name;

            @JsonProperty("phoneNumber")
            private String phoneNumber;

            @JsonProperty("detailedAddress")
            private String detailedAddress;

            @JsonProperty("Region")
            private String region;

            @JsonProperty("Label")
            private String label;
        }
    }
}

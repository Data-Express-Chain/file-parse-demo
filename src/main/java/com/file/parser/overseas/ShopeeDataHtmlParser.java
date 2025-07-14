package com.file.parser;


import com.file.bo.ResponseData;
import com.file.bo.ShopeeData;
import com.file.constant.ErrorCode;
import com.file.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ShopeeDataHtmlParser {

    public ResponseData<String> parseShopeeDataHtmlToJson(String daId, String filePath) {
        log.info("parseShopeeDataHtmlToJson started, daId:{}", daId);
        String json;

        try {
            if (filePath.contains("coin")) {
                ShopeeData.CoinData shopeeDataCoin = parseShopeeDataCoinHtml(filePath);
                json = JsonUtils.convertObjectToJson(shopeeDataCoin);
            } else if (filePath.contains("payment")) {
                ShopeeData.BankAccountBriefData shopeeDataPayment = parseShopeeDataPaymentHtml(filePath);
                json = JsonUtils.convertObjectToJson(shopeeDataPayment);
            } else if (filePath.contains("profile")) {
                ShopeeData.UserProfileData shopeeDataProfile = parseShopeeDataProfileHtml(filePath);
                json = JsonUtils.convertObjectToJson(shopeeDataProfile);
            } else if (filePath.contains("address")) {
                ShopeeData.AddressListData shopeeDataAddress = parseShopeeDataAddressHtml(filePath);
                json = JsonUtils.convertObjectToJson(shopeeDataAddress);
            } else if (filePath.contains("purchase")) {
                ShopeeData.PurchaseListData shopeeDataPurchase = parseShopeeDataPurchaseHtml(filePath);
                json = JsonUtils.convertObjectToJson(shopeeDataPurchase);
            } else {
                throw new RuntimeException("the file name is not supported");
            }
        } catch (IOException e) {
            log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseShopeeDataHtmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseShopeeDataHtmlToJson completed, daId:{}, json:{}", daId, json);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    public ShopeeData.CoinData parseShopeeDataCoinHtml(String filePath) throws IOException {
        ShopeeData.CoinData coinData = new ShopeeData.CoinData();
        List<ShopeeData.CoinData.Earning> earningList = new ArrayList<>();

        File input = new File(filePath);

        Document doc = Jsoup.parse(input, "UTF-8");
        coinData.setCoinsAvailable(doc.getElementsByClass("YRMUSY").get(0).text());

        Elements elements = doc.getElementsByClass("MfcbPl");
        for (Element element: elements) {
            ShopeeData.CoinData.Earning earning = new ShopeeData.CoinData.Earning();
            earning.setCoin(element.getElementsByClass("V4NzHd").get(0).text());
            earning.setDueTime(element.getElementsByClass("A0xKoZ").get(0).text());
            earningList.add(earning);
        }

        coinData.setEarningList(earningList);
        return coinData;
    }

    public ShopeeData.BankAccountBriefData parseShopeeDataPaymentHtml(String filePath) throws IOException {
        ShopeeData.BankAccountBriefData shopeeDataPayment = new ShopeeData.BankAccountBriefData();
        List<ShopeeData.BankAccountBriefData.BankAccountBrief> accountBriefList = new ArrayList<>();

        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");

        Elements elements = doc.getElementsByClass("ba-card__content");
        for (Element element : elements) {
            ShopeeData.BankAccountBriefData.BankAccountBrief accountBrief = new ShopeeData.BankAccountBriefData.BankAccountBrief();
            accountBrief.setBankName(element.getElementsByClass("ba-card__bank-name").get(0).text());
            accountBrief.setCardNo(element.getElementsByClass("bacc-acc-no").get(0).text());
            Elements badge = element.getElementsByClass("bacc-default-badge");
            if (badge.size() > 0) {
                accountBrief.setLabel(badge.get(0).text());
            }
            accountBriefList.add(accountBrief);
        }
        shopeeDataPayment.setBankAccountBrief(accountBriefList);
        return shopeeDataPayment;
    }

    private ShopeeData.UserProfileData parseShopeeDataProfileHtml(String filePath) throws IOException {
        ShopeeData.UserProfileData shopeeDataProfile = new ShopeeData.UserProfileData();
        ShopeeData.UserProfileData.UserProfile userProfile = new ShopeeData.UserProfileData.UserProfile();
        userProfile.setBio("");
        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");

        Elements elements = doc.getElementsByTag("tbody");
        if (elements.isEmpty()) {
            throw new RuntimeException("html can not find tbody tag");
        }

        Element tbodyElement = elements.get(0);
        Elements trElements = tbodyElement.getElementsByTag("tr");
        trElements.forEach(element -> {
            Elements tdElements = element.getElementsByTag("td");
            if (tdElements.size() >= 2) {
                Element label = tdElements.get(0).getElementsByTag("label").get(0);
                if (label != null) {
                    String labelText = label.text().trim();
                    switch (labelText) {
                        case "Username":
//                            String userName = tdElements.get(1).getElementsByTag("input").get(0).attr("value");
//                            userProfile.setName(userName);
                            break;
                        case "Nama":
                            String name = tdElements.get(1).getElementsByTag("input").get(0).attr("value");
                            userProfile.setName(name);
                            break;
                        case "Email":
                            String email = tdElements.get(1).getElementsByClass("OrX172").text();
                            userProfile.setEmail(email);
                            break;
                        case "Nomor Telepon":
                            String phoneNumber = tdElements.get(1).getElementsByClass("OrX172").text();
                            userProfile.setPhoneNumber(phoneNumber);
                            break;
                        case "Nama Toko":
//                            String shopName = tdElements.get(1).getElementsByTag("input").get(0).attr("value");
                            break;
                        case "Jenis Kelamin":
                            Element genderElement = tdElements.get(1).selectFirst(".stardust-radio.stardust-radio--checked");
                            if (genderElement != null) {
                                userProfile.setGender(genderElement.text());
                            }
                            break;
                        case "Tanggal lahir":
                            String dateOfBirth = tdElements.get(1).getElementsByClass("OrX172").text();
                            userProfile.setDateOfBirth(dateOfBirth);
                            break;
                    }
                }
            }
        });

        shopeeDataProfile.setUserProfile(userProfile);
        return shopeeDataProfile;
    }


    private ShopeeData.AddressListData parseShopeeDataAddressHtml(String filePath) throws IOException {

        ShopeeData.AddressListData shopeeDataAddress = new ShopeeData.AddressListData();
        List<ShopeeData.AddressListData.Address> addressList = new ArrayList<>();
        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");
        Elements addressElements = doc.select(".Oy0a7C.inZjsh");
        addressElements.forEach(element -> {
            ShopeeData.AddressListData.Address address = new ShopeeData.AddressListData.Address();
            address.setName(element.selectFirst(".xwB4_Q").text());
            address.setPhoneNumber(element.selectFirst(".urSLUA.SDYBn1.PoI6l8").text());
            Elements elements = element.select(".W_MyuV > .PoI6l8");
            if (elements.size() >= 2) {
                address.setDetailedAddress(elements.get(0).text());
                address.setRegion(elements.get(1).text());
            } else if (elements.size() == 1) {
                address.setDetailedAddress(elements.first().text());
                address.setRegion("");
            } else {
                address.setDetailedAddress("");
                address.setRegion("");
            }
            address.setLabel(Optional.ofNullable(element.selectFirst(".CTpOx2.TXhBWf.F4fNIN"))
                    .map(Element::text).orElse(""));

            addressList.add(address);
        });
        shopeeDataAddress.setAddressList(addressList);
        return shopeeDataAddress;
    }

    private ShopeeData.PurchaseListData parseShopeeDataPurchaseHtml(String filePath) throws IOException {

        ShopeeData.PurchaseListData purchaseListData = new ShopeeData.PurchaseListData();
        List<ShopeeData.PurchaseListData.Purchase> purchaseList = new ArrayList<>();
        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");
        Elements elements = doc.select(".YL_VlX");

        elements.forEach(element -> {
            String shopName = Optional.ofNullable(element.selectFirst(".UDaMW3"))
                    .map(Element::text)
                    .orElse("");
            String orderTotal = Optional.ofNullable(element.selectFirst(".t7TQaf"))
                    .map(Element::text)
                    .orElse("");
            String orderStatus = Optional.ofNullable(element.selectFirst(".bv3eJE"))
                    .map(Element::text)
                    .orElse("");

            Elements itemNameElements = element.select(".mZ1OWk");
            itemNameElements.forEach(element1 -> {
                ShopeeData.PurchaseListData.Purchase purchase = new ShopeeData.PurchaseListData.Purchase();
                purchase.setOrderNumber(Integer.toString(purchaseList.size() + 1));
                purchase.setMerchantName(shopName);
                purchase.setOrderStatus(orderStatus);

                String originalPrice = Optional.ofNullable(element.selectFirst(".q6Gzj5"))
                        .map(Element::text)
                        .orElse("");
                String discountedPrice = Optional.ofNullable(element.selectFirst(".nW_6Oi"))
                        .map(Element::text)
                        .orElse("");

                if (StringUtils.isBlank(discountedPrice)) {
                    discountedPrice = originalPrice;
                }

                purchase.setProductName(Optional.ofNullable(element.selectFirst(".DWVWOJ"))
                        .map(Element::text)
                        .orElse(""));
                purchase.setProductSpec(Optional.ofNullable(element.selectFirst(".rsautk"))
                        .map(Element::text)
                        .orElse(""));
                purchase.setQuantity(Optional.ofNullable(element.selectFirst(".j3I_Nh"))
                        .map(Element::text)
                        .orElse(""));
                purchase.setOriginalPrice(originalPrice);
                purchase.setDiscountedPrice(discountedPrice);
                purchase.setQuantity_1(Optional.ofNullable(element.selectFirst(".j3I_Nh"))
                        .map(Element::text)
                        .orElse(""));
                purchase.setTotalPrice(orderTotal);

                purchaseList.add(purchase);
            });
        });
        purchaseListData.setPurchaseList(purchaseList);

        return purchaseListData;
    }

    public static void main(String[] args) throws IOException {
        ShopeeDataHtmlParser shopeeHtmlParser = new ShopeeDataHtmlParser();
        String json;
        json = shopeeHtmlParser.parseShopeeDataHtmlToJson("", "D:\\data\\file\\chrome-shopee-data\\2507101\\profile.html").getData();
        System.out.println(json);
//
        json = shopeeHtmlParser.parseShopeeDataHtmlToJson("", "D:\\data\\file\\chrome-shopee-data\\2507101\\payment.html").getData();
        System.out.println(json);

        json = shopeeHtmlParser.parseShopeeDataHtmlToJson("", "D:\\data\\file\\chrome-shopee-data\\2507101\\address.html").getData();
        System.out.println(json);
//
        json = shopeeHtmlParser.parseShopeeDataHtmlToJson("", "D:\\data\\file\\chrome-shopee-data\\2507101\\coin.html").getData();
        System.out.println(json);

        json = shopeeHtmlParser.parseShopeeDataHtmlToJson("", "D:\\data\\file\\chrome-shopee-data\\2507101\\purchase.html").getData();
        System.out.println(json);
    }
}

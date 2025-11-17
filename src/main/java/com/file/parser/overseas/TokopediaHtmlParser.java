package com.file.parser.overseas;

import com.file.bo.ResponseData;
import com.file.bo.overseas.TokopediaData;
import com.file.constant.ErrorCode;
import com.file.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TokopediaHtmlParser {

    public ResponseData<String> parseTokopediaDataHtmlToJson(String daId, String filePath) {
        log.info("parseTokopediaDataHtmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json;

        try {
            if (filePath.contains("user-settings")) {
                TokopediaData.UserProfileData userProfileData = parseTokopediaDataProfileHtml(filePath);
                json = JsonUtils.convertObjectToJson(userProfileData);
            } else if (filePath.contains("address")) {
                TokopediaData.AddressesData addressesData = parseTokopediaDataAddressHtml(filePath);
                json = JsonUtils.convertObjectToJson(addressesData);
            } else if (filePath.contains("bank")) {
                TokopediaData.BankAccountsData bankAccountsData = parseTokopediaDataBankHtml(filePath);
                json = JsonUtils.convertObjectToJson(bankAccountsData);
            } else if (filePath.contains("order-list")) {
                TokopediaData.OrderListData orderListData = parseTokopediaDataOrderListHtml(filePath);
                json = JsonUtils.convertObjectToJson(orderListData);
            } else {
                throw new RuntimeException("the file name is not supported");
            }
        } catch (IOException e) {
            log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseTokopediaDataHtmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseTokopediaDataHtmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    private TokopediaData.UserProfileData parseTokopediaDataProfileHtml(String filePath) throws IOException {
        TokopediaData.UserProfileData userProfileData = new TokopediaData.UserProfileData();
        TokopediaData.UserProfileData.UserProfile userProfile = new TokopediaData.UserProfileData.UserProfile();
        TokopediaData.UserProfileData.WalletData walletData = new TokopediaData.UserProfileData.WalletData();

        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");

        // 提取姓名
        Elements nameElements = doc.select(".css-5hicrt");
        if (!nameElements.isEmpty()) {
            userProfile.setFullName(nameElements.first().text());
        }

        // 提取邮箱
        Elements emailElements = doc.select(".css-5hicrt");
        for (Element element : emailElements) {
            Element parent = element.parent();
            if (parent != null && parent.text().contains("Email")) {
                userProfile.setEmail(element.text());
                break;
            }
        }

        // 提取手机号
        Elements phoneElements = doc.select(".css-5hicrt");
        for (Element element : phoneElements) {
            Element parent = element.parent();
            if (parent != null && parent.text().contains("Nomor HP")) {
                userProfile.setPhone(element.text());
                break;
            }
        }

        // 提取性别
        Elements genderElements = doc.select(".css-5hicrt");
        for (Element element : genderElements) {
            Element parent = element.parent();
            if (parent != null && parent.text().contains("Jenis Kelamin")) {
                userProfile.setGender(element.text());
                break;
            }
        }

        // 提取出生日期
        Elements birthdayElements = doc.select(".css-5hicrt");
        for (Element element : birthdayElements) {
            Element parent = element.parent();
            if (parent != null && parent.text().contains("Tanggal Lahir")) {
                userProfile.setBirthday(element.text());
                break;
            }
        }

        // 提取钱包余额
        Elements saldoElements = doc.select(".css-ruo41b p");
        for (Element element : saldoElements) {
            if (element.text().contains("Saldo")) {
                // 下一个兄弟元素是余额值
                Element nextElement = element.nextElementSibling();
                if (nextElement != null) {
                    walletData.setSaldo(nextElement.text());
                }
                break;
            }
        }

        Elements gopayElements = doc.select(".css-ruo41b p.sidebarUserProfileLabel");
        for (Element element : gopayElements) {
            if (element.text().contains("GoPay")) {
                // 下一个兄弟元素是GoPay余额值
                Element nextElement = element.nextElementSibling();
                if (nextElement != null) {
                    walletData.setGopay(nextElement.text());
                }
                break;
            }
        }

        userProfile.setWallet(walletData);
        userProfileData.setUserProfile(userProfile);

        return userProfileData;
    }

    private TokopediaData.AddressesData parseTokopediaDataAddressHtml(String filePath) throws IOException {
        TokopediaData.AddressesData addressesData = new TokopediaData.AddressesData();
        List<TokopediaData.AddressesData.AddressData> addressList = new ArrayList<>();

        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");

        // 选择所有地址卡片
        Elements addressCards = doc.select("section[data-testid='address-card']");

        for (Element card : addressCards) {
            TokopediaData.AddressesData.AddressData addressData = new TokopediaData.AddressesData.AddressData();

            // 提取地址标签（如：Rumah, Kantor, Store等）
            Elements labelElements = card.select(".css-fnug95-unf-heading b");
            if (!labelElements.isEmpty()) {
                addressData.setLabel(labelElements.first().text());
            }

            // 提取收件人姓名
            Elements recipientElements = card.select(".css-1c3p7uj-unf-heading b");
            if (!recipientElements.isEmpty()) {
                addressData.setRecipient(recipientElements.first().text());
            }

            // 提取手机号
            Elements phoneElements = card.select(".css-83l3x0-unf-heading");
            if (!phoneElements.isEmpty()) {
                addressData.setPhone(phoneElements.first().text());
            }

            // 提取完整地址
            Elements addressElements = card.select(".css-hi2ng4-unf-heading");
            if (!addressElements.isEmpty()) {
                addressData.setFullAddress(addressElements.first().text());
            }

            // 判断是否是主要地址
            String dataSelected = card.attr("data-selected");
            if ("true".equals(dataSelected)) {
                addressData.setIsMain(true);
            }

            // 检查是否有"Utama"标签
            Elements mainLabelElements = card.select(".css-1k4oqim-unf-label p");
            for (Element label : mainLabelElements) {
                if ("Utama".equals(label.text())) {
                    addressData.setIsMain(true);
                    break;
                }
            }

            // 判断是否已精确定位
            Elements pinpointElements = card.select(".css-1td20w-unf-heading b, .css-72vsdz-unf-heading b");
            if (!pinpointElements.isEmpty()) {
                String pinpointText = pinpointElements.first().text();
                addressData.setIsPinpoint("Sudah Pinpoint".equals(pinpointText));
            }

            addressList.add(addressData);
        }

        addressesData.setAddresses(addressList);
        return addressesData;
    }

    private TokopediaData.BankAccountsData parseTokopediaDataBankHtml(String filePath) throws IOException {
        TokopediaData.BankAccountsData bankAccountsData = new TokopediaData.BankAccountsData();
        List<TokopediaData.BankAccountsData.BankAccountData> bankAccounts = new ArrayList<>();

        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");

        // 查找所有银行账户项
        Elements bankItems = doc.select("[data-testid='sba-bank-item']");

        // 如果上面的选择器找不到，尝试使用更通用的选择器
        if (bankItems.isEmpty()) {
            // 根据HTML结构，银行账户信息在包含银行logo和账户信息的容器中
            bankItems = doc.select(".css-4pcr56, .ei0wc5h3");
        }

        for (Element bankItem : bankItems) {
            TokopediaData.BankAccountsData.BankAccountData bankAccount = new TokopediaData.BankAccountsData.BankAccountData();

            // 提取银行名称
            Element bankNameElement = bankItem.selectFirst("[data-testid='sba-bank-item-name']");
            if (bankNameElement != null) {
                bankAccount.setBankName(bankNameElement.text());
            } else {
                // 备选方案：从logo的alt属性获取银行名称
                Element logoElement = bankItem.selectFirst("img");
                if (logoElement != null) {
                    bankAccount.setBankName(logoElement.attr("alt"));
                }
            }

            // 提取账号
            Element accountNumberElement = bankItem.selectFirst("[data-testid='sba-bank-item-account']");
            if (accountNumberElement != null) {
                // 账号通常在第一个div中
                Element numberElement = accountNumberElement.selectFirst(".css-p7hlqy");
                if (numberElement != null) {
                    bankAccount.setAccountNumber(numberElement.text());
                } else {
                    // 如果没有特定class，尝试获取第一个子div的文本
                    Elements children = accountNumberElement.children();
                    if (!children.isEmpty()) {
                        bankAccount.setAccountNumber(children.first().text());
                    }
                }

                // 提取账户持有人
                Element holderElement = accountNumberElement.selectFirst(".css-1fpjeno");
                if (holderElement != null) {
                    String holderText = holderElement.text();
                    // 移除 "a.n " 前缀
                    if (holderText.startsWith("a.n ")) {
                        bankAccount.setAccountHolder(holderText.substring(4));
                    } else {
                        bankAccount.setAccountHolder(holderText);
                    }
                }
            }

            // 只有当成功提取到银行名称时才添加到列表
            if (bankAccount.getBankName() != null && !bankAccount.getBankName().isEmpty()) {
                bankAccounts.add(bankAccount);
            }
        }

        bankAccountsData.setBankAccounts(bankAccounts);
        return bankAccountsData;
    }

    private TokopediaData.OrderListData parseTokopediaDataOrderListHtml(String filePath) throws IOException {
        TokopediaData.OrderListData orderListData = new TokopediaData.OrderListData();
        List<TokopediaData.OrderListData.OrderData> orderList = new ArrayList<>();

        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");

        // 查找所有订单项
        Elements orderItems = doc.select("[data-testid^='orderItem-']");

        for (Element orderItem : orderItems) {
            TokopediaData.OrderListData.OrderData orderData = new TokopediaData.OrderListData.OrderData();

            // 提取订单编号 - 从data-testid属性中提取
            String dataTestId = orderItem.attr("data-testid");
            if (dataTestId.startsWith("orderItem-")) {
                orderData.setId(dataTestId.substring("orderItem-".length()));
            }

            // 提取订单日期
            Element dateElement = orderItem.selectFirst(".css-fuge0h-unf-heading");
            if (dateElement != null) {
                orderData.setDate(dateElement.text());
            }

            // 提取订单状态
            Element statusElement = orderItem.selectFirst("[data-testid='badge-text']");
            if (statusElement != null) {
                orderData.setStatus(statusElement.text());
            }

            // 提取店铺名称
            Element shopNameElement = orderItem.selectFirst("[data-testid^='shopName-']");
            if (shopNameElement != null) {
                orderData.setShop(shopNameElement.text());
            }

            // 提取商品信息
            Element productElement = orderItem.selectFirst(".css-1tt01h3-unf-heading");
            if (productElement != null) {
                orderData.setProduct(productElement.text());
            }

            // 提取数量和价格信息
            Element qtyPriceElement = orderItem.selectFirst(".label-info.css-1i78yju-unf-heading");
            if (qtyPriceElement != null) {
                String qtyPriceText = qtyPriceElement.text();
                String pattern = "\\d+\\s+barang\\s+x\\s+Rp[\\d.]+";
                if (!qtyPriceText.matches(pattern)) {
                    orderData.setQty("");
                    orderData.setPrice("");
                } else {
                    String qty = qtyPriceText.split(" ")[0].trim();
                    orderData.setQty(qty);

                    // 从数量价格文本中提取价格信息
                    // 格式通常是: "1 barang x Rp1.114.000"
                    if (qtyPriceText.contains("x")) {
                        String[] parts = qtyPriceText.split("x");
                        if (parts.length > 1) {
                            orderData.setPrice(parts[1].trim());
                        }
                    }
                }
            }

            // 提取总价
            Element totalElement = orderItem.selectFirst(".sum-price .css-e9op4x-unf-heading");
            if (totalElement != null) {
                orderData.setTotal(totalElement.text());
            }

            // 检查是否有其他商品
            Element otherProductsElement = orderItem.selectFirst("[data-testid='ctaSeeOtherProducts']");
            if (otherProductsElement != null) {
                // 如果有其他商品，可以在product字段中标记
                String currentProduct = orderData.getProduct();
                if (currentProduct != null) {
                    orderData.setProduct(currentProduct + " " + otherProductsElement.text());
                }
            }

            orderList.add(orderData);
        }

        orderListData.setOrderList(orderList);
        return orderListData;
    }

    public static void main(String[] args) throws IOException {
        TokopediaHtmlParser parser = new TokopediaHtmlParser();
        String folder = "html";
        String orderListJson = parser.parseTokopediaDataHtmlToJson("", "D:\\data\\file\\tokopedia\\"+ folder +"\\chrome-tokopedia-data_order-list.html").getData();
        System.out.println(orderListJson);

       String addressJson = parser.parseTokopediaDataHtmlToJson("", "D:\\data\\file\\tokopedia\\"+ folder +"\\chrome-tokopedia-data_address.html").getData();
       System.out.println(addressJson);

       String bankJson = parser.parseTokopediaDataHtmlToJson("", "D:\\data\\file\\tokopedia\\"+ folder +"\\chrome-tokopedia-data_bank.html").getData();
       System.out.println(bankJson);

       String profileJson = parser.parseTokopediaDataHtmlToJson("", "D:\\data\\file\\tokopedia\\"+ folder +"\\chrome-tokopedia-data_user-settings.html").getData();
       System.out.println(profileJson);
    }
}
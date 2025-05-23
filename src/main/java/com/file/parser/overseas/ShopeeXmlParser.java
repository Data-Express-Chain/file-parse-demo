package com.file.parser.overseas;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.file.bo.ResponseData;
import com.file.bo.overseas.ShopeeData;
import com.file.constant.ErrorCode;
import com.file.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ShopeeXmlParser {

    public ResponseData<String> parseAddressXmlToJson(String daId, String filePath) {
        log.info("parseAddressXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json;

        try {
            ShopeeData.AddressListData addressListData = parseAddressXml(filePath);
            json = JsonUtils.convertObjectToJson(addressListData);
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseAddressXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseAddressXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    public ResponseData<String> parseCoinXmlToJson(String daId, String filePath) {
        log.info("parseCoinXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json;

        try {
            ShopeeData.CoinData coinData = parseCoinXml(filePath);
            json = JsonUtils.convertObjectToJson(coinData);
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseCoinXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseCoinXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    public ResponseData<String> parseProfileXmlToJson(String daId, String filePath) {
        log.info("parseProfileXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json;

        try {
            ShopeeData.UserProfileData userProfileData = parseUserProfileXml(filePath);
            json = JsonUtils.convertObjectToJson(userProfileData);
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseProfileXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseProfileXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    public ResponseData<String> parsePaymentXmlToJson(String daId, String filePath) {
        log.info("parsePaymentXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json;

        try {
            ShopeeData.BankAccountBriefData bankAccountBriefData = parsePaymentXml(filePath);
            json = JsonUtils.convertObjectToJson(bankAccountBriefData);
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parsePaymentXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parsePaymentXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    public ResponseData<String> parsePurchaseXmlToJson(String daId, String filePath) {
        log.info("parsePurchaseXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json;

        try {
            ShopeeData.PurchaseListData purchaseListData = parsePurchaseXml(filePath);
            json = JsonUtils.convertObjectToJson(purchaseListData);
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parsePurchaseXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parsePurchaseXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    private List<String> parseNodeTexts(List<Element> elements) {
        List<String> nodeTextList = new ArrayList<>();
        elements.forEach(element -> {
            String text = element.attributeValue("text");
            if (StringUtils.isNotBlank(text)) {
                nodeTextList.add(text);
            }
            nodeTextList.addAll(parseNodeTexts(element.elements()));
        });

        return nodeTextList;
    }

    public List<String> parseNodeTextsWithoutResourceId(List<Element> elements) {
        List<String> nodeTextList = new ArrayList<>();
        Stack<Element> stack = new Stack<>();
        stack.addAll(elements);

        while (!stack.isEmpty()) {
            Element element = stack.pop();
            String resId = element.attributeValue("resource-id");
            String text = element.attributeValue("text");

            if (StringUtils.isNotBlank(text) && StringUtils.isEmpty(resId)) {
                nodeTextList.add(text);
            }

            // Add child elements to the stack in reverse order to maintain the original processing order
            List<Element> children = element.elements();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }

        Collections.reverse(nodeTextList);
        return nodeTextList;
    }

    private ShopeeData.AddressListData parseAddressXml(String filePath) throws DocumentException {
        ShopeeData.AddressListData addressListData = new ShopeeData.AddressListData();
        List<ShopeeData.AddressListData.Address> addressList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        Element rootElement = document.getRootElement();
        Stack<Element> elementStack = new Stack<>();
        elementStack.push(rootElement);
        while (!elementStack.isEmpty()) {
            Element currentElement = elementStack.pop();
            String id = currentElement.attributeValue("resource-id");
            if (id != null && id.contains("addressRow")) {
                // 检查到地址元素列表，开始parse
                List<String> strs = parseNodeTexts(currentElement.elements());
                if (strs.size() > 0) {
                    ShopeeData.AddressListData.Address address = new ShopeeData.AddressListData.Address();
                    address.setName(strs.get(0));
                    if (strs.size() > 1) {
                        address.setPhoneNumber(strs.get(1));
                    }
                    if (strs.size() > 2) {
                        address.setDetailedAddress(strs.get(2));
                    }
                    if (strs.size() > 3) {
                        address.setRegion(strs.get(3));
                    }
                    if (strs.size() > 4) {
                        address.setLabel(strs.get(4));
                    }
                    addressList.add(address);
                }
            }
            Iterator<Element> iterator = currentElement.elementIterator();
            while (iterator.hasNext()) {
                elementStack.push(iterator.next());
            }
        }
        addressListData.setAddressList(addressList);
        return addressListData;
    }

    private ShopeeData.CoinData parseCoinXml(String filePath) throws DocumentException {
        ShopeeData.CoinData coinData = new ShopeeData.CoinData();
        List<ShopeeData.CoinData.Earning> earningList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));
        List<String> strs = parseNodeTexts(document.getRootElement().elements());
        for (int i = 0; i < strs.size(); i++) {
            String str = strs.get(i);
            if (str.contains("Koin") && str.contains("Saya") && i < strs.size() - 1) {
                coinData.setCoinsAvailable(strs.get(i + 1));
            }
            Pattern pattern = Pattern.compile("(\\d+)\\s+(.*?)\\s+(\\d{2}-\\d{2}-\\d{4})");
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                ShopeeData.CoinData.Earning earning = new ShopeeData.CoinData.Earning();
                earning.setCoin(matcher.group(1));
                earning.setDueTime(matcher.group(3));
                earningList.add(earning);
            }
        }
        coinData.setEarningList(earningList);

        return coinData;
    }

    private ShopeeData.UserProfileData parseUserProfileXml(String filePath) throws DocumentException {
        ShopeeData.UserProfileData userProfileData = new ShopeeData.UserProfileData();
        ShopeeData.UserProfileData.UserProfile userProfile = new ShopeeData.UserProfileData.UserProfile();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        Element rootElement = document.getRootElement();
        Stack<Element> elementStack = new Stack<>();
        elementStack.push(rootElement);
        while (!elementStack.isEmpty()) {
            Element currentElement = elementStack.pop();
            String resId = currentElement.attributeValue("resource-id");
            if (resId != null && resId.contains("nick_name_item")) {
                for (int i = 0; i < currentElement.nodeCount(); i++) {
                    if (currentElement.node(i) instanceof Element) {
                        Element element = (Element) currentElement.node(i);
                        if (element.attributeValue("resource-id").contains("subtitle")) {
                            userProfile.setName(element.attributeValue("text"));
                        }
                    }
                }
            }
            if (resId != null && resId.contains("bio_item")) {
                for (int i = 0; i < currentElement.nodeCount(); i++) {
                    if (currentElement.node(i) instanceof Element) {
                        Element element = (Element) currentElement.node(i);
                        if (element.attributeValue("resource-id").contains("subtitle")) {
                            userProfile.setBio(element.attributeValue("text"));
                        }
                    }
                }
            }
            if (resId != null && resId.contains("gender_item")) {
                for (int i = 0; i < currentElement.nodeCount(); i++) {
                    if (currentElement.node(i) instanceof Element) {
                        Element element = (Element) currentElement.node(i);
                        if (element.attributeValue("resource-id").contains("subtitle")) {
                            userProfile.setGender(element.attributeValue("text"));
                        }
                    }
                }
            }
            if (resId != null && resId.contains("birthday_item")) {
                for (int i = 0; i < currentElement.nodeCount(); i++) {
                    if (currentElement.node(i) instanceof Element) {
                        Element element = (Element) currentElement.node(i);
                        if (element.attributeValue("resource-id").contains("subtitle")) {
                            userProfile.setDateOfBirth(element.attributeValue("text"));
                        }
                    }
                }
            }
            if (resId != null && resId.contains("phone_item")) {
                for (int i = 0; i < currentElement.nodeCount(); i++) {
                    if (currentElement.node(i) instanceof Element) {
                        Element element = (Element) currentElement.node(i);
                        if (element.attributeValue("resource-id").contains("subtitle")) {
                            userProfile.setPhoneNumber(element.attributeValue("text"));
                        }
                    }
                }
            }
            if (resId != null && resId.contains("email_item")) {
                for (int i = 0; i < currentElement.nodeCount(); i++) {
                    if (currentElement.node(i) instanceof Element) {
                        Element element = (Element) currentElement.node(i);
                        if (element.attributeValue("resource-id").contains("subtitle")) {
                            userProfile.setEmail(element.attributeValue("text"));
                        }
                    }
                }
            }
            Iterator<Element> iterator = currentElement.elementIterator();
            while (iterator.hasNext()) {
                elementStack.push(iterator.next());
            }
        }
        userProfileData.setUserProfile(userProfile);
        return userProfileData;
    }

    // 遍历所有的ViewGroup，当发现前一个ViewGroup的text 是Rekening Bank的时候，开始读取接下来的子element的每个元素
    private ShopeeData.BankAccountBriefData parsePaymentXml(String filePath) throws DocumentException {
        ShopeeData.BankAccountBriefData bankAccountBriefData = new ShopeeData.BankAccountBriefData();
        List<ShopeeData.BankAccountBriefData.BankAccountBrief> bankAccountBriefs = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        Stack<Element> stack = new Stack<>();
        stack.push(document.getRootElement());
        boolean foundRekeningBankStart = false;
        boolean foundRekeningBankEnd = false;

        while (!stack.isEmpty()) {
            Element currentElement = stack.pop();

            // 检查当前元素是否是 android.view.ViewGroup；如果是，检查是否到了rekening bank的开始位
            String className = currentElement.attributeValue("class");
            if ("android.view.ViewGroup".equals(className)) {
                List<Element> elements = currentElement.elements();
                String text = "";
                for (Element element : elements) {
                    if (element.attributeValue("class").equals("android.widget.TextView")) {
                        text = element.attributeValue("text");
                    }
                }
                if (text.equals("Rekening Bank")) {
                    foundRekeningBankStart = true;
                }
                if (text.equals("Tambah Rekening Bank")) {
                    foundRekeningBankEnd = true;
                }
            }

            // 当已经找到了rekening bank start且没end，对此时遇到的每一个ViewGroup，读取他下面的所有text并写入bankAccountBrief
            if (foundRekeningBankStart && !foundRekeningBankEnd && className.equals("android.view.ViewGroup")) {
                List<String> strs = parseNodeTexts(currentElement.elements());
                if (strs.size() > 0 && !strs.get(0).contains("Rekening")) {
                    // 移除包含Tambah Rekening Bank的外层元素
                    boolean isOuterElement = false;
                    for (String str : strs) {
                        if (str.equalsIgnoreCase("Tambah Rekening Bank")) {
                            isOuterElement = true;
                            break;
                        }
                    }
                    // 移除所有Telah Ditambahkan（xml不可见但确实有）
                    Iterator<String> iterator = strs.iterator();
                    while (iterator.hasNext()) {
                        String str = iterator.next();
                        if (str.equalsIgnoreCase("Telah Ditambahkan")) {
                            iterator.remove();
                        }
                    }
                    // 非最外层，且元素数至少要>1个（银行名和卡号），才会被加入
                    if (!isOuterElement && strs.size() > 1) {
                        ShopeeData.BankAccountBriefData.BankAccountBrief brief = new ShopeeData.BankAccountBriefData.BankAccountBrief();
                        brief.setBankName(strs.get(0));
                        brief.setCardNo(strs.get(1));
                        if (strs.size() > 2) {
                            brief.setLabel(strs.get(2));
                        }
                        bankAccountBriefs.add(brief);
                    }
                }
            }

            // 将所有子元素加入栈
            for (int i = currentElement.nodeCount() - 1; i >= 0; i--) {
                if (currentElement.node(i) instanceof Element) {
                    stack.push((Element) currentElement.node(i));
                }
            }
        }

        // Added 250520: 去重逻辑
        List<ShopeeData.BankAccountBriefData.BankAccountBrief> bankAccountNonDupBriefs = new ArrayList<>();
        for (ShopeeData.BankAccountBriefData.BankAccountBrief brief : bankAccountBriefs) {
            boolean alreadyExisted = false;
            for (ShopeeData.BankAccountBriefData.BankAccountBrief existingBrief : bankAccountNonDupBriefs) {
                if (compareBankAccountBrief(brief, existingBrief)) {
                    alreadyExisted = true;
                    break;
                }
            }
            if (!alreadyExisted) {
                bankAccountNonDupBriefs.add(brief);
            }
        }

        bankAccountBriefData.setBankAccountBrief(bankAccountNonDupBriefs);
        return bankAccountBriefData;
    }

    private boolean compareBankAccountBrief(ShopeeData.BankAccountBriefData.BankAccountBrief briefA, ShopeeData.BankAccountBriefData.BankAccountBrief briefB) {
        if (briefA == null && briefB == null) {
            return true;
        }
        if (briefA == null || briefB == null) {
            return false;
        }
        if (StringUtils.isEmpty(briefA.getCardNo())) {
            briefA.setCardNo("");
        }
        if (StringUtils.isEmpty(briefB.getCardNo())) {
            briefB.setCardNo("");
        }
        if (StringUtils.isEmpty(briefA.getBankName())) {
            briefA.setBankName("");
        }
        if (StringUtils.isEmpty(briefB.getBankName())) {
            briefB.setBankName("");
        }
        if (StringUtils.isEmpty(briefA.getLabel())) {
            briefA.setLabel("");
        }
        if (StringUtils.isEmpty(briefB.getLabel())) {
            briefB.setLabel("");
        }
        return briefA.getBankName().equals(briefB.getBankName()) && briefA.getLabel().equals(briefB.getLabel()) && briefA.getCardNo().equals(briefB.getCardNo());
    }

    @Data
    private static class PurchaseAssignInfo {
        @JsonProperty("orderNumber")
        private String orderNumber;

        @JsonProperty("merchantName")
        private boolean merchantName;

        @JsonProperty("orderStatus")
        private boolean orderStatus;

        @JsonProperty("productName")
        private boolean productName;

        // productSpec本来就没有，可以不检查
        @JsonProperty("productSpec")
        private boolean productSpec;

        @JsonProperty("quantity")
        private boolean quantity;

        @JsonProperty("originalPrice")
        private boolean originalPrice;

        @JsonProperty("discountedPrice")
        private boolean discountedPrice;

        @JsonProperty("quantity_1")
        private boolean quantity_1;

        @JsonProperty("totalPrice")
        private boolean totalPrice;
    }

    private ShopeeData.PurchaseListData parsePurchaseXml(String filePath) throws DocumentException {
        ShopeeData.PurchaseListData purchaseListData = new ShopeeData.PurchaseListData();
        List<ShopeeData.PurchaseListData.Purchase> purchaseList = new ArrayList<>();
        List<Element> purchaseElements = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));
        Element rootElement = document.getRootElement();
        // 首先找到想要的elements
        Stack<Element> elementStack = new Stack<>();
        elementStack.push(rootElement);
        while (!elementStack.isEmpty()) {
            Element currentElement = elementStack.pop();
            String id = currentElement.attributeValue("resource-id");
            if (id != null && id.contains("labelShopName")) {
                purchaseElements.add(currentElement);
            }
            Iterator<Element> iterator = currentElement.elementIterator();
            while (iterator.hasNext()) {
                elementStack.push(iterator.next());
            }
        }
        Collections.reverse(purchaseElements);

        // 初始化purchase及对应assignInfo
        List<PurchaseAssignInfo> purchaseAssignInfoList = new ArrayList<>();
        for (int i = 0; i < purchaseElements.size(); i++) {
            ShopeeData.PurchaseListData.Purchase purchase = new ShopeeData.PurchaseListData.Purchase();
            purchase.setOrderNumber(String.valueOf(i));
            purchaseList.add(purchase);

            PurchaseAssignInfo purchaseAssignInfo = new PurchaseAssignInfo();
            purchaseAssignInfo.setOrderNumber(String.valueOf(i));
            purchaseAssignInfoList.add(purchaseAssignInfo);
        }

        // 查element parent，当前dom要求连续查三次parent；然后查询这个element下所有的元素
        for (int i = 0; i < purchaseElements.size(); i++) {
            ShopeeData.PurchaseListData.Purchase currentPurchase = purchaseList.get(i);
            PurchaseAssignInfo currentPurchaseAssignInfo = purchaseAssignInfoList.get(i);

            Element parentNode = purchaseElements.get(i).getParent().getParent().getParent();
            Stack<Element> purchaseElementStack = new Stack<>();
            purchaseElementStack.push(parentNode);
            while (!purchaseElementStack.isEmpty()) {
                Element currentElement = purchaseElementStack.pop();
                String resId = currentElement.attributeValue("resource-id");
                if (resId != null && resId.contains("labelShopName")) {
                    currentPurchase.setMerchantName(currentElement.attributeValue("text"));
                    currentPurchaseAssignInfo.setMerchantName(true);
                }
                if (resId != null && resId.contains("labelOrderStatus")) {
                    currentPurchase.setOrderStatus(currentElement.attributeValue("text"));
                    currentPurchaseAssignInfo.setOrderStatus(true);
                }
                if (resId != null && resId.contains("labelItemName")) {
                    currentPurchase.setProductName(currentElement.attributeValue("text"));
                    currentPurchaseAssignInfo.setProductName(true);
                }
                if (resId != null && resId.contains("labelItemVariation")) {
                    currentPurchase.setProductSpec(currentElement.attributeValue("text"));
                    currentPurchaseAssignInfo.setProductSpec(true);
                }
                if (resId != null && resId.contains("labelItemPriceBeforeDiscount")) {
                    currentPurchase.setOriginalPrice(currentElement.attributeValue("text"));
                    currentPurchaseAssignInfo.setOriginalPrice(true);
                }
                if (resId != null && resId.contains("labelItemQty")) {
                    currentPurchase.setQuantity(currentElement.attributeValue("text"));
                    currentPurchaseAssignInfo.setQuantity(true);
                }
                if (resId != null && resId.equals("labelItemPrice")) {
                    currentPurchase.setDiscountedPrice(currentElement.attributeValue("text"));
                    currentPurchaseAssignInfo.setDiscountedPrice(true);
                }
                // 处理新版通过sectionItemInfo来解决的
                if (resId != null && resId.contains("sectionItemInfo")) {
                    List<String> strs = parseNodeTextsWithoutResourceId(currentElement.elements());
                    if (strs.size() > 0) {
                        currentPurchase.setProductName(strs.get(0));
                        currentPurchaseAssignInfo.setProductName(true);
                    }
                    if (strs.size() > 1) {
                        currentPurchase.setProductSpec(strs.get(1));
                        currentPurchaseAssignInfo.setProductSpec(true);
                    }
                }
                Iterator<Element> iterator = currentElement.elementIterator();
                while (iterator.hasNext()) {
                    purchaseElementStack.push(iterator.next());
                }
            }
        }
        // 查询total product和cost
        for (int i = 0; i < purchaseElements.size(); i++) {
            ShopeeData.PurchaseListData.Purchase currentPurchase = purchaseList.get(i);
            PurchaseAssignInfo currentPurchaseAssignInfo = purchaseAssignInfoList.get(i);

            Element parentNode = purchaseElements.get(i).getParent().getParent().getParent();
            for (Element element : parentNode.elements()) {
                String text = element.attributeValue("text");
                if (text.contains("Total")) {
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        currentPurchase.setQuantity_1(matcher.group());
                        currentPurchaseAssignInfo.setQuantity_1(true);
                    }
                } else if (!text.trim().isEmpty()) {
                    currentPurchase.setTotalPrice(text.trim());
                    currentPurchaseAssignInfo.setTotalPrice(true);
                }
            }
        }
        // Added 250519: 对于没有被assign完整数据的，移出
        List<ShopeeData.PurchaseListData.Purchase> concretePurchaseList = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < purchaseList.size(); i++) {
            ShopeeData.PurchaseListData.Purchase currentPurchase = purchaseList.get(i);
            PurchaseAssignInfo currentPurchaseAssignInfo = purchaseAssignInfoList.get(i);
            if (currentPurchaseAssignInfo.merchantName && currentPurchaseAssignInfo.orderStatus
                    && currentPurchaseAssignInfo.quantity_1 && currentPurchaseAssignInfo.totalPrice) {
                // 必须全有才会添加
                ShopeeData.PurchaseListData.Purchase purchase = new ShopeeData.PurchaseListData.Purchase();
                purchase.setOrderNumber(String.valueOf(index));
                purchase.setMerchantName(currentPurchase.getMerchantName());
                purchase.setOrderStatus(currentPurchase.getOrderStatus());
                purchase.setProductName(currentPurchase.getProductName());
                purchase.setProductSpec(currentPurchase.getProductSpec());
                purchase.setQuantity(currentPurchase.getQuantity());
                purchase.setOriginalPrice(currentPurchase.getOriginalPrice());
                purchase.setDiscountedPrice(currentPurchase.getDiscountedPrice());
                purchase.setQuantity_1(currentPurchase.getQuantity_1());
                purchase.setTotalPrice(currentPurchase.getTotalPrice());
                concretePurchaseList.add(purchase);
                index++;
            }
        }

        purchaseListData.setPurchaseList(concretePurchaseList);
        return purchaseListData;
    }

    public static void main(String[] args) {
        ShopeeXmlParser parser = new ShopeeXmlParser();
        String addressJson = parser.parseAddressXmlToJson("", "D:\\data\\file\\shopee\\shopee-address.xml").getData();
        System.out.println(addressJson);
        String earningJson = parser.parseCoinXmlToJson("", "D:\\data\\file\\shopee\\shopee-coin.xml").getData();
        System.out.println(earningJson);
        String userProfileJson = parser.parseProfileXmlToJson("", "D:\\data\\file\\shopee\\shopee-profile.xml").getData();
        System.out.println(userProfileJson);
        String bankBriefJson = parser.parsePaymentXmlToJson("", "D:\\data\\file\\shopee\\shopee-payment.xml").getData();
        System.out.println(bankBriefJson);
        String purchaseJson = parser.parsePurchaseXmlToJson("", "D:\\data\\file\\shopee\\shopee-purchaseList.xml").getData();
        System.out.println(purchaseJson);
        for (int i = 1; i < 24; i++) {
            String purchaseJsonI = parser.parsePurchaseXmlToJson("", "D:\\data\\file\\shopee\\app-shopee-info_purchase_list_" + String.valueOf(i) + ".xml").getData();
            System.out.println(purchaseJsonI);
        }
        String bankBriefJson2 = parser.parsePaymentXmlToJson("", "D:\\data\\file\\shopee\\app-shopee-info_payment.xml").getData();
        System.out.println(bankBriefJson2);
    }
}

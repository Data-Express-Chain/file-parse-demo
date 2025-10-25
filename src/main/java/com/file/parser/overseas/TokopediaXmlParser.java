package com.file.parser.overseas;

import com.file.bo.ResponseData;
import com.file.bo.overseas.TokopediaData;
import com.file.constant.ErrorCode;
import com.file.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TokopediaXmlParser {

    public ResponseData<String> parseTokopediaXmlToJson(String daId, String filePath) {
        log.info("parseTokopediaXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json = null;

        try {
            if (filePath.contains("profile")) {
                TokopediaData.UserProfileData userProfileData = parseProfileXml(filePath);
                json = JsonUtils.convertObjectToJson(userProfileData);
            } else if (filePath.contains("address")) {
                TokopediaData.AddressesData addressesData = parseAddressXml(filePath);
                json = JsonUtils.convertObjectToJson(addressesData);
            } else if (filePath.contains("bank")) {
                TokopediaData.BankAccountsData bankAccountsData = parseBankXml(filePath);
                json = JsonUtils.convertObjectToJson(bankAccountsData);
            } else if (filePath.contains("transactions")) {
                TokopediaData.OrderListData orderListData = parseTransactionXml(filePath);
                json = JsonUtils.convertObjectToJson(orderListData);
            } else {
                throw new RuntimeException("the file name is not supported");
            }
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseTokopediaXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(), ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseTokopediaXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    private TokopediaData.UserProfileData parseProfileXml(String filePath) throws DocumentException {
        TokopediaData.UserProfileData userProfileData = new TokopediaData.UserProfileData();
        TokopediaData.UserProfileData.UserProfile userProfile = new TokopediaData.UserProfileData.UserProfile();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        String fullName = getTextByLabelXPath(document, "Nama");
        if (fullName != null) {
            userProfile.setFullName(fullName);
        }
        String email = getTextByLabelXPath(document, "Email");
        if (email != null) {
            userProfile.setEmail(email);
        }
        String phone = getTextByLabelXPath(document, "Nomor HP");
        if (phone != null) {
            userProfile.setPhone(phone);
        }
        String gender = getTextByLabelXPath(document, "Jenis Kelamin");
        if (gender != null) {
            userProfile.setGender(gender);
        }
        String birthday = getTextByLabelXPath(document, "Tanggal Lahir");
        if (birthday != null) {
            userProfile.setBirthday(birthday);
        }

        userProfileData.setUserProfile(userProfile);
        return userProfileData;
    }

    private String getTextByLabelXPath(Document document, String label) {
        try {
            // 直接使用XPath定位到目标文本
            String xpath = "//android.widget.TextView[@text='" + label + "']/following-sibling::android.view.View[1]//android.widget.TextView";
            Node targetNode = document.selectSingleNode(xpath);

            if (targetNode != null) {
                Element targetElement = (Element) targetNode;
                return targetElement.attributeValue("text");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private TokopediaData.AddressesData parseAddressXml(String filePath) throws DocumentException {
        TokopediaData.AddressesData addressesData = new TokopediaData.AddressesData();
        List<TokopediaData.AddressesData.AddressData> addressList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        // 查找所有card_address元素
        List<Node> cardAddressNodes = document.selectNodes("//*[contains(@resource-id, 'card_address')]");
        for (Node cardAddress : cardAddressNodes) {
            Node viewGroupNode = cardAddress.selectSingleNode("./*[@index='0' and @class='android.view.ViewGroup']");
            Element viewGroupElement = (Element) viewGroupNode;
            if (viewGroupElement == null) continue;
            TokopediaData.AddressesData.AddressData addressData = new TokopediaData.AddressesData.AddressData();
            for (Element sibling : viewGroupElement.elements()) {
                String resId = sibling.attributeValue("resource-id");
                String text = sibling.attributeValue("text");
                if (resId != null && resId.contains("address_name")) {
                    addressData.setLabel(text);
                } else if (resId != null && resId.contains("receiver_name")) {
                    addressData.setRecipient(text);
                } else if (resId != null && resId.contains("receiver_phone")) {
                    addressData.setPhone(text);
                } else if (resId != null && resId.contains("address_detail")) {
                    addressData.setFullAddress(text);
                } else if  (resId != null && resId.contains("lbl_main_address")) {
                    addressData.setIsMain(true);
                } else if (resId != null && resId.contains("tv_pinpoint_state")) {
                    addressData.setIsPinpoint(text.equals("Sudah Pinpoint"));
                }
            }
            // 数据完整才加入
            if (addressData.getFullAddress() != null) {
                addressList.add(addressData);
            }
        }

        addressesData.setAddresses(addressList);
        return addressesData;
    }

    private TokopediaData.BankAccountsData parseBankXml(String filePath) throws DocumentException {
        TokopediaData.BankAccountsData bankAccountsData = new TokopediaData.BankAccountsData();
        List<TokopediaData.BankAccountsData.BankAccountData> bankAccountList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        // 查找所有bank_logo元素
        List<Node> bankLogoNode = document.selectNodes("//android.widget.Image[@text='Bank Logo']");
        for (Node logoNode : bankLogoNode) {
            Element logoElement = (Element) logoNode;
            // 获取父节点后面的三个兄弟节点
            String xpath = logoElement.getPath() + "/../following-sibling::*[position()<=3]";
            List<Node> bankCardNodes = document.selectNodes(xpath);
            if (bankCardNodes.size() < 3) continue;

            TokopediaData.BankAccountsData.BankAccountData bankAccountData = new TokopediaData.BankAccountsData.BankAccountData();
            Element bankNameElement = (Element) bankCardNodes.get(0);
            bankAccountData.setBankName(bankNameElement.attributeValue("text"));
            Element accountNumberElement = (Element) bankCardNodes.get(1);
            bankAccountData.setAccountNumber(accountNumberElement.attributeValue("text"));
            Element accountHolderElement = (Element) bankCardNodes.get(2);
            bankAccountData.setAccountHolder(accountHolderElement.attributeValue("text").replace("a.n ", ""));
            bankAccountList.add(bankAccountData);
        }

        bankAccountsData.setBankAccounts(bankAccountList);
        return bankAccountsData;
    }

    private TokopediaData.OrderListData parseTransactionXml(String filePath) throws DocumentException {
        TokopediaData.OrderListData orderListData = new TokopediaData.OrderListData();
        List<TokopediaData.OrderListData.OrderData> orderList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        // 查找所有product_data元素
        List<Node> productDataNodes = document.selectNodes("//*[contains(@resource-id, 'cl_data_product')]");
        for (Node productDataNode : productDataNodes) {
            Element productDataElement = (Element) productDataNode;
            TokopediaData.OrderListData.OrderData orderData = new TokopediaData.OrderListData.OrderData();
            for (Element sibling : productDataElement.elements()) {
                String resId = sibling.attributeValue("resource-id");
                String text = sibling.attributeValue("text");
                if (resId != null && resId.contains("tv_uoh_date")) {
                    orderData.setDate(text);
                } else if (resId != null && resId.contains("label_uoh_order")) {
                    orderData.setStatus(text);
                } else if (resId != null && resId.contains("rl_uoh_product")) {
                    // 产品信息
                    for (Element productSibling : sibling.elements()) {
                        String productResId = productSibling.attributeValue("resource-id");
                        String productText = productSibling.attributeValue("text");
                        if (productResId != null && productResId.contains("tv_uoh_product_name")) {
                            orderData.setProduct(productText);
                        } else if (productResId != null && productResId.contains("tv_uoh_product_desc")) {
                            orderData.setQty(productText.replace(" barang", ""));
                        }
                    }
                } else if (resId != null && resId.contains("containerButtons")) {
                   // 价格信息
                    for (Element priceSibling : sibling.elements()) {
                        if (priceSibling.attributeValue("index").equals("0")) {
                            for (Element priceChild : priceSibling.elements()) {
                                String priceChildResId = priceChild.attributeValue("resource-id");
                                String priceChildText = priceChild.attributeValue("text");
                                if (priceChildResId != null && priceChildResId.contains("tv_uoh_total_belanja_value")) {
                                    orderData.setTotal(priceChildText);
                                }
                            }
                        }
                    }
                }
            }
            // 数据完整才加入
            if (orderData.getDate() != null && orderData.getTotal() != null) {
                orderList.add(orderData);
            }
        }

        orderListData.setOrderList(orderList);
        return orderListData;
    }

    public static void main(String[] args) {
        TokopediaXmlParser xmlParser = new TokopediaXmlParser();
        String folder = "xml";
        String userProfileJson = xmlParser.parseTokopediaXmlToJson("", "D:\\data\\file\\tokopedia\\"+ folder + "\\hwapp-tokopedia-info_profile.xml").getData();
        System.out.println(userProfileJson);

        String addressJson = xmlParser.parseTokopediaXmlToJson("", "D:\\data\\file\\tokopedia\\"+ folder +"\\hwapp-tokopedia-info_address.xml").getData();
        System.out.println(addressJson);

        String bankJson = xmlParser.parseTokopediaXmlToJson("", "D:\\data\\file\\tokopedia\\"+ folder +"\\hwapp-tokopedia-info_bank.xml").getData();
        System.out.println(bankJson);

        //String transactionJson = xmlParser.parseTokopediaXmlToJson("", "D:\\data\\file\\tokopedia\\"+ folder +"\\hwapp-tokopedia-info_transactions.xml").getData();
        //System.out.println(transactionJson);

        for (int i=0; i<5; i++) {
            //String json = xmlParser.parseTokopediaXmlToJson("", "D:\\data\\file\\tokopedia\\"+ folder +"\\hwapp-tokopedia-info_transactions_"+ String.valueOf(i) + ".xml").getData();
            //System.out.println(json);
        }
    }
}

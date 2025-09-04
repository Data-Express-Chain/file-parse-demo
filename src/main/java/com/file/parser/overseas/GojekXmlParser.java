package com.file.parser.overseas;

import com.file.bo.ResponseData;
import com.file.bo.overseas.GojekData;
import com.file.constant.ErrorCode;
import com.file.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

@Slf4j
public class GojekXmlParser {

    public ResponseData<String> parseGojekXmlToJson(String daId, String filePath) {
        log.info("parseGojekXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json = null;

        try {
            if (filePath.contains("profile")) {
                GojekData.UserProfileData data = parseUserProfileXml(filePath);
                json = JsonUtils.convertObjectToJson(data);
            } else if (filePath.contains("transactions")) {
                GojekData.TransactionListData data = parseTransactionXml(filePath);
                json = JsonUtils.convertObjectToJson(data);
            } else {
                throw new RuntimeException("the file name is not supported");
            }
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseGojekXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(), ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseGojekXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    public ResponseData<String> parseProfileXmlToJson(String daId, String filePath) {
        log.info("parseProfileXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json = null;

        try {
            GojekData.UserProfileData userProfileData = parseUserProfileXml(filePath);
            json = JsonUtils.convertObjectToJson(userProfileData);
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseProfileXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(), ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseProfileXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    public ResponseData<String> parseTransactionXmlToJson(String daId, String filePath) {
        log.info("parseTransactionXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json = null;

        try {
            GojekData.TransactionListData transactionListData = parseTransactionXml(filePath);
            json = JsonUtils.convertObjectToJson(transactionListData);
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseTransactionXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(), ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseTransactionXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    private GojekData.UserProfileData parseUserProfileXml(String filePath) throws DocumentException {
        GojekData.UserProfileData userProfileData = new GojekData.UserProfileData();
        GojekData.UserProfileData.UserProfile userProfile = new GojekData.UserProfileData.UserProfile();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        Element rootElement = document.getRootElement();
        Stack<Element> elementStack = new Stack<>();
        elementStack.push(rootElement);
        while (!elementStack.isEmpty()) {
            Element currentElement = elementStack.pop();
            String resId = currentElement.attributeValue("resource-id");
            if (resId != null && resId.contains("tv_phone")) {
                userProfile.setPhoneNumber(currentElement.attributeValue("text"));
                Element parent = currentElement.getParent();
                // 获取所有同级元素
                List<Element> siblings = parent.elements();
                for (Element sibling : siblings) {
                    if (sibling.attributeValue("class").equals("android.widget.TextView")) {
                        if (sibling.attributeValue("index").equals("1")) {
                            userProfile.setName(sibling.attributeValue("text"));
                        }
                        if (sibling.attributeValue("index").equals("2")) {
                            userProfile.setEmail(sibling.attributeValue("text"));
                        }
                    }
                }
                break;
            }
            Iterator<Element> iterator = currentElement.elementIterator();
            while (iterator.hasNext()) {
                elementStack.push(iterator.next());
            }
        }

        userProfileData.setUserProfile(userProfile);
        return userProfileData;
    }

    private GojekData.TransactionListData parseTransactionXml(String filePath) throws DocumentException {
        GojekData.TransactionListData transactionListData = new GojekData.TransactionListData();
        List<GojekData.TransactionListData.TransactionData> transactionList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        Element rootElement = document.getRootElement();
        Stack<Element> elementStack = new Stack<>();
        elementStack.push(rootElement);
        Element transactionListElement = null;
        while (!elementStack.isEmpty()) {
            Element currentElement = elementStack.pop();
            String resId = currentElement.attributeValue("resource-id");
            if (resId != null && resId.contains("tvAmount")) {
                transactionListElement = currentElement.getParent().getParent();
                break;
            }
            Iterator<Element> iterator = currentElement.elementIterator();
            while (iterator.hasNext()) {
                elementStack.push(iterator.next());
            }
        }

        if (transactionListElement != null) {
            GojekData.TransactionListData.TransactionData transactionData = null;
            List<GojekData.TransactionListData.TransactionDetail> transactionDetails = new ArrayList<>();
            List<Element> transactions = transactionListElement.elements();
            for (Element transaction : transactions) {
                String transactionType = transaction.attributeValue("class");
                if (transactionType.equals("android.widget.TextView")) {
                    if (transactionData != null && transactionData.getTransactionDetails() != null) {
                        transactionList.add(transactionData);
                        transactionListData.setTransactions(transactionList);
                    }
                    transactionData = new GojekData.TransactionListData.TransactionData();
                    transactionData.setDate(transaction.attributeValue("text"));
                    transactionDetails = new ArrayList<>();
                } else if (transactionType.equals("android.view.ViewGroup")) {
                    List<Element> detailElements = transaction.elements();
                    String description = "";
                    String amount = "";
                    String method = "";
                    String ext = "";
                    for (Element detailElement : detailElements) {
                        String resId = detailElement.attributeValue("resource-id");
                        if (resId != null && resId.contains("tvDescription")) {
                            description = detailElement.attributeValue("text");
                        } else if (resId != null && resId.contains("tvAmount")) {
                            amount = detailElement.attributeValue("text");
                        } else if (resId != null && resId.contains("tvNotes")) {
                            ext = detailElement.attributeValue("text");
                        } else if (resId != null && resId.contains("rvPaymentMethods")) {
                            List<Element> childElements = getAllChildElements(detailElement);
                            for (Element childElement : childElements) {
                                String childClass = childElement.attributeValue("class");
                                if (childClass.equals("android.widget.TextView")) {
                                    method = childElement.attributeValue("text");
                                }
                            }
                        }
                    }
                    // 都不为空才是有用的数据
                    if (StringUtils.isNotBlank(description) && StringUtils.isNotBlank(amount) && StringUtils.isNotBlank(method)) {
                        GojekData.TransactionListData.TransactionDetail transactionDetail = new GojekData.TransactionListData.TransactionDetail();
                        transactionDetail.setDescription(description);
                        transactionDetail.setAmount(amount);
                        transactionDetail.setMethod(method);
                        transactionDetail.setExt(ext);
                        transactionDetails.add(transactionDetail);
                        if (transactionData != null) {
                            transactionData.setTransactionDetails(transactionDetails);
                        }
                    }
                }
            }

            // 最后数据一个也要加进来
            if (transactionData != null && transactionData.getTransactionDetails() != null) {
                transactionList.add(transactionData);
                transactionListData.setTransactions(transactionList);
            }
        }

        return transactionListData;
    }

    private List<Element> getAllChildElements(Element element) {
        List<Element> allElements = new ArrayList<>();
        // 遍历所有直接子元素
        for (Element child : element.elements()) {
            // 添加子元素
            allElements.add(child);
            // 递归调用获取所有子元素的子元素
            allElements.addAll(getAllChildElements(child));
        }
        return allElements;
    }

    public static void main(String[] args) {
        GojekXmlParser xmlParser = new GojekXmlParser();
        String userProfileJson = xmlParser.parseProfileXmlToJson("", "D:\\data\\file\\gojek\\hwapp-gojek-info_profile.xml").getData();
        System.out.println(userProfileJson);

        String transactionJson = xmlParser.parseTransactionXmlToJson("", "D:\\data\\file\\gojek\\hwapp-gojek-info_transactions.xml").getData();
        System.out.println(transactionJson);

        for (int i=0; i<3; i++) {
            String json = xmlParser.parseTransactionXmlToJson("", "D:\\data\\file\\gojek\\hwapp-gojek-info_transactions_"+ String.valueOf(i) + ".xml").getData();
            System.out.println(json);
        }
    }
}

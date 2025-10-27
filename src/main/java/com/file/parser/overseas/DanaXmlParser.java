package com.file.parser.overseas;

import com.file.bo.ResponseData;
import com.file.bo.overseas.DanaData;
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
public class DanaXmlParser {

    public ResponseData<String> parseDanaXmlToJson(String daId, String filePath) {
        log.info("parseDanaXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json = null;

        try {
            if (filePath.contains("profile")) {
                DanaData.UserProfileData profile = parseProfileXml(filePath);
                json = JsonUtils.convertObjectToJson(profile);
            } else if (filePath.contains("activities")) {
                DanaData.TransactionListData transactionListData = parseTransactionListXml(filePath);
                json = JsonUtils.convertObjectToJson(transactionListData);
            } else {
                throw new RuntimeException("the file name is not supported");
            }
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseDanaXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(), ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseDanaXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    private DanaData.UserProfileData parseProfileXml(String filePath) throws DocumentException {
        DanaData.UserProfileData userProfileData = new DanaData.UserProfileData();
        DanaData.UserProfileData.DanaProfile danaProfile = new DanaData.UserProfileData.DanaProfile();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        // 查找Real Name
        Node nameNode = document.selectSingleNode("//android.widget.TextView[@text='Real Name']/following-sibling::android.widget.TextView[1]");
        if (nameNode != null) {
            Element nameElement = (Element) nameNode;
            danaProfile.setName(nameElement.attributeValue("text"));
        }
        // 查找Email
        Node emailNode = document.selectSingleNode("//android.widget.TextView[@text='Email Address']/following-sibling::android.widget.TextView[1]");
        if (emailNode != null) {
            Element emailElement = (Element) emailNode;
            danaProfile.setEmail(emailElement.attributeValue("text"));
        }
        // 查找Mobile Number
        Node mobileNode = document.selectSingleNode("//android.widget.TextView[@text='Change Mobile Number']/following-sibling::android.widget.TextView[1]");
        if (mobileNode != null) {
            Element mobileElement = (Element) mobileNode;
            danaProfile.setNo(mobileElement.attributeValue("text"));
        }

        userProfileData.setProfile(danaProfile);
        return userProfileData;
    }

    private DanaData.TransactionListData parseTransactionListXml(String filePath) throws DocumentException {
        DanaData.TransactionListData transactionListData = new DanaData.TransactionListData();
        List<DanaData.DanaTran> danaTrans = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        // 查找所有Rp相关元素
        List<Node> rpNodes = document.selectNodes("//android.widget.TextView[contains(@text, 'Rp')]");
        for (Node rpNode : rpNodes) {
            Element rpElement = (Element) rpNode;
            String rpText = rpElement.attributeValue("text");

            DanaData.DanaTran danaTran = new DanaData.DanaTran();
            danaTran.setJumlah(rpText);
            Element parentElement = rpElement.getParent();
            for (Element childElement : parentElement.elements()) {
                if (childElement == rpElement) continue;
                String childClass = childElement.attributeValue("class");
                if (childClass != null && childClass.contains("android.view.View")) {
                    for (Element sibling : childElement.elements()) {
                        String indexString = sibling.attributeValue("index");
                        if (indexString != null && indexString.equals("0")) {
                            String childText = sibling.attributeValue("text");
                            danaTran.setTransaksi(childText);
                        } else if (indexString != null && indexString.equals("1")) {
                            String childText = sibling.attributeValue("text");
                            danaTran.setTanggalWaktu(childText);
                        }
                    }
                } else if (childClass != null && childClass.contains("android.widget.TextView")) {
                    String childText = childElement.attributeValue("text");
                    danaTran.setMetodePembayaran(childText);
                }
            }
            // 数据完整才加入
            if (danaTran.getTransaksi() != null && danaTran.getMetodePembayaran() != null) {
                danaTrans.add(danaTran);
            }
        }

        transactionListData.setTransactionList(danaTrans);
        return transactionListData;
    }

    public static void main(String[] args) {
        DanaXmlParser xmlParser = new DanaXmlParser();
        String userProfileJson = xmlParser.parseDanaXmlToJson("", "D:\\data\\file\\dana\\hwapp-dana-info_profile.xml").getData();
        System.out.println(userProfileJson);

        String transactionJson = xmlParser.parseDanaXmlToJson("", "D:\\data\\file\\dana\\hwapp-dana-info_activities.xml").getData();
        System.out.println(transactionJson);

    }
}

package com.file.parser.overseas;

import com.file.bo.ResponseData;
import com.file.constant.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DanaParser {

    private DanaPdfParser pdfParser = new DanaPdfParser();
    private DanaXmlParser xmlParser = new DanaXmlParser();

    public ResponseData<String> parseDanaToJson(String daId, String filePath) {
        log.info("parseDanaToJson started, daId:{}, filePath:{}", daId, filePath);

        try {
            if (filePath.endsWith(".pdf")) {
                return pdfParser.parseDanaPdfToJson(daId, filePath);
            } else if (filePath.endsWith(".xml")) {
                return xmlParser.parseDanaXmlToJson(daId, filePath);
            } else {
                throw new RuntimeException("the file name is not supported");
            }
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseDanaToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(), ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }
    }

    public static void main(String[] args) {
        DanaParser danaParser = new DanaParser();
        String json = "";
        json = danaParser.parseDanaToJson("", "D:\\data\\file\\dana\\hwapp-dana-info_[1].pdf").getData();
        System.out.println(json);

        json = danaParser.parseDanaToJson("", "D:\\data\\file\\dana\\hwapp-dana-info_profile.xml").getData();
        System.out.println(json);
    }
}

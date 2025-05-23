package com.file.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * xml文件解析器基类
 * @author anyspa
 */

@Slf4j
public class BaseXmlParser {

    public List<String> getNodeTextList(String filePath) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));

        return parseNode(document.getRootElement().elements());
    }

    private List<String> parseNode(List<Element> elements) {
        List<String> nodeTextList = new ArrayList<>();
        elements.forEach(element -> {
            String text = element.attributeValue("text");
            if (StringUtils.isNotBlank(text)) {
                nodeTextList.add(text);
            }
            nodeTextList.addAll(parseNode(element.elements()));
        });

        return nodeTextList;
    }
}

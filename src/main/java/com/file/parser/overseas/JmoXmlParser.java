package com.file.parser.overseas;

import com.file.bo.ResponseData;
import com.file.bo.overseas.JmoData;
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
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JmoXmlParser {

    public ResponseData<String> parsJmoXmlToJson(String daId, String filePath) {
        log.info("parsJmoXmlToJson started, daId:{}", daId);
        String json;


        if (filePath.contains("account")) {
            return parseProfileXmlToJson(daId, filePath);
        } else if (filePath.contains("card")) {
            return parseKartuDigitalXmlsToJson(daId, filePath);
        } else {
            throw new RuntimeException("the file name is not supported");
        }

    }


    public ResponseData<String> parseKartuDigitalXmlsToJson(String daId, String filePaths) {
        log.info("parseKartuDigitalXmlsToJson started, daId:{}, filePath:{}", daId, filePaths);
        String json;

        try {
            JmoData.KartuDigitalData kartuDigitalData = parseKartuDigitalXml(filePaths);
            json = JsonUtils.convertObjectToJson(kartuDigitalData);
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseAddressXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseKartuDigitalXmlsToJson completed, daId:{}, filePath:{}", daId, filePaths);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    private JmoData.KartuDigitalData parseKartuDigitalXml(String filePath) throws DocumentException {
        JmoData.KartuDigitalData kartuDigitalData = new JmoData.KartuDigitalData();
        JmoData.KartuDigital kartuDigital = new JmoData.KartuDigital();
        List<String> kartuDigitalPrograms = new ArrayList<>();
        SAXReader reader = new SAXReader();

        Document document = reader.read(new File(filePath));
        Element rootElement = document.getRootElement();
        List<Element> allResourceIdElems = findAllSubElementsWithPartialResourceId(rootElement.elements(), "com.bpjstku:id");

        for (Element element : allResourceIdElems) {
            String resId = element.attributeValue("resource-id");
            String text = element.attributeValue("text");

            if (resId.contains("tvMembershipStatus") && !resId.contains("Label")) {
                kartuDigital.setStatusKepesertaan(text);
            }
            if (resId.contains("tvInfoSegmentName")) {
                kartuDigital.setSegmenPeserta(text);
            }
            if (resId.contains("tvInfoCompanyName") && !resId.contains("Label")) {
                kartuDigital.setPerusahaanTempatBekerja(text);
            }
            if (resId.contains("tvInfoLastWages") && !resId.contains("Label")) {
                kartuDigital.setUpahTerakhir(text);
            }
            if (resId.contains("tvInfoLastPayment") && !resId.contains("Label") && !resId.contains("Value")) {
                kartuDigital.setPembayaranIuranTerakhir(text);
            }
            if (resId.contains("tvInfoPension") && !resId.contains("Label")) {
                kartuDigital.setTanggalUsiaPensiun(text);
            }
            if (resId.contains("tvPeriodPaymentPension") && !resId.contains("Label")) {
                kartuDigital.setMasaLaluJaminanPensiun(text);
            }
            if (resId.contains("tvBeginingLossJobMember") && !resId.contains("Label") && !resId.contains("Value")) {
                kartuDigital.setTanggalKepesertaanAwalJaminanKehilanganPekerjaa(text);
            }
            if (resId.contains("tvPeriodPaymentlossJob") && !resId.contains("Label")) {
                kartuDigital.setMasaLaluJaminanKehilanganPekerjaan(text);
            }
            if (resId.contains("tvProgramItem")) {
                kartuDigitalPrograms.add(text);
            }
        }

        String allPrograms = String.join(",", kartuDigitalPrograms);
        kartuDigital.setProgramYangDikuti(allPrograms.trim());

        kartuDigitalData.setKartuDigital(kartuDigital);
        return kartuDigitalData;
    }


    private List<Element> findAllSubElementsWithPartialResourceId(List<Element> elements, String partialResId) {
        List<Element> elementList = new ArrayList<>();
        elements.forEach(element -> {
            String resId = element.attributeValue("resource-id");
            if (!StringUtils.isBlank(resId) && resId.contains(partialResId)) {
                elementList.add(element);
            }
            elementList.addAll(findAllSubElementsWithPartialResourceId(element.elements(), partialResId));
        });

        return elementList;
    }

    public List<String> mergeKartuDigitalJsons(List<String> kartuDigitalJsonList) {
        JmoData.KartuDigitalData mergedData = new JmoData.KartuDigitalData();
        JmoData.KartuDigital mergedKartuDigital = new JmoData.KartuDigital();
        List<String> allPrograms = new ArrayList<>();
        List<String> res = new ArrayList<>();
        for (String json : kartuDigitalJsonList) {
            if (json.contains("\"profile\"")) {
                res.add(json);
                continue;
            }
            JmoData.KartuDigitalData data = JsonUtils.convertJsonToObject(json, JmoData.KartuDigitalData.class);
            JmoData.KartuDigital kartuDigital = data.getKartuDigital();

            // 合并 programs
            if (!StringUtils.isBlank(kartuDigital.getProgramYangDikuti())) {
                String[] programs = kartuDigital.getProgramYangDikuti().split(",");
                allPrograms.addAll(Arrays.asList(programs));
            }

            if (!StringUtils.isBlank(kartuDigital.getStatusKepesertaan())) {
                mergedKartuDigital.setStatusKepesertaan(kartuDigital.getStatusKepesertaan());
            }
            if (!StringUtils.isBlank(kartuDigital.getSegmenPeserta())) {
                mergedKartuDigital.setSegmenPeserta(kartuDigital.getSegmenPeserta());
            }
            if (!StringUtils.isBlank(kartuDigital.getPerusahaanTempatBekerja())) {
                mergedKartuDigital.setPerusahaanTempatBekerja(kartuDigital.getPerusahaanTempatBekerja());
            }
            if (!StringUtils.isBlank(kartuDigital.getUpahTerakhir())) {
                mergedKartuDigital.setUpahTerakhir(kartuDigital.getUpahTerakhir());
            }
            if (!StringUtils.isBlank(kartuDigital.getPembayaranIuranTerakhir())) {
                mergedKartuDigital.setPembayaranIuranTerakhir(kartuDigital.getPembayaranIuranTerakhir());
            }
            if (!StringUtils.isBlank(kartuDigital.getTanggalUsiaPensiun())) {
                mergedKartuDigital.setTanggalUsiaPensiun(kartuDigital.getTanggalUsiaPensiun());
            }
            if (!StringUtils.isBlank(kartuDigital.getMasaLaluJaminanPensiun())) {
                mergedKartuDigital.setMasaLaluJaminanPensiun(kartuDigital.getMasaLaluJaminanPensiun());
            }
            if (!StringUtils.isBlank(kartuDigital.getTanggalKepesertaanAwalJaminanKehilanganPekerjaa())) {
                mergedKartuDigital.setTanggalKepesertaanAwalJaminanKehilanganPekerjaa(kartuDigital.getTanggalKepesertaanAwalJaminanKehilanganPekerjaa());
            }
            if (!StringUtils.isBlank(kartuDigital.getMasaLaluJaminanKehilanganPekerjaan())) {
                mergedKartuDigital.setMasaLaluJaminanKehilanganPekerjaan(kartuDigital.getMasaLaluJaminanKehilanganPekerjaan());
            }

        }

        String mergedPrograms = String.join(",", allPrograms);
        mergedKartuDigital.setProgramYangDikuti(mergedPrograms);

        mergedData.setKartuDigital(mergedKartuDigital);
        res.add(JsonUtils.convertObjectToJson(mergedData));
        return res;
    }

    /// ///////////////////////////////////////////////////////////////
    public ResponseData<String> parseProfileXmlToJson(String daId, String filePath) {
        log.info("parseProfileXmlToJson started, daId:{}, filePath:{}", daId, filePath);
        String json;

        try {
            JmoData.ProfileData profileData = parseProfileXml(filePath);
            json = JsonUtils.convertObjectToJson(profileData);
        } catch (Exception e) {
            log.error("|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseAddressXmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseProfileXmlToJson completed, daId:{}, filePath:{}", daId, filePath);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    private JmoData.ProfileData parseProfileXml(String filePath) throws DocumentException {
        JmoData.ProfileData profileData = new JmoData.ProfileData();
        JmoData.Profile profile = new JmoData.Profile();

        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));
        Element rootElement = document.getRootElement();
        List<Element> allResourceIdElems = findAllSubElementsWithPartialResourceId(rootElement.elements(), "com.bpjstku:id");
        for (Element element : allResourceIdElems) {
            String resId = element.attributeValue("resource-id");
            String text = element.attributeValue("text");
            if (resId.contains("tvUserName")) {
                profile.setName(text);
            }
            if (resId.contains("tvUserCategory")) {
                profile.setProgram(text);
            }
            if (resId.contains("tvUserIdCard")) {
                profile.setKptPaspor(text);
            }
            if (resId.contains("tvUserPhoneNumber")) {
                profile.setNormorHp(text);
            }
        }

        profileData.setProfile(profile);
        return profileData;
    }


    public static void main(String[] args) {
        JmoXmlParser parser = new JmoXmlParser();
        List<String> kartuFiles = new ArrayList<>();
        kartuFiles.add("D:\\data\\file\\jmo\\digital-card-1.xml");
        kartuFiles.add("D:\\data\\file\\jmo\\digital-card-2.xml");
        List<String> kartuDigitals = new ArrayList<>();
        kartuFiles.forEach(path -> {
            String res = parser.parseKartuDigitalXmlsToJson("", path).getData();

            System.out.println(res);
            kartuDigitals.add(res);

        });
        String kartuDigitalJson = parser.mergeKartuDigitalJsons(kartuDigitals).get(0);
        System.out.println(kartuDigitalJson);
        String profileJson = parser.parsJmoXmlToJson("", "D:\\data\\file\\jmo\\account.xml").getData();
        System.out.println(profileJson);
    }
}
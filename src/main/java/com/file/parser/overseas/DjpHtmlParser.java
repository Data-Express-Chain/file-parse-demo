package com.file.parser.overseas;

import com.file.bo.ResponseData;
import com.file.bo.overseas.DjpData;
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
public class DjpHtmlParser {

    public ResponseData<String> parseDjpHtmlToJson(String daId, String filePath) {
        log.info("parseDjpHtmlToJson started, daId:{}", daId);
        String json;

        try {
            if (filePath.contains("profile")) {
                DjpData.ProfileData profileData = parseDjpProfileHtml(filePath);
                json = JsonUtils.convertObjectToJson(profileData);
            } else if (filePath.contains("payment")) {
                DjpData.PaymentData paymentData = parseDjpPaymentHtml(filePath);
                json = JsonUtils.convertObjectToJson(paymentData);
            } else if (filePath.contains("declaration")) {
                DjpData.DeclarationData declarationData = parseDjpDeclarationHtml(filePath);
                json = JsonUtils.convertObjectToJson(declarationData);
            } else {
                throw new RuntimeException("the file name is not supported");
            }
        } catch (IOException e) {
            log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseDjpHtmlToJson failed", e);
            return new ResponseData<>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(),
                    ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseDjpHtmlToJson completed, daId:{}, json:{}", daId, json);
        return new ResponseData<>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    public DjpData.DeclarationData parseDjpDeclarationHtml(String filePath) throws IOException {
        DjpData.DeclarationData declarationData = new DjpData.DeclarationData();
        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");

        List<DjpData.RiwayatPelaporan> riwayatPelaporanList = new ArrayList<>();
        Elements rpRows = doc.getElementsByClass("odd");
        Elements rpRows2 = doc.getElementsByClass("even");
        rpRows.addAll(rpRows2);
        for (Element rpItem : rpRows) {
            DjpData.RiwayatPelaporan rp = new DjpData.RiwayatPelaporan();
            Elements rpElements = rpItem.getAllElements();
            if (rpElements.size() <= 1) {
                continue;
            }
            // 处理dummy隐藏属性
            if (rpElements.get(1).text().contains("ditemukan")) {
                continue;
            }
            for (int i = 1; i < rpElements.size(); i++) {
                Element element = rpElements.get(i);
                if (i == 1) {
                    rp.setJenisSpt(element.text());
                }
                if (i == 2) {
                    rp.setTahunMasapajak(element.text());
                }
                if (i == 3) {
                    rp.setPembetulanKe(element.text());
                }
                if (i == 4) {
                    rp.setStatus(element.text());
                }
            }
            riwayatPelaporanList.add(rp);
        }
        declarationData.setRiwayatPelaporan(riwayatPelaporanList);
        return declarationData;
    }

    public DjpData.PaymentData parseDjpPaymentHtml(String filePath) throws IOException {
        DjpData.PaymentData paymentData = new DjpData.PaymentData();
        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");

        List<DjpData.Pembayaran> pembayaranList = new ArrayList<>();
        Elements paymentRows = doc.getElementsByClass("odd");
        Elements paymentRows2 = doc.getElementsByClass("even");
        paymentRows.addAll(paymentRows2);
        for (Element paymentItem : paymentRows) {
            DjpData.Pembayaran pembayaran = new DjpData.Pembayaran();
            Elements paymentElements = paymentItem.getAllElements();
            if (paymentElements.size() <= 1) {
                continue;
            }
            // 处理dummy隐藏属性
            if (paymentElements.get(1).text().contains("ditemukan")) {
                continue;
            }
            for (int i = 1; i < paymentElements.size(); i++) {
                Element element = paymentElements.get(i);
                if (i == 1) {
                    pembayaran.setTahunMasapajak(element.text());
                }
                if (i == 2) {
                    pembayaran.setTanggalBayar(element.text());
                }
                if (i == 3) {
                    pembayaran.setNtpn(element.text());
                }
                if (i == 4) {
                    pembayaran.setNominalBayar(element.text().trim());
                }
            }
            pembayaranList.add(pembayaran);
        }

        paymentData.setPembayaran(pembayaranList);
        return paymentData;
    }

    public DjpData.ProfileData parseDjpProfileHtml(String filePath) throws IOException {
        DjpData.ProfileData profileData = new DjpData.ProfileData();
        File input = new File(filePath);
        Document doc = Jsoup.parse(input, "UTF-8");

        // 并列关系，三个子结构全尝试解析
        DjpData.DataUtama dataUtama = new DjpData.DataUtama();
        Element utamaElement = doc.getElementById("kt_tabs_data_utama");
        if (utamaElement != null) {
            Elements utamaTitleElements = utamaElement.getElementsByClass("kt-font-md");
            Elements utamaContentElements = utamaElement.getElementsByClass("kt-font-bolder");
            for (int i = 0; i < utamaTitleElements.size(); i++) {
                String title = utamaTitleElements.get(i).text();
                String content = utamaContentElements.get(i).text().trim();
                if (title.equalsIgnoreCase("NPWP15")) {
                    dataUtama.setNpwp15(content);
                }
                if (title.equalsIgnoreCase("NITKU")) {
                    dataUtama.setNitku(content);
                }
                if (title.equalsIgnoreCase("NIK/NPWP16")) {
                    dataUtama.setNikNpwp16(content);
                }
                if (title.equalsIgnoreCase("Nama")) {
                    dataUtama.setNama(content);
                }
                if (title.contains("Tempat")) {
                    dataUtama.setTempatLahir(content);
                }
                if (title.contains("Tanggal")) {
                    dataUtama.setTanggalLahir(content);
                }
            }
        }

        DjpData.DataLainnya dataLainnya = new DjpData.DataLainnya();
        Element lainnyaElement = doc.getElementById("kt_tabs_data_lainnya");
        if (lainnyaElement != null) {
            Elements lainnyaTitleElements = lainnyaElement.getElementsByClass("kt-font-md");
            Elements lainnyaContentElements = lainnyaElement.getElementsByClass("kt-font-bolder");
            for (int i = 0; i < lainnyaTitleElements.size(); i++) {
                String title = lainnyaTitleElements.get(i).text();
                String content = lainnyaContentElements.get(i).text().trim();
                if (title.equalsIgnoreCase("Alamat")) {
                    dataLainnya.setAlamat(content);
                }
                if (title.equalsIgnoreCase("Kebangsaan")) {
                    dataLainnya.setKebangsaan(content);
                }
                if (title.contains("Handphone")) {
                    dataLainnya.setHandphone(content);
                }
                if (title.contains("Email")) {
                    dataLainnya.setEmail(content);
                }
            }
        }

        DjpData.AnggotaKeluarga anggotaKeluarga = new DjpData.AnggotaKeluarga();
        Element anggotaElement = doc.getElementById("kt_tabs_family_tax_unit");
        if (anggotaElement != null) {
            Elements anggotaContentElements = anggotaElement.getElementsByClass("odd");
            if (anggotaContentElements.size() > 0) {
                Element anggotaContentElement = anggotaContentElements.get(0);
                Elements allElems = anggotaContentElement.getAllElements();
                if (allElems.size() > 1) {
                    for (int i = 1; i < allElems.size(); i++) {
                        Element element = allElems.get(i);
                        if (i == 1) {
                            anggotaKeluarga.setNoKK(element.text());
                        }
                        if (i == 2) {
                            anggotaKeluarga.setNik(element.text());
                        }
                        if (i == 3) {
                            anggotaKeluarga.setNama(element.text());
                        }
                        if (i == 4) {
                            anggotaKeluarga.setTempatLahir(element.text());
                        }
                        if (i == 5) {
                            anggotaKeluarga.setTglLahir(element.text());
                        }
                        if (i == 6) {
                            anggotaKeluarga.setStatusHubKeluarga(element.text());
                        }
                        if (i == 7) {
                            anggotaKeluarga.setPekerjaan(element.text());
                        }
                        if (i == 8) {
                            anggotaKeluarga.setStatus(element.text().trim());
                        }
                    }
                }
            }
        }
        profileData.setDataUtama(dataUtama);
        profileData.setDataLainnya(dataLainnya);
        profileData.setAnggotaKeluarga(anggotaKeluarga);
        return profileData;
    }

    public static void main(String[] args) throws IOException {
        DjpHtmlParser djpHtmlParser = new DjpHtmlParser();
        String json = djpHtmlParser.parseDjpHtmlToJson("", "D:\\data\\file\\chrome-djp-data\\chrome-djp-data_profile.html").getData();
        System.out.println(json);
        json = djpHtmlParser.parseDjpHtmlToJson("", "D:\\data\\file\\chrome-djp-data\\chrome-djp-data_payment.html").getData();
        System.out.println(json);
        json = djpHtmlParser.parseDjpHtmlToJson("", "D:\\data\\file\\chrome-djp-data\\chrome-djp-data_declaration.html").getData();
        System.out.println(json);
    }
}

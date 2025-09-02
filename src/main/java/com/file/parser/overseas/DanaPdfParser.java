package com.file.parser.overseas;

import com.file.bo.ResponseData;
import com.file.bo.overseas.DanaData;
import com.file.bo.overseas.DanaTran;
import com.file.constant.ErrorCode;
import com.file.parser.BasePdfParser;
import com.file.util.JsonUtils;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import technology.tabula.*;
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DanaPdfParser extends BasePdfParser {

    public ResponseData<String> parseDanaPdfToJson(String daId, String filePath) {
        log.info("parseDanaPdfToJson started, daId: {}", daId);
        String json = null;

        try {
            DanaData danaData = parseDanaPdf(filePath);
            json = JsonUtils.convertObjectToJson(danaData);
        } catch (Exception e) {
            log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), daId, "", "", "parseDanaPdfToJson failed", e);
            return new ResponseData<String>(null, ErrorCode.FILE_PARSE_EXCEPTION.getCode(), ErrorCode.FILE_PARSE_EXCEPTION.getMsg());
        }

        log.info("parseDanaPdfToJson completed, daId: {}", daId);
        return new ResponseData<String>(json, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg());
    }

    private DanaData parseDanaPdf(String filePath) {
        DanaData danaData = parseDanaHeader(filePath);

        List<DanaTran> danaTrans = parseDanaTrans(filePath);
        danaData.setDanaTrans(danaTrans);

        return danaData;
    }

    private DanaData parseDanaHeader(String filePath) {
        DanaData danaData = new DanaData();
        String pdfHeaderText = getPdfTextByStripper2(filePath);
        List<List<String>> tranFieldsList = parseTransTextToTranFieldsList2(pdfHeaderText);

        try {
            // 只取前三行数据
            String monthStr = tranFieldsList.get(0).get(0);
            String month = monthStr.substring(monthStr.indexOf("Riwayat Aktivitas") + "Riwayat Aktivitas".length(), monthStr.indexOf("| Halaman 1 dari 1")).trim();
            danaData.setMonth(month);
            String name = tranFieldsList.get(1).get(0);
            danaData.setName(name);
            String tranFields = tranFieldsList.get(2).get(0);
            Pattern pattern = Pattern.compile("\\d{12,}");
            Matcher matcher = pattern.matcher(tranFields);
            if (matcher.find()) {
                String no = matcher.group();
                danaData.setNo(no);
            }
            String diterbitkanPada = tranFields.substring(tranFields.indexOf("Dokumen diterbitkan pada") + "Dokumen diterbitkan pada".length()).trim();
            danaData.setDiterbitkanPada(diterbitkanPada);
        } catch (Exception e) {
            log.error("parseDanaHeader failed", e);
            throw new RuntimeException(e);
        }

        return danaData;
    }

    private List<DanaTran> parseDanaTrans(String filePath) {
        List<DanaTran> danaTrans = new ArrayList<>();

        StringBuilder tanggalWaktu = new StringBuilder();
        StringBuilder transaksi = new StringBuilder();
        StringBuilder metodePembayaran = new StringBuilder();
        StringBuilder jumlah = new StringBuilder();

        // 每行的记录是否读取完成
        Boolean rowResult = false;
        List<List<String>> rowList = parseFileToRowList(filePath);
        for (int i = 0; i < rowList.size(); i++) {
            List<String> row = rowList.get(i);
            if (row.get(0).contains("Tanggal & Waktu") || row.get(0).contains("Tidak ada transaksi di periode ini")) {
                continue;
            }
            List<String> nextStrings = new ArrayList<>();
            if (i < rowList.size() - 1) {
                nextStrings = rowList.get(i + 1);
            }

            if (row.size() == 4) {
                if (isDanaDateFormat(row.get(0))) {
                    //日期多加个空格
                    tanggalWaktu.append(row.get(0)).append(" ");
                } else {
                    tanggalWaktu.append(row.get(0));
                }
                transaksi.append(row.get(1));
                metodePembayaran.append(row.get(2));
                jumlah.append(row.get(3));
            }

            if (nextStrings.isEmpty() || isDanaDateFormat(nextStrings.get(0))) {
                rowResult = true;
            }

            if (rowResult && StringUtils.isNotBlank(tanggalWaktu)) {
                DanaTran danaTran = new DanaTran();
                danaTran.setTanggalWaktu(tanggalWaktu.toString());
                danaTran.setTransaksi(transaksi.toString());
                danaTran.setMetodePembayaran(metodePembayaran.toString());
                danaTran.setJumlah(jumlah.toString());
                danaTrans.add(danaTran);

                tanggalWaktu.setLength(0);
                transaksi.setLength(0);
                metodePembayaran.setLength(0);
                jumlah.setLength(0);
                rowResult = false;
            }
        }
        return danaTrans;
    }

    private List<List<String>> parseFileToRowList(String filePath) {
        //页面所有table的记录，每一个元素代表下面一条记录cellList
        List<List<String>> rowList = new ArrayList<>();

        // 1. 读取文件
        File pdf = new File(filePath);

        // 2. pdfbox读取PDDocument
        try (PDDocument pdfDocument = PDDocument.load(pdf)) {

            // 3. tabula新建ObjectExtractor和NurminenDetectionAlgorithm，同时准备接收表格Rectangle的结构
            ObjectExtractor objectExtractor = new ObjectExtractor(pdfDocument);
            SpreadsheetDetectionAlgorithm detectionAlgorithm = new SpreadsheetDetectionAlgorithm();
            BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();

            // 4. 获取每页的PageIterator
            PageIterator pages = objectExtractor.extract();

            // 5. 解析每页的Rectangle(table的位置)
            while (pages.hasNext()) {
                Page page = pages.next();
                List<Rectangle> tablesOnPage = detectionAlgorithm.detect(page);
                if (tablesOnPage.size() > 0) {
                    Rectangle rectangle = tablesOnPage.get(0);
                    for (Rectangle r : tablesOnPage) {
                        if (r.getHeight() > 0) {
                            rectangle = r;
                            break;
                        }
                    }

                    if (rectangle != null) {
                        float top = findTextBottom(pdfDocument, page.getPageNumber(), "Dokumen diterbitkan pada");
                        rectangle.setTop(top);
                        rectangle.setRight((float) page.getWidth());
                        float bottom = findTextBottom(pdfDocument, page.getPageNumber(), "DANA Indonesia terdaftar");
                        rectangle.setBottom(bottom - 10);

                        Page area = page.getArea(rectangle);
                        List<Table> tableList = null;
                        try {
                            tableList = bea.extract(area);
                        } catch (Exception e) {
                            log.info("rectangle extract table fail.");
                            continue;
                        }
                        for (Table table : tableList) {
                            for (int i = 0; i < table.getRowCount(); i++) {
                                List<String> cellList = new ArrayList<>();
                                for (int j = 0; j < table.getColCount(); j++) {
                                    cellList.add(table.getCell(i, j).getText(false));
                                }
                                rowList.add(cellList);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return rowList;
    }

    private static float findTextBottom(PDDocument document, int pageIndex, String searchText) throws IOException {
        float[] y = new float[1];
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) {
                StringBuilder sb = new StringBuilder();
                for (TextPosition tp : textPositions) {
                    sb.append(tp.getUnicode());
                }
                if (sb.toString().contains(searchText)) {
                    TextPosition tp = textPositions.get(0);
                    y[0] = tp.getY();
                }
            }
        };
        stripper.setSortByPosition(true);
        stripper.setStartPage(pageIndex);
        stripper.setEndPage(pageIndex);
        stripper.getText(document);
        return y[0];
    }

    public List<List<String>> parseTransTextToTranFieldsList2(String transText) {
        // 这个List每个元素代表一条记录的文本字符串
        List<String> tranTextList = Splitter.on(System.getProperty("line.separator", "\n")).splitToList(transText);
        List<List<String>> tranFieldsList = new ArrayList<List<String>>();

        for (int i = 0; i < tranTextList.size() - 1; i++) {
            List<String> tranFields = new ArrayList<>();
            tranFields.add(tranTextList.get(i));
            Collections.addAll(tranFieldsList, tranFields);
        }

        return tranFieldsList;
    }

    // 校验字符串是否为日期格式
    private static boolean isDanaDateFormat(String str) {
        String regex = "^\\d{1,2} (Jan|Feb|Mar|Apr|Mei|Jun|Jul|Agu|Sep|Okt|Nov|Des) \\d{4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static void main(String[] args) {
        DanaPdfParser pdfParser = new DanaPdfParser();

        String json = "";
        json = pdfParser.parseDanaPdfToJson("", "D:\\data\\file\\dana\\hwapp-dana-info_[1].pdf").getData();
        System.out.println(json);

        json = pdfParser.parseDanaPdfToJson("", "D:\\data\\file\\dana\\hwapp-dana-info_[2].pdf").getData();
        System.out.println(json);
    }
}

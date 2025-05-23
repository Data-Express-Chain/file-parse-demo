package com.file.parser;

import com.file.constant.ErrorCode;
import com.file.parser.traprange.PDFTableExtractor;
import com.file.parser.traprange.entity.Table;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PDF格式流水文件解析器基类
 * @author lingfenghe
 *
 */
@Slf4j
public class BasePdfParser {

	public String parsePdfHeaderText(String filePath) {
		String pdfHeaderText = "";
		try (PDDocument pdDocument = PDDocument.load(new File(filePath))) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(true);
			stripper.setStartPage(1);
			stripper.setEndPage(1);
			pdfHeaderText = stripper.getText(pdDocument);
			pdfHeaderText = pdfHeaderText.replace(System.getProperty("line.separator", "\n"), "");
		} catch (Exception e) {
			log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), "", "", "", "parsePdfHeaderText failed", e);
			throw new RuntimeException(e);
		}
		return pdfHeaderText;
	}

	public String parsePdfHeaderText2(String filePath) {
		String pdfHeaderText = "";
		try (PDDocument pdDocument = PDDocument.load(new File(filePath))) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(false);
			stripper.setStartPage(1);
			stripper.setEndPage(1);
			pdfHeaderText = stripper.getText(pdDocument);
			pdfHeaderText = pdfHeaderText.replace(System.getProperty("line.separator", "\n"), "");
		} catch (Exception e) {
			log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), "", "", "", "parsePdfHeaderText2 failed", e);
			throw new RuntimeException(e);
		}
		return pdfHeaderText;
	}

	public List<List<String>> parseTransTextToTranFieldsList(String transText) {
		// 这个List每个元素代表一条记录的文本字符串
		List<String> tranTextList = Splitter.on(System.getProperty("line.separator", "\n")).splitToList(transText);
		List<List<String>> tranFieldsList = new ArrayList<List<String>>();

		for (int i = 0; i < tranTextList.size() - 1; i++) {
			// 每一个wechatTranFieldList代表一条记录的所有字段，一个List一条记录
			List<String> wechatTranFields = Splitter.on(";").splitToList(tranTextList.get(i));
			Collections.addAll(tranFieldsList, wechatTranFields);
		}

		return tranFieldsList;
	}

	public List<List<String>> parseTransTextToTranFieldsList(String transText, String separator) {
		// 这个List每个元素代表一条记录的文本字符串
		List<String> tranTextList = Splitter.on(System.getProperty("line.separator", "\n")).splitToList(transText);
		List<List<String>> tranFieldsList = new ArrayList<List<String>>();

		for (int i = 0; i < tranTextList.size() - 1; i++) {
			// 每一个wechatTranFieldList代表一条记录的所有字段，一个List一条记录
			List<String> wechatTranFields = Splitter.on(separator).splitToList(tranTextList.get(i));
			Collections.addAll(tranFieldsList, wechatTranFields);
		}

		return tranFieldsList;
	}

	/**
	 * 将pdf每一行的记录转化为一行一行的text字符串
	 *
	 * @param extractor
	 * @return
	 */
	public String extractPdfToText(PDFTableExtractor extractor) {
		List<Table> tables = extractor.extract();
		StringBuilder sb = new StringBuilder();
		for (Table table : tables) {
			sb.append(table.toString());
			sb.append(System.getProperty("line.separator", "\n"));
		}
		return sb.toString();
	}

	/**
	 * 将pdf每一行的记录转化为一行一行的text字符串
	 *
	 * @param extractor
	 * @param separator 分隔符
	 * @return
	 */
	public String extractPdfToText(PDFTableExtractor extractor, String separator) {
		List<Table> tables = extractor.extract(separator);
		StringBuilder sb = new StringBuilder();
		for (Table table : tables) {
			sb.append(table.toString());
			sb.append(System.getProperty("line.separator", "\n"));
		}
		return sb.toString();
	}

	public Integer getPdfPageNumber(String filePath) {
		Integer pdfPageNumber = 0;
		try (PDDocument pdDocument = PDDocument.load(new File(filePath))) {
			pdfPageNumber = pdDocument.getNumberOfPages();
		} catch (Exception e) {
			log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), "", "", "", "getPdfPageNumber failed", e);
			throw new RuntimeException(e);
		}
		return pdfPageNumber;
	}

	public String getPdfTextByStripper(String filePath) {
		String pdfText = "";
		try (PDDocument pdDocument = PDDocument.load(new File(filePath))) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(false);
			stripper.setStartPage(1);
			stripper.setEndPage(pdDocument.getNumberOfPages());
			pdfText = stripper.getText(pdDocument);
		} catch (Exception e) {
			log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), "", "", "", "getPdfTextByStripper failed", e);
			throw new RuntimeException(e);
		}
		return pdfText;
	}

	public String getPdfTextByStripper2(String filePath) {
		String pdfText = "";
		try (PDDocument pdDocument = PDDocument.load(new File(filePath))) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(true);
			stripper.setStartPage(1);
			stripper.setEndPage(pdDocument.getNumberOfPages());
			pdfText = stripper.getText(pdDocument);
		} catch (Exception e) {
			log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), "", "", "", "getPdfTextByStripper2 failed", e);
			throw new RuntimeException(e);
		}
		return pdfText;
	}

	/**
	 * 获取最后一页PDF的文本行数
	 *
	 * @param filePath
	 * @param pdfPageNumber
	 * @return
	 */
	public Integer getLineNumberInLastPage(String filePath, Integer pdfPageNumber) {
		PDFTableExtractor extractor = (new PDFTableExtractor()).setSource(filePath);
		extractor.addPage(pdfPageNumber - 1);
		List<Table> tables = extractor.extract();
		int lineNumberInLastPdf = tables.get(0).getRows().size();
		return lineNumberInLastPdf;
	}

	public String parsePdfPageTextByPageNumber(String filePath, Integer pageNumber) {
		String pdfPageText = "";
		try (PDDocument pdDocument = PDDocument.load(new File(filePath))) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(false);
			stripper.setStartPage(pageNumber);
			stripper.setEndPage(pageNumber);
			pdfPageText = stripper.getText(pdDocument);
			pdfPageText = pdfPageText.replace(System.getProperty("line.separator", "\n"), "");
		} catch (Exception e) {
			log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), "", "", "", "parsePdfPageTextByPageNumber failed", e);
			throw new RuntimeException(e);
		}
		return pdfPageText;
	}

	public String parsePdfHeaderField(String pdfHeaderText, String confText) {
		String fieldValue = null;
		try {
			String leftText = null;
			String rightText = null;
			List<String> confTextList = Splitter.on("|").splitToList(confText);
			if (confTextList.size() == 1) {
				leftText = confTextList.get(0);
			} else {
				leftText = confTextList.get(0);
				rightText = confTextList.get(1);
			}

			int beginIndex = pdfHeaderText.indexOf(leftText) + leftText.length();
			if (StringUtils.isNotEmpty(rightText)) {
				fieldValue = pdfHeaderText.substring(beginIndex, pdfHeaderText.indexOf(rightText)).trim();
			} else {
				fieldValue = pdfHeaderText.substring(beginIndex).trim();
			}
		} catch (Exception e) {
			log.error("parsePdfHeaderField confText:{} meet exception: {}", confText, e);
		}
		return fieldValue;
	}

	public String parsePdfHeaderNullableField(String pdfHeaderText, String confText) {
		String fieldValue = null;
		try {
			String leftText = null;
			String rightText = null;
			List<String> confTextList = Splitter.on("|").splitToList(confText);
			if (confTextList.size() == 1) {
				leftText = confTextList.get(0);
			} else {
				leftText = confTextList.get(0);
				rightText = confTextList.get(1);
			}

			if (pdfHeaderText.indexOf(leftText) < 0) {
				return StringUtils.EMPTY;
			}

			int beginIndex = pdfHeaderText.indexOf(leftText) + leftText.length();
			if (StringUtils.isNotEmpty(rightText)) {
				if (pdfHeaderText.indexOf(rightText) < 0) {
					return StringUtils.EMPTY;
				}
				fieldValue = pdfHeaderText.substring(beginIndex, pdfHeaderText.indexOf(rightText)).trim();
			} else {
				fieldValue = pdfHeaderText.substring(beginIndex).trim();
			}
		} catch (Exception e) {
			log.error("parsePdfHeaderField confText:{} meet exception: {}", confText, e);
		}
		return fieldValue;
	}

}

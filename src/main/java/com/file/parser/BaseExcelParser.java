package com.file.parser;

import com.file.constant.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class BaseExcelParser {

	private static final String FILE_SUFFIX_XLS = "xls";

	private static final String FILE_SUFFIX_XLSX = "xlsx";

	private static final String COMMA = ",";

	public void convertExcelToCsv(String excelFilePath, String csvFilePath) {
		FileInputStream fis = null;
		Workbook workbook = null;

		try {//NOSONAR
			StringBuilder data = new StringBuilder();

			File inputFile = new File(excelFilePath);
			fis = new FileInputStream(new File(excelFilePath));
			File outputFile = new File(csvFilePath);
			String ext = FilenameUtils.getExtension(inputFile.getName());
			if (ext.equalsIgnoreCase(FILE_SUFFIX_XLSX)) {
				workbook = new XSSFWorkbook(fis);
			} else if (ext.equalsIgnoreCase(FILE_SUFFIX_XLS)) {
				workbook = new HSSFWorkbook(fis);
			}

			int numberOfSheets = workbook.getNumberOfSheets();
			Row row;
			Cell cell;
			for (int i = 0; i < numberOfSheets; i++) {
				Sheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rowIterator = sheet.iterator();
				while (rowIterator.hasNext()) {
					row = rowIterator.next();
					Iterator<Cell> cellIterator = row.cellIterator();
					while (cellIterator.hasNext()) {
						cell = cellIterator.next();
						switch (cell.getCellType()) {
							case BOOLEAN:
								data.append(cell.getBooleanCellValue() + COMMA);
								break;
							case NUMERIC:
								data.append(cell.getNumericCellValue() + COMMA);
								break;
							case STRING:
								data.append(cell.getStringCellValue() + COMMA);
								break;
							case BLANK:
								data.append(StringUtils.EMPTY + COMMA);
								break;
							default:
								data.append(cell + COMMA);

						}
					}
					data.append(System.getProperty("line.separator", "\n"));
				}
			}
			FileUtils.write(outputFile, data, "UTF-8");
		} catch (Exception e) {
			log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), "", "", "", "convertExcelToCsv", e);
			throw new RuntimeException(e);
		}  finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (workbook != null) {
					workbook.close();
				}
			} catch (IOException e) {
				log.error("OnError|{}||{}|{}|{}|{}", ErrorCode.FILE_PARSE_EXCEPTION.getCode(), "", "", "", "IOException", e);
			}
		}
	}

	public static void main (String[] args) {
		String excelFilePath = "E:\\data\\file\\建设银行.xls";
		String csvFilePath = "E:\\data\\file\\建设银行.csv";
		BaseExcelParser baseExcelParser = new BaseExcelParser();
		baseExcelParser.convertExcelToCsv(excelFilePath, csvFilePath);

	}

}

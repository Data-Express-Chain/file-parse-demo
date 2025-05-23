/**
* Copyright (C) 2015, GIAYBAC
*
* Released under the MIT license
*/
package com.file.parser.traprange.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tho Mar 22, 2015 3:49:22 PM
 */
public class Table {

    private final int pageIdx;
    private final String separator;
    private final List<TableRow> rows = new ArrayList<>();
    private final int columnsCount;

//	public Table(int idx, int columnsCount) {
//        this.pageIdx = idx;
//        this.columnsCount = columnsCount;
//    }

    public Table(int idx, int columnsCount, String separator) {
        this.pageIdx = idx;
        this.columnsCount = columnsCount;
        this.separator = separator;
    }

    public int getPageIdx() {
        return pageIdx;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public String toHtml() {
        return toString(true);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    private String toString(boolean inHtmlFormat) {
        StringBuilder retVal = new StringBuilder();
        if (inHtmlFormat) {
            retVal.append("<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='utf-8'>")
                    .append("</head>")
                    .append("<body>");
            retVal.append("<table border='1'>");
        }
        for (TableRow row : rows) {
            if (inHtmlFormat) {
                retVal.append("<tr>");
            } else if (retVal.length() > 0) {
                retVal.append(System.getProperty("line.separator", "\n"));
            }
            int cellIdx = 0;//pointer of row.cells
            int columnIdx = 0;//pointer of columns
            while (columnIdx < columnsCount) {
                if (cellIdx < row.getCells().size()) {
                    TableCell cell = row.getCells().get(cellIdx);
                    if (cell.getIdx() == columnIdx) {
                        if (inHtmlFormat) {
                            retVal.append("<td>")
                                    .append(cell.getContent())
                                    .append("</td>");
                        } else {
                            if (cell.getIdx() != 0) {
                                retVal.append(separator);
                            }
                            retVal.append(cell.getContent());
                        }
                        cellIdx++;
                        columnIdx++;
                    } else if (columnIdx < cellIdx) {
                        if (inHtmlFormat) {
                            retVal.append("<td>")
                                    .append("</td>");
                        } else if (columnIdx != 0) {
                            retVal.append(separator);
                        }
                        columnIdx++;
                    } else {
                        throw new RuntimeException("Invalid state");
                    }
                } else {
                    if (inHtmlFormat) {
                        retVal.append("<td>")
                                .append("</td>");
                    } else if (columnIdx != 0) {
                        retVal.append(separator);
                    }
                    columnIdx++;
                }

            }
            if (inHtmlFormat) {
                retVal.append("</tr>");
            }
        }
        if (inHtmlFormat) {
            retVal.append(
                    "</table>")
                    .append("</body>")
                    .append("</html>");
        }
        return retVal.toString();
    }
}

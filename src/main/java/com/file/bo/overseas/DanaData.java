package com.file.bo.overseas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DanaData {

    /**
     * 姓名
     */
    @JsonProperty("name")
    private String name;

    /**
     * 账号
     */
    @JsonProperty("no")
    private String no;

    /**
     * 账单所属期
     */
    @JsonProperty("month")
    private String month;

    /**
     * 文档发布时间
     */
    @JsonProperty("diterbitkanPada")
    private String diterbitkanPada;

    /**
     * 交易明细
     */
    @JsonProperty("transactionList")
    private List<DanaTran> danaTrans;
}


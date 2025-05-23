package com.file.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseData<T> {
	
	private T data;
	
	private Integer errorCode;
	
	private String errorMessage;
	
}
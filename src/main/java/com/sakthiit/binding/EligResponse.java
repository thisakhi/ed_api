package com.sakthiit.binding;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EligResponse {

	
	private String planName;
	private String PlanStatus;
	private LocalDate planStartDate;
	private LocalDate planEndDate;
	private Double  benefitAmt;
	private String denialReson;
}

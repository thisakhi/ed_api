package com.sakthiit.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sakthiit.binding.EligResponse;
import com.sakthiit.service.EligService;

@RestController
public class EdRestController {

	@Autowired
	private EligService eligService;

	@GetMapping("/eligibility/{caseNum}")
	public ResponseEntity<EligResponse> determineEligibility(@PathVariable Long caseNum) {

		EligResponse eligibilty = eligService.deterMineEligibilty(caseNum);

		return new ResponseEntity<EligResponse>(eligibilty, HttpStatus.OK);
	}
}

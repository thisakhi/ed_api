package com.sakthiit.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sakthiit.binding.EligResponse;
import com.sakthiit.entity.CitizenAppEntiry;
import com.sakthiit.entity.CoTriggersEntity;
import com.sakthiit.entity.DcCasesEntity;
import com.sakthiit.entity.DcChildrensEntity;
import com.sakthiit.entity.DcEducationEntity;
import com.sakthiit.entity.DcIncomeEntity;
import com.sakthiit.entity.EligDtlsEntity;
import com.sakthiit.entity.PlanEntity;
import com.sakthiit.repo.CitizenAppRepositery;
import com.sakthiit.repo.CoTriggersRepo;
import com.sakthiit.repo.DcCasesRepo;
import com.sakthiit.repo.DcChildrenRepo;
import com.sakthiit.repo.DcEducationRepo;
import com.sakthiit.repo.DcIncomeRepo;
import com.sakthiit.repo.EligDtlsRepo;
import com.sakthiit.repo.PlanRepo;

@Service
public class EligServiceImpl implements EligService {

	@Autowired
	private DcCasesRepo caseRepo;

	@Autowired
	private PlanRepo planRepo;

	@Autowired
	private DcIncomeRepo incomeRepo;

	@Autowired
	private DcChildrenRepo childrenRepo;

	@Autowired
	private CitizenAppRepositery citizenAppRepo;

	@Autowired
	private DcEducationRepo educationRepo;

	@Autowired
	private EligDtlsRepo eligDtlsRepo;

	@Autowired
	private CoTriggersRepo coTriggersRepo;

	public EligResponse deterMineEligibilty(Long caseNum) {

		Optional<DcCasesEntity> casesEntity = caseRepo.findById(caseNum);
		Integer planId = null;
		String planName = null;
		Integer appId = null;

		if (casesEntity.isPresent()) {
			DcCasesEntity dcCasesEntity = casesEntity.get();
			planId = dcCasesEntity.getPlanId();
			appId = dcCasesEntity.getAppId();
		}

		Optional<PlanEntity> planEntity = planRepo.findById(planId);

		if (planEntity.isPresent()) {

			PlanEntity plan = planEntity.get();
			planName = plan.getPlanName();
		}

		Optional<CitizenAppEntiry> app = citizenAppRepo.findById(appId);
		Integer age = null;
		CitizenAppEntiry citizenAppEntiry = null;

		if (app.isPresent()) {
			citizenAppEntiry = app.get();
			LocalDate dob = citizenAppEntiry.getDob();
			LocalDate now = LocalDate.now();
			age = Period.between(dob, now).getYears();
		}

		EligResponse eligResponse = executePlanContidions(caseNum, planName, age);

		EligDtlsEntity eligDtlsEntity = new EligDtlsEntity();
		BeanUtils.copyProperties(eligResponse, eligDtlsEntity);
		eligDtlsEntity.setCaseNum(caseNum);
		eligDtlsEntity.setHolderName(citizenAppEntiry.getFullname());
		eligDtlsEntity.setHolderSnn(citizenAppEntiry.getSsn());
		eligDtlsRepo.save(eligDtlsEntity);

		CoTriggersEntity triggersEntity = new CoTriggersEntity();
		triggersEntity.setCaseNum(caseNum);
		triggersEntity.setTrgStatus("Pending");
		coTriggersRepo.save(triggersEntity);

		return eligResponse;
	}

	private EligResponse executePlanContidions(Long caseNum, String planName, Integer age) {

		EligResponse eligResponse = new EligResponse();
		eligResponse.setPlanName(planName);
		DcIncomeEntity dcIncomeEntity = incomeRepo.findByCaseNum(caseNum);

		if ("SNAP".equals(planName)) {
			Double empIncome = dcIncomeEntity.getEmpIncome();
			if (empIncome <= 300) {

				eligResponse.setPlanStatus("AP");
			} else {
				eligResponse.setPlanStatus("DN");
				eligResponse.setDenialReson("High Income");
			}

		} else if ("CCAP".equals(planName)) {

			List<DcChildrensEntity> childs = childrenRepo.findByCaseNum(caseNum);
			boolean ageCondition = true;
			boolean kidsCountCondition = false;

			if (!childs.isEmpty()) {
				kidsCountCondition = true;
				for (DcChildrensEntity entity : childs) {
					Integer childrenAge = entity.getChildrenAge();

					if (childrenAge > 16) {
						ageCondition = false;
						break;
					}
				}
			}

			if (dcIncomeEntity.getEmpIncome() <= 300 && kidsCountCondition && ageCondition) {

				eligResponse.setPlanStatus("AP");
			} else {
				eligResponse.setPlanStatus("DN");
				eligResponse.setDenialReson("Not Satisfied business rules.");
			}

		} else if ("Medicaid".equals(planName)) {
			Double empIncome = dcIncomeEntity.getEmpIncome();
			Double propIncome = dcIncomeEntity.getPropertyIncome();

			if (empIncome <= 300 && propIncome == 0) {

				eligResponse.setPlanStatus("AP");
			} else {
				eligResponse.setPlanStatus("DN");
				eligResponse.setDenialReson("Hign Income");
			}

		} else if ("Medicaid".equals(planName)) {

			if (age >= 65) {

				eligResponse.setPlanStatus("AP");
			} else {
				eligResponse.setPlanStatus("DN");
				eligResponse.setDenialReson("Age Not Matched");
			}

		} else if ("NJW".equals(planName)) {
			DcEducationEntity education = educationRepo.findByCaseNum(caseNum);
			Integer graduationYear = education.getGraduationYear();
			int curYear = LocalDate.now().getYear();

			if (dcIncomeEntity.getEmpIncome() == 0 && graduationYear < curYear) {
				eligResponse.setPlanStatus("AP");
			} else {
				eligResponse.setPlanStatus("DN");
				eligResponse.setDenialReson("Rules Not Satisified");
			}
		}

		if (eligResponse.getPlanStatus().equals("AP")) {

			eligResponse.setPlanStartDate(LocalDate.now());
			eligResponse.setPlanEndDate(LocalDate.now().plusMonths(6));
			eligResponse.setBenefitAmt(350.00);
		}

		return eligResponse;
	}

}

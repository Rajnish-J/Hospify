package com.HospitalAppointmentScheduling.BO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.HospitalAppointmentScheduling.CustomExceptions.AppointmentBookingDateException;
import com.HospitalAppointmentScheduling.CustomExceptions.AppointmentException;
import com.HospitalAppointmentScheduling.CustomExceptions.DateException;
import com.HospitalAppointmentScheduling.CustomExceptions.DateOfBirthException;
import com.HospitalAppointmentScheduling.CustomExceptions.EmailException;
import com.HospitalAppointmentScheduling.CustomExceptions.IdException;
import com.HospitalAppointmentScheduling.CustomExceptions.PasswordException;
import com.HospitalAppointmentScheduling.CustomExceptions.PatientException;
import com.HospitalAppointmentScheduling.CustomExceptions.PhoneNumberException;
import com.HospitalAppointmentScheduling.CustomExceptions.ReasonException;
import com.HospitalAppointmentScheduling.DAO.AppointmentsRepo;
import com.HospitalAppointmentScheduling.DAO.PatientRepo;
import com.HospitalAppointmentScheduling.Entity.AppointmentsVO;
import com.HospitalAppointmentScheduling.Entity.PatientVO;

@Component
public class AppointmentsBO {

	@Autowired
	AppointmentsRepo appointmentsRepo;

	@Autowired
	PatientRepo pRepo;

	// Insert method:
	public AppointmentsVO insertAppointments(AppointmentsVO vo) throws IdException, EmailException, PasswordException,
			PhoneNumberException, AppointmentException, PatientException, DateOfBirthException {
		if (validatePatient(vo.getPatient())) {
			appointmentsRepo.save(vo);
			return vo;
		} else {
			throw new AppointmentException("ERROR: in creating appointments");
		}

	}

	public AppointmentsVO insertAppointmentsWithPatientID(AppointmentsVO vo)
			throws IdException, EmailException, PasswordException, PhoneNumberException, AppointmentException,
			PatientException, DateOfBirthException, AppointmentBookingDateException, ReasonException {
		if (vo.getPatient().getPatientId() != null && validatePatID(vo.getPatient().getPatientId())
				&& validateAppointmentBookingDate(vo.getAppointmentDate()) && isValidReason(vo.getReason())) {
			PatientVO pvo = pRepo.findById(vo.getPatient().getPatientId()).get();
			vo.setPatient(pvo);
			if (validatePatient(pvo)) {
				appointmentsRepo.save(vo);
			}
			return vo;
		} else {
			throw new AppointmentException("ERROR: in creating appointments");
		}
	}

	// fetchById method:
	public AppointmentsVO fetchByID(Long id) throws IdException {
		if (validateApptID(id)) {
			AppointmentsVO ret = appointmentsRepo.findById(id).get();
			return ret;
		}
		return null;

	}

	// fetchAll method:
	public List<AppointmentsVO> fetchAll() {
		return appointmentsRepo.findAll();
	}

	public AppointmentsVO updateAppointmentDetails(AppointmentsVO vo, Long id) throws IdException {
		if (validateApptID(id)) {
			AppointmentsVO existingData = appointmentsRepo.findById(id).get();
			existingData.setAppointmentDate(vo.getAppointmentDate());
			existingData.setReason(vo.getReason());

			appointmentsRepo.save(existingData);
			return existingData;
		}
		return null;

	}

	// delete method:
	public String deleteAppointment(long id) throws IdException {
		String ret = "patient ID does not exists in the database give the valid ID";
		if (validateApptID(id)) {
			appointmentsRepo.deleteByAppointmentId(id);
			if (appointmentsRepo.existsById(id)) {
				ret = "appointment deleted";
			}
		}
		return ret;
	}

	// fetches all the appointments respect to the patient id:
	public List<AppointmentsVO> findAllAppointmentsByPatientID(long id) throws IdException {
		if (validatePatID(id)) {
			List<AppointmentsVO> apptList = appointmentsRepo.findAllApptByPatientId(id);
			if (apptList.isEmpty() || apptList == null) {
				return (new ArrayList<>());
			}
			return apptList;
		}
		return null;
	}

	// returns the number that the particular date having count
	public long countOfAppointmentsByDate(LocalDate date) {
		long ret = appointmentsRepo.countAppointmentsByDate(date);
		return ret;
	}

	// Appointment by between two days:
	public List<AppointmentsVO> findAppointmentsByPatientIdAndDateRange(LocalDate sd, LocalDate ld, long id)
			throws DateException, IdException, AppointmentException {
		validatePatID(id);
		if (sd.isAfter(ld)) {
			throw new DateException("start date could be before the end date");
		}
		List<AppointmentsVO> list = appointmentsRepo.findAppointmentsByPatientIdAndDateRange(sd, ld, id);
		if (list.size() <= 0) {
			throw new AppointmentException("No records available");
		}
		return list;
	}

	// fetches all the appointments with respect to logged in patient id and given
	// date:
	public List<AppointmentsVO> findAppointmentsByPatientIdAndDate(LocalDate ld, long id)
			throws IdException, AppointmentException {
		validatePatID(id);
		List<AppointmentsVO> list = appointmentsRepo.findAppointmentsByPatientIdAndDate(ld, id);
		if (list.size() <= 0) {
			throw new AppointmentException("No records available");
		}
		return list;
	}

	// ascending order:
	public List<AppointmentsVO> ascendingDate() throws AppointmentException {
		List<AppointmentsVO> list = appointmentsRepo.fetchApptsAscendingDate();
		if (!(list.size() > 0)) {
			throw new AppointmentException("ERROR: There is no Records in the DataBase");
		}
		return list;
	}

	// validations

	// validation methods:
	public boolean validatePhoneNumber(String phoneNumber) throws PhoneNumberException {
		if (phoneNumber == null || phoneNumber.length() != 10) {
			throw new PhoneNumberException("ERROR: The phone number lenght is atleast 10");
		}

		char firstChar = phoneNumber.charAt(0);
		if (firstChar != '9' && firstChar != '8' && firstChar != '7' && firstChar != '6') {
			throw new PhoneNumberException("ERROR: Phone number must start with 9, 8, 7, or 6.");
		}

		for (char c : phoneNumber.toCharArray()) {
			if (!Character.isDigit(c)) {
				throw new PhoneNumberException("ERROR: Phone number can only contain digits.");
			}
		}
		return true;
	}

	// validation for email
	public boolean validateEmail(String email) throws EmailException {
		if (email == null || email.isEmpty()) {
			throw new EmailException("ERROR: Email field could not be empty");
		}
		int atCount = 0;
		for (char c : email.toCharArray()) {
			if (c == '@') {
				atCount++;
				if (atCount == 1) {
					break;
				}
			}
		}
		if (atCount == 0) {
			throw new EmailException("ERROR: Email should contain atleast one " + "@" + " charactre in it");
		}

		if (email.contains("..")) {
			throw new EmailException("ERROR: Email cannot contain consecutive dots.");
		}
		return atCount == 1;
	}

	// validations for password
	public boolean validatePassword(String password) throws PatientException, PasswordException {

		if (password == null) {
			throw new PasswordException("ERROR: password field could not be empty");
		} else if (password.length() < 8 || password.length() > 12) {
			throw new PasswordException("ERROR: password length in between 8 to 12");
		}

		boolean hasUppercase = false;
		boolean hasLowercase = false;
		boolean hasDigit = false;
		boolean hasSpecial = false;

		for (char c : password.toCharArray()) {
			if (Character.isUpperCase(c)) {
				hasUppercase = true;
			} else if (Character.isLowerCase(c)) {
				hasLowercase = true;
			} else if (Character.isDigit(c)) {
				hasDigit = true;
			} else {
				hasSpecial = true;
			}
		}
		if (!(hasUppercase)) {
			throw new PasswordException("ERROR: password could have atleast one Uppercase letter");
		}
		if (!(hasLowercase)) {
			throw new PasswordException("ERROR: password could have atleast one Lowercase letter");
		}
		if (!(hasDigit)) {
			throw new PasswordException("ERROR: password could have atleast one Digit");
		}
		if (!(hasSpecial)) {
			throw new PasswordException("ERROR: password could have atleast one Special character");
		}

		return hasUppercase && hasLowercase && hasDigit && hasSpecial;
	}

	// checks the Patient ID Checking
	public boolean validatePatID(Long id) throws IdException {
		List<Long> pID = pRepo.fetchPatientId();

		boolean contains = pID.contains(id);
		if (!contains) {
			throw new IdException("ERROR: patient ID not exist in the database");
		}
		if (id == null) {
			throw new IdException("ERROR: patient Id field could not be null");
		} else if (id <= 0) {
			throw new IdException("ERROR: patient ID could not be negative or zero");
		}

		return true;
	}

	// checks the Patient ID Checking
	public boolean validateApptID(Long id) throws IdException {
		if (id == null) {
			throw new IdException("ERROR: patient ID field could not be null");
		}
		if (id <= 0) {
			throw new IdException("ERROR: patient ID could not be negative or zero");
		}
		List<Long> aID = appointmentsRepo.fetchAppointmentIds();
		boolean contains = aID.contains(id);

		if (!contains) {
			throw new IdException("ERROR: patient ID does not exist in the database");
		}

		return true;
	}

	// checks if the list of appointments could be greater than zero
	public boolean validateAppointmentCount(PatientVO vo) throws AppointmentException {
		if (vo.getAppointments().size() <= 0) {
			throw new AppointmentException("ERROR: Appointments could not be zero..");
		}
		return vo.getAppointments().size() > 0;
	}

	// checks for the new appointments could not be in the past
	public boolean validateAppointmentBookingDate(LocalDate ld) throws AppointmentBookingDateException {

		LocalDate today = LocalDate.now();

		if (!(ld.isAfter(today) || ld.isEqual(today))) {
			throw new AppointmentBookingDateException("Appointment booking date could not be in the past");
		}
		return true;

	}

	// checks for the DOB could not be in the future
	public boolean validateDOB(LocalDate ld) throws DateOfBirthException {
		LocalDate today = LocalDate.now();

		if (ld.isAfter(today)) {
			throw new DateOfBirthException("date of birth could not be in the future");
		}
		if (today.minusYears(18).isBefore(ld)) {
			throw new DateOfBirthException("ERROR: Patient must be at least 18 years old.");
		}
		return true;
	}

	public boolean validateCombinedName(String firstName, String lastName) throws PatientException {
		String combinedName = firstName + " " + lastName;

		if (combinedName.length() > 50) {
			throw new PatientException("ERROR: Combined first and last name cannot exceed 50 characters.");
		}

		for (char c : combinedName.toCharArray()) {
			if (!Character.isAlphabetic(c) && !Character.isSpaceChar(c)) {
				throw new PatientException("ERROR: Combined first and last name contains invalid characters.");
			}
		}

		if (combinedName.contains("  ")) {
			throw new PatientException("ERROR: Combined first and last name cannot contain consecutive spaces.");
		}

		return true;
	}

	public boolean validateFirstName(String firstName) throws PatientException {
		if (firstName == null || firstName.isEmpty()) {
			throw new PatientException("ERROR: First name cannot be empty.");
		}

		if (firstName.length() < 2) {
			throw new PatientException("ERROR: First name must be at least 2 characters long.");
		}

		for (char c : firstName.toCharArray()) {
			if (!Character.isAlphabetic(c)) {
				throw new PatientException("ERROR: First name can only contain alphabetic characters.");
			}
		}

		if (firstName.trim().length() != firstName.length()) {
			throw new PatientException("ERROR: First name cannot have leading or trailing spaces.");
		}

		return true;
	}

	public boolean validateLastName(String lastName) throws PatientException {
		if (lastName == null || lastName.isEmpty()) {
			throw new PatientException("ERROR: Last name cannot be empty.");
		}

		if (lastName.length() < 2) {
			throw new PatientException("ERROR: Last name must be at least 2 characters long.");
		}

		for (char c : lastName.toCharArray()) {
			if (!Character.isAlphabetic(c)) {
				throw new PatientException("ERROR: Last name can only contain alphabetic characters.");
			}
		}

		if (lastName.equalsIgnoreCase("N/A") || lastName.equalsIgnoreCase("Unknown")) {
			throw new PatientException("ERROR: Last name cannot be 'N/A' or 'Unknown'.");
		}

		return true;
	}

	private boolean isValidReason(String reason) throws ReasonException {
		boolean flag = true;
		int reasonLength = reason.length();
		if (reasonLength > 31) {
			throw new ReasonException("Reason is too long, hence your patient could not registered");
		}
		if (reasonLength <= 0) {
			throw new ReasonException("Reason is too short, hence your patient could not registered");
		}
		return flag;
	}

	// Main validation method to validate a patient object:
	public boolean validatePatient(PatientVO vo)
			throws PatientException, PhoneNumberException, EmailException, PasswordException, DateOfBirthException {
		return validatePhoneNumber(vo.getPatientPhone()) && validateEmail(vo.getPatientEmail())
				&& validatePassword(vo.getPatientPassword())
				&& validateCombinedName(vo.getFirstName(), vo.getLastName()) && validateFirstName(vo.getFirstName())
				&& validateLastName(vo.getLastName()) && validateDOB(vo.getDob());

	}

}

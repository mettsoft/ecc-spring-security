package com.ecc.spring_xml.web;

import java.util.Locale;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import com.ecc.spring_xml.dto.PersonDTO;
import com.ecc.spring_xml.factory.PersonFactory;
import com.ecc.spring_xml.service.PersonService;
import com.ecc.spring_xml.service.RoleService;
import com.ecc.spring_xml.util.FileUploadBean;
import com.ecc.spring_xml.util.DateUtils;
import com.ecc.spring_xml.util.NumberUtils;
import com.ecc.spring_xml.util.ValidationUtils;
import com.ecc.spring_xml.util.ValidationException;

@Controller
@RequestMapping("/persons")
public class PersonController {
	private static final String DEFAULT_COMMAND_NAME = "command";
	private static final String FORM_PARAMETER_PERSON_ID = "id";

	private static final String FORM_PARAMETER_PERSON_ROLE_IDS = "personRoleIds";

	private static final String FORM_PARAMETER_PERSON_CONTACT_TYPE = "contactType";
	private static final String FORM_PARAMETER_PERSON_CONTACT_DATA = "contactData";

	private static final String QUERY_PARAMETER_SEARCH_TYPE = "querySearchType";
	private static final String QUERY_PARAMETER_PERSON_LAST_NAME = "queryLastName";
	private static final String QUERY_PARAMETER_ROLE_ID = "queryRoleId";
	private static final String QUERY_PARAMETER_BIRTHDAY = "queryBirthday";
	private static final String QUERY_PARAMETER_ORDER_BY = "queryOrderBy";
	private static final String QUERY_PARAMETER_ORDER_TYPE = "queryOrderType";

	private static final String VIEW_PARAMETER_ROLE_ITEMS = "roleItems";
	private static final String VIEW_PARAMETER_ERROR_MESSAGES = "errorMessages";
	private static final String VIEW_PARAMETER_SUCCESS_MESSAGE = "successMessage";
	private static final String VIEW_PARAMETER_ACTION = "action";
	private static final String VIEW_PARAMETER_HEADER = "headerTitle";
	private static final String VIEW_PARAMETER_DATA = "data";
	private static final String VIEW_PARAMETER_ASSIGNED_CONTACT_TYPES = "assignedContactTypes";
	private static final String VIEW_PARAMETER_ASSIGNED_ROLE_IDS = "assignedRoleIds";
	private static final String VIEW_PARAMETER_LOCALE = "locale";

	private static final String ATTRIBUTE_FORCE_CREATE_MODE = "isCreateMode";

	@Autowired
	private PersonService personService;

	@Autowired
	private PersonFactory personFactory;

	@Autowired
	private RoleService roleService;

	@Autowired
	private MessageSource messageSource;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		if (personService.supports(binder.getTarget().getClass())) {
			binder.setValidator(personService);		
		}
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(DateUtils.DATE_FORMAT, true));
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView list(HttpServletRequest request, Locale locale) {
		ModelAndView modelView = new ModelAndView("person");
		modelView.addObject(VIEW_PARAMETER_LOCALE, locale);

		PersonDTO person = new PersonDTO();
		Integer personId = NumberUtils.createInteger(request.getParameter(FORM_PARAMETER_PERSON_ID));
		if (request.getAttribute(ATTRIBUTE_FORCE_CREATE_MODE) != null || personId == null) {
			modelView.addObject(VIEW_PARAMETER_HEADER, messageSource.getMessage("person.headerTitle.create", null, locale));
			modelView.addObject(VIEW_PARAMETER_ACTION, "/create");
		}
		else {
			try {
				person = personService.get(personId);
			}
			catch (ValidationException cause) {
				request.setAttribute(ATTRIBUTE_FORCE_CREATE_MODE, true);
				throw cause;
			}
			modelView.addAllObjects(constructViewParametersFromPerson(person));
			modelView.addObject(VIEW_PARAMETER_HEADER, messageSource.getMessage("person.headerTitle.update", null, locale));
			modelView.addObject(VIEW_PARAMETER_ACTION, "/update");
		}
		modelView.addObject(DEFAULT_COMMAND_NAME, person);
		modelView.addAllObjects(queryToViewParameters(request));
		modelView.addObject(VIEW_PARAMETER_ROLE_ITEMS, roleService.list());

		if (RequestContextUtils.getInputFlashMap(request) != null) {
			modelView.addObject(VIEW_PARAMETER_SUCCESS_MESSAGE, RequestContextUtils.getInputFlashMap(request).get(VIEW_PARAMETER_SUCCESS_MESSAGE));
		}

		modelView.addObject(VIEW_PARAMETER_DATA, personService.list(
			request.getParameter(QUERY_PARAMETER_PERSON_LAST_NAME),
			NumberUtils.createInteger(request.getParameter(QUERY_PARAMETER_ROLE_ID)),	
			DateUtils.parse(request.getParameter(QUERY_PARAMETER_BIRTHDAY)),
			request.getParameter(QUERY_PARAMETER_ORDER_BY),
			request.getParameter(QUERY_PARAMETER_ORDER_TYPE)));
		return modelView;
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String create(HttpServletRequest request, @Validated PersonDTO person, BindingResult bindingResult, Locale locale) {
		if (bindingResult != null && bindingResult.hasErrors()) {
			throw new ValidationException(bindingResult.getAllErrors(), person);
		}

		personService.create(person);

		String message = messageSource.getMessage("person.successMessage.create", new Object[] { person.getName() }, locale);
		RequestContextUtils.getOutputFlashMap(request).put(VIEW_PARAMETER_SUCCESS_MESSAGE, message);
		return "redirect:/persons";
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String upload(HttpServletRequest request, FileUploadBean file, Locale locale) {
		request.setAttribute(ATTRIBUTE_FORCE_CREATE_MODE, true);
		PersonDTO person = personFactory.createPersonDTO(file.getFile().getBytes());
		personService.validate(person, DEFAULT_COMMAND_NAME);
		return create(request, person, null, locale);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(HttpServletRequest request, @Validated PersonDTO person, BindingResult bindingResult, Locale locale) {
		if (bindingResult.hasErrors()) {
			throw new ValidationException(bindingResult.getAllErrors(), person);
		}
		personService.update(person);

		String message = messageSource.getMessage("person.successMessage.update", new Object[] { person.getName() }, locale);
		RequestContextUtils.getOutputFlashMap(request).put(VIEW_PARAMETER_SUCCESS_MESSAGE, message);
		return "redirect:/persons";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String delete(HttpServletRequest request, PersonDTO person, Locale locale) {
		request.setAttribute(ATTRIBUTE_FORCE_CREATE_MODE, true);
		personService.delete(person.getId());

		String message = messageSource.getMessage("person.successMessage.delete", new Object[] { person.getName() }, locale);
		RequestContextUtils.getOutputFlashMap(request).put(VIEW_PARAMETER_SUCCESS_MESSAGE, message);
		return "redirect:/persons";
	}

	@ExceptionHandler({ ValidationException.class })
	public ModelAndView exceptionHandler(HttpServletRequest request, HttpServletResponse response, ValidationException cause, Locale locale) {
		ModelAndView modelView = list(request, locale);
		List<ObjectError> errors = cause.getAllErrors();
		Object target = cause.getTarget();

		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		if (request.getAttribute(ATTRIBUTE_FORCE_CREATE_MODE) == null) {
			modelView.addAllObjects(constructViewParametersFromPerson((PersonDTO) target));
			modelView.addObject(DEFAULT_COMMAND_NAME, target);				
		}
		modelView.addObject(VIEW_PARAMETER_ERROR_MESSAGES, ValidationUtils.localize(errors, messageSource, locale));
		cause.printStackTrace();
		if (cause.getCause() != null) {
			cause.printStackTrace();
		}

		return modelView;
	}

	private Map<String, Object> queryToViewParameters(HttpServletRequest request) {
		Map<String, Object> parameters = new HashMap<>(6);
		parameters.put(QUERY_PARAMETER_SEARCH_TYPE, request.getParameter(QUERY_PARAMETER_SEARCH_TYPE));
		parameters.put(QUERY_PARAMETER_PERSON_LAST_NAME, request.getParameter(QUERY_PARAMETER_PERSON_LAST_NAME));
		parameters.put(QUERY_PARAMETER_ROLE_ID, request.getParameter(QUERY_PARAMETER_ROLE_ID));
		parameters.put(QUERY_PARAMETER_BIRTHDAY, request.getParameter(QUERY_PARAMETER_BIRTHDAY));
		parameters.put(QUERY_PARAMETER_ORDER_BY, request.getParameter(QUERY_PARAMETER_ORDER_BY));
		parameters.put(QUERY_PARAMETER_ORDER_TYPE, request.getParameter(QUERY_PARAMETER_ORDER_TYPE));
		return parameters;
	}

	private Map<String, Object> constructViewParametersFromPerson(PersonDTO person) {
		Map<String, Object> parameters = new HashMap<>(2); 
		parameters.put(VIEW_PARAMETER_ASSIGNED_ROLE_IDS, person.getRoles().stream().map(t -> t.getId().toString()).collect(Collectors.joining(",")));
		parameters.put(VIEW_PARAMETER_ASSIGNED_CONTACT_TYPES, person.getContacts().stream().map(t -> "'" + t.getContactType() + "'").collect(Collectors.joining(",")));
		return parameters;
	} 
}
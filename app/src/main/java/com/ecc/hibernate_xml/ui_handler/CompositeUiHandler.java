package com.ecc.hibernate_xml.ui_handler;

import java.util.List;
import java.util.ArrayList;

import com.ecc.hibernate_xml.app.InputException;
import com.ecc.hibernate_xml.app.InputHandler;

public class CompositeUiHandler extends UiHandler {

	private static String MAIN_PROMPT = "Please choose what operation you want to perform:\n%sOperation: ";
	private static String BACK_OPTION = "0. Close Menu\n";

	private List<UiHandler> uiHandlers;
	private Boolean shouldRelinquishControl;

	public CompositeUiHandler() {
		this(null);
	}

	public CompositeUiHandler(String operationName) {
		super(operationName);	
		uiHandlers = new ArrayList<>();
		shouldRelinquishControl = false;
	}

	public CompositeUiHandler add(UiHandler uiHandler) {
		uiHandlers.add(uiHandler);
		return this;
	}

	@Override
	public void onHandle() throws Exception {
		shouldRelinquishControl = false;
		String userPrompt = String.format(MAIN_PROMPT, buildOperationPrompt());
		Integer optionIndex = InputHandler.getNextLine(userPrompt, Integer::valueOf);
		if (optionIndex > 0) {
			try {
				uiHandlers.get(optionIndex - 1).handle();			
			}
			catch (Exception cause) {
				throw new InputException(cause);
			}
		}
		else {
			shouldRelinquishControl = true;
		}
	}

	private String buildOperationPrompt() {		
		StringBuilder builder = new StringBuilder(BACK_OPTION);

		for (int i = 0; i < uiHandlers.size(); i++) {
			builder.append(String.format("%d. %s\n", i + 1, uiHandlers.get(i).getOperationName()));
		}

		return builder.toString();
	}

	@Override 
	protected Boolean relinquishControl() {
		return shouldRelinquishControl;
	}
}
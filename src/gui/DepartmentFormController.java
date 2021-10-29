package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.exceptions.ValidationException;
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable {
	
	private Department entityDepartment;
	private DepartmentService service;
	
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
	
	@FXML
	private TextField textFieldId;

	@FXML
	private TextField textFieldName;
	
	@FXML
	private Button buttonSave;
	
	@FXML
	private Button buttonCancel;
	
	@FXML
	private Label errorLabel;
	
	
	@FXML
	public void onButtonSaveAction(ActionEvent event) {
		if(entityDepartment == null) {
			throw new IllegalStateException("entity was NULL");
		}
		if(service == null) {
			throw new IllegalStateException("service was NULL");
		}
		try {
			entityDepartment = getFormData();
			service.saveOrUpdate(entityDepartment);
			notifyDataChangeLinsteners();
			Utils.currentStage(event).close();
		}
		catch(ValidationException e) {
			setErrorMessages(e.getErrors());
		}
		catch(DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeLinsteners() {
		for(DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
		
	}

	private Department getFormData() {
		Department department = new Department();
		
		ValidationException exception = new ValidationException("Validation Error");
		
		department.setId(Utils.tryParseToInt(textFieldId.getText()));
		
		if(textFieldName.getText() == null || textFieldName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		department.setName(textFieldName.getText());
		
		if(exception.getErrors().size() > 0) {
			throw exception;
		}
		
		return department;
	}

	@FXML
	public void onButtonCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}
	
	public void setDepartment(Department entityDepartment) {
		this.entityDepartment = entityDepartment;
	}
	
	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}
	
	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}
	
	public void updateFormData() {
		if(entityDepartment == null) {
			throw new IllegalStateException("Entity was NULL");
		}
		
		textFieldId.setText(String.valueOf(entityDepartment.getId()));
		textFieldName.setText(entityDepartment.getName());
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(textFieldId);
		Constraints.setTextFieldMaxLength(textFieldName, 30);
	}
	
	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		
		if(fields.contains("name")) {
			errorLabel.setText(errors.get("name"));
		}
	}

}

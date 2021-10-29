package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entitySeller;
	private SellerService service;
	private DepartmentService departmentService;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField textFieldId;

	@FXML
	private TextField textFieldName;

	@FXML
	private TextField textFieldEmail;

	@FXML
	private DatePicker datePickerBirthDate;

	@FXML
	private TextField textFieldBaseSalary;

	@FXML
	private ComboBox<Department> comboBoxDepartment;

	@FXML
	private Button buttonSave;

	@FXML
	private Button buttonCancel;

	@FXML
	private Label errorLabel;

	@FXML
	private Label errorLabelEmail;

	@FXML
	private Label errorLabelBirthDate;

	@FXML
	private Label errorLabelBaseSalary;

	private ObservableList<Department> obsList;

	@FXML
	public void onButtonSaveAction(ActionEvent event) {
		if (entitySeller == null) {
			throw new IllegalStateException("entity was NULL");
		}
		if (service == null) {
			throw new IllegalStateException("service was NULL");
		}
		try {
			entitySeller = getFormData();
			service.saveOrUpdate(entitySeller);
			notifyDataChangeLinsteners();
			Utils.currentStage(event).close();
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeLinsteners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}

	}

	private Seller getFormData() {
		Seller sellerObj = new Seller();

		ValidationException exception = new ValidationException("Validation Error");

		sellerObj.setId(Utils.tryParseToInt(textFieldId.getText()));

		if (textFieldName.getText() == null || textFieldName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		sellerObj.setName(textFieldName.getText());

		if (textFieldEmail.getText() == null || textFieldEmail.getText().trim().equals("")) {
			exception.addError("email", "Field can't be empty");
		}
		sellerObj.setEmail(textFieldEmail.getText());

		if (datePickerBirthDate.getValue() == null) {
			exception.addError("birthDate", "Field can't be empty");
		} else {
			Instant instant = Instant.from(datePickerBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
			sellerObj.setBirthDate(Date.from(instant));
		}

		if (textFieldBaseSalary.getText() == null || textFieldBaseSalary.getText().trim().equals("")) {
			exception.addError("baseSalary", "Field can't be empty");
		}
		sellerObj.setBaseSalary(Utils.tryParseToDouble(textFieldBaseSalary.getText()));
		
		sellerObj.setDepartment(comboBoxDepartment.getValue());

		if (exception.getErrors().size() > 0) {
			throw exception;
		}

		return sellerObj;
	}

	@FXML
	public void onButtonCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	public void setSeller(Seller entitySeller) {
		this.entitySeller = entitySeller;
	}

	public void setServices(SellerService service, DepartmentService departmentService) {
		this.service = service;
		this.departmentService = departmentService;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	public void updateFormData() {
		if (entitySeller == null) {
			throw new IllegalStateException("Entity was NULL");
		}

		textFieldId.setText(String.valueOf(entitySeller.getId()));
		textFieldName.setText(entitySeller.getName());
		textFieldEmail.setText(entitySeller.getEmail());
		Locale.setDefault(Locale.US);
		textFieldBaseSalary.setText(String.format("%.2f", entitySeller.getBaseSalary()));
		if (entitySeller.getBirthDate() != null) {
			datePickerBirthDate
					.setValue(LocalDate.ofInstant(entitySeller.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		if (entitySeller.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		} else {

			comboBoxDepartment.setValue(entitySeller.getDepartment());
		}

	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(textFieldId);
		Constraints.setTextFieldMaxLength(textFieldName, 70);
		Constraints.setTextFieldMaxLength(textFieldEmail, 30);
		Constraints.setTextFieldDouble(textFieldBaseSalary);
		Utils.formatDatePicker(datePickerBirthDate, "dd/MM/yyyy");

		initializeComboBoxDepartment();
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();

		errorLabel.setText((fields.contains("name") ? errors.get("name") : ""));
		errorLabelEmail.setText((fields.contains("email") ? errors.get("email") : ""));
		errorLabelBaseSalary.setText((fields.contains("baseSalary") ? errors.get("baseSalary") : ""));
		errorLabelBirthDate.setText((fields.contains("birthDate") ? errors.get("birthDate") : ""));
	}

	public void loadAssociatedObjects() {
		if (departmentService == null) {
			throw new IllegalStateException("departmentService was NULL");
		}
		List<Department> list = departmentService.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

}

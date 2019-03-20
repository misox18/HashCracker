package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import hasher.Hasher;
import hasher.HasherFactory;
import Enums.CrackerType;
import Enums.HasherType;
import cracker.BruteForceHashCracker;
import cracker.DictionaryHashCracker;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class GuiController {
	
	@FXML private TextField _msgLenghtField;
	@FXML private TextField _msgField;
	@FXML private TextField _hashField;
	@FXML private TextField _slcFileField;
	@FXML private TextField _excStartField;
	@FXML private TextField _excEndField;
	@FXML private TextField _crackedMsgField;
	@FXML private ComboBox<HasherType> _comboHashType;
	@FXML private ComboBox<CrackerType> _comboCrackType;
	@FXML private Button _generateBtn;
	@FXML private Button _slcFileBtn;
	@FXML private Button _crackerBtn;
	
	private List<Character> defaultChars = Arrays.asList(
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'W', 'X', 'Y', 'Z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
	private Stage _stage = new Stage();
	private FileChooser _fileChooser = new FileChooser();
	private File _dictionary = null;
	private Optional<String> _crackedString = null;
	
	@FXML
	private void initialize() {
		
		_comboHashType.getItems().setAll(HasherType.values());
		_comboHashType.getSelectionModel().selectFirst();
		
		_comboCrackType.getItems().setAll(CrackerType.values());
		_comboCrackType.getSelectionModel().selectFirst();
		
		_slcFileBtn.setVisible(false);
		_slcFileField.setVisible(false);
		_dictionary = null;
		
		_msgLenghtField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if (oldValue != newValue) {
						_hashField.setText("");
						_crackedMsgField.setText("");
						_crackedString = null;
					}
					if(!newValue.matches("\\d*")) {
						_msgLenghtField.setText(newValue.replaceAll("[^\\d]", ""));
						return;
					}
					if(!newValue.isEmpty() && _msgField.getText().length() > Integer.parseInt(newValue)) {
						_msgField.setText("");
						return;
					}
			}
		});
		
		_msgField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (oldValue != newValue) {
					_hashField.setText("");
					_crackedMsgField.setText("");
					_crackedString = null;
				}
				if (_msgField.getText().length() > Integer.parseInt(_msgLenghtField.getText())) {
	                String s = _msgField.getText().substring(0, Integer.parseInt(_msgLenghtField.getText()));
	                _msgField.setText(s);
	            }
			}
		});
		
		_comboHashType.valueProperty().addListener(new ChangeListener<HasherType>() {
			@Override
			public void changed(ObservableValue<? extends HasherType> observable, HasherType oldValue, HasherType newValue) {
				if(oldValue != newValue) {
					_hashField.setText("");
					_crackedMsgField.setText("");
					_crackedString = null;
				}
			}
		});
		
		_comboCrackType.valueProperty().addListener(new ChangeListener<CrackerType>() {
			@Override
			public void changed(ObservableValue<? extends CrackerType> observable, CrackerType oldValue, CrackerType newValue) {
				_crackedMsgField.setText("");
				if(newValue == CrackerType.Dictionary) {
					_slcFileBtn.setVisible(true);
					_slcFileField.setVisible(true);
				} else {
					_dictionary = null;
					_slcFileField.setText("");
					_slcFileBtn.setVisible(false);
					_slcFileField.setVisible(false);
					_dictionary = null;
				}
			}
		});
	}
	
	public void generateBtnClicked() {
		if(!_msgField.getText().isEmpty()) {
			Hasher hasher = HasherFactory.getHasher(_comboHashType.getSelectionModel().getSelectedItem());
			_hashField.setText(hasher.getHash(_msgField.getText()));
		}
	}
	
	public void selectFileBtnClicked() {
		_fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
		_dictionary = _fileChooser.showOpenDialog(_stage);
		if(_dictionary != null) {
			_slcFileField.setText(_dictionary.getName());
		}
	}
	
	public void crackerBtnClicked() {
		CrackerType type = _comboCrackType.getSelectionModel().getSelectedItem();
		switch (type) {
		case BruteForce:
			if(!_hashField.getText().isEmpty() && !_msgLenghtField.getText().isEmpty()) {
				try {
					_crackedString = BruteForceHashCracker.getInstance().crackHash(_hashField.getText(), _comboHashType.getSelectionModel().getSelectedItem(), 
							defaultChars, Integer.parseInt(_msgLenghtField.getText()), _excStartField.getText(), _excEndField.getText());
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			if(_crackedString != null) {
				_crackedMsgField.setText(_crackedString.get());	
			}
			break;
		case Dictionary:
			if (_dictionary != null && !_hashField.getText().isEmpty()) {
				try {
					_crackedString = DictionaryHashCracker.getInstance().crackHash(_hashField.getText(), _dictionary);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(_crackedString != null) {
				_crackedMsgField.setText(_crackedString.get());	
			}
			break;
		default:
			break;
		}
	}

}

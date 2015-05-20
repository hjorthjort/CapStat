package capstat.view;

import capstat.infrastructure.EventBus;
import capstat.model.Admittance;
import capstat.model.Birthday;
import capstat.model.UserLedger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.net.URL;
import java.text.DateFormat;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by Jakob on 16/05/15.
 */
//ToDo: On input username check if it's available using ul.isNicknameValid(); in method usernameTaken()

public class RegisterController implements Initializable{
    EventBus eb = EventBus.getInstance();
    UserLedger ul = UserLedger.getInstance();
    Scene scene;
    @FXML TextField usernameInput;
    @FXML PasswordField passField, passField1;
    @FXML TextField fullNameInput;
    @FXML DatePicker birthDateInput;
    @FXML ComboBox attendYear, attendLP;
    @FXML Label wrongBirthdayLabel, wrongAttendLabel, passwordMatchLabel, usernameTakenLabel, nameLabel;
    Date today;
    DateFormat dateYear, dateMonth, dateDay;
    Integer admittanceYear, lp, inputYear, inputMonth, inputDay, todayYear, todayMonth, todayDay;
    Birthday birth;
    Admittance admitTime;
    Boolean testFailed = false;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wrongAttendLabel.setVisible(false);
        wrongBirthdayLabel.setVisible(false);
        passwordMatchLabel.setVisible(false);
        usernameTakenLabel.setVisible(false);
        nameLabel.setVisible(false);
        birthDateInput.setValue(null);
        attendYear.setValue(null);
    }

    @FXML private void registerPressed() throws NumberFormatException, IOException {
        if(checkInput()){
            return;
        }
        try{
            registerUser();
        } catch (IOException e){
            throw new IOException("path not found");
        }
    }
    private void registerUser() throws IOException{
        ul.registerUser(usernameInput.getText(), fullNameInput.getText(), passField.getText(), birth, admitTime);
        scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml")), 600, 450);
        eb.notify(Main.USER_REGISTERED);
        eb.dataNotify(Main.CHANGE_SCENE, scene);
        ul.printUsers();
    }
    private boolean checkInput(){
        testFailed = false;
        today = new Date();
        dateYear = new SimpleDateFormat("yyyy");
        dateMonth = new SimpleDateFormat("MM");
        dateDay = new SimpleDateFormat("dd");
        todayYear = Integer.parseInt(dateYear.format(today));
        todayMonth = Integer.parseInt(dateMonth.format(today));
        todayDay = Integer.parseInt(dateDay.format(today));
        if(fullNameInput.getText().isEmpty()){
            updateName();
        } else {
            nameLabel.setVisible(false);
        }
        if(usernameInput.getText().isEmpty()){
            usernameTakenLabel.setText("Pick a username!");
            updateUsername();
        } else {
            checkUsernameValid();
        }
        if(birthDateInput.getValue() == null){
            updateBirth();
        } else {
            wrongBirthdayLabel.setVisible(false);
            inputYear = birthDateInput.getValue().getYear();
            inputMonth = birthDateInput.getValue().getMonth().getValue();
            inputDay = birthDateInput.getValue().getDayOfMonth();
            birth = new Birthday(inputYear, inputMonth, inputDay);
            if(todayYear<inputYear){
                updateBirth();
            } else if(todayYear==inputYear && todayMonth<inputMonth){
                updateBirth();
            } else if(todayYear==inputYear && todayMonth==inputMonth && todayDay<inputDay){
                updateBirth();
            }
        }
        if(passwordsMatch()) {
            passwordMatchLabel.setVisible(false);
        } else {
            updatePasswords();
        }
        if(attendYear.getValue() == null){
            updateLP();
        } else {;
            try {
                admittanceYear = Integer.parseInt(attendYear.getSelectionModel().getSelectedItem().toString());
            } catch (NumberFormatException e){
                updateLP();
            }
            wrongAttendLabel.setVisible(false);
        }
        if(attendLP.getSelectionModel().getSelectedItem() == null){
            updateLP();
        } else {
            wrongAttendLabel.setVisible(false);
            try {
                lp = Integer.parseInt(attendLP.getSelectionModel().getSelectedItem().toString());
            } catch (NumberFormatException e){
                updateLP();
            }
            try {
                admitTime = new Admittance(admittanceYear, lp);
            } catch (NullPointerException e){
                updateLP();
            }
            if (lp > 4 || lp < 1) {
                updateLP();
            } else {
                wrongAttendLabel.setVisible(false);
            }
        }
        return testFailed;
    }
    private void updateName(){
        nameLabel.setVisible(true);
        testFailed = true;
    }
    private void updateLP(){
        wrongAttendLabel.setVisible(true);
        testFailed = true;
    }
    private void updateBirth(){
        wrongBirthdayLabel.setVisible(true);
        testFailed = true;
    }
    private void updatePasswords(){
        passwordMatchLabel.setVisible(true);
        testFailed = true;
    }
    private void updateUsername(){
        usernameTakenLabel.setVisible(true);
        testFailed = true;
    }
    private void checkUsernameValid(){
        if(ul.isNicknameValid(usernameInput.getText())){
            usernameTakenLabel.setVisible(false);
            return;
        }
        usernameTakenLabel.setText("That username is taken/invalid!");
        updateUsername();
    }
    private boolean passwordsMatch(){
        if(passField.getText().isEmpty() && passField1.getText().isEmpty()){
            passwordMatchLabel.setText("Enter a password!");
            return false;
        }
        passwordMatchLabel.setText("Passwords don't match!");
        return passField.getText().equals(passField1.getText());
    }

}

package com.unito.prog3.fmail.client;

import com.unito.prog3.fmail.model.Email;
import com.unito.prog3.fmail.model.MailClient;
import com.unito.prog3.fmail.support.Support;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.unito.prog3.fmail.support.Support.alertMethod;

public class SendPageController implements Initializable {
    private MailClient client;

    @FXML
    private TextArea area_sendpage;
    @FXML
    private TextField recipient_sendpage;
    @FXML
    private TextField object_sendpage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    public void initModel(MailClient client) {
        this.client = client;
    }

    /**
     *If the server is offline, a popup is sent. Otherwise the inserted fields are analyzed, it is checked if there are more than one mail and the sendEmail () function is called.
     */
    public void SendmailButton(ActionEvent event) {
        if(client.isConnect()) {
            String recipient = recipient_sendpage.getText();
            String text = area_sendpage.getText();
            String object = object_sendpage.getText();

            //Split recipient if there are more than one
            String[] recipients = recipient.split(" ");
            boolean recipients_corrects = true;
            for (String s : recipients) {
                System.out.println(client.getMailbox().getAccount_name());
                if (!Support.match_account(s) || Objects.equals(client.getMailbox().getAccount_name(), s)) {
                    recipients_corrects = false;
                }
            }
            //Sends the email to all recipients
            List<String> recipients_failed = new ArrayList<>();
            if (recipients_corrects) {
                for (String s : recipients) {
                    if (!client.sendEmail(new Email(client.getMailbox().getAccount_name(), s, object, text))) {
                        recipients_failed.add(s);
                    }
                }
                //Check if there were any recipients to whom the email could not be sent
                if (!recipients_failed.isEmpty()) {
                    String recipients_failed_string = "";
                    for (String s : recipients_failed) {
                        recipients_failed_string += s + "\n";
                    }
                    alertMethod("Mail to the following recipients: " + recipients_failed_string + " have not been sent");
                    recipient_sendpage.clear();
                } else {
                    alertMethod("Mail sent successfully to all the recipients");
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                }
            } else {
                alertMethod("Check the mail account inserted");
            }
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }
}
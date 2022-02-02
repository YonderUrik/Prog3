package com.unito.prog3.fmail.client;

import com.unito.prog3.fmail.ClientMain;
import com.unito.prog3.fmail.model.Email;
import com.unito.prog3.fmail.model.MailClient;
import com.unito.prog3.fmail.support.Support;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static com.unito.prog3.fmail.support.Support.alertMethod;

public class HomeController implements Initializable {
    private MailClient client;

    private ObservableList<Email> email_rcvd;
    private ObservableList<Email> email_sent;
    private ObservableList<Email> email_del;

    @FXML
    private TextField account_name_text;
    @FXML
    private ListView<Email> ListView_rcvd;
    @FXML
    private ListView<Email> ListView_sent;
    @FXML
    private ListView<Email> ListView_del;

    @FXML
    private Tab receivedTab;
    @FXML
    private Tab sentTab;
    @FXML
    private Tab delTab;

    /**
     * The initialize function sets everything we need to be able to view the email lists
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Setting for the received mail list view
        ListView_rcvd.setOrientation(Orientation.VERTICAL);
        ListView_rcvd.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        ListView_rcvd.getSelectionModel().selectedItemProperty().addListener((observableValue, email, t1) -> {
            FXMLLoader viewLoader = new FXMLLoader(ClientMain.class.getResource("ViewmailPage.fxml"));
            Parent root;
            try {
                root = viewLoader.load();
                ViewPageController viewPageController= viewLoader.getController();
                viewPageController.initModel(client, t1);
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {e.printStackTrace();}
        });
        ListView_rcvd.setCellFactory(emailrcvdView -> new Support.cellVisual());

        //Setting for the sent mail list view
        ListView_sent.setOrientation(Orientation.VERTICAL);
        ListView_sent.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ListView_sent.getSelectionModel().selectedItemProperty().addListener((observableValue, email, t1) -> {
            FXMLLoader viewSentLoader = new FXMLLoader(ClientMain.class.getResource("ViewmailPageSent.fxml"));
            Parent root;
            try {
                root = viewSentLoader.load();
                ViewPageSentController viewPageSentController = viewSentLoader.getController();
                viewPageSentController.initModel(client, t1);
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {e.printStackTrace();}
        });
        ListView_sent.setCellFactory(emailsentView -> new Support.cellVisual());

        //Setting for the deleted mail list view
        ListView_del.setOrientation(Orientation.VERTICAL);
        ListView_del.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ListView_del.getSelectionModel().selectedItemProperty().addListener((observableValue, email, t1) -> {
        FXMLLoader viewDelLoader = new FXMLLoader(ClientMain.class.getResource("ViewmailPageDel.fxml"));
        Parent root;
        try {
            root = viewDelLoader.load();
            ViewPageDelController viewPageDelController = viewDelLoader.getController();
            viewPageDelController.initModel(t1);
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {e.printStackTrace();}
    });
        ListView_del.setCellFactory(emaildelView -> new Support.cellVisual());
    }

    /**
     * Initialize the model within the controller and start the automatic update for the emails.
     * @param client MODEL
     */
    public void initModel(MailClient client) {
        this.client = client;
        account_name_text.setText(client.getMailbox().getAccount_name());

        email_rcvd = FXCollections.observableList(client.getMailbox().getAllMailRcvd());
        ListView_rcvd.setItems(email_rcvd);
        email_sent = FXCollections.observableList(client.getMailbox().getAllMailSent());
        ListView_sent.setItems(email_sent);
        email_del = FXCollections.observableList(client.getMailbox().getAllMailDel());
        ListView_del.setItems(email_del);

        automaticUpdate();
    }

    /**
     *If the server is offline, a popup is sent. Otherwise, the SendPage is opened.
     */
    @FXML
    private void SendPageButton() throws IOException {
        if(client.isConnect()) {
            FXMLLoader sendloader = new FXMLLoader(ClientMain.class.getResource("SendmailPage.fxml"));
            Parent root = sendloader.load();
            SendPageController sendPageController = sendloader.getController();
            sendPageController.initModel(client);
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

    /**
     *If the server is offline, a popup is sent. Otherwise, the updateMailbox() function is called.
     */
    @FXML
    private void updateButton() {
        if (client.isConnect()) {
            if (client.updateAction()) {
                changeView();
                alertMethod("Mailbox has been updated successfully");
                System.out.println(client.getMailbox().toString());
            } else {
                alertMethod("An error occurred updating the mailbox");
            }
        } else {
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

    /**
     *If the server is offline, a popup is sent. Otherwise, the deleteMails() function is called.
     */
    @FXML
    private void deleteButton() {
        if(client.isConnect()) {
            if (client.deleteAction("delete_all","","")) {
                alertMethod("Mails deleted have been completely erased");
            } else {
                alertMethod("An error occurred while deleting the emails");
            }
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

    /**
     * It triggers a timer that updates the mailing lists every 5000ms
     */
    private void automaticUpdate() {
        Timer timer_update = new Timer();
        timer_update.schedule(new TimerTask() {
            @Override
            public void run() {
            Platform.runLater(() -> {
                if (client.updateAction()) {
                    changeView();
                }

            });
            }
        }, 0, 5000);
    }

    /**
     * Refresh the listVIew that has received a new mail
     */
    private void changeView(){
        int new_size;

        if(receivedTab.isSelected()){
            new_size = client.checkChangeMail('r');
            System.out.println(new_size);

            if(new_size > email_rcvd.size()){
                for(int i = email_rcvd.size(); i < new_size; i++){
                    email_rcvd.add(client.getMailbox().getAllMailRcvd().get(i));
                }

            }else if(new_size < email_rcvd.size() && new_size > 0) {
                for (int i = email_rcvd.size(); i > new_size; i--) {
                    email_rcvd.remove(i - 1);
                }

            }else if(new_size == 0){
                email_rcvd.clear();
            }

        }else if(sentTab.isSelected()){
            new_size = client.checkChangeMail('s');
            System.out.println(new_size);

            if(new_size > email_sent.size()){
                for(int i = email_sent.size(); i < new_size; i++){
                    email_sent.add(client.getMailbox().getAllMailSent().get(i));
                }

            }else if(new_size < email_sent.size() && new_size > 0){
                for(int i = email_sent.size(); i > new_size; i--){
                    email_sent.remove(i - 1);
                }

            }else if(new_size == 0){
                email_sent.clear();
            }

        }else if(delTab.isSelected()){
            new_size = client.checkChangeMail( 'd');

            if(new_size > email_del.size()){
                for(int i = email_del.size(); i < new_size; i++){
                    email_del.add(client.getMailbox().getAllMailDel().get(i));
                }
            }
        }
    }
}

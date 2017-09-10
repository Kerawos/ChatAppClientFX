package pl.mareksowa.controllers;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import pl.mareksowa.DialogUtils;
import pl.mareksowa.models.ChatSocket;
import pl.mareksowa.models.IMessageObserver;
import pl.mareksowa.models.MessageFactory;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable, IMessageObserver {

    private ChatSocket socket;

    @FXML
    TextArea txtChatDisplay;

    @FXML
    TextField txtUserInput;


    public Controller(){
        socket = ChatSocket.getSocket();
    }

    public void initialize(URL location, ResourceBundle resources) {
        socket.connect();
        sendNickPaket(DialogUtils.createNickDialog(null));
        txtUserInput.requestFocus();
        txtChatDisplay.setWrapText(true); // zeby nie zawijlo tekstu
        socket.setObserver(this); //ustawianie obserwatora
        txtUserInput.setOnKeyPressed(s->{ // wysylanie po kliknieciu entera
            if (s.getCode() == KeyCode.ENTER){
                sendMessagePacket(txtUserInput.getText());
                txtUserInput.clear();
            }
        });
    }

    @Override
    public void handleMessage(String s) { // co robimy z odbieranym typem wiadomosci
        Type token = new TypeToken<MessageFactory>(){}.getType();
        MessageFactory factory = MessageFactory.GSON.fromJson(s, token);
        switch (factory.getMessageType()){
            case SEND_MESSAGE:{
                txtChatDisplay.appendText("\n" + factory.getMessage());
                break;
            }
            case NICK_NOT_FREE:{
                //DialogUtils.createNickDialog(factory.getMessage());
                Platform.runLater(()->sendNickPaket(DialogUtils.createNickDialog(factory.getMessage()))); // zadanie wykonane w watku golwnym
                break;
            }
            case USER_JOIN:{
                //to sie przyda jak bedzie lista obok jako widok
                txtChatDisplay.appendText("\n @@@ USER: ~~> " + factory.getMessage() + " ~~ JOIN\n");
                break;
            }

            case USER_LEFT:{
                txtChatDisplay.appendText("\n@@@ ~~ " + factory.getMessage() + " ~~ LEFT..\n");
                break;
            }
        }
    }

    private void sendNickPaket(String nick){
        MessageFactory factory = new MessageFactory();
        factory.setMessageType(MessageFactory.MessageType.SET_NICK);
        factory.setMessage(nick);
        sendMessage(factory);
    }

    private void sendMessagePacket(String message){
        MessageFactory factory = new MessageFactory();
        factory.setMessageType(MessageFactory.MessageType.SEND_MESSAGE);
        factory.setMessage(message);
        sendMessage(factory);
    }

    public void sendMessage(MessageFactory factory){
        socket.sendMessage(MessageFactory.GSON.toJson(factory));
    }


}

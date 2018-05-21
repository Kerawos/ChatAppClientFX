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

/**
 * Control state
 */
public class Controller implements Initializable, IMessageObserver {

    private ChatSocket socket;

    @FXML TextArea txtChatDisplay;
    @FXML TextField txtUserInput;

    public Controller(){
        socket = ChatSocket.getSocket();
    }


    /**
     * Starting Method from JavaFX [Initializable]
     */
    public void initialize(URL location, ResourceBundle resources) {
        String localhostURL;
        //private String localhostURL = "ws://192.168.0.15:8080/chat"; // Ja
        //private String localhostURL = "ws://192.168.1.37:8080/chat"; // Pawel
        //private String localhostURL = "ws://192.168.1.22:8080/chat"; // Oskar
        localhostURL = DialogUtils.createLocalHostURL();
        if (localhostURL.equals("")){
            localhostURL = "ws://localhost:8080/chat";
        } else {
            localhostURL = "ws://" + localhostURL + ":8080/chat";
        }
        System.out.println("localhost: " + localhostURL);
        socket.connect(localhostURL);
        sendNickPacket(DialogUtils.createNickDialog(null));
        //createLocalHostURL
        txtUserInput.requestFocus();
        txtChatDisplay.setWrapText(true);
        socket.setObserver(this);
        txtUserInput.setOnKeyPressed(s->{
            if (s.getCode() == KeyCode.ENTER){
                sendMessagePacket(txtUserInput.getText());
                txtUserInput.clear();
            }
        });
    }

    /**
     * Method responsible managing message after received, react for type of message
     * @param s stored message text
     */
    @Override
    public void handleMessage(String s) {
        Type token = new TypeToken<MessageFactory>(){}.getType();
        MessageFactory factory = MessageFactory.GSON.fromJson(s, token);
        switch (factory.getMessageType()){
            case SEND_MESSAGE:{
                txtChatDisplay.appendText("\n" + factory.getMessage());
                break;
            }
            case NICK_NOT_FREE:{
                //DialogUtils.createNickDialog(factory.getMessage());
                Platform.runLater(()->sendNickPacket(DialogUtils.createNickDialog(factory.getMessage())));
                break;
            }
            case USER_JOIN:{
                txtChatDisplay.appendText("\n SERVER: USER: ~~> " + factory.getMessage() + " ~~ JOIN\n");
                break;
            }

            case USER_LEFT:{
                txtChatDisplay.appendText("\nSERVER: USER: ~~> " + factory.getMessage() + " ~~ LEFT..\n");
                break;
            }
        }
    }

    /**
     * Method responsible for send user name information
     * @param nick stored user name
     */
    private void sendNickPacket(String nick){
        MessageFactory factory = new MessageFactory();
        factory.setMessageType(MessageFactory.MessageType.SET_NICK);
        factory.setMessage(nick);
        sendMessage(factory);
    }

    private void setLocalHostURL(String url){

    }

    /**
     * Method responsible for send message information (type, message)
     * @param message stored message information
     */
    private void sendMessagePacket(String message){
        MessageFactory factory = new MessageFactory();
        factory.setMessageType(MessageFactory.MessageType.SEND_MESSAGE);
        factory.setMessage(message);
        sendMessage(factory);
    }

    /**
     * Method responsible for push message thru Json to connection to server
     * @param factory prefab to create message
     */
    public void sendMessage(MessageFactory factory){
        socket.sendMessage(MessageFactory.GSON.toJson(factory));
    }


}

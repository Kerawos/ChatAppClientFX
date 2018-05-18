package pl.mareksowa.models;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@ClientEndpoint
public class ChatSocket {

    private String localhostURL = "ws://localhost:8080/chat";
    //private String localhostURL = "ws://192.168.1.37:8080/chat"; // Pawel
    //private String localhostURL = "ws://192.168.1.22:8080/chat"; // Oskar

    private static ChatSocket socket = new ChatSocket();

    public static ChatSocket getSocket(){ //singleton
        return socket;
    }

    private WebSocketContainer webSocketContainer;
    private Session session; //info about connection

    private ChatSocket(){
        webSocketContainer = ContainerProvider.getWebSocketContainer();
    }

    private IMessageObserver observer;

    public IMessageObserver getObserver() {
        return observer;
    }

    public void setObserver(IMessageObserver observer) {
        this.observer = observer;
    }

    @OnOpen
    public void open(Session session){
        this.session = session;
        System.out.println("Connect");
    }

    //handle message
    @OnMessage
    public void message(Session session, String message){
        observer.handleMessage(message);
    }

    public void sendMessage (String message){
        try {
            session.getBasicRemote().sendText(message); // open streem client<->server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //connect to server
    public void connect(){
        try {
            webSocketContainer.connectToServer(this, new URI(localhostURL));
        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}

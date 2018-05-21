package pl.mareksowa.models;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@ClientEndpoint
public class ChatSocket {

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

    /**
     * Method responsible for handling message
     */
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

    /**
     * Method responsible connect to server
     */
    public void connect(String localhostURL){
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

package pl.mareksowa.models;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@ClientEndpoint
public class ChatSocket {

    private static ChatSocket socket = new ChatSocket(); //laduje sie przy deklaracji

    public static ChatSocket getSocket(){ //singleton
        return socket;
    }

    private WebSocketContainer webSocketContainer; //java EE dostarcza samych interfejsuw a logike wypelniaja serwisy/contenery etc.
    // metod abstrakcynych nie mozemy uruchomic a kontenery wypelniaja logike i te metody. classpth szuka i napotyka kontener i pozwala uruchomic
    private Session session; //informacje na temat uzyskanego polaczenia

    private ChatSocket(){
        webSocketContainer = ContainerProvider.getWebSocketContainer();
    }

    private IMessageObserver observer; //pojedynczy obserwator

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

    @OnMessage //wtedy wykona sie gdy przyjdzie do nas wiadomosc
    public void message(Session session, String message){
        observer.handleMessage(message);
    }

    public void sendMessage (String message){
        try {
            session.getBasicRemote().sendText(message); //getBasicRemote - strumien pomiedzy kliente, zwraca strumien
            // pomiedzy 2 punktami i do niej mozemy wyslac sobie wiadomosc
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void connect(){
        try {
            webSocketContainer.connectToServer(this, new URI("ws://localhost:8080/chat"));
            //webSocketContainer.connectToServer(this, new URI("ws://192.168.1.37:8080/chat"));  // testing PC: pa
            //webSocketContainer.connectToServer(this, new URI("ws://192.168.1.22:8080/chat"));  // testing PC: os
        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}

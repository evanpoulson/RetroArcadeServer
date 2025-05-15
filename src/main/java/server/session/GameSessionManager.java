package server.session;

import server.player.PlayerHandler;
import server.utility.GameType;
import server.utility.ThreadMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameSessionManager implements Runnable{

    private SessionContext context;
    private final BlockingQueue<ThreadMessage> inbox;

    public GameSessionManager() {
        inbox = new LinkedBlockingQueue<>();
    }

    public SessionContext getContext() {
        return context;
    }

    public void setContext(SessionContext context) {
        this.context = context;
    }

    @Override
    public void run() {

    }
}

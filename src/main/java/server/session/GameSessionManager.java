package server.session;

import server.player.PlayerHandler;
import server.utility.GameType;

public class GameSessionManager implements Runnable{

    private SessionContext context;

    public GameSessionManager() {
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

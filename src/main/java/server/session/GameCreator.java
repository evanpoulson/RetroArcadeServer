package server.session;

import server.matchmaking.MatchmakingManager;
import server.player.PlayerHandler;
import server.utility.GameType;

import java.util.Set;

public class GameCreator implements Runnable{
    private final MatchmakingManager matchmakingManager;

    public GameCreator() {
        this.matchmakingManager = new MatchmakingManager();
        //Start this GameCreator’s run-loop on a virtual thread
        Thread.ofVirtual()
                .name("RetroArcadeServer-GameCreator")
                .start(this);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                //1. Pull matches
                var matches = matchmakingManager.collectAllMatches();
                //2. Create sessions
                for (var m : matches) {
                    createGameSession(m.p1(), m.p2(), m.gameType());
                }
                //3. Wait one second before next tick
                Thread.sleep(1_000);
            }
        } catch (InterruptedException e) {
            //Graceful shutdown
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            //Log and continue (so the thread doesn’t die)
            //This is where server logger call will go
        }
    }

    //Maybe shouldn't be void?
    public boolean createGameSession(PlayerHandler p1, PlayerHandler p2, GameType gameType) {
        try {

            GameSessionManager gameSession = new GameSessionManager();

            String sessionID = p1.getID() + ":" + p2.getID();
            Set<PlayerHandler> participants = Set.of(p1, p2);

            SessionContext context = new SessionContext(sessionID, gameType, gameSession, participants);
            gameSession.setContext(context);

            Thread.ofVirtual()
                    .name("gameSessionManager")
                    .start(gameSession);
            //Should also register game sessions on the session registry, but how do we want to do that?
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}

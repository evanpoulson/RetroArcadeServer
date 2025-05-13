package server.profile;

public class Profile {
    private String email;
    private String hashedPassword;
    private String nickname;
    private String bio;
    private boolean isOnline;
    private String currentGame;
    //private PlayerRanking playerRanking;
    //private FriendsList friendsList;
    //private GameHistory gameHistory;
    private String profilePicFilePath;
    private String username;
    private int id;

    public Profile(String email, String hashedPassword, String nickname, String bio, boolean isOnline, String currentGame,
             String profilePicFilePath, String username, int id) {

        this.email = email;
        this.hashedPassword = hashedPassword;
        this.nickname = nickname;
        this.bio = bio;
        this.isOnline = isOnline;
        this.currentGame = currentGame;
        //this.playerRanking = playerRanking;
        //this.friendsList = friendsList;
        //this.gameHistory = gameHistory;
        this.profilePicFilePath = profilePicFilePath;
        this.username = username;
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public void setHashedPassword(final String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    public String getBio() {
        return this.bio;
    }

    public void setBio(final String bio) {
        this.bio = bio;
    }

    public boolean isOnline() {
        return this.isOnline;
    }

    public void setOnline(final boolean online) {
        this.isOnline = online;
    }

    public String getCurrentGame() {
        return this.currentGame;
    }

    public void setCurrentGame(final String currentGame) {
        this.currentGame = currentGame;
    }

    public String getProfilePicFilePath() {
        return this.profilePicFilePath;
    }

    public void setProfilePicFilePath(final String profilePicFilePath) {
        this.profilePicFilePath = profilePicFilePath;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public int getId() {
        return this.id;
    }

    public void setId(final int id) {
        this.id = id;
    }
}

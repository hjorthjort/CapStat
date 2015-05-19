package capstat.model;

import java.time.LocalDate;

/**
 * @author Christian Persson
 */
public class CapStat {

    private static CapStat instance;
    private UserLedger userLedger;
    private User loggedInUser;
    private final User guestUser;

    private CapStat() {
        this.userLedger = UserLedger.getInstance();
        this.guestUser = UserFactory.createGuestUser();
        this.loggedInUser = guestUser;
    }

    /**
     * Returns the only instance of CapStat.
     * @return the only instance of CapStat
     */
    public static CapStat getInstance() {
        if (instance == null) {
            instance = new CapStat();
        }
        return instance;
    }

    //Setters

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    public void setGuestLoggedIn() {
        this.loggedInUser = this.guestUser;
    }

    //Getters

    /**
     * Checks whether a given nickname is a valid nickname. A valid nickname is any nickname that is unique among other nicknames, and contains one or more of the characters A-Z, a-z, ÅÄÖ, åäö, 0-9, (), period, underscore, and space.
     * @param nickname the nickname to validate
     * @return true if the nickname is valid; false otherwise
     */
    public boolean isNicknameValid(String nickname) {
        return this.userLedger.isNicknameValid(nickname);
    }

    public boolean userIsLoggedIn() {
        return this.loggedInUser != this.guestUser;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    //Users in system Operations
    /**
     * Registers a new user in the system.
     * @param nickname the nickname of the new User
     * @param name the name of the new User
     * @param password the plaintext password of the new User
     * @param birthday the LocalDate representing birthday of the new User
     * @param admittance the Admittance instance of the new User
     *
     * @pre this.isNicknameValid(nickname) == true
     */
    public void registerUser(String nickname, String name, String password,
                             LocalDate
            birthday, Admittance admittance) {
        this.userLedger.registerNewUser(nickname, name, password, birthday,
                admittance.getYear(), admittance.getReadingPeriod().ordinal()
                        +1);
    }

    public void printUsers() {
        System.out.println(this.userLedger.toString());
    }
}

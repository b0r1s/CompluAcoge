package gal.boris.compluacoge.logic;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountManager {

    private final InfoLogin infoLogin;
    private FirebaseAuth mAuth;

    public AccountManager() {
        this.infoLogin = new InfoLogin();
    }

    public InfoLoginReadOnly getInfoLogin() {
        return infoLogin;
    }

    public void initialize() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
    }

    public void signInSilently() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            infoLogin.signIn(currentUser);
        }
    }

    public void signIn(FirebaseUser user) {
        infoLogin.signIn(user);
    }

    public void signOut() {
        infoLogin.signOut();
        mAuth.signOut();
    }

}

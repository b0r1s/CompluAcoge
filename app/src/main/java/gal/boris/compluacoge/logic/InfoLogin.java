package gal.boris.compluacoge.logic;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.auth.FirebaseUser;

public class InfoLogin implements InfoLoginReadOnly {

    private MutableLiveData<Boolean> signedIn;
    private LiveData<Boolean> signedInDistinct;
    private FirebaseUser user;
    private TypeUser typeUser;

    public InfoLogin() {
        this.signedIn = new MutableLiveData<>(false);
        this.signedInDistinct = Transformations.distinctUntilChanged(signedIn);
    }

    @Override
    public LiveData<Boolean> isSignedIn() {
        return signedInDistinct;
    }

    @Override
    public TypeUser getTypeUser() {
        return typeUser;
    }

    @Override
    public FirebaseUser getUser() {
        return user;
    }

    public void signIn(FirebaseUser user) {
        this.signedIn.setValue(true);
        this.user = user;
    }

    public void setTypeUser(TypeUser typeUser) {
        this.typeUser = typeUser;
    }

    public void signOut() {
        this.signedIn.setValue(false);
        this.user = null;
    }
}

package gal.boris.compluacoge.logic;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseUser;

public interface InfoLoginReadOnly {

    LiveData<Boolean> isSignedIn();

    TypeUser getTypeUser();

    FirebaseUser getUser();

    void setTypeUser(TypeUser typeUser);

}

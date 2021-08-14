package gal.boris.compluacoge.logic;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import gal.boris.compluacoge.data.objects.AProcedure;
import gal.boris.compluacoge.data.objects.Procedure;

public class MyViewModel extends AndroidViewModel {

    private final Flags flags;
    private final Repository repository;
    private final AccountManager accountManager;

    public Flags getFlags() {
        return flags;
    }

    public Repository getRepository() {
        return repository;
    }

    public MyViewModel(Application application) {
        super(application);

        this.accountManager = new AccountManager();
        this.repository = new Repository(accountManager.getInfoLogin());
        this.flags = new Flags(application.getBaseContext());
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public LiveData<Boolean> isSignedIn() {
        return accountManager.getInfoLogin().isSignedIn();
    }

    public boolean addNewProcDraft(Procedure proc) {
        return repository.getCloudDB().addNewProcDraft(proc);
    }

    public boolean updateNewProcDraft(Procedure newDraft, Procedure oldDraft) {
        return repository.getCloudDB().updateNewProcDraft(newDraft,oldDraft);
    }

    public boolean deleteDraft(Procedure draft) {
        return repository.getCloudDB().deleteDraft(draft);
    }

    public boolean publishDraft(Procedure newDraft, Procedure oldDraft) {
        return closeAllVisibleVersions(newDraft) && repository.getCloudDB().publishDraft(newDraft,oldDraft);
    }

    public boolean closeAllVisibleVersions(Procedure proc) {
        return repository.getCloudDB().closeAllVisibleVersions(proc);
    }

    public boolean addAProc(AProcedure aproc) {
        return repository.getCloudDB().addAProc(aproc);
    }

    public boolean updateAProc(AProcedure newAproc, AProcedure oldAproc) {
        return repository.getCloudDB().updateAProc(newAproc,oldAproc);
    }

    public boolean deleteAProc(AProcedure aproc) {
        return repository.getCloudDB().deleteAProc(aproc);
    }
}

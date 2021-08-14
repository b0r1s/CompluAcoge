package gal.boris.compluacoge.data.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gal.boris.compluacoge.data.pojos.DataPrivateUser;

public class PrivateUser {

    private final DataPrivateUser dataPrivateUser;
    private final Map<String, Map<String, Map<String,AProcedure>>> aproceduresMap;
    private final List<AProcedure> aproceduresList;

    public PrivateUser(DataPrivateUser dataPrivateUser, Map<String, Map<String, Map<String,AProcedure>>> aproceduresMap) {
        this.dataPrivateUser = dataPrivateUser;
        this.aproceduresMap = aproceduresMap;
        this.aproceduresList = new ArrayList<>(aproceduresMap.values().stream()
                        .flatMap(p -> p.values().stream())
                        .flatMap(p -> p.values().stream())
                        .collect(Collectors.toList()));
    }

    public Map<String,Object> toMap(){
        return dataPrivateUser.toMap();
    }

    // --- Getters ---

    public List<String> getPrivateKeywords() {
        return dataPrivateUser.getPrivateKeywords();
    }

    public String getEmail() {
        return dataPrivateUser.getEmail();
    }

    public List<AProcedure> getAProceduresList() {
        return aproceduresList;
    }

    public AProcedure getAProcedure(String inst, String idProc, String versionProc) {
        Map<String,Map<String,AProcedure>> aux = aproceduresMap.get(inst);
        if(aux != null) {
            Map<String,AProcedure> aux2 = aux.get(idProc);
            if(aux2!=null) {
                return aux2.get(versionProc);
            }
        }
        return null;
    }

    public Set<String> getModeratorOfInstitutions() {
        return dataPrivateUser.getModeratorOfInstitutions();
    }

    public Set<String> getExpertInProc() {
        return dataPrivateUser.getExpertInProc();
    }

    public String getNumberSocialWorker() {
        return dataPrivateUser.getNumberSocialWorker();
    }

    public String getID() {
        return dataPrivateUser.getID();
    }

    // --- Setters ---

    public List<String> addAProc(AProcedure aproc) {
        String idInst = aproc.getPublicInstitution().getID();
        String idProc = aproc.getIdProcedure();
        String versionProc = aproc.getVersionProcedure();

        //Update this class
        aproceduresMap.putIfAbsent(idInst,new HashMap<>());
        aproceduresMap.get(idInst).putIfAbsent(idProc,new HashMap<>());
        aproceduresMap.get(idInst).get(idProc).put(versionProc,aproc);
        aproceduresList.add(aproc);

        //Update DataPrivateUser
        dataPrivateUser.getAproceduresMap().putIfAbsent(idInst,new HashMap<>());
        dataPrivateUser.getAproceduresMap().get(idInst).putIfAbsent(idProc,new HashMap<>());
        dataPrivateUser.getAproceduresMap().get(idInst).get(idProc).put(versionProc,aproc.toMap());

        return new ArrayList<>(Collections.singletonList(DataPrivateUser.KEY_APROCEDURES));
    }

    public List<String> deleteAProc(AProcedure aproc) {
        String idInst = aproc.getPublicInstitution().getID();
        String idProc = aproc.getIdProcedure();
        String versionProc = aproc.getVersionProcedure();

        //Update this class
        AProcedure savedAproc = aproceduresMap.get(idInst).get(idProc).remove(versionProc);
        if(aproceduresMap.get(idInst).get(idProc).isEmpty()) {
            aproceduresMap.get(idInst).remove(idProc);
            if(aproceduresMap.get(idInst).isEmpty()) {
                aproceduresMap.remove(idInst);
            }
        }
        aproceduresList.remove(savedAproc);

        //Update DataPrivateUser
        dataPrivateUser.getAproceduresMap().get(idInst).get(idProc).remove(versionProc);
        if(dataPrivateUser.getAproceduresMap().get(idInst).get(idProc).isEmpty()) {
            dataPrivateUser.getAproceduresMap().get(idInst).remove(idProc);
            if(dataPrivateUser.getAproceduresMap().get(idInst).isEmpty()) {
                dataPrivateUser.getAproceduresMap().remove(idInst);
            }
        }

        return new ArrayList<>(Collections.singletonList(DataPrivateUser.KEY_APROCEDURES));
    }

}

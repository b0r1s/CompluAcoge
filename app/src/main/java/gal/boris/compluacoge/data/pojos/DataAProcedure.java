package gal.boris.compluacoge.data.pojos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gal.boris.compluacoge.data.IDProcedure;
import gal.boris.compluacoge.extras.Box;

public class DataAProcedure implements IDProcedure {

    private String idInstitution;
    private String idProcedure;
    private String versionProcedure;

    private static final String KEY_ID_STEPS_DONE = "idStepsDone";
    private List<String> idStepsDone;

    public static final String KEY_CREATED = "created";
    private Long created;
    public static final String KEY_LAST_MODIFIED = "lastModified";
    private Long lastModified;

    private DataAProcedure() {}

    public DataAProcedure(String idProcedure, String versionProcedure, String idInstitution, List<String> idStepsDone, Long created, Long lastModified) {
        this.idProcedure = idProcedure;
        this.versionProcedure = versionProcedure;
        this.idInstitution = idInstitution;
        this.idStepsDone = idStepsDone;
        this.created = created;
        this.lastModified = lastModified;
    }

    public static Map<String,Map<String,Map<String,DataAProcedure>>> parseMultiple(@NonNull Map<String, Map<String,Map<String,Object>>> data, @NonNull Box<Boolean> dataMissing) {
        Map<String,Map<String,Map<String,DataAProcedure>>> mapInst = new HashMap<>();
        for(Map.Entry<String,Map<String,Map<String,Object>>> entryIdInst : data.entrySet()) {
            String idInst = entryIdInst.getKey();
            Map<String,Map<String, DataAProcedure>> mapProc = new HashMap<>();
            for(Map.Entry<String,Map<String,Object>> entryIdProc : entryIdInst.getValue().entrySet()) {
                String idProc = entryIdProc.getKey();
                Map<String,DataAProcedure> mapVersion = new HashMap<>();
                for(Map.Entry<String,Object> entryVersion : entryIdProc.getValue().entrySet()) {
                    String versionProc = entryVersion.getKey();
                    DataAProcedure dataAProcedure = parse(idInst,idProc,versionProc,(Map<String, Object>) entryVersion.getValue(),dataMissing);
                    mapVersion.put(versionProc, dataAProcedure);
                }
                mapProc.put(idProc,mapVersion);
            }
            mapInst.put(idInst, mapProc);
        }
        return mapInst;
    }

    private static DataAProcedure parse(String idInst, String idProcedure, String versionProcedure, @NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        DataAProcedure aProcedure = new DataAProcedure();
        aProcedure.setIdInstitution(idInst);
        aProcedure.setIdProcedure(idProcedure);
        aProcedure.setVersionProcedure(versionProcedure);

        List<String> idStepsDone = new ArrayList<>();
        if(data.containsKey(KEY_ID_STEPS_DONE)) {
            idStepsDone = ((List<String>) data.get(KEY_ID_STEPS_DONE));
        }
        else {
            dataMissing.set(true);
        }
        aProcedure.setIdStepsDone(idStepsDone);

        Long created = 0L;
        if(data.containsKey(KEY_CREATED)) {
            created = ((Long) data.get(KEY_CREATED));
        }
        else {
            dataMissing.set(true);
        }
        aProcedure.setCreated(created);

        Long lastModified = 0L;
        if(data.containsKey(KEY_LAST_MODIFIED)) {
            lastModified = ((Long) data.get(KEY_LAST_MODIFIED));
        }
        else {
            dataMissing.set(true);
        }
        aProcedure.setLastModified(lastModified);

        return aProcedure;
    }

    public static Map<String,Map<String,Map<String,Object>>> toMapMultiple(@NonNull Map<String,Map<String,Map<String,DataAProcedure>>> map) {
        Map<String,Map<String,Map<String,Object>>> data = new HashMap<>();
        for(Map.Entry<String,Map<String,Map<String,DataAProcedure>>> entryInst : map.entrySet()) {
            Map<String,Map<String,Object>> dataIdProc = new HashMap<>();
            for(Map.Entry<String,Map<String,DataAProcedure>> entryIdProc : entryInst.getValue().entrySet()) {
                Map<String,Object> dataVersionProc = new HashMap<>();
                for(Map.Entry<String,DataAProcedure> entryVersionProc : entryIdProc.getValue().entrySet()) {
                    dataVersionProc.put(entryVersionProc.getKey(),entryVersionProc.getValue().toMap());
                }
                dataIdProc.put(entryIdProc.getKey(),dataVersionProc);
            }
            data.put(entryInst.getKey(),dataIdProc);
        }
        return data;
    }

    public Map<String,Object> toMap() {
        Map<String,Object> data = new HashMap<>();
        data.put(KEY_ID_STEPS_DONE, idStepsDone);
        data.put(KEY_CREATED, created);
        data.put(KEY_LAST_MODIFIED, lastModified);
        return data;
    }

    // --- Getters ---

    public String getIdProcedure() {
        return idProcedure;
    }

    public String getVersionProcedure() {
        return versionProcedure;
    }

    public String getIdInstitution() {
        return idInstitution;
    }

    public List<String> getIdStepsDone() {
        return idStepsDone;
    }

    public Long getCreated() {
        return created;
    }

    public Long getLastModified() {
        return lastModified;
    }

    // --- Setters ---

    private void setIdProcedure(String idProcedure) {
        this.idProcedure = idProcedure;
    }

    private void setVersionProcedure(String versionProcedure) {
        this.versionProcedure = versionProcedure;
    }

    private void setIdInstitution(String idInstitution) {
        this.idInstitution = idInstitution;
    }

    public void setIdStepsDone(List<String> idStepsDone) {
        this.idStepsDone = idStepsDone;
    }

    private void setCreated(Long created) {
        this.created = created;
    }

    private void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataAProcedure that = (DataAProcedure) o;
        return Objects.equals(idProcedure, that.idProcedure) &&
                Objects.equals(versionProcedure, that.versionProcedure) &&
                Objects.equals(idInstitution, that.idInstitution) &&
                Objects.equals(idStepsDone, that.idStepsDone) &&
                Objects.equals(created, that.created) &&
                Objects.equals(lastModified, that.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProcedure, versionProcedure, idInstitution, idStepsDone, created, lastModified);
    }
}

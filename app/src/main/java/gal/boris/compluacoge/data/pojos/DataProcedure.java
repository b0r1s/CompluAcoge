package gal.boris.compluacoge.data.pojos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gal.boris.compluacoge.data.IDProcedure;
import gal.boris.compluacoge.extras.Box;

public class DataProcedure implements IDProcedure {

    private String idProcedure;
    private String versionProcedure;

    private static final String KEY_PUBLIC_ID_INST = "publicIdInstitution";
    private String publicIdInstitution;
    private static final String KEY_NAME = "name";
    private String name;
    private static final String KEY_SHORT_DESCRIPTION = "shortDescription";
    private String shortDescription;
    private static final String KEY_LONG_DESCRIPTION = "longDescription";
    private String longDescription;
    private static final String KEY_TAGS = "tags";
    private List<String> tags;
    private static final String KEY_CLOSED = "closed";
    private boolean closed;
    public static final String KEY_CREATED = "created";
    private Long created;
    public static final String KEY_LAST_MODIFIED = "lastModified";
    private Long lastModified;

    private static final String KEY_MAP_TREE_STEPS = "mapTreeSteps";
    private Map<String, List<String>> mapTreeSteps; //step -> parent_step

    private static final String KEY_SEARCH_INDEX_NAME = "searchIndexName";
    private String searchIndexName;
    private static final String KEY_SEARCH_WORDS = "searchWords";
    private Map<String,Long> searchWords;

    private DataProcedure() {}

    public DataProcedure(DataProcedure that) {
        this.idProcedure = that.idProcedure;
        this.versionProcedure = that.versionProcedure;
        this.publicIdInstitution = that.publicIdInstitution;
        this.name = that.name;
        this.shortDescription = that.shortDescription;
        this.longDescription = that.longDescription;
        this.tags = new ArrayList<>(that.tags);
        this.closed = that.closed;
        this.created = that.created;
        this.lastModified = that.lastModified;
        this.mapTreeSteps = new HashMap<>();
        for(Map.Entry<String,List<String>> entry : that.mapTreeSteps.entrySet()) {
            this.mapTreeSteps.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        this.searchIndexName = that.searchIndexName;
        this.searchWords = new HashMap<>(that.searchWords);
    }

    public static DataProcedure createEmpty(String idProcedure, String versionProcedure, String publicIdInstitution) {
        DataProcedure proc = new DataProcedure();
        proc.idProcedure = idProcedure;
        proc.versionProcedure = versionProcedure;
        proc.publicIdInstitution = publicIdInstitution;
        proc.name = "";
        proc.shortDescription = proc.longDescription = "";
        proc.tags = new ArrayList<>();
        proc.closed = false;
        proc.created = proc.lastModified = System.currentTimeMillis();
        proc.mapTreeSteps = new HashMap<>();
        proc.searchIndexName = "";
        proc.searchWords = new HashMap<>();
        return proc;
    }

    public static Map<String,Map<String,DataProcedure>> parseMultiple(@NonNull Map<String, Map<String,Object>> data, @NonNull Box<Boolean> dataMissing) {
        Map<String,Map<String,DataProcedure>> mapProc = new HashMap<>();
        for(Map.Entry<String,Map<String,Object>> entryIdProc : data.entrySet()) {
            String idProc = entryIdProc.getKey();
            Map<String,DataProcedure> mapVersion = new HashMap<>();
            for(Map.Entry<String,Object> entryVersion : entryIdProc.getValue().entrySet()) {
                String versionProc = entryVersion.getKey();
                mapVersion.put(versionProc, parse(idProc,versionProc,(Map<String, Object>) entryVersion.getValue(),dataMissing));
            }
            mapProc.put(idProc,mapVersion);
        }
        return mapProc;
    }

    private static DataProcedure parse(String idProcedure, String versionProcedure, @NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        DataProcedure procedure = new DataProcedure();
        procedure.setIdProcedure(idProcedure);
        procedure.setVersionProcedure(versionProcedure);

        String publicIdInstitution = "public_id_institution";
        if(data.containsKey(KEY_PUBLIC_ID_INST)) {
            publicIdInstitution = ((String) data.get(KEY_PUBLIC_ID_INST));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setPublicIdInstitution(publicIdInstitution);

        String name = "Procedure X";
        if(data.containsKey(KEY_NAME)) {
            name = ((String) data.get(KEY_NAME));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setName(name);

        String shortDescription = "This is a short description";
        if(data.containsKey(KEY_SHORT_DESCRIPTION)) {
            shortDescription = ((String) data.get(KEY_SHORT_DESCRIPTION));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setShortDescription(shortDescription);

        String longDescription = "This is a looooooong description";
        if(data.containsKey(KEY_LONG_DESCRIPTION)) {
            longDescription = ((String) data.get(KEY_LONG_DESCRIPTION));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setLongDescription(longDescription);

        List<String> tags = new ArrayList<>();
        if(data.containsKey(KEY_TAGS)) {
            tags = ((List<String>) data.get(KEY_TAGS));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setTags(tags);

        boolean closed = false;
        if(data.containsKey(KEY_CLOSED)) {
            closed = ((Boolean) data.get(KEY_CLOSED));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setClosed(closed);

        Map<String, List<String>> mapTreeSteps = new HashMap<>();
        if(data.containsKey(KEY_MAP_TREE_STEPS)) {
            mapTreeSteps = ((Map<String, List<String>>) data.get(KEY_MAP_TREE_STEPS));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setMapTreeSteps(mapTreeSteps);

        Long created = 0L;
        if(data.containsKey(KEY_CREATED)) {
            created = ((Long) data.get(KEY_CREATED));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setCreated(created);

        Long lastModified = 0L;
        if(data.containsKey(KEY_LAST_MODIFIED)) {
            lastModified = ((Long) data.get(KEY_LAST_MODIFIED));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setLastModified(lastModified);

        Map<String,Long> searchWords = new HashMap<>();
        if(data.containsKey(KEY_SEARCH_WORDS)) {
            searchWords = ((Map<String,Long>) data.get(KEY_SEARCH_WORDS));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setSearchWords(searchWords);

        String searchIndexName = "";
        if(data.containsKey(KEY_SEARCH_INDEX_NAME)) {
            searchIndexName = ((String) data.get(KEY_SEARCH_INDEX_NAME));
        }
        else {
            dataMissing.set(true);
        }
        procedure.setSearchIndexName(searchIndexName);

        return procedure;
    }

    /*
    public static Map<String,Object> toMapMultiple(@NonNull Map<String,Map<String,DataProcedure>> mapProc) {
        Map<String,Object> map = new HashMap<>();
        for(Map.Entry<String,Map<String,DataProcedure>> entry : mapProc.entrySet()) {
            map.put(entry.getKey(),entry.getValue().entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey,d -> d.getValue().toMap(),(prev,next)->prev)));
        }
        return map;
    }
     */

    public Map<String,Object> toMap() {
        Map<String,Object> data = new HashMap<>();
        data.put(KEY_PUBLIC_ID_INST, publicIdInstitution);
        data.put(KEY_NAME, name);
        data.put(KEY_SHORT_DESCRIPTION, shortDescription);
        data.put(KEY_LONG_DESCRIPTION, longDescription);
        data.put(KEY_TAGS, tags);
        data.put(KEY_CLOSED, closed);
        data.put(KEY_MAP_TREE_STEPS, mapTreeSteps);
        data.put(KEY_CREATED, created);
        data.put(KEY_LAST_MODIFIED, lastModified);
        data.put(KEY_SEARCH_INDEX_NAME, searchIndexName);
        data.put(KEY_SEARCH_WORDS, searchWords);
        return data;
    }

    // --- Getters ---

    public String getIdProcedure() {
        return idProcedure;
    }

    public String getVersionProcedure() {
        return versionProcedure;
    }

    public String getPublicIdInstitution() {
        return publicIdInstitution;
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean getClosed() {
        return closed;
    }

    public Long getCreated() {
        return created;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public Map<String, List<String>> getMapTreeSteps() {
        return mapTreeSteps;
    }

    public String getSearchIndexName() {
        return searchIndexName;
    }

    public Map<String, Long> getSearchWords() {
        return searchWords;
    }

    // --- Setters ---

    public void setIdProcedure(String idProcedure) {
        this.idProcedure = idProcedure;
    }

    public void setVersionProcedure(String versionProcedure) {
        this.versionProcedure = versionProcedure;
    }

    private void setPublicIdInstitution(String publicIdInstitution) {
        this.publicIdInstitution = publicIdInstitution;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    private void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public void setMapTreeSteps(Map<String, List<String>> mapTreeSteps) {
        this.mapTreeSteps = mapTreeSteps;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setSearchIndexName(String searchIndexName) {
        this.searchIndexName = searchIndexName;
    }

    public void setSearchWords(Map<String, Long> searchWords) {
        this.searchWords = searchWords;
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataProcedure that = (DataProcedure) o;
        return closed == that.closed &&
                Objects.equals(idProcedure, that.idProcedure) &&
                Objects.equals(versionProcedure, that.versionProcedure) &&
                Objects.equals(publicIdInstitution, that.publicIdInstitution) &&
                Objects.equals(name, that.name) &&
                Objects.equals(shortDescription, that.shortDescription) &&
                Objects.equals(longDescription, that.longDescription) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(created, that.created) &&
                Objects.equals(lastModified, that.lastModified) &&
                Objects.equals(mapTreeSteps, that.mapTreeSteps) &&
                Objects.equals(searchWords, that.searchWords) &&
                Objects.equals(searchIndexName, that.searchIndexName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProcedure, versionProcedure, publicIdInstitution, name, shortDescription, longDescription, tags, closed, created, lastModified, mapTreeSteps, searchIndexName, searchWords);
    }
}

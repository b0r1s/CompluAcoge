package gal.boris.compluacoge.logic.search;

import java.util.Map;
import java.util.Objects;

import gal.boris.compluacoge.extras.Triple;

public class ProcSummary {

    private final static String KEY_TITLE = "title";
    private final String title;
    private final static String KEY_WORDS = "words";
    private Map<String,Long> words;
    private final static String KEY_UID_INST = "uidInst";
    private String uidInst;
    private final static String KEY_NAME_INST = "nameInst";
    private String nameInst;
    private final static String KEY_PROC_ID = "procId";
    private String procId;
    private final static String KEY_PROC_VERSION = "procVersion";
    private String procVersion;

    private long maxFrequency;

    public ProcSummary(Map<String,Object> data) {
        this.title = (String) data.get(KEY_TITLE);
        this.words = (Map<String,Long>) data.get(KEY_WORDS);
        this.maxFrequency = words.remove(" ");
        this.uidInst = (String) data.get(KEY_UID_INST);
        this.nameInst = (String) data.get(KEY_NAME_INST);
        this.procId = (String) data.get(KEY_PROC_ID);
        this.procVersion = (String) data.get(KEY_PROC_VERSION);
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Long> getWords() {
        return words;
    }

    public String getUidInst() {
        return uidInst;
    }

    public String getNameInst() {
        return nameInst;
    }

    public String getProcId() {
        return procId;
    }

    public String getProcVersion() {
        return procVersion;
    }

    public Triple<String,String,String> getIdentifier() {
        return new Triple<>(uidInst,procId,procVersion);
    }

    public long getMaxFrequency() {
        return maxFrequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcSummary that = (ProcSummary) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(words, that.words) &&
                Objects.equals(uidInst, that.uidInst) &&
                Objects.equals(nameInst, that.nameInst) &&
                Objects.equals(procId, that.procId) &&
                Objects.equals(procVersion, that.procVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, words, uidInst, nameInst, procId, procVersion);
    }
}

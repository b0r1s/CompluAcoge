package gal.boris.compluacoge.logic.search;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import gal.boris.compluacoge.extras.AppExecutors;
import gal.boris.compluacoge.extras.Triple;

public class SearchIndex {

    private String query;
    private final MutableLiveData<Pair<Boolean,List<ProcSummary>>> result;
    private FutureTask<Void> calculatingFuture;

    private final ConcurrentMap<Triple<String,String,String>,ProcSummary> procs; //from DB
    private final ConcurrentMap<String,Map<Triple<String,String,String>,Pair<ProcSummary,Long>>> wordToProcs; //generated
    private final ConcurrentNavigableMap<String,Map<Triple<String,String,String>,Pair<ProcSummary,Double>>> wordToProcsTFIDF; //generated

    public SearchIndex() {
        this.query = "";
        this.result = new MutableLiveData<>(new Pair<>(true, new ArrayList<>()));
        this.calculatingFuture = null;

        this.procs = new ConcurrentHashMap<>();
        this.wordToProcs = new ConcurrentHashMap<>();
        this.wordToProcsTFIDF = new ConcurrentSkipListMap<>();
    }

    //only supports add or delete ProcSummary
    //as we have a readers - writers problem, maybe we could just call this once :) todo
    public void updateTables(Map<String,Object> data) {
        data.remove("isFull");

        Map<Triple<String,String,String>, ProcSummary> newProcs = new HashMap<>();
        for(Object o : data.values()) {
            ProcSummary newProc = new ProcSummary((Map<String,Object>) o);
            Triple<String,String,String> newProcBasic = newProc.getIdentifier();
            newProcs.put(newProcBasic,newProc);
        }

        Map<Triple<String,String,String>, ProcSummary> procsToAdd = new HashMap<>();
        Map<Triple<String,String,String>, ProcSummary> procsToDelete = new HashMap<>(procs);
        for(Map.Entry<Triple<String,String,String>, ProcSummary> entry : newProcs.entrySet()) {
            procsToDelete.remove(entry.getKey());
            if(!procs.containsKey(entry.getKey())) {
                procsToAdd.put(entry.getKey(),entry.getValue());
            }
        }
        procs.clear();
        procs.putAll(newProcs);

        Set<String> wordsToUpdateTFIDF = new HashSet<>();
        for(Map.Entry<Triple<String,String,String>, ProcSummary> entry : procsToAdd.entrySet()) {
            for(Map.Entry<String,Long> wordInfo : entry.getValue().getWords().entrySet()) {
                wordsToUpdateTFIDF.add(wordInfo.getKey());
                wordToProcs.put(wordInfo.getKey(), new HashMap<>(
                        Map.of(entry.getKey(),new Pair<>(entry.getValue(),wordInfo.getValue()))));
            }
        }
        for(Map.Entry<Triple<String,String,String>, ProcSummary> entry : procsToDelete.entrySet()) {
            for(Map.Entry<String,Long> wordInfo : entry.getValue().getWords().entrySet()) {
                //String word = wordInfo.getKey().toLowerCase(); todo
                wordsToUpdateTFIDF.add(wordInfo.getKey());
                wordToProcs.get(wordInfo.getKey()).remove(entry.getKey());
            }
        }

        for(String word : wordsToUpdateTFIDF) {
            if (wordToProcs.get(word).isEmpty()) {
                wordToProcs.remove(word);
                wordToProcsTFIDF.remove(word);
                continue;
            }
            wordToProcsTFIDF.remove(word);
            Map<Triple<String,String,String>,Pair<ProcSummary,Double>> map = new HashMap<>();
            //IDF
            double idf = Math.log((double) procs.size() / wordToProcs.get(word).size());
            for(Map.Entry<Triple<String,String,String>,Pair<ProcSummary,Long>> entry : wordToProcs.get(word).entrySet()) {
                double tf = 0.5 + 0.5*((double) entry.getValue().second / entry.getValue().first.getMaxFrequency());
                map.put(entry.getKey(), new Pair<>(entry.getValue().first, tf*idf));
            }
            wordToProcsTFIDF.put(word,map);
        }

        String query = this.query;
        this.query = "";
        searchSentence(query);
    }

    private void updateLiveData(Map<Triple<String,String,String>,Pair<Integer,Double>> frequencies, boolean end) {
        List<ProcSummary> newList = frequencies.entrySet().stream()
                .sorted((a,b) -> {
                    int compValue = (-1)*Integer.compare(a.getValue().first,b.getValue().first);
                    return compValue != 0 ? compValue : (-1)*Double.compare(a.getValue().second, b.getValue().second);
                })
                .map(entry -> procs.get(entry.getKey()))
                .collect(Collectors.toList());
        result.postValue(new Pair<>(end, newList));
    }

    public LiveData<Pair<Boolean, List<ProcSummary>>> getResult() {
        return result;
    }

    public void searchSentence(String query) {
        if(this.query.equals(query)) {
            return;
        }
        if(this.calculatingFuture!=null && !this.calculatingFuture.isCancelled()) {
            this.calculatingFuture.cancel(true);
        }
        this.query = query;
        this.result.setValue(new Pair<>("".equals(query), new ArrayList<>()));
        this.result.postValue(new Pair<>("".equals(query), new ArrayList<>()));
        if(!"".equals(query)) {
            this.calculatingFuture = new FutureTask<>(() -> {
                Map<Triple<String,String,String>,Pair<Integer,Double>> frequencies = new HashMap<>(); //Frequency, TF-IDF
                boolean partialLastWord = !query.endsWith(" ");
                for(ProcSummary proc : procs.values()) {
                    if(proc.getTitle().contains(query)) {
                        frequencies.put(proc.getIdentifier(),new Pair<>(Integer.MAX_VALUE, 0.0));
                    }
                }
                if(Thread.interrupted()) {
                    return;
                }
                updateLiveData(frequencies,false);

                String[] wordsArray = query.split(" ");
                List<String> wordsNotEmpty = Arrays.stream(wordsArray, 0, wordsArray.length)
                        .filter(w -> !w.isEmpty()).collect(Collectors.toList());
                List<String> words = wordsNotEmpty.subList(0, wordsNotEmpty.size() + (partialLastWord ? -1 : 0));
                for(String word : words) {
                    Map<Triple<String,String,String>,Pair<ProcSummary,Double>> map = wordToProcsTFIDF.get(word);
                    for(Map.Entry<Triple<String,String,String>,Pair<ProcSummary,Double>> entry : map.entrySet()) {
                        Pair<Integer,Double> pair = frequencies.containsKey(entry.getKey()) ? frequencies.get(entry.getKey()) : new Pair<>(0, 0.0);
                        Pair<Integer,Double> newPair = new Pair<>(pair.first == Integer.MAX_VALUE ? Integer.MAX_VALUE : pair.first+2, pair.second + entry.getValue().second);
                        frequencies.put(entry.getKey(), newPair);
                    }
                    if(Thread.interrupted()) {
                        return;
                    }
                    updateLiveData(frequencies,false);
                }

                if(partialLastWord) {
                    String lastWord = wordsNotEmpty.get(wordsNotEmpty.size()-1);
                    String nextLexLastWord = lastWord.substring(0,lastWord.length()-1) + (char) (lastWord.charAt(lastWord.length()-1)+1);
                    String keyBeginning = wordToProcsTFIDF.ceilingKey(lastWord);
                    keyBeginning = keyBeginning!=null ? keyBeginning : "";
                    String keyEnd = wordToProcsTFIDF.lowerKey(nextLexLastWord);
                    keyEnd = keyEnd!=null ? keyEnd : "";

                    Map<String, Map<Triple<String, String, String>, Pair<ProcSummary, Double>>> map =
                            keyBeginning.compareTo(keyEnd)<=0 ? wordToProcsTFIDF.subMap(keyBeginning,true,keyEnd,true) : new HashMap<>();
                    for(Map<Triple<String, String, String>, Pair<ProcSummary, Double>> mapProc : map.values()) {
                        for(Map.Entry<Triple<String,String,String>,Pair<ProcSummary,Double>> entry : mapProc.entrySet()) {
                            Pair<Integer,Double> pair = frequencies.containsKey(entry.getKey()) ? frequencies.get(entry.getKey()) : new Pair<>(0, 0.0);
                            Pair<Integer,Double> newPair = new Pair<>(pair.first == Integer.MAX_VALUE ? Integer.MAX_VALUE : pair.first+1, pair.second + entry.getValue().second);
                            frequencies.put(entry.getKey(), newPair);
                        }
                        if(Thread.interrupted()) {
                            return;
                        }
                        updateLiveData(frequencies,false);
                    }
                }

                if(Thread.interrupted()) {
                    return;
                }
                updateLiveData(frequencies,true);

            }, null);
            AppExecutors.getInstance().cachePool().execute(calculatingFuture);
        }
    }


}

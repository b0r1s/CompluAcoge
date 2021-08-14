package gal.boris.compluacoge.extras;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gal.boris.compluacoge.data.IDProcedure;

public class AdapterUtils {

    public static void fuseSorted(RecyclerView.Adapter<?> adapter, List<? extends IDProcedure> oldProcs, List<? extends IDProcedure> newProcs) {
        Set<Pair<String,String>> newIDs = newProcs.stream()
                .map(p -> new Pair<>(p.getIdProcedure(),p.getVersionProcedure()))
                .collect(Collectors.toCollection(HashSet::new));
        for (int i = 0; i < oldProcs.size(); i++) {
            IDProcedure ap = oldProcs.get(i);
            if(!newIDs.contains(new Pair<>(ap.getIdProcedure(),ap.getVersionProcedure()))) {
                oldProcs.remove(i);
                adapter.notifyItemRemoved(i);
                i--;
            }
        }

        Set<Pair<String,String>> oldIDs = oldProcs.stream()
                .map(p -> new Pair<>(p.getIdProcedure(),p.getVersionProcedure()))
                .collect(Collectors.toCollection(HashSet::new));
        int i_old = 0;
        for (int i = 0; i < newProcs.size(); i++) {
            IDProcedure procNew = newProcs.get(i);
            if(oldIDs.contains(new Pair<>(procNew.getIdProcedure(),procNew.getVersionProcedure()))) {
                IDProcedure procOld = oldProcs.get(i_old);
                if(!procNew.idEquals(procOld)) {
                    int pos = -1;
                    for (int j = i_old+1; j < oldProcs.size(); j++) {
                        if(oldProcs.get(j).idEquals(procNew)) {
                            pos = j;
                            break;
                        }
                    }
                    if(pos == -1) {
                        throw new IllegalStateException();
                    }
                    adapter.notifyItemMoved(pos,i);
                    move(oldProcs,pos,i);
                }
                if(!procNew.equals(procOld)) {
                    adapter.notifyItemChanged(i);
                }
                i_old++;
            } else {
                adapter.notifyItemInserted(i);
            }
        }
    }

    private static void move(List list, int from, int to) {
        Object obj = list.remove(from);
        list.add(to,obj);
    }
}

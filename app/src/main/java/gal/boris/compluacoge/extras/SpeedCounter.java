package gal.boris.compluacoge.extras;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpeedCounter {

    private List<Long> acceses;
    private int total;

    private static final int MAX_TOTAL_TICKS = 1000;
    private static final int MAX_TICKS_PER_MIN = 20;

    public SpeedCounter() {
        this.acceses = new ArrayList<>();
        this.total = 0;
    }

    public boolean newTick(long tick) {
        acceses.add(tick);
        total++;
        acceses = acceses.stream().filter(t -> tick-t<=60*1000).collect(Collectors.toList());
        return total>= MAX_TOTAL_TICKS || acceses.size()>=MAX_TICKS_PER_MIN;
    }

}

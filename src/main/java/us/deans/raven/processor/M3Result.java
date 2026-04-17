package us.deans.raven.processor;

import java.util.Collections;
import java.util.Map;

public class M3Result {

    private final long mariaDbCount;
    private final long mongoDbCount;
    private final boolean match;
    private final String message;
    private final Map<String, Long> orphans;

    public M3Result(long mariaDbCount, long mongoDbCount, boolean match, String message, Map<String, Long> orphans) {
        this.mariaDbCount = mariaDbCount;
        this.mongoDbCount = mongoDbCount;
        this.match = match;
        this.message = message;
        this.orphans = Collections.unmodifiableMap(orphans);
    }

    public long getMariaDbCount()       { return mariaDbCount; }
    public long getMongoDbCount()       { return mongoDbCount; }
    public boolean isMatch()            { return match; }
    public String getMessage()          { return message; }
    public Map<String, Long> getOrphans() { return orphans; }
}

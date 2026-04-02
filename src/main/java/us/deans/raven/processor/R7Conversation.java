package us.deans.raven.processor;

import java.util.List;

/**
 * Output data object for Operation R7 (Conversation Linking).
 * Represents one root-to-leaf path through the reply tree — a single
 * conversation thread.
 */
public class R7Conversation {

    private final List<R7Post> posts; // ordered: root post first, leaf post last

    public R7Conversation(List<R7Post> posts) {
        this.posts = List.copyOf(posts);
    }

    public List<R7Post> getPosts() {
        return posts;
    }

    public int length() {
        return posts.size();
    }
}

package com.shallowinggg.util.array;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.shallowinggg.util.PreConditions.checkArgument;

/**
 * @author dingshimin
 */
public class ResourceLeakDetector<T> {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceLeakDetector.class);

    private static Set<ResourceLeakTracker<?>> allTracks;
    private final ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
    private String resourceType;

    static {
        allTracks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public ResourceLeakDetector(String resourceType) {
        this.resourceType = resourceType;
    }


    public ResourceLeakTracker<T> track(T obj) {
        reportTrack();
        return new DefaultResourceLeakTracker<>(obj, refQueue, allTracks);
    }

    private void reportTrack() {
        if(!LOG.isErrorEnabled()) {
            return;
        }

        for(;;) {
            @SuppressWarnings("unchecked")
            DefaultResourceLeakTracker<T> tracker = (DefaultResourceLeakTracker<T>) refQueue.poll();
            if(tracker == null) {
                return;
            }

            if(!tracker.dispose()) {
                continue;
            }

            LOG.error("LEAK: {}.free() was not called before it's garbage-collected.", resourceType);
        }
    }


    private static final class DefaultResourceLeakTracker<T> extends WeakReference<T> implements ResourceLeakTracker<T> {
        private Set<ResourceLeakTracker<?>> allTracks;
        private int hash;

        DefaultResourceLeakTracker(T obj, ReferenceQueue<? super T> queue, Set<ResourceLeakTracker<?>> allTracks) {
            super(obj, queue);
            hash = System.identityHashCode(obj);
            allTracks.add(this);
            this.allTracks = allTracks;
        }

        @Override
        public boolean close(T obj) {
            checkArgument(hash == System.identityHashCode(obj));
            if(allTracks.remove(this)) {
                clear();
                return true;
            }
            return false;
        }

        boolean dispose() {
            clear();
            return allTracks.remove(this);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

}

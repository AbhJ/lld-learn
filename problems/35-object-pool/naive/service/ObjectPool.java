/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ObjectPool.java — Generic pool using synchronized ArrayList for idle objects
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ObjectPool<T> { // <T> = generic pool; works with any resource type
    private final List<PooledObject<T>> idleObjects = new ArrayList<>();   // private = internal; guarded by synchronized
    private final List<PooledObject<T>> activeObjects = new ArrayList<>(); // tracks borrowed objects
    private final ObjectFactory<T> factory;            // private final = strategy for creating/destroying objects
    private final ValidationStrategy<T> validator;     // strategy pattern = swappable validation logic
    private final PoolConfig config;

    public ObjectPool(ObjectFactory<T> factory, ValidationStrategy<T> validator, PoolConfig config) {
        this.factory = factory;
        this.validator = validator;
        this.config = config;
        for (int i = 0; i < config.getMinSize(); i++) {
            idleObjects.add(new PooledObject<>(factory.create()));
        }
    }

    public synchronized T borrow() { // synchronized = only one thread borrows/returns at a time
        while (!idleObjects.isEmpty()) {
            PooledObject<T> po = idleObjects.remove(idleObjects.size() - 1);
            if (validator.validate(po.getObject())) {
                po.markBorrowed();
                activeObjects.add(po);
                return po.getObject();
            } else {
                factory.destroy(po.getObject());
            }
        }
        if (activeObjects.size() < config.getMaxSize()) {
            PooledObject<T> po = new PooledObject<>(factory.create());
            po.markBorrowed();
            activeObjects.add(po);
            return po.getObject();
        }
        throw new RuntimeException("Pool exhausted: max=" + config.getMaxSize());
    }

    public synchronized void returnObject(T obj) { // synchronized = prevents concurrent modification
        Iterator<PooledObject<T>> it = activeObjects.iterator();
        while (it.hasNext()) {
            PooledObject<T> po = it.next();
            if (po.getObject() == obj) {
                it.remove();
                po.markReturned();
                if (validator.validate(obj)) {
                    idleObjects.add(po);
                } else {
                    factory.destroy(obj);
                }
                return;
            }
        }
    }

    public synchronized int evict() {
        int evicted = 0;
        Iterator<PooledObject<T>> it = idleObjects.iterator();
        while (it.hasNext() && idleObjects.size() - evicted > config.getMinSize()) {
            PooledObject<T> po = it.next();
            if (po.getIdleTimeMs() > config.getMaxIdleTimeMs()) {
                it.remove();
                factory.destroy(po.getObject());
                evicted++;
            }
        }
        return evicted;
    }

    public synchronized int getIdleCount() { return idleObjects.size(); }
    public synchronized int getActiveCount() { return activeObjects.size(); }
}

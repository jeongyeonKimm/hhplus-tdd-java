package io.hhplus.tdd;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class UserLockManager {

    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public void lockUser(long userId) {
        ReentrantLock lock = lockMap.computeIfAbsent(userId, id -> new ReentrantLock());
        lock.lock();
    }

    public void unlockUser(long userId) {
        ReentrantLock lock = lockMap.get(userId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}

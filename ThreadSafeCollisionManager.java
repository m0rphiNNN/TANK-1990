// ThreadSafeCollisionManager.java
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.awt.Graphics2D;

public class ThreadSafeCollisionManager extends CollisionManager {
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ConcurrentLinkedQueue<Bullet> bulletQueue = new ConcurrentLinkedQueue<>();
    
    @Override
    public void addBullet(Bullet bullet) {
        if (bullet != null) {
            bulletQueue.offer(bullet);
        }
    }
    
    @Override
    public void update() {
        rwLock.writeLock().lock();
        try {
            while (!bulletQueue.isEmpty()) {
                Bullet bullet = bulletQueue.poll();
                if (bullet != null) {
                    super.addBullet(bullet);
                }
            }
            
            super.update();
            
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        rwLock.readLock().lock();
        try {
            super.render(g);
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
// ThreadSafeTank.java
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeTank extends Tank {
    private final ReentrantLock tankLock = new ReentrantLock();
    private volatile boolean shouldStop = false;
    
    public ThreadSafeTank(float x, float y, boolean player, String tankType, ResourceManager resourceManager) {
        super(x, y, player, tankType, resourceManager);
    }
    
    public void threadSafeMove(Direction dir) {
        tankLock.lock();
        try {
            if (!shouldStop && isActive()) {
                move(dir);
            }
        } finally {
            tankLock.unlock();
        }
    }
    
    public Bullet threadSafeShoot() {
        tankLock.lock();
        try {
            if (!shouldStop && isActive()) {
                return shoot();
            }
            return null;
        } finally {
            tankLock.unlock();
        }
    }
    
    public float getThreadSafeX() {
        tankLock.lock();
        try {
            return getX();
        } finally {
            tankLock.unlock();
        }
    }
    
    public float getThreadSafeY() {
        tankLock.lock();
        try {
            return getY();
        } finally {
            tankLock.unlock();
        }
    }
    
    public void stopThread() {
        shouldStop = true;
    }
}
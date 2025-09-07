// EnemyTankWorker.java

import java.util.Random;

public class EnemyTankWorker implements Runnable {
    private ThreadSafeTank enemyTank;
    private Eagle eagle;
    private CollisionManager collisionManager;
    private Random random = new Random();
    private volatile boolean running = true;
    
    public EnemyTankWorker(ThreadSafeTank enemyTank, Eagle eagle, 
                          CollisionManager collisionManager) {
        this.enemyTank = enemyTank;
        this.eagle = eagle;
        this.collisionManager = collisionManager;
    }
    
    @Override
    public void run() {
        while (running && enemyTank.isActive()) {
            try {
                updateEnemyAI();
                
                enemyTank.update();
                
                if (random.nextDouble() < 0.01) {
                    Bullet bullet = enemyTank.threadSafeShoot();
                    if (bullet != null) {
                        collisionManager.addBullet(bullet);
                    }
                }
                
                Thread.sleep(16); 
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Enemy tank error: " + e.getMessage());
            }
        }
    }
    
    private void updateEnemyAI() {
        if (enemyTank.isFrozen()) return;
        
        float targetX = eagle.getX();
        float targetY = eagle.getY();
        
        GameObject.Direction targetDirection = enemyTank.getDirectionToTarget(targetX, targetY);
        
        if (random.nextInt(100) < 60) {
            enemyTank.threadSafeMove(targetDirection);
        } else {
            GameObject.Direction[] directions = GameObject.Direction.values();
            enemyTank.threadSafeMove(directions[random.nextInt(directions.length)]);
        }
    }
    
    public void stop() {
        running = false;
        enemyTank.stopThread();
    }
}
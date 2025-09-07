// TankThreadManager.java
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class TankThreadManager {
    private PlayerTankThread playerThread;
    private ExecutorService enemyThreadPool;
    private List<EnemyTankWorker> enemyWorkers;
    private static final int MAX_ENEMY_THREADS = 4;
    
    public TankThreadManager() {
        enemyThreadPool = Executors.newFixedThreadPool(MAX_ENEMY_THREADS);
        enemyWorkers = new ArrayList<>();
    }
    
    public void startPlayerThread(ThreadSafeTank playerTank, InputHandler inputHandler, 
                                 CollisionManager collisionManager) {
        playerThread = new PlayerTankThread(playerTank, inputHandler, collisionManager);
        playerThread.start();
    }
    
    public void startEnemyThread(ThreadSafeTank enemyTank, Eagle eagle, 
                               CollisionManager collisionManager) {
        EnemyTankWorker worker = new EnemyTankWorker(enemyTank, eagle, collisionManager);
        enemyWorkers.add(worker);
        enemyThreadPool.submit(worker);
    }
    
    public void shutdown() {
        if (playerThread != null) {
            playerThread.stopThread();
            try {
                playerThread.join(1000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        for (EnemyTankWorker worker : enemyWorkers) {
            worker.stop();
        }
        
        enemyThreadPool.shutdown();
        try {
            if (!enemyThreadPool.awaitTermination(2, TimeUnit.SECONDS)) {
                enemyThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            enemyThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        enemyWorkers.clear();
    }
}
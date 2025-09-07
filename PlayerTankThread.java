// PlayerTankThread.java
public class PlayerTankThread extends Thread {
    private ThreadSafeTank playerTank;
    private InputHandler inputHandler;
    private CollisionManager collisionManager;
    private volatile boolean running = true;
    private static final int UPDATE_RATE = 60; 
    
    public PlayerTankThread(ThreadSafeTank playerTank, InputHandler inputHandler, 
                           CollisionManager collisionManager) {
        this.playerTank = playerTank;
        this.inputHandler = inputHandler;
        this.collisionManager = collisionManager;
        setName("PlayerTankThread");
    }
    
    @Override
    public void run() {
        long targetTime = 1000 / UPDATE_RATE;
        
        while (running) {
            long startTime = System.currentTimeMillis();
            
            try {
                handlePlayerInput();
                
                playerTank.update();
                
            } catch (Exception e) {
                System.err.println("Player thread error: " + e.getMessage());
            }
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            long sleepTime = targetTime - elapsedTime;
            
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    private void handlePlayerInput() {
        if (!playerTank.isActive()) return;
        
        if (inputHandler.isUpPressed()) {
            playerTank.setDirection(GameObject.Direction.UP);
            playerTank.threadSafeMove(GameObject.Direction.UP);
        } else if (inputHandler.isDownPressed()) {
            playerTank.setDirection(GameObject.Direction.DOWN);
            playerTank.threadSafeMove(GameObject.Direction.DOWN);
        } else if (inputHandler.isLeftPressed()) {
            playerTank.setDirection(GameObject.Direction.LEFT);
            playerTank.threadSafeMove(GameObject.Direction.LEFT);
        } else if (inputHandler.isRightPressed()) {
            playerTank.setDirection(GameObject.Direction.RIGHT);
            playerTank.threadSafeMove(GameObject.Direction.RIGHT);
        }
        
        if (inputHandler.isFireJustPressed()) {
            Bullet bullet = playerTank.threadSafeShoot();
            if (bullet != null) {
                collisionManager.addBullet(bullet);
            }
        }
    }
    
    public void stopThread() {
        running = false;
        playerTank.stopThread();
    }
}
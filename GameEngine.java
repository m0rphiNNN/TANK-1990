import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameEngine {
    private ResourceManager resourceManager;
    private InputHandler inputHandler;
    private static GameEngine instance;

    private Tank playerTank;
    private static Level currentLevel;
    private CollisionManager collisionManager;
    private List<Tank> allTanks;
    
    private MenuManager menuManager; 
    private GameOverManager gameOverManager;

    
    private static final int GAME_WIDTH = Level.getGridWidth() * Level.CELL_SIZE; 
    private static final int GAME_HEIGHT = Level.getGridHeight() * Level.CELL_SIZE;

    private int currentStage = 1;
    private long stageTransitionStartTime;
    private static final long STAGE_TRANSITION_DURATION = 1500; 
    private int lastLifeScoreThreshold = 20000;

    private TankThreadManager threadManager;
    private boolean useMultithreading = true;

    private Map<PowerUp.PowerUpType, Long> activePowerUps = new HashMap<>();
    private static final long SPEED_BOOST_DURATION = 10000; 

    private enum GameState {
    MENU, PLAYING, GAME_OVER, VICTORY, STAGE_TRANSITION,INITIAL_STAGE_TRANSITION
    }
    
    private GameState currentState;
    private int score;
    
    public GameEngine(ResourceManager resourceManager, InputHandler inputHandler) {
        instance = this;
        this.resourceManager = resourceManager;
        this.inputHandler = inputHandler;
        
        currentState = GameState.MENU;
        score = 0;

        resourceManager.loadArrowImage();
        resourceManager.loadLogoImage();
        menuManager = new MenuManager(resourceManager, GAME_WIDTH, GAME_HEIGHT);

        initializeGame();
    }

    public static GameEngine getInstance() {
        return instance;
    }

    public void addScore(int points) {
    this.score += points;
    
    if (score >= lastLifeScoreThreshold && playerTank != null && playerTank.isActive()) {
        playerTank.addLife();
        lastLifeScoreThreshold += 20000;
    }
    }   

    private void initializeGame() {
        if (useMultithreading) {
            initializeMultithreadedGame();
        } 
        else {
            initializeSingleThreadedGame();
        }
    }

    private void initializeMultithreadedGame() {
    ThreadSafeTank playerTankSafe = new ThreadSafeTank(Level.PLAYER_SPAWN_X, Level.PLAYER_SPAWN_Y, true, "player", resourceManager);
    playerTankSafe.setSpawnPosition(Level.PLAYER_SPAWN_X, Level.PLAYER_SPAWN_Y);
    playerTankSafe.setInvulnerable();
    
    playerTank = playerTankSafe; 
    
    threadManager = new TankThreadManager();
    threadManager.startPlayerThread(playerTankSafe, inputHandler, collisionManager);
    
    initializeCommonGame();
    }     
    
    private void initializeSingleThreadedGame() {
    playerTank = new Tank(Level.PLAYER_SPAWN_X, Level.PLAYER_SPAWN_Y, true, "player", resourceManager);
    playerTank.setSpawnPosition(Level.PLAYER_SPAWN_X, Level.PLAYER_SPAWN_Y);
    playerTank.setInvulnerable();
    
    initializeCommonGame();
    }

    private void initializeCommonGame() {
    currentLevel = new Level(resourceManager);
    
    collisionManager = new CollisionManager();
    collisionManager.setResourceManager(resourceManager);
    
    allTanks = new ArrayList<>();
    allTanks.add(playerTank);
    allTanks.addAll(currentLevel.getEnemies());
    
    collisionManager.setWalls(currentLevel.getWalls());
    collisionManager.setTanks(allTanks);
    collisionManager.setPowerUps(currentLevel.getPowerUps());
    collisionManager.setPlayerTank(playerTank);
    collisionManager.setEagle(currentLevel.getEagle());

    gameOverManager = new GameOverManager(resourceManager, GAME_WIDTH, GAME_HEIGHT);
    }

    public static Level getCurrentLevel() {
        return currentLevel;
    }
    
    public void update() {
    switch (currentState) {
        case MENU:
            updateMenu();
            break;
        case PLAYING:
            updateGame();
            break;
        case GAME_OVER:
        case VICTORY:
            updateGameOver();
            break;
        case STAGE_TRANSITION:
            updateStageTransition();
            break;
        case INITIAL_STAGE_TRANSITION:
                updateInitialStageTransition();
                break;
    }
}
    
    private void updateMenu() {
        menuManager.update(inputHandler);
        
        if (inputHandler.isStartPressed()) {
            currentStage = 1; 
            currentState = GameState.INITIAL_STAGE_TRANSITION;
            stageTransitionStartTime = System.currentTimeMillis();
        }
    }

    private void updateStageTransition() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - stageTransitionStartTime > STAGE_TRANSITION_DURATION) {
            initializeNextStage();
            currentState = GameState.PLAYING;
        }
    }

    private void updateInitialStageTransition() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - stageTransitionStartTime > STAGE_TRANSITION_DURATION) {
            currentState = GameState.PLAYING;
        }
    }

    private void initializeNextStage() {
        int playerHealth = currentLevel.getPlayerHealth();
        
        currentStage++;
        
        currentLevel = new Level(resourceManager, currentStage);
        
        currentLevel.setPlayerHealth(playerHealth);
        
        playerTank = new Tank(Level.PLAYER_SPAWN_X, Level.PLAYER_SPAWN_Y, true, "player", resourceManager);
        playerTank.setSpawnPosition(Level.PLAYER_SPAWN_X, Level.PLAYER_SPAWN_Y);
        playerTank.setHealth(playerHealth);
        playerTank.setInvulnerable();
        
        allTanks.clear();
        allTanks.add(playerTank);
        allTanks.addAll(currentLevel.getEnemies());
        
        collisionManager.setWalls(currentLevel.getWalls());
        collisionManager.setTanks(allTanks);
        collisionManager.setPowerUps(currentLevel.getPowerUps());
        collisionManager.setPlayerTank(playerTank);
        collisionManager.setEagle(currentLevel.getEagle());
    }


    private void updateGame() {
        if (inputHandler.isEscapePressed()) {
            gameOverManager.stopAnimation();
            currentState = GameState.MENU;
            return;
        }
        
        handlePlayerInput();
        
        currentLevel.update();
        
        updateTanksList();
        
        collisionManager.update();
        
        handleEnemyActions();
        
        checkActivePowerUps();
        
        checkGameState();
    }
    
    private void startStageTransition() {
        currentState = GameState.STAGE_TRANSITION;
        stageTransitionStartTime = System.currentTimeMillis();
    }
    
    private void handlePlayerInput() {
        if (!playerTank.isActive()) return;
        
        if (useMultithreading) {
        playerTank.update();
        return;
        }

        if (inputHandler.isUpPressed()) {
            playerTank.setDirection(GameObject.Direction.UP);
            playerTank.move(GameObject.Direction.UP);
        } else if (inputHandler.isDownPressed()) {
            playerTank.setDirection(GameObject.Direction.DOWN);
            playerTank.move(GameObject.Direction.DOWN);
        } else if (inputHandler.isLeftPressed()) {
            playerTank.setDirection(GameObject.Direction.LEFT);
            playerTank.move(GameObject.Direction.LEFT);
        } else if (inputHandler.isRightPressed()) {
            playerTank.setDirection(GameObject.Direction.RIGHT);
            playerTank.move(GameObject.Direction.RIGHT);
        }
        
        if (inputHandler.isFireJustPressed()) {
            Bullet bullet = playerTank.shoot();
            if (bullet != null) {
                collisionManager.addBullet(bullet);
            }
        }
        
        playerTank.update();
    }
    
    private void updateTanksList() {
        allTanks.clear();
        allTanks.add(playerTank);
        allTanks.addAll(currentLevel.getEnemies());
        collisionManager.setTanks(allTanks);
    }
    
    private void handleEnemyActions() {
        for (Tank enemy : currentLevel.getEnemies()) {
            if (enemy.isActive() && Math.random() < 0.01) { 
                collisionManager.addBullet(enemy.shoot());
            }
        }
    }
    
    public void activatePowerUp(PowerUp.PowerUpType type) {
        switch (type) {
            case STAR:
                activePowerUps.put(type, System.currentTimeMillis() + SPEED_BOOST_DURATION);
                break;
            default:
                break;
        }
    }

    private void checkActivePowerUps() {
        long currentTime = System.currentTimeMillis();
        
        if (activePowerUps.containsKey(PowerUp.PowerUpType.STAR) && 
            currentTime > activePowerUps.get(PowerUp.PowerUpType.STAR)) {
            
            playerTank.resetSpeed();
            activePowerUps.remove(PowerUp.PowerUpType.STAR);
        }
    }

    private void updateGameOver() {
        gameOverManager.update();
        
        if (inputHandler.isRestartPressed()) {
            gameOverManager.stopAnimation();
            resetGame();
        }
    }
    
    private void checkGameState() {
    if (!playerTank.isActive() && currentState != GameState.GAME_OVER) {
        currentState = GameState.GAME_OVER;
        gameOverManager.startAnimation(score);
    }
    
    if (currentLevel.isGameOver() && currentState != GameState.GAME_OVER) {
        currentState = GameState.GAME_OVER;
        gameOverManager.startAnimation(score);
    }
    if (currentLevel.isStageCompleted() && currentState == GameState.PLAYING) {
        score += 100 * currentStage; 
        startStageTransition();
    }
}
    
    public void render(Graphics2D g) {
        switch (currentState) {
            case MENU:
                renderMenu(g);
                break;
            case PLAYING:
                renderGame(g);
                break;
            case GAME_OVER:
                renderGameOver(g);
                break;
            case VICTORY:
                renderVictory(g);
                break;
            case STAGE_TRANSITION:
                renderStageTransition(g);
            break;
            case INITIAL_STAGE_TRANSITION:
                renderInitialStageTransition(g);
                break;
        }
    }

    private void renderInitialStageTransition(Graphics2D g) {
        g.setColor(new Color(128, 128, 128)); 
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Press Start 2P", Font.BOLD, 40));
        String stageText = "STAGE  1"; 
        int textWidth = g.getFontMetrics().stringWidth(stageText);
        g.drawString(stageText, GAME_WIDTH/2 - textWidth/2, GAME_HEIGHT/2);
    }    

    private void renderStageTransition(Graphics2D g) {
    
        g.setColor(new Color(128, 128, 128)); 
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
       
         g.setColor(Color.BLACK);
        g.setFont(new Font("Press Start 2P", Font.BOLD, 40));
        String stageText = "STAGE  " + (currentStage +1); 
        int textWidth = g.getFontMetrics().stringWidth(stageText);
        g.drawString(stageText, GAME_WIDTH/2 - textWidth/2, GAME_HEIGHT/2);
    }
    
    private void renderMenu(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        menuManager.render(g);
    }
    
    private void renderGame(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        currentLevel.render(g);
        
        if (playerTank.isActive()) {
            playerTank.render(g);
        }
    
        for (Tank enemy : currentLevel.getEnemies()) {
            if (enemy.isActive()) {
                enemy.render(g);
            }
        }
        
        collisionManager.render(g);
        
        renderUI(g);
    }
    
    private void renderGameOver(Graphics2D g) {
        renderGame(g);
    
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        gameOverManager.render(g);
        
        if (!gameOverManager.isAnimationActive()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to Restart", GAME_WIDTH/2 - 80, GAME_HEIGHT/2 + 50);
        }
    }
    
    private void renderVictory(Graphics2D g) {
        renderGame(g);
        
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("VICTORY!", GAME_WIDTH/2 - 80, GAME_HEIGHT/2 - 50);
        
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + score, GAME_WIDTH/2 - 40, GAME_HEIGHT/2);
        g.drawString("Press R to Play Again", GAME_WIDTH/2 - 100, GAME_HEIGHT/2 + 40);
    }
    
    private void renderUI(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Battle City - Tank 1990", 10, 20);
        
        if (playerTank.isActive()) {
           g.drawString("Lives: " + playerTank.getHealth(), 10, 40);
        g.drawString("Enemies: " + currentLevel.getRemainingEnemies() + "/" + Level.MAX_ENEMIES_PER_STAGE, 10, 60);
        g.drawString("Score: " + score, 10, 80);
        g.drawString("Stage: " + currentStage, 10, 100); 
        }
    }
    
    public void resetGame() {
        if (threadManager != null) {
        threadManager.shutdown();
        threadManager = null;
        }
    
        if (collisionManager != null) {
            collisionManager.resetGameOverState();
        }
        
        if (currentLevel != null) {
            currentLevel.resetGameOverState();
        }
        
        currentStage = 1;
        score = 0;
        initializeGame();
        currentState = GameState.PLAYING;
    }
    
    public boolean isInMenuState() {
        return currentState == GameState.MENU;
    }
    
    public void startGameFromMenu() {
        if (currentState == GameState.MENU) {
            currentStage = 1;
            currentState = GameState.INITIAL_STAGE_TRANSITION;
            stageTransitionStartTime = System.currentTimeMillis();
        }
    }
    
    public void forcePlayerShoot() {
        if (currentState == GameState.PLAYING && playerTank != null && playerTank.isActive()) {
            Bullet bullet = playerTank.shoot();
            if (bullet != null) {
                collisionManager.addBullet(bullet);
            }
        }
    }
    
    public void forceReturnToMenu() {
        if (currentState == GameState.PLAYING || 
            currentState == GameState.GAME_OVER || 
            currentState == GameState.VICTORY) {
            currentState = GameState.MENU;
        }
    }
}
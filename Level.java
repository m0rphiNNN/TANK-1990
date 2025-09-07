import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Level {
    private static final int GRID_WIDTH = 15;  
    private static final int GRID_HEIGHT = 15; 
    public static final int CELL_SIZE = 32;
    private static final int PLAYABLE_AREA_START = 1;
    private static final int PLAYABLE_AREA_SIZE = 13;
    public static final int MAX_ENEMIES_PER_STAGE = 20; 
    public static final int MAX_ACTIVE_ENEMIES = 4;
    private static final long ENEMY_RESPAWN_DELAY = 1000; 
    private static final long POWER_UP_SPAWN_INTERVAL = 30000;
    
    public static final float PLAYER_SPAWN_X = (PLAYABLE_AREA_START + PLAYABLE_AREA_SIZE/2 - 2) * CELL_SIZE;
    public static final float PLAYER_SPAWN_Y = (PLAYABLE_AREA_START + PLAYABLE_AREA_SIZE - 3) * CELL_SIZE;
    private static final int[] FLASHING_TANK_INDEXES = {3, 10, 17}; 
    private static final int[][] ENEMY_SPAWN_POSITIONS_GRID = {
        {PLAYABLE_AREA_START + 1, PLAYABLE_AREA_START + 1},   
        {PLAYABLE_AREA_START + 6, PLAYABLE_AREA_START + 1},   
        {PLAYABLE_AREA_START + 11, PLAYABLE_AREA_START + 1}, 
    };
    
    private List<Wall> walls;
    private List<Tank> enemies;
    private List<PowerUp> powerUps;
    private Eagle eagle;
    private ResourceManager resourceManager;
    private Random random;
    
    private int currentStage = 1;
    private int enemiesKilled = 0;      
    private int enemiesSpawned = 0;     
    private int playerHealth = 3;
    private boolean gameOverTriggered = false;
    private boolean waitingForPowerupSpawn = false;

    private Map<Integer, Long> respawnTimers = new HashMap<>(); 
    private int enemyIdCounter = 0;
    private long lastPowerUpSpawnTime;
    
    public Level(ResourceManager resourceManager) {
        this(resourceManager, 1); 
    }

    public Level(ResourceManager resourceManager, int stage) {
        this.currentStage = stage;
        this.resourceManager = resourceManager;
        this.walls = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.random = new Random();
        this.lastPowerUpSpawnTime = System.currentTimeMillis();
        
        createSimpleLevel();
        createInitialEnemyTanks();
    }
    
    public static int getPlayableAreaStart() { return PLAYABLE_AREA_START; }
    public static int getPlayableAreaSize() { return PLAYABLE_AREA_SIZE; }
    public static int getPlayableGridWidth() { return PLAYABLE_AREA_SIZE; }
    public static int getPlayableGridHeight() { return PLAYABLE_AREA_SIZE; }
    public static int getGridWidth() { return GRID_WIDTH; }
    public static int getGridHeight() { return GRID_HEIGHT; }

    public static boolean isInPlayableArea(float x, float y) {
        float minX = PLAYABLE_AREA_START * CELL_SIZE;
        float maxX = (PLAYABLE_AREA_START + PLAYABLE_AREA_SIZE) * CELL_SIZE - 1;
        float minY = PLAYABLE_AREA_START * CELL_SIZE;
        float maxY = (PLAYABLE_AREA_START + PLAYABLE_AREA_SIZE) * CELL_SIZE - 1;
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
    
    public void setPlayerHealth(int health) { this.playerHealth = health; }
    public int getPlayerHealth() { return playerHealth; }
    public boolean isStageCompleted() { return enemiesKilled >= MAX_ENEMIES_PER_STAGE && !gameOverTriggered; }
    public int getRemainingEnemies() { return MAX_ENEMIES_PER_STAGE - enemiesKilled; }
    public void enemyKilled() { enemiesKilled++; }
    public void triggerGameOver() { gameOverTriggered = true; }
    public List<Wall> getWalls() { return walls; }
    public List<Tank> getEnemies() { return enemies; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public Eagle getEagle() { return eagle; }
    public boolean isGameOver() { return eagle.isDestroyed() || gameOverTriggered; }
    public void resetGameOverState() { gameOverTriggered = false; }

    private void createInitialEnemyTanks() {
        int maxInitialEnemies = Math.min(MAX_ACTIVE_ENEMIES, ENEMY_SPAWN_POSITIONS_GRID.length);
        
        for (int i = 0; i < maxInitialEnemies; i++) {
            if (enemiesSpawned >= MAX_ENEMIES_PER_STAGE) {
                break;
            }
            
            String tankType = getTankTypeForStage();
            float x = ENEMY_SPAWN_POSITIONS_GRID[i][0] * CELL_SIZE;
            float y = ENEMY_SPAWN_POSITIONS_GRID[i][1] * CELL_SIZE;
            
            Tank enemyTank = new Tank(x, y, false, tankType, resourceManager);
            enemyTank.setId(++enemyIdCounter);
            
            boolean isFlashingTank = false;
            for (int idx : FLASHING_TANK_INDEXES) {
                if (enemiesSpawned == idx) {
                    isFlashingTank = true;
                    break;
                }
            }
            enemyTank.setFlashing(isFlashingTank);
            
            enemies.add(enemyTank);
            enemiesSpawned++;
        }
    }

    private String getTankTypeForStage() {
        String[] tankTypes = {"basic", "fast", "power", "armor"};
        int typeIndex;
        
        if (currentStage <= 2) {
            int roll = random.nextInt(100);
            if (roll < 70) typeIndex = 0;
            else if (roll < 90) typeIndex = 1;
            else typeIndex = 2;
        } 
        else if (currentStage <= 5) {
            int roll = random.nextInt(100);
            if (roll < 40) typeIndex = 0;
            else if (roll < 70) typeIndex = 1;
            else if (roll < 90) typeIndex = 2;
            else typeIndex = 3;
        }
        else {
            int roll = random.nextInt(100);
            if (roll < 20) typeIndex = 0;
            else if (roll < 50) typeIndex = 1;
            else if (roll < 80) typeIndex = 2;
            else typeIndex = 3;
        }
        
        return tankTypes[typeIndex];
    }

    private void createSimpleLevel() {
    eagle = new Eagle((PLAYABLE_AREA_START + PLAYABLE_AREA_SIZE/2) * CELL_SIZE, 
                   (PLAYABLE_AREA_START + PLAYABLE_AREA_SIZE - 1) * CELL_SIZE, 
                   resourceManager);
        
        if (currentStage == 1) {
            createStage1Map();
        } else {
            createRandomMap();
        }
        
        resetEagleProtectionWalls();
    }

    private void createStage1Map() {
        String[] mapLayout = {
            "   S   S     ",
            "TB S   B B B ", 
            "TB TTT B BSB ",
            "T  B  BB S  I", 
            "T  B BS  BTBS",
            "TT B B  SBT  ", 
            " BBB TTSB TB ", 
            "   STB B B B ",
            "SBWS B B WWBW", 
            "WB B BBB BSBW", 
            "TB B        W", 
            "WB   BB  B BW", 
            "WB B B B BBB "  
        };

        boolean[][] protectedCells = new boolean[PLAYABLE_AREA_SIZE][PLAYABLE_AREA_SIZE];
        
        for (int[] spawnPos : ENEMY_SPAWN_POSITIONS_GRID) {
            int x = spawnPos[0] - PLAYABLE_AREA_START;
            int y = spawnPos[1] - PLAYABLE_AREA_START;
            
            if (x >= 0 && x < PLAYABLE_AREA_SIZE && y >= 0 && y < PLAYABLE_AREA_SIZE) {
                protectedCells[x][y] = true;
            }
        }

        for (int y = 0; y < mapLayout.length; y++) {
            String row = mapLayout[y];
            for (int x = 0; x < row.length(); x++) {
                char cell = row.charAt(x);
                
                if (x < PLAYABLE_AREA_SIZE && y < PLAYABLE_AREA_SIZE && protectedCells[x][y]) {
                    continue;
                }
                
                int worldX = (PLAYABLE_AREA_START + x) * CELL_SIZE;
                int worldY = (PLAYABLE_AREA_START + y) * CELL_SIZE;
                
                switch (cell) {
                    case 'B': 
                        walls.add(new Wall(worldX, worldY, Wall.WallType.BRICK, resourceManager));
                        break;
                    case 'S': 
                        walls.add(new Wall(worldX, worldY, Wall.WallType.STEEL, resourceManager));
                        break;
                    case 'W': 
                        walls.add(new Wall(worldX, worldY, Wall.WallType.WATER, resourceManager));
                        break;
                    case 'T': 
                        walls.add(new Wall(worldX, worldY, Wall.WallType.TREES, resourceManager));
                        break;
                    case 'I':
                    walls.add(new Wall(worldX, worldY, Wall.WallType.ICE, resourceManager));
                    break;
                }
            }
        }
    }
    
    private void createRandomMap() {
    int playerX = (int)PLAYER_SPAWN_X / CELL_SIZE;
    int playerY = (int)PLAYER_SPAWN_Y / CELL_SIZE;
    
    int eagleX = (int)eagle.getX() / CELL_SIZE;
    int eagleY = (int)eagle.getY() / CELL_SIZE;
    
    boolean[][] protectedCells = new boolean[GRID_WIDTH][GRID_HEIGHT];
    
    for (int[] spawnPos : ENEMY_SPAWN_POSITIONS_GRID) {
        int x = spawnPos[0];
        int y = spawnPos[1];
        
        for (int i = x-1; i <= x+1; i++) {
            for (int j = y-1; j <= y+1; j++) {
                if (i >= 0 && i < GRID_WIDTH && j >= 0 && j < GRID_HEIGHT) {
                    protectedCells[i][j] = true;
                }
            }
        }
    }

    for (int i = 0; i < 25; i++) {
        int x = 1 + random.nextInt(GRID_WIDTH - 2);
        int y = 1 + random.nextInt(GRID_HEIGHT - 2);

        if (isValidWallPosition(x, y, playerX, playerY, eagleX, eagleY) && !protectedCells[x][y]) {
            walls.add(new Wall(x * CELL_SIZE, y * CELL_SIZE, Wall.WallType.BRICK, resourceManager));
        }
    }

        for (int i = 0; i < 4; i++) {
            int x = 1 + random.nextInt(GRID_WIDTH - 2);
            int y = 1 + random.nextInt(GRID_HEIGHT - 2);
            if (isValidWallPosition(x, y, playerX, playerY, eagleX, eagleY)) {
                walls.add(new Wall(x * CELL_SIZE, y * CELL_SIZE, Wall.WallType.WATER, resourceManager));
            }

            x = 1 + random.nextInt(GRID_WIDTH - 2);
            y = 1 + random.nextInt(GRID_HEIGHT - 2);
            if (isValidWallPosition(x, y, playerX, playerY, eagleX, eagleY)) {
                walls.add(new Wall(x * CELL_SIZE, y * CELL_SIZE, Wall.WallType.ICE, resourceManager));
            }

            x = 1 + random.nextInt(GRID_WIDTH - 2);
            y = 1 + random.nextInt(GRID_HEIGHT - 2);
            if (isValidWallPosition(x, y, playerX, playerY, eagleX, eagleY)) {
                walls.add(new Wall(x * CELL_SIZE, y * CELL_SIZE, Wall.WallType.TREES, resourceManager));
            }
        }
    }

    public void resetEagleProtectionWalls() {
        int eagleX = (int)eagle.getX() / CELL_SIZE;
        int eagleY = (int)eagle.getY() / CELL_SIZE;

        walls.removeIf(wall -> 
            (Math.abs(wall.getX() / CELL_SIZE - eagleX) <= 1 && Math.abs(wall.getY() / CELL_SIZE - eagleY) <= 1)
        );

        walls.add(new Wall((eagleX-1) * CELL_SIZE, eagleY * CELL_SIZE, Wall.WallType.BRICK, resourceManager));
        walls.add(new Wall(eagleX * CELL_SIZE, (eagleY-1) * CELL_SIZE, Wall.WallType.BRICK, resourceManager));
        walls.add(new Wall((eagleX+1) * CELL_SIZE, eagleY * CELL_SIZE, Wall.WallType.BRICK, resourceManager));

        walls.add(new Wall((eagleX-1) * CELL_SIZE, (eagleY-1) * CELL_SIZE, Wall.WallType.BRICK, resourceManager));
        walls.add(new Wall((eagleX+1) * CELL_SIZE, (eagleY-1) * CELL_SIZE, Wall.WallType.BRICK, resourceManager));
    }
    
    private boolean isValidWallPosition(int x, int y, int playerX, int playerY, int eagleX, int eagleY) {
        if (Math.abs(x - playerX) <= 1 && Math.abs(y - playerY) <= 1) {
            return false;
        }
        
        if (Math.abs(x - eagleX) <= 1 && Math.abs(y - eagleY) <= 1) {
            return false;
        }
        
        for (int[] enemyPos : ENEMY_SPAWN_POSITIONS_GRID) {
            int enemyX = enemyPos[0];
            int enemyY = enemyPos[1];
            
            if (Math.abs(x - enemyX) <= 1 && Math.abs(y - enemyY) <= 1) {
                return false;
            }
        }
        
        return true;
    }

    public void addNewEnemyTank() {
        if (enemiesSpawned >= MAX_ENEMIES_PER_STAGE) {
            return;
        }
        
        if (enemies.size() >= MAX_ACTIVE_ENEMIES) {
            return;
        }
        
        String tankType = getTankTypeForStage();
        
        int spawnIndex = random.nextInt(ENEMY_SPAWN_POSITIONS_GRID.length);
        float x = ENEMY_SPAWN_POSITIONS_GRID[spawnIndex][0] * CELL_SIZE;
        float y = ENEMY_SPAWN_POSITIONS_GRID[spawnIndex][1] * CELL_SIZE;
        
        boolean positionClear = true;
        for (Tank tank : enemies) {
            if (tank.isActive() && 
                Math.abs(tank.getX() - x) < CELL_SIZE && 
                Math.abs(tank.getY() - y) < CELL_SIZE) {
                positionClear = false;
                break;
            }
        }
        if (positionClear) {
            Tank newTank = new Tank(x, y, false, tankType, resourceManager);
            newTank.setId(++enemyIdCounter);

            boolean isFlashingTank = false;
            for (int idx : FLASHING_TANK_INDEXES) {
                if (enemiesSpawned == idx) {
                    isFlashingTank = true;
                    break;
                }
            }
            newTank.setFlashing(isFlashingTank);

            if (isFlashingTank) {
                powerUps.clear();
            }
            
            enemies.add(newTank);
            enemiesSpawned++;
        }
    }

    public void scheduleEnemyRespawn(int enemyId) {
        enemiesKilled++;
        
        Tank killedTank = findTankById(enemyId);
        if (killedTank != null && killedTank.isFlashing()) {
            waitingForPowerupSpawn = true;
            spawnPowerUp();
        }
        
        if (enemiesSpawned < MAX_ENEMIES_PER_STAGE) {
            respawnTimers.put(enemyId, System.currentTimeMillis() + ENEMY_RESPAWN_DELAY);
        }
    }

     private Tank findTankById(int id) {
        for (Tank tank : enemies) {
            if (tank.getId() == id) {
                return tank;
            }
        }
        return null;
    }

    private void spawnPowerUp() {
        if (!waitingForPowerupSpawn) return;
        
        powerUps.clear();
        
        int[] spawnPos;
        boolean validPosition;
        int x, y;
        
    do {
        validPosition = true;
        spawnPos = PowerUp.getRandomSpawnLocation(random);
        x = spawnPos[0] + Level.getPlayableAreaStart();
        y = spawnPos[1] + Level.getPlayableAreaStart();
            
        int eagleX = (int)eagle.getX() / CELL_SIZE;
        int eagleY = (int)eagle.getY() / CELL_SIZE;
            
         if (Math.abs(x - eagleX) < 2 && Math.abs(y - eagleY) < 2) {
            validPosition = false;
            continue;
        }
        for (Wall wall : walls) {
            if (wall.isActive() && 
                x * CELL_SIZE == wall.getX() && 
                y * CELL_SIZE == wall.getY() &&
                wall.blocksMovement()) { 
                validPosition = false;
                break;
            }
        }
    } while (!validPosition);

    PowerUp.PowerUpType[] types = PowerUp.PowerUpType.values();
    PowerUp.PowerUpType randomType = types[random.nextInt(types.length)];
    
    powerUps.add(new PowerUp(x * CELL_SIZE, y * CELL_SIZE, randomType, resourceManager));
    waitingForPowerupSpawn = false;
    }

        public void render(Graphics2D g) {
        drawBoundaries(g);

        for (Wall wall : walls) {
            if (wall.isActive() && wall.getWallType() != Wall.WallType.TREES) {
                wall.render(g);
            }
        }
        
        eagle.render(g);

        for (PowerUp powerUp : powerUps) {
            if (powerUp.isActive()) {
                powerUp.render(g);
            }
        }
        
        for (Wall wall : walls) {
            if (wall.isActive() && wall.getWallType() == Wall.WallType.TREES) {
                wall.render(g);
            }
        }
    }

    private void drawBoundaries(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);

        g.fillRect(0, 0, GRID_WIDTH * CELL_SIZE, PLAYABLE_AREA_START * CELL_SIZE);

        g.fillRect(0, (PLAYABLE_AREA_START + PLAYABLE_AREA_SIZE) * CELL_SIZE, 
                 GRID_WIDTH * CELL_SIZE, (GRID_HEIGHT - PLAYABLE_AREA_START - PLAYABLE_AREA_SIZE) * CELL_SIZE);

        g.fillRect(0, 0, PLAYABLE_AREA_START * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);

        g.fillRect((PLAYABLE_AREA_START + PLAYABLE_AREA_SIZE) * CELL_SIZE, 0, 
                 (GRID_WIDTH - PLAYABLE_AREA_START - PLAYABLE_AREA_SIZE) * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
    }
    
    public void update() {
        enemies.removeIf(enemy -> !enemy.isActive());

        walls.removeIf(wall -> !wall.isActive());
     
        powerUps.removeIf(powerUp -> !powerUp.isActive());
        
        for (PowerUp powerUp : powerUps) {
            powerUp.update();
        }
        
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Long>> it = respawnTimers.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry<Integer, Long> entry = it.next();
            if (currentTime >= entry.getValue()) {
                addNewEnemyTank(); 
                it.remove();
            }
        }
        
        if (currentTime - lastPowerUpSpawnTime > POWER_UP_SPAWN_INTERVAL) {
            spawnRandomPowerUp();
            lastPowerUpSpawnTime = currentTime;
        }
        
        for (Tank enemy : enemies) {
            if (!enemy.isActive()) continue;
            
             if (enemy.isFrozen()) {
                enemy.update();
                continue;
            }
            
            float targetX = eagle.getX();
            float targetY = eagle.getY();
            
            GameObject.Direction targetDirection = enemy.getDirectionToTarget(targetX, targetY);
            
            if (random.nextInt(100) < 60) {
                enemy.move(targetDirection);
            } else {
                GameObject.Direction[] directions = GameObject.Direction.values();
                enemy.move(directions[random.nextInt(directions.length)]);
            }
            
            enemy.update();
        }
        
        if (enemies.size() < MAX_ACTIVE_ENEMIES && enemiesSpawned < MAX_ENEMIES_PER_STAGE && random.nextInt(100) < 5) {
            addNewEnemyTank();
        }
    }
    
    private void spawnRandomPowerUp() {
        int x, y;
        boolean validPosition;
        
        do {
            validPosition = true;
            x = 2 + random.nextInt(GRID_WIDTH - 4);
            y = 2 + random.nextInt(GRID_HEIGHT - 4);
            
            for (Wall wall : walls) {
                if (wall.isActive() && 
                    x * CELL_SIZE == wall.getX() && 
                    y * CELL_SIZE == wall.getY()) {
                    validPosition = false;
                    break;
                }
            }
            
            for (Tank tank : enemies) {
                if (tank.isActive() &&
                    Math.abs(x * CELL_SIZE - tank.getX()) < CELL_SIZE &&
                    Math.abs(y * CELL_SIZE - tank.getY()) < CELL_SIZE) {
                    validPosition = false;
                    break;
                }
            }
            
            if (Math.abs(x * CELL_SIZE - eagle.getX()) < CELL_SIZE &&
                Math.abs(y * CELL_SIZE - eagle.getY()) < CELL_SIZE) {
                validPosition = false;
            }
            
        } while (!validPosition);
        
        PowerUp.PowerUpType[] types = PowerUp.PowerUpType.values();
        PowerUp.PowerUpType randomType = types[random.nextInt(types.length)];
        
        powerUps.add(new PowerUp(x * CELL_SIZE, y * CELL_SIZE, randomType, resourceManager));
    }
}
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CollisionManager {
    private List<Wall> walls;
    private List<Tank> tanks;
    private List<Bullet> bullets;
    private List<PowerUp> powerUps;
    private Tank playerTank;
    private Eagle eagle;
    private ResourceManager resourceManager;
    private List<Explosion> explosions = new ArrayList<>();

    private boolean gameOverScheduled = false;
    private static final long GAME_OVER_DELAY = 2000;
    private static final long SHOVEL_DURATION = 15000;

    public CollisionManager() {
        bullets = new ArrayList<>();
    }
    
    public void setWalls(List<Wall> walls) {
        this.walls = walls;
    }
    
    public void setTanks(List<Tank> tanks) {
        this.tanks = tanks;
    }
    
    public void setPowerUps(List<PowerUp> powerUps) {
        this.powerUps = powerUps;
    }
    
    public void setPlayerTank(Tank playerTank) {
        this.playerTank = playerTank;
    }
    
    public void setEagle(Eagle eagle) {
        this.eagle = eagle;
    }
    
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
    
    public void addBullet(Bullet bullet) {
        if (bullet != null) {
            bullets.add(bullet);
        }
    }

    private Level getLevelReference() {
        return GameEngine.getCurrentLevel();
    }

    public void update() {
    checkPlayerBulletLimits();

    for (int i = bullets.size() - 1; i >= 0; i--) {
        Bullet bullet = bullets.get(i);
        bullet.update();
        
        if (!bullet.isActive()) {
            bullets.remove(i);
            continue;
        }
        
        if (walls != null) {
            for (Wall wall : walls) {
                if (wall.isActive() && bullet.intersects(wall)) {
                    if (wall.blocksBullet()) {
                        createExplosion(bullet.getX(), bullet.getY());
                        if (wall.getWallType() == Wall.WallType.STEEL && 
                            bullet.isFromPlayer() && bullet.isSteelPiercing()) {
                            wall.setActive(false);
                        } else if (wall.getWallType() == Wall.WallType.STEEL) {
                            bullet.setActive(false);
                        } else {
                            bullet.setActive(false);
                            if (wall.isDestructible()) {
                                wall.setActive(false);
                            }
                        }
                        break;
                    }
                }
            }
        }

        for (int k = explosions.size() - 1; k >= 0; k--) {
        Explosion explosion = explosions.get(k);
        explosion.update();
        
        if (!explosion.isActive()) {
            explosions.remove(k);
            }
        }
        
        if (eagle != null && bullet.intersects(eagle) && !eagle.isDestroyed()) {
            bullet.setActive(false);
            eagle.destroy();

            if (!gameOverScheduled) {
                gameOverScheduled = true;
                scheduleGameOver();
            }

            break;
        }
        
        if (tanks != null) {
            for (Tank tank : tanks) {
                if (!bullet.isActive()) break;
                
                if (tank.isActive() && bullet.intersects(tank)) {
                    if (bullet.isFromPlayer() != tank.isPlayer()) {
                        createExplosion(bullet.getX(), bullet.getY());

                        bullet.setActive(false);
                        tank.takeDamage();
                        
                        if (!tank.isActive() && tank.isPlayer()) {
                            Level level = getLevelReference();
                            if (level != null) {
                                level.setPlayerHealth(tank.getHealth());
                            }
                            
                            if (!gameOverScheduled) {
                                gameOverScheduled = true;
                                scheduleGameOver();
                            }
                        }

                        if (!tank.isActive() && !tank.isPlayer() && bullet.isFromPlayer()) {
                            GameEngine gameEngine = GameEngine.getInstance();
                             if (gameEngine != null) {
                                gameEngine.addScore(tank.getPointValue());
                            }
                            
                            Level level = getLevelReference();
                            if (level != null) {
                                level.scheduleEnemyRespawn(tank.getId());
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        for (int j = i - 1; j >= 0; j--) {
            Bullet otherBullet = bullets.get(j);
            if (bullet.isActive() && otherBullet.isActive() && bullet.intersects(otherBullet)) {
                 float midX = (bullet.getX() + otherBullet.getX()) / 2;
                float midY = (bullet.getY() + otherBullet.getY()) / 2;
                createExplosion(midX, midY);

                bullet.setActive(false);
                otherBullet.setActive(false);
                break;
            }
        }
    }
    
    if (tanks != null && powerUps != null) {
    for (Tank tank : tanks) {
        if (tank.isActive()) {
            for (PowerUp powerUp : powerUps) {
                if (powerUp.isActive() && tank.intersects(powerUp)) {
                    if (tank.isPlayer()) {
                        applyPowerUp(powerUp); 
                    } else {
                        applyEnemyPowerUp(tank, powerUp); 
                    }
                    powerUp.setActive(false);
                    break; 
                }
            }
        }
    }
    }
    
    handleTankCollisions();
    }
    private void scheduleGameOver() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Level level = getLevelReference();
                if (level != null) {
                    level.triggerGameOver();
                }
                timer.cancel();
            }
        }, GAME_OVER_DELAY);
    }
    
    private void handleTankCollisions() {
        if (tanks == null) return;
        
        for (Tank tank : tanks) {
            if (!tank.isActive()) continue;
            
            if (walls != null) {
                for (Wall wall : walls) {
                    if (wall.isActive() && wall.blocksMovement() && tank.intersects(wall)) {
                        pushBackTank(tank);
                        break;
                    }
                }
            }

            if (eagle != null && tank.intersects(eagle)) {
                if (eagle.blocksMovement()) {
                    pushBackTank(tank);
                }
            }
            
            for (Tank otherTank : tanks) {
                if (tank != otherTank && tank.isActive() && otherTank.isActive() && tank.intersects(otherTank)) {
                    pushBackTank(tank);
                    break;
                }
            }
        }
    }

    private void applyEnemyPowerUp(Tank enemyTank, PowerUp powerUp) {
    if (powerUp == null || enemyTank == null) return;

    try {
        switch (powerUp.getType()) {
            case HELMET:
                enemyTank.setInvulnerable();
                break;
            case STAR:
                enemyTank.increasePowerLevel(); 
                break;
            case TIMER:
                if (playerTank != null && playerTank.isActive()) {
                    playerTank.freeze();
                    new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                playerTank.unfreeze();
                            }
                        }, 
                        3000
                    );
                }
                break;
            case GRENADE:
                if (playerTank != null && playerTank.isActive()) {
                    playerTank.takeDamage();
                }
                break;
            case SHOVEL:
            clearBricksAroundEnemyTank(enemyTank);
            case TANK:
                break;
        }
    } catch (Exception e) {
        System.out.println("DÜŞMAN POWER-UP HATASI");
    }
    }

    private void clearBricksAroundEnemyTank(Tank enemyTank) {
    if (walls == null || enemyTank == null) return;
    
    int tankGridX = (int)(enemyTank.getX() / 32);
    int tankGridY = (int)(enemyTank.getY() / 32);
    
    
    int[][] directions = {
        {-1, -1}, {0, -1}, {1, -1}, 
        {-1,  0},          {1,  0},  
        {-1,  1}, {0,  1}, {1,  1}   
    };
    
    List<Wall> wallsToRemove = new ArrayList<>();
    
    for (int[] dir : directions) {
        int checkX = tankGridX + dir[0];
        int checkY = tankGridY + dir[1];
        
        for (Wall wall : walls) {
            if (wall.isActive() && wall.getWallType() == Wall.WallType.BRICK) {
                int wallGridX = (int)(wall.getX() / 32);
                int wallGridY = (int)(wall.getY() / 32);
                
                if (wallGridX == checkX && wallGridY == checkY) {
                    wallsToRemove.add(wall);
                    
                    System.out.println("Düşman tank brick kaldırdı: (" + checkX + "," + checkY + ")");
                }
            }
        }
    }
    
    for (Wall wall : wallsToRemove) {
        wall.setActive(false);
    }
    }
    
    private void pushBackTank(Tank tank) {
        switch (tank.getDirection()) {
            case UP:    tank.setY(tank.getY() + 2); break;
            case DOWN:  tank.setY(tank.getY() - 2); break;
            case LEFT:  tank.setX(tank.getX() + 2); break;
            case RIGHT: tank.setX(tank.getX() - 2); break;
        }
    }
    
    private void applyPowerUp(PowerUp powerUp) {
    if (powerUp == null || playerTank == null) return;

    try {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine != null) {
            gameEngine.addScore(PowerUp.POINTS_VALUE);
        }
        
        switch (powerUp.getType()) {
            case HELMET:
                playerTank.setInvulnerable();
                break;
            case SHOVEL:
                protectEagleWithSteel();
                break;
            case STAR:
                playerTank.increasePowerLevel();
                break;
            case GRENADE:
                destroyAllEnemies();
                break;
            case TANK:
                playerTank.addLife();
                break;
            case TIMER:
                freezeEnemies();
                break;
        }
    } catch (Exception e) {
        System.out.println("HATA");
    }
}
    
    private void protectEagleWithSteel() {
        if (eagle == null || walls == null || resourceManager == null) return;
        
        int eagleX = (int)eagle.getX() / 32;
        int eagleY = (int)eagle.getY() / 32;

        final List<Wall> oldWalls = new ArrayList<>();
        List<Wall> wallsToRemove = new ArrayList<>();
        
        for (Wall wall : walls) {
        if (Math.abs(wall.getX() / 32 - eagleX) <= 1 && Math.abs(wall.getY() / 32 - eagleY) <= 1) {
            oldWalls.add(wall);
            wallsToRemove.add(wall);
        }
    }
    
        walls.removeAll(wallsToRemove);
        
        walls.add(new Wall((eagleX-1) * 32, eagleY * 32, Wall.WallType.STEEL, resourceManager));
        walls.add(new Wall(eagleX * 32, (eagleY-1) * 32, Wall.WallType.STEEL, resourceManager));
        walls.add(new Wall((eagleX+1) * 32, eagleY * 32, Wall.WallType.STEEL, resourceManager));
        walls.add(new Wall((eagleX-1) * 32, (eagleY-1) * 32, Wall.WallType.STEEL, resourceManager));
        walls.add(new Wall((eagleX+1) * 32, (eagleY-1) * 32, Wall.WallType.STEEL, resourceManager));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                walls.removeIf(wall -> 
                    (Math.abs(wall.getX() / 32 - eagleX) <= 1 && Math.abs(wall.getY() / 32 - eagleY) <= 1)
                );
                
                walls.addAll(oldWalls);
            }
        }, SHOVEL_DURATION);

        walls.removeIf(wall -> 
            (Math.abs(wall.getX() / 32 - eagleX) <= 1 && Math.abs(wall.getY() / 32 - eagleY) <= 1)
        );
        
        walls.add(new Wall((eagleX-1) * 32, eagleY * 32, Wall.WallType.STEEL, resourceManager));
        walls.add(new Wall(eagleX * 32, (eagleY-1) * 32, Wall.WallType.STEEL, resourceManager));
        walls.add(new Wall((eagleX+1) * 32, eagleY * 32, Wall.WallType.STEEL, resourceManager));
        
        walls.add(new Wall((eagleX-1) * 32, (eagleY-1) * 32, Wall.WallType.STEEL, resourceManager));
        walls.add(new Wall((eagleX+1) * 32, (eagleY-1) * 32, Wall.WallType.STEEL, resourceManager));
    }
    
    private void destroyAllEnemies() {
        if (tanks == null) return;
        
        Level level = getLevelReference();
        if (level == null) return;

        for (Tank tank : tanks) {
        if (tank != null && !tank.isPlayer() && tank.isActive()) {
            tank.setDestroyedByGrenade(true);
            tank.setActive(false);
        
            level.scheduleEnemyRespawn(tank.getId());
        }
    }
    }
    
    private void freezeEnemies() {
    if (tanks == null) return;
    
    for (Tank tank : tanks) {
        if (tank != null && !tank.isPlayer() && tank.isActive()) {
            tank.freeze();
            
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        tank.unfreeze();
                    }
                }, 
                5000
            );
        }
    }
}
    
    public void render(Graphics2D g) {
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                bullet.render(g);
            }
        }

        for (Explosion explosion : explosions) {
        if (explosion.isActive()) {
            explosion.render(g);
            }
        }
    }

public void checkPlayerBulletLimits() {
    if (playerTank == null) return;
    
    int activeBulletCount = 0;
    for (Bullet bullet : bullets) {
        if (bullet.isActive() && bullet.isFromPlayer()) {
            activeBulletCount++;
        }
    }
    
    int maxBullets = (playerTank.getPowerLevel() >= 2) ? 2 : 1;
    
    if (activeBulletCount > maxBullets) {
        int count = activeBulletCount;
        for (int i = bullets.size() - 1; i >= 0 && count > maxBullets; i--) {
            Bullet bullet = bullets.get(i);
            if (bullet.isActive() && bullet.isFromPlayer()) {
                bullet.setActive(false);
                count--;
            }
        }
    }
    }

    public int getActiveBulletCount() {
        int count = 0;
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) count++;
        }
        return count;
    }

    public void resetGameOverState() {
        gameOverScheduled = false;
    }

    private void createExplosion(float x, float y) {
    explosions.add(new Explosion(x, y, resourceManager));
    }
}
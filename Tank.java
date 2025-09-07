import java.awt.*;

public class Tank extends GameObject {
    
    private static final float DEFAULT_SPEED = 2.0f;
    private static final long INVULNERABILITY_DURATION = 1500; 
    private static final int MAX_POWER_LEVEL = 3;
    
    private ResourceManager resourceManager;
    private boolean player;
    private String tankType;
    private int id;
    
    private int health;
    private int maxHealth;
    private float spawnX, spawnY;
    
    private float speed;
    private boolean frozen = false;
    
    private boolean invulnerable = false;
    private long invulnerabilityEndTime = 0;
    
    private long lastShotTime;

    private boolean flashing = false;
    private boolean destroyedByGrenade = false;
    private int powerLevel = 0;

    
    public Tank(float x, float y, boolean player, String tankType, ResourceManager resourceManager) {
        super(x, y, 32, 32);
        this.player = player;
        this.tankType = tankType;
        this.resourceManager = resourceManager;
        this.speed = DEFAULT_SPEED;
        
        if (player) {
            this.maxHealth = 3;
        } else {
            if (tankType.equals("armor")) {
                this.maxHealth = 2;
            } else {
                this.maxHealth = 1;
            }
        }
        
        this.health = this.maxHealth;
        this.lastShotTime = 0;
        
        this.spawnX = x;
        this.spawnY = y;
    }
    
    public void freeze() {
        frozen = true;
        speed = 0;
    }

    public void unfreeze() {
        frozen = false;
        resetSpeed();
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setDestroyedByGrenade(boolean value) {
        this.destroyedByGrenade = value;
    }

    public boolean wasDestroyedByGrenade() {
        return destroyedByGrenade;
    }

    public boolean isFlashing() {
        return flashing;
    }
    
    public void setFlashing(boolean flashing) {
        this.flashing = flashing;
    }
    
    public void increasePowerLevel() {
        if (powerLevel < MAX_POWER_LEVEL) {
            powerLevel++;
        }
    }
    
    public int getPowerLevel() {
        return powerLevel;
    }
    
    public void resetPowerLevel() {
        powerLevel = 0;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public void setHealth(int health) {
        this.maxHealth = health;
        this.health = health;
    }

    @Override
    public void update() {
        updateBounds();
        
        if (invulnerable && System.currentTimeMillis() > invulnerabilityEndTime) {
            invulnerable = false;
        }
    }
    
     @Override
    public void render(Graphics2D g) {
        if (flashing && (System.currentTimeMillis() / 150) % 2 == 0) {
            Composite originalComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            drawTank(g);
            g.setComposite(originalComposite);
        } else if (invulnerable && (System.currentTimeMillis() / 200) % 2 == 0) {
            Composite originalComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            drawTank(g);
            g.setComposite(originalComposite);
        } else {
            drawTank(g);
        }
    }
    
    private void drawTank(Graphics2D g) {
        String imageKey = player ? "tank_player_" : "enemy_" + tankType + "_";
        
        switch (direction) {
            case UP:
                g.drawImage(resourceManager.getImage(imageKey + "up"), (int)x, (int)y, null);
                break;
            case DOWN:
                g.drawImage(resourceManager.getImage(imageKey + "down"), (int)x, (int)y, null);
                break;
            case LEFT:
                g.drawImage(resourceManager.getImage(imageKey + "left"), (int)x, (int)y, null);
                break;
            case RIGHT:
                g.drawImage(resourceManager.getImage(imageKey + "right"), (int)x, (int)y, null);
                break;
        }
    }
    
    public void move(Direction dir) {
        this.direction = dir;
        
        float newX = x;
        float newY = y;
        
        switch (dir) {
            case UP:
                newY -= speed;
                break;
            case DOWN:
                newY += speed;
                break;
            case LEFT:
                newX -= speed;
                break;
            case RIGHT:
                newX += speed;
                break;
        }

    if (Level.isInPlayableArea(newX, newY) && 
        Level.isInPlayableArea(newX + width - 1, newY) &&
        Level.isInPlayableArea(newX, newY + height - 1) && 
        Level.isInPlayableArea(newX + width - 1, newY + height - 1)) {
        x = newX;
        y = newY;
        updateBounds();
    }
    }
    
    public Direction getDirectionToTarget(float targetX, float targetY) {
        float dx = targetX - x;
        float dy = targetY - y;
        
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }
    }
    
    public Bullet shoot() {
        if (frozen) return null;

        long currentTime = System.currentTimeMillis();
        long cooldown = player ? 250 : 500;
        
        if (powerLevel == 0 && currentTime - lastShotTime < cooldown) {
            return null;
        }

        if (powerLevel >= 1 && currentTime - lastShotTime < cooldown / 1.5) {
            return null;
        }
        
        lastShotTime = currentTime;
        
        float bulletX = x;
        float bulletY = y;
        
        switch (direction) {
            case UP:
                bulletX = x + width / 2 - 4;
                bulletY = y - 8;
                break;
            case DOWN:
                bulletX = x + width / 2 - 4;
                bulletY = y + height;
                break;
            case LEFT:
                bulletX = x - 8;
                bulletY = y + height / 2 - 4;
                break;
            case RIGHT:
                bulletX = x + width;
                bulletY = y + height / 2 - 4;
                break;
        }

        boolean fastBullet = (powerLevel >= 1); 
        boolean steelPiercing = (powerLevel >= 3); 
        
        return new Bullet(bulletX, bulletY, direction, player, resourceManager, 
                        fastBullet, steelPiercing);
    }
    
    public boolean isPlayer() {
        return player;
    }
    
    public void takeDamage() {
        if (invulnerable && player) {return;}
        
        health--;

        if (health <= 0) {
            if (player) {
                respawn();
            } 
            else {
                active = false;
            }
        } 
        else {
            if (player) {
                setInvulnerable();
            } 
        }
    }
    
    private void respawn() {
        maxHealth--;
        
        if (maxHealth <= 0) {
            active = false;
        } else {
            health = 1;
            
            if (spawnX == 0 && spawnY == 0) {
                x = Level.PLAYER_SPAWN_X;
                y = Level.PLAYER_SPAWN_Y;
            } else {
                x = spawnX;
                y = spawnY;
            }
            
            direction = Direction.UP;
            setInvulnerable();
        }
    }
    
    public void setInvulnerable() {
        invulnerable = true;
        invulnerabilityEndTime = System.currentTimeMillis() + INVULNERABILITY_DURATION;
    }
    
    public boolean isInvulnerable() {
        return invulnerable;
    }
    
    public int getHealth() {
        return maxHealth;
    }
    
    public void setSpeed(float speed) {
        this.speed = Math.max(0, speed);
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public void boostSpeed() {
        this.speed = DEFAULT_SPEED * 1.5f;
    }
    
    public void resetSpeed() {
        this.speed = DEFAULT_SPEED;
    }
    
    public void setSpawnPosition(float x, float y) {
        this.spawnX = x;
        this.spawnY = y;
    }

    public void addLife() {
        maxHealth++;
    }

    public int getPointValue() {
        if (player) return 0; 
        
        switch (tankType) {
            case "basic":  return 100;
            case "fast":   return 200;
            case "power":  return 300;
            case "armor":  return 400;
            default:       return 100;
        }
    }
}
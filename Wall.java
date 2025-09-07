import java.awt.*;

public class Wall extends GameObject {
    public enum WallType {
        BRICK, 
        STEEL,   
        WATER,  
        ICE,     
        TREES    
    }
    
    private WallType wallType;
    private ResourceManager resourceManager;
    
    public Wall(float x, float y, WallType wallType, ResourceManager resourceManager) {
        super(x, y, 32, 32);
        this.wallType = wallType;
        this.resourceManager = resourceManager;
    }
    
    @Override
    public void update() {}
    
    @Override
    public void render(Graphics2D g) {
        String imageKey;
        
        switch (wallType) {
            case BRICK:
                imageKey = "brick";
                break;
            case STEEL:
                imageKey = "steel";
                break;
            case WATER:
                imageKey = "water";
                break;
            case ICE:
                imageKey = "ice";
                break;
            case TREES:
                imageKey = "trees";
                break;
            default:
                imageKey = "brick";
        }
        
       if (resourceManager != null && resourceManager.isImageLoaded(imageKey)) {
        g.drawImage(resourceManager.getImage(imageKey), (int)x, (int)y, null);
        } 
    }
    
    public WallType getWallType() {
        return wallType;
    }
    
    public boolean isDestructible() {
        return wallType == WallType.BRICK;
    }
    
    public boolean blocksMovement() {
    return wallType == WallType.BRICK || wallType == WallType.STEEL || wallType == WallType.WATER;
    }
    
    public boolean blocksBullet() {
        return wallType == WallType.BRICK || wallType == WallType.STEEL;
    }
}
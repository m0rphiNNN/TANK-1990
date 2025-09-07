import java.awt.*;

public abstract class GameObject {
    protected float x, y;
    protected int width, height;
    protected boolean active = true;
    protected Rectangle bounds;
    protected Direction direction;
    
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    
    public GameObject(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.direction = Direction.UP;
        this.bounds = new Rectangle((int)x, (int)y, width, height);
    }
    
    public abstract void update();
    public abstract void render(Graphics2D g);
    
    public void updateBounds() {
        bounds.x = (int)x;
        bounds.y = (int)y;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean intersects(GameObject other) {
        return this.bounds.intersects(other.bounds);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setX(float x) {
        this.x = x;
        updateBounds();
    }
    
    public void setY(float y) {
        this.y = y;
        updateBounds();
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    public Direction getDirection() {
        return direction;
    }
}
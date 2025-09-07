import java.awt.*;
import java.awt.image.BufferedImage;

public class GameOverManager {
    
    private BufferedImage gameOverImage;
    private ResourceManager resourceManager;
    private int screenWidth;  
    private int screenHeight; 
    
    private boolean animationActive = false;
    private float gameOverY;
    private float targetY;
    private long animationStartTime;
    private long scoreDisplayTime;
    private boolean showScore = false;
    private int score;
    
    private static final long ANIMATION_DELAY = 500; 
    private static final long SCORE_DELAY = 800; 
    private static final float ANIMATION_SPEED = 2.0f; 
    
    public GameOverManager(ResourceManager resourceManager,int screenWidth,int screenHeight) {
        this.resourceManager = resourceManager;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        if (resourceManager.isImageLoaded("game_over")) {
            gameOverImage = resourceManager.getImage("game_over");
        } else {}
    }
    
    public void startAnimation(int score) {
        this.score = score;
        animationActive = true;
        showScore = false;
        animationStartTime = System.currentTimeMillis();
        gameOverY = screenHeight; 
        targetY = screenHeight/2 -40; 
        scoreDisplayTime = 0;
        }
    
    public void update() {
        if (!animationActive) return;
        
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - animationStartTime;
        
        if (elapsedTime < ANIMATION_DELAY) {
            return;
        }
        
        if (gameOverY > targetY) {
            gameOverY -= ANIMATION_SPEED;
            
            if (gameOverY <= targetY) {
                gameOverY = targetY;
                scoreDisplayTime = currentTime;
                showScore = false;
            }
        } 
        else if (!showScore && currentTime - scoreDisplayTime >= SCORE_DELAY) {
            showScore = true;
        }
    }
    
    public void render(Graphics2D g) {
        if (!animationActive) return;
        
        int imageWidth = gameOverImage.getWidth();
        int centerX = screenWidth / 2 - imageWidth / 2;
        
        g.drawImage(gameOverImage, centerX, (int)gameOverY, null);

        if (showScore) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String scoreText = "SCORE: " + score;
            int scoreWidth = g.getFontMetrics().stringWidth(scoreText);
            g.drawString(scoreText, screenWidth / 2 - scoreWidth / 2, (int)gameOverY + 60);
        }
    }
    
    public boolean isAnimationActive() {
        return animationActive;
    }
    
    public void stopAnimation() {
        animationActive = false;
    }

}
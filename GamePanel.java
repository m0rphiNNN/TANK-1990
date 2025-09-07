import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel implements KeyListener, Runnable {
    private static final int BASE_WIDTH = Level.getGridWidth() * Level.CELL_SIZE;  
    private static final int BASE_HEIGHT = Level.getGridHeight() * Level.CELL_SIZE; 
    private static final int FPS = 60;
    private static final int TARGET_TIME = 1000000000 / FPS;
    
    private Thread gameThread;
    private boolean running = false;
    
    private GameEngine gameEngine;
    private ResourceManager resourceManager;
    private InputHandler inputHandler;
    
    // Ölçekleme için değişkenler
    private int currentWidth = BASE_WIDTH;
    private int currentHeight = BASE_HEIGHT;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    
    private boolean spaceKeyProcessed = false;
    private long lastSpaceKeyTime = 0;
    private static final long SPACE_KEY_COOLDOWN = 250; 
    
    public GamePanel() {
        initializePanel();
        initializeComponents();
        startGameLoop();
    }
    
    private void initializePanel() {
        setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        setMinimumSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        setMaximumSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
    }
    
    private void initializeComponents() {
        resourceManager = new ResourceManager();
        inputHandler = new InputHandler();
        gameEngine = new GameEngine(resourceManager, inputHandler);
    }
    
    private void startGameLoop() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public void run() {
        long startTime;
        long elapsedTime;
        long waitTime;
        
        requestFocus();
        
        while (running) {
            startTime = System.nanoTime();
            
            update();
            repaint();
            
            elapsedTime = System.nanoTime() - startTime;
            waitTime = TARGET_TIME - elapsedTime;
            
            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime / 1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void update() {
        if (inputHandler.isRestartPressed()) {
            gameEngine.resetGame();
        }

        long currentTime = System.currentTimeMillis();
        if (spaceKeyProcessed && currentTime - lastSpaceKeyTime > SPACE_KEY_COOLDOWN) {
            spaceKeyProcessed = false;
        }
        
        inputHandler.update();
        gameEngine.update();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.scale(scaleX, scaleY);
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
        gameEngine.render(g2d);
    }
    
@Override
public void keyPressed(KeyEvent e) {
    inputHandler.keyPressed(e.getKeyCode());
    
    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
        long currentTime = System.currentTimeMillis();
        if (!spaceKeyProcessed && currentTime - lastSpaceKeyTime > SPACE_KEY_COOLDOWN) {
            gameEngine.forcePlayerShoot();
            spaceKeyProcessed = true;
            lastSpaceKeyTime = currentTime;
        }
    }
}
    
    @Override
    public void keyReleased(KeyEvent e) {
        inputHandler.keyReleased(e.getKeyCode());
        
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            spaceKeyProcessed = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }
    
    public void stopGame() {
        running = false;
    }
    
    public void forceTryStartGame() {
        if (gameEngine.isInMenuState()) {
            gameEngine.startGameFromMenu();
        }
    }
    
    public void setScaleDimensions(int width, int height) {
        currentWidth = width;
        currentHeight = height;
        
        scaleX = (double) width / BASE_WIDTH;
        scaleY = (double) height / BASE_HEIGHT;
        
        setPreferredSize(new Dimension(currentWidth, currentHeight));
        setSize(new Dimension(currentWidth, currentHeight));
        
        revalidate();
        repaint();
    }
    
    public int getBaseWidth() {
        return BASE_WIDTH;
    }
    
    public int getBaseHeight() {
        return BASE_HEIGHT;
    }
}
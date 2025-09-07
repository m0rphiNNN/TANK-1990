import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class GameWindow extends JFrame {
    private static final int WINDOW_WIDTH = 544;
    private static final int WINDOW_HEIGHT = 544;
    private static final String GAME_TITLE = "Battle City - Tank 1990";
    
    private GamePanel gamePanel;
    
    private boolean isFullScreen = false;
    
    public GameWindow() {
        initializeWindow();
        createComponents();
        setupLayout();  
        setupListeners();   
        finalizeWindow();
    }
    
    private void initializeWindow() {
        setTitle(GAME_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setWindowIcon();
    }
    
    private void setWindowIcon() {
        try {
            ResourceManager tempResourceManager = new ResourceManager();
            BufferedImage logoImage = tempResourceManager.getImage("logo");
            
            if (logoImage != null) {
                setIconImage(logoImage);
            } 
        } catch (Exception e) {
            System.out.println("HATA");
        }
    }

    private void createComponents() {
        gamePanel = new GamePanel();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.BLACK);
        centerPanel.add(gamePanel);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private void setupListeners() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeGamePanel();
            }
        });
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                keyPressed(e);
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                keyReleased(e);
            }
            return false;
        });
    }
    
    private void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F11) {
            toggleFullScreen();
        }
        
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
            gamePanel.forceTryStartGame();
        }
        
        gamePanel.keyPressed(e);
    }
    
    private void keyReleased(KeyEvent e) {
        gamePanel.keyReleased(e);
    }
    
    private void finalizeWindow() {
        pack();
        setVisible(true);
        gamePanel.requestFocusInWindow();
    }
    
    private void resizeGamePanel() {
        Dimension contentSize = getContentPane().getSize();
        Dimension panelSize = gamePanel.getPreferredSize();
        
        double gameRatio = (double) panelSize.width / panelSize.height;
        double contentRatio = (double) contentSize.width / contentSize.height;
        
        int newWidth, newHeight;
        
        if (contentRatio > gameRatio) {
            newHeight = contentSize.height;
            newWidth = (int)(newHeight * gameRatio);
        } else {
            newWidth = contentSize.width;
            newHeight = (int)(newWidth / gameRatio);
        }
        
        gamePanel.setScaleDimensions(newWidth, newHeight);
        
        revalidate();
    }
    
    public void toggleFullScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = ge.getDefaultScreenDevice();
        
        if (isFullScreen) {
            dispose();
            setUndecorated(false);
            device.setFullScreenWindow(null);
            
            setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            setLocationRelativeTo(null);
            setResizable(true);
            
            isFullScreen = false;
        } else {
            dispose();
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            device.setFullScreenWindow(this);
            isFullScreen = true;
        }
        
        setVisible(true);
        resizeGamePanel();
        gamePanel.requestFocusInWindow();

        if (!isFullScreen) {
            setWindowIcon();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new GameWindow();
        });
    }
}
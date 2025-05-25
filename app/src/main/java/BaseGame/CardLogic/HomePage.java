package BaseGame.CardLogic;

import java.util.Set;
import java.util.HashSet;
import java.awt.event.KeyEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import BaseGame.App;
import BaseGame.DiscordBot;
import BaseGame.Rectangles.*;
import processing.core.PConstants;


// TODO - implement rules page
public class HomePage extends DeckHandler {

    private PictureRectangle confirmButton;
    private Rectangle userNameRect;
    private String currentName;
    private String textFieldText;
    private boolean isNameValid = false;

    private boolean specialKeysList[] = new boolean[256];
    private static final Set<Integer> IGNORED_KEYS = new HashSet<Integer>() {{
        add(16);
        add(17);
        add(18);
        add(20);
        add(27);
        add(127);
    }};

    public HomePage(App app) {
        super(app, 0);
        setup();
    }

    @Override
    public void setup() {
        confirmButton = new PictureRectangle(0.9f, 0.9f, 120 * app.scaleFactor, 44 * app.scaleFactor, "app/src/main/resources/Button/unpressed_confirm.png");
        userNameRect = new Rectangle(0.5f, 0.9f, 400, 50);
        currentName = "";
        textFieldText = "";
        try {
            String test = new String(Files.readAllBytes(Paths.get("misc/username.txt")), StandardCharsets.UTF_8).trim();
            if (!test.isEmpty()) {
                textFieldText = test;
            }
        }
        catch (Exception e) {}
    }

    @Override
    public String encodeGameState() {
        return null;
    }

    @Override
    public boolean handleMouseClick(int mouseX, int mouseY) {
        return false;
    }

    @Override
    public void handleMousePress(int mouseX, int mouseY) {

    }

    @Override
    public void handleMouseDrag(int mouseX, int mouseY) {
        
    }

    @Override
    public void handleMouseRelease(int mouseX, int mouseY) {
    }

    @Override
    public void draw() {
        app.textSize(50f);
        app.color(255);
        String text = (isNameValid) ?
            ((app.waitingForGame()) 
                ? "Go to discord to join or host a game!"
                : "Waiting for host to start game")
            : "Your Name: " + textFieldText;
        app.textAlign(PConstants.CENTER);
        app.text(text, 0.5f * app.displayWidth, 0.5f * app.displayHeight);

        if(!currentName.isEmpty()) {
            text = "Hello " + currentName + "!";
            app.text(text, 0.5f * app.displayWidth, 0.3f * app.displayHeight);
        }
    }

    @Override
    public String initializeDeck() {
        return null;
    }

    @Override
    public void initializeDeck(String str) {
    }

    @Override
    public void nextTurn(String boardState) {
    }

    @Override
    public void handleKeyTyped(char keyChar) {
        if (keyChar == App.BACKSPACE) {
            if (textFieldText.length() > 0) {
                textFieldText = textFieldText.substring(0, textFieldText.length() - 1);
            }
        } else if (keyChar == App.ENTER) {
            if (isNameValid = checkNameValidity()) {
                if (currentName.isEmpty()) {
                    currentName = textFieldText;
                    DiscordBot.initializeBot(app, currentName);
                    try {
                        Files.write(Paths.get("misc/username.txt"), currentName.getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Invalid name");
            }
        }
        else if (textFieldText.length() < 30) {
            if (keyChar >= 32 && keyChar <= 126) {
                textFieldText += keyChar;
            }
        }
        
    }

    private boolean checkNameValidity() {
        return !textFieldText.isEmpty() && currentName.isEmpty();
    }
}

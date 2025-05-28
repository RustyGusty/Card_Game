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

    private String currentName;
    private String textFieldText;
    private int textFieldIndex; // 0 = Enter name, 1 = Waiting for host, 2 = Waiting for confirmation, 3 = Game ready

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
        currentName = "";
        textFieldText = "";
        textFieldIndex = 0;
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
        app.textAlign(PConstants.CENTER);

        if(!currentName.isEmpty()) {
            app.text("Hello " + currentName + "!", 0.5f * app.displayWidth, 0.3f * app.displayHeight);
        }
        switch (textFieldIndex) {
            case 0:
                app.text("Enter your name: " + textFieldText, 0.5f * app.displayWidth, 0.5f * app.displayHeight);
                break;
            case 1:
                if (app.getHost().isEmpty()) {
                    app.text("Go to discord to join or host a game!", 0.5f * app.displayWidth, 0.5f * app.displayHeight);
                    break;
                } else {
                    if(app.waitingForGame()) {
                        app.text("User " + app.getHost() + " has started a game!\nPress Enter to join!", 0.5f * app.displayWidth, 0.5f * app.displayHeight);
                        break;
                    } else {
                        textFieldIndex = 2; // Move to next state once you've joined a game, don't break in order to fall through to the next case
                    }
                }
            case 2:
                app.text("Waiting for " + app.getHost() + " to start!", 0.5f * app.displayWidth, 0.5f * app.displayHeight);
                break;
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
            if (currentName.isEmpty()) {
                if (checkNameValidity()) {
                    if (currentName.isEmpty()) {
                        currentName = textFieldText;
                        DiscordBot.initializeBot(app, currentName);
                        try {
                            Files.write(Paths.get("misc/username.txt"), currentName.getBytes(StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        textFieldIndex = 1;
                    }
                } else {
                    System.out.println("Invalid name");
                }
            } else {
                if (textFieldIndex == 1 && !app.getHost().isEmpty()) {
                    app.bot.sendBotMessage("$add_player" + app.getHost() + "->" + currentName);
                    textFieldIndex = 2;
                }
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

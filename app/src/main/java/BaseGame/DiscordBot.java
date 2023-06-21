package BaseGame;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import BaseGame.CardLogic.HomePage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot extends ListenerAdapter{
    
    private static App app;
    private static String user;
    private String currentGame;
    private MessageChannel curChannel;

    public static void initializeBot(App app, String user) {
        DiscordBot.app = app;
        DiscordBot.user = user;
        
        try {
            String token = new String(Files.readAllBytes(Paths.get("token.txt")), StandardCharsets.UTF_8).trim();

            JDA jda = JDABuilder.createLight(token)
                    .addEventListeners(new DiscordBot())
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES)
                    .build();
            
            jda.getRestPing().queue(ping ->
                // shows ping in milliseconds
                System.out.println("Logged in with ping: " + ping)
            );

            jda.updateCommands().addCommands(
                Commands.slash("host_pontinho", "Host a game of Pontinho"),
                Commands.slash("start_game", "Start the game as the host")
                ).queue();

            jda.awaitReady();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        app.bot = this;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        String message = event.getMessage().getContentRaw();
        if(author.isBot()) {
            System.out.println(message);
            if(message.startsWith("Next turn:")) {
                makeMove(message);
            } else if(message.startsWith("A game has been started by")) {
                startNonHostGame(message);
            }
            return;
        }
    }

    private void startNonHostGame(String message) {
        if(app.curPlayerNumber == -1) {
            for(int i = 0; i < 2; i++)
                message = message.substring(message.indexOf("\n"));
            String args[] = message.split("\n\n");
            System.out.println(args);
            app.makePlayerList(args[0], user);
            String initialDeck = args[1].substring(args[1].indexOf(":"));
            initialDeck = initialDeck.substring(2);
            System.out.println(initialDeck);
            app.startGame(initialDeck);
        }
    }

    private void makeMove(String message) {
        String[] move = message.split("z");
        app.makeMove(move[move.length - 1]);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String args[] = event.getComponentId().split(":");
        String host = args[0];
        // Joiner branch: Set up the correct DeckHandler
        if (!host.equals(user)) {
            if(event.getUser().getName().equals(user)){
                if(app.waitingForGame()) {
                    currentGame = args[1];
                    app.loadGame(currentGame);
                }
            }
            return;
        }
        // Host branch: Add the player to the player list, and give proper replies
        addPlayerToHost(event);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "host_pontinho":  
                hostGame(event, "pontinho");
                break;
            case "start_game":
                startGame(event);
                break;
        }
    }

    /**
     * The host starts the game and sends a list of all the players
     * and the starting deck to everyone
     * @param event
     */
    private void startGame(SlashCommandInteractionEvent event) {
        if(event.getUser().getName().equals(user)) {
            if(app.thisPlayerNumber != 0) {
                event.reply("You are not a host!").setEphemeral(true).queue();
                return;
            }
            if(app.numPlayers <= 1){
                event.reply(String.format("Need %d+ players to start. Currently have: %d",
                app.minPlayerCount, app.numPlayers)).setEphemeral(true).queue();
                return;
            }
            String initialDeck = app.getQueuedStartingDeck();
            app.startGame(initialDeck);
            event.reply(String.format("A game has been started by %s!\n%s\nGame ID: %s",
            user,
            app.playerListToString(),
            initialDeck)).queue();
        }
    }

    /**
     * Called only on the host's seession, the host adds the user of the button
     * to their game, unless the game has already started or the player is already in
     * @param event
     */
    private void addPlayerToHost(ButtonInteractionEvent event) {
        if(!app.curGameHandler.getClass().equals(HomePage.class)) {
            event.reply("The game has already started!").setEphemeral(true).queue();
            return;
        }
        for(Player pl : app.playerList) 
            if(pl.isPlayer(event.getUser().getName())) {
                event.reply("You are already in this game!").setEphemeral(true).queue();
                return;
            }
        app.addPlayer(event.getUser().getName());
        event.reply(String.format("%s has successfully joined %s's game!",
            event.getUser().getName(),
            user
            )).queue();
    }

    /**
     * The caller of /hostPontinho hosts the game and creates a button
     * with their name and the game as the button id
     * @param event 
     * @param game 
     */
    private void hostGame(SlashCommandInteractionEvent event, String game) {
        if(event.getUser().getName().equals(user)) {
            curChannel = event.getChannel();
            currentGame = game;
            app.hostGame(user, game);
            event.reply(user + " has hosted a game!")
                .addActionRow(Button.primary(String.format("%s:%s", user, currentGame), "Join game!"))
                .queue();
        }
    }

    public void processMove(String encodeGameState) {
        curChannel.sendMessage("Next Turn: P" + app.curPlayerNumber + "z" + encodeGameState);
    }
}

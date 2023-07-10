package BaseGame;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import BaseGame.CardLogic.HomePage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot extends ListenerAdapter {

    private static final String GAME_OPTIONS[];

    static {
        GAME_OPTIONS = new String[1];
        GAME_OPTIONS[0] = "pontinho";
    }

    private static App app;
    private static String user;
    private static TextChannel privateChannel;
    private String currentGame;

    public static void initializeBot(App app, String user) {
        DiscordBot.app = app;
        DiscordBot.user = user;

        try {
            String token = new String(Files.readAllBytes(Paths.get("misc/token.txt")), StandardCharsets.UTF_8).trim();

            JDA jda = JDABuilder.createLight(token)
                    .addEventListeners(new DiscordBot())
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES)
                    .build();

            jda.getRestPing().queue(ping ->
            // shows ping in milliseconds
            System.out.println(user + " logged in with ping: " + ping));

            OptionData hostOptions = new OptionData(OptionType.STRING, "name",
                    "Name of game to play (use autocomplete)", true, true);

            jda.updateCommands().addCommands(
                    Commands.slash("host", "Host a game of Pontinho").addOptions(hostOptions),
                    Commands.slash("start_game", "Start the game as the host"),
                    Commands.slash("reset", "Cancels all hosted and queued games"),
                    Commands.slash("next_round", "Host starts the next round of the game")).queue();

            jda.awaitReady();
            List<TextChannel> tempList = jda.getTextChannelsByName("priv-bot-channel", true);
            if (tempList.isEmpty()) {
                throw new Exception("Text channel named \"priv-bot-channel\" not found");
            }
            privateChannel = tempList.get(0);
            privateChannel.sendMessage(user + " has logged in successfully!").queue();
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
        if (author.isBot()
                && event.getChannel().getName().equals("priv-bot-channel")
                && message.startsWith("$")) {
            message = message.substring(1);
            if (message.startsWith("next_turn")) {
                makeMove(message.substring(9));
            } else if (message.startsWith("game_start")) {
                startNonHostGame(message.substring(10));
            } else if (message.startsWith("new_round")) {
                startNextRound(message);
            }
            return;
        }
    }

    // USERNAME->ID->Player\nList
    private void startNonHostGame(String message) {
        String args[] = message.split("->"); // [host, game ID, Player List]
        if (app.thisPlayerNumber == -1 && app.isHost(args[0])) {
            app.makePlayerList(args[2], user);
            app.startGame(args[1]);
        }
    }

    private void startNextRound(String message) {
        String[] args = message.split("->");
        if(app.isHost(args[0])) {
            app.nextRound(args[1]);
        }
    }

    private void makeMove(String message) {
        String args[] = message.split("->"); // [host, move]
        if(app.isHost(args[0]))
            app.makeMove(args[1]);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String args[] = event.getComponentId().split(":");
        String host = args[0];
        // Joiner branch: Set up the correct DeckHandler
        if (!host.equals(user)) {
            if (event.getUser().getName().equals(user)) {
                if (app.waitingForGame()) {
                    currentGame = args[1];
                    app.queueGame(currentGame, host);
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
            case "host":
                hostGame(event);
                break;
            case "start_game":
                startHostGame(event);
                break;
            case "reset":
                reset(event);
                break;
            case "next_round":
                nextRound(event);
                break;
        }
    }

    /**
     * The host starts the game and sends a list of all the players
     * and the starting deck to everyone
     * 
     * @param event
     */
    private void startHostGame(SlashCommandInteractionEvent event) {
        if (event.getUser().getName().equals(user)) {
            if (!isHost()) {
                event.reply("You are not a host!").setEphemeral(true).queue();
                return;
            }
            if (app.numPlayers <= 1) {
                event.reply(String.format("Need %d+ players to start. Currently have: %d",
                        app.minPlayerCount, app.numPlayers)).setEphemeral(true).queue();
                return;
            }
            String initialDeck = app.getQueuedStartingDeck();
            app.startGame(initialDeck);
            event.reply(String.format("A game has been started by %s!\n%s",
                    user,
                    app.playerListToString())).queue();
            privateChannel.sendMessage(String.format("$game_start%s->%s->%s",
                    user,
                    initialDeck,
                    app.playerListToString())).queue();
        }
    }

    /**
     * Called only on the host's seession, the host adds the user of the button
     * to their game, unless the game has already started or the player is already
     * in
     * 
     * @param event
     */
    private void addPlayerToHost(ButtonInteractionEvent event) {
        if (!app.curGameHandler.getClass().equals(HomePage.class)) {
            event.reply("The game has already started!").setEphemeral(true).queue();
            return;
        }
        for (Player pl : app.playerList)
            if (pl.isPlayer(event.getUser().getName())) {
                event.reply("You are already in this game!").setEphemeral(true).queue();
                return;
            }
        if (app.waitingForGame()) {
            event.reply("That player is not currently hosting a game!").setEphemeral(true).queue();
            return;
        }
        app.addPlayer(event.getUser().getName());
        event.reply(String.format("%s has successfully joined %s's game!",
                event.getUser().getName(),
                user)).queue();
    }

    /**
     * The caller of /hostPontinho hosts the game and creates a button
     * with their name and the game as the button id
     * 
     * @param event
     * @param game
     */
    private void hostGame(SlashCommandInteractionEvent event) {

        if (event.getUser().getName().equals(user)) {
            String game = event.getOption("name").getAsString().toLowerCase();
            currentGame = null;
            for (String gameOption : GAME_OPTIONS)
                if (gameOption.equals(game)) {
                    currentGame = gameOption;
                    break;
                }
            if (currentGame == null) {
                event.reply(event.getOption("name").getAsString() + " is not a valid game!")
                        .setEphemeral(true).queue();
                return;
            }
            app.hostGame(user, game);
            event.reply(user + " has hosted a game!")
                    .addActionRow(Button.primary(String.format("%s:%s", user, currentGame), "Join game!"))
                    .queue();
        }
    }

    private void reset(SlashCommandInteractionEvent event) {
        if (event.getUser().getName().equals(user)) {
            app.reset();
            event.reply(user + " successfully cancelled their games.").queue();
        } else if (isHost()) {
            app.removePlayer(event.getUser().getName());
        }
    }

    private void nextRound(SlashCommandInteractionEvent event) {
        if (event.getUser().getName().equals(user)) {
            if (!isHost())
                event.reply("You are not a host!").setEphemeral(true).queue();
            else {
                if (!app.roundOver())
                    event.reply("The round is not over yet!").setEphemeral(true).queue();
                else {
                    event.reply(String.format("$new_round%s->%s",
                            user,
                            app.initializeDeck())).queue();
                }
            }
        }
    }

    private boolean isHost() {
        return app.thisPlayerNumber == 0;
    }

    public void processMove(String encodeGameState) {
        privateChannel.sendMessage("$next_turn" + app.getHost() + "->" + encodeGameState).queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("host") && event.getFocusedOption().getName().equals("name")) {
            List<Command.Choice> options = Stream.of(GAME_OPTIONS)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue())) 
                    // only display words that start with the user's current input
                    .map(word -> new Command.Choice(word, word)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }
}

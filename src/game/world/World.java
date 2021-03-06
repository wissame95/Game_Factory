package game.world;

import dataMapping.Data;
import engine.Graphics;
import engine.Physics;
import engine.Sound;
import game.graphicItems.Text;
import game.world.camera.Camera;
import game.world.entities.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static engine.Launcher.poolThread;

public class World {

	private Player player;
	private List<Obstacle> plateau;
    private List<Coin> coinsList;
    private Door doorOut;

	private List<PotentialCollision> listPC;
	private Collection<Callable<Integer>> isStuckRoutines;

	private Camera scroller;

    private Pause pauseDisplay;
    private static EndingScreen endingScreen;
    private HashMap<Integer, Callable<Integer>>  keyCommandsToActionInWorld;
    private HashMap<Integer, Callable<Integer>>  keyCommandsToActionInEndingScreen;
    private Collection<Callable<Integer>> actionToExecute = new ArrayList<>();

    private enum Context {ISPLAYING , INPAUSE, ISOVER};
    private static Context context;

    private static boolean worldOver;

    private static long timestart;

	public World(Player player, Camera camera, Door door, ArrayList<Obstacle> plateau) {
	    pauseDisplay = new Pause(new Text("Pause", Data.fontsMap.get("tron"), Color.red));
        endingScreen = null;

        genereCommande();

        isStuckRoutines = new ArrayList<>();

		this.player = player;
		this.doorOut = door;
		this.plateau = plateau;

        coinsList = new ArrayList<>();
		listPC = new ArrayList<>();

        scroller = camera;

		generate();

		worldOver = false;
		context = Context.ISPLAYING;

        timestart = System.currentTimeMillis();
	}

	/**
	 * Associe à certaines touches du clavier une commande
	 * permettant de déplacer le Player ou de mettre le jeu en Pause.
	 */
	private void genereCommande(){
        keyCommandsToActionInWorld = new HashMap<>();
        keyCommandsToActionInWorld.put(Keyboard.KEY_LEFT, () -> {
            player.leftWanted();
            return 0;
        });
        keyCommandsToActionInWorld.put(Keyboard.KEY_RIGHT, () -> {
            player.rightWanted();
            return 0;
        });
        keyCommandsToActionInWorld.put(Keyboard.KEY_SPACE, () -> {
            player.jumpWanted();
            return 0;
        });
        keyCommandsToActionInWorld.put(Keyboard.KEY_P, () -> {
            switchTo(Context.INPAUSE);
            return 0;
        });


        keyCommandsToActionInEndingScreen = new HashMap<>();
        keyCommandsToActionInEndingScreen.put(Keyboard.KEY_SPACE, () -> {
            worldOver = true;
            return 0;
        });
    }

	/**
	 * Génère les données du World.
	 */
	private void generate() {
        Collection<PotentialCollision> pcCollection = new ArrayList<>();
        Collection<PotentialCollision> pcCollectionDoor = new ArrayList<>();

        plateau.forEach(obstacle -> pcCollection.add(new PotentialCollision(player, obstacle)));

        pcCollection.forEach(pc -> pc.setActionIfCollision(() -> Physics.replaceAfterCollision(pc)));

        pcCollectionDoor.add(new PotentialCollision(player, doorOut));
        pcCollectionDoor.forEach(pc -> pc.setActionIfCollision(() -> playerWin().run()));

        listPC.addAll(pcCollection);
        listPC.addAll(pcCollectionDoor);

        listPC.forEach(pc -> {
            isStuckRoutines.add(collisionRoutine(pc));
        });

	}

	/**
	 * Crée un Callable correspondant à la gestion des collisions entre le Player et un Obstacle donné.
	 * @param pcX	la PotentialCollision liant le Player à l'Obstacle
	 * @return		le Callable créé
	 */
    public static Callable<Integer> collisionRoutine(PotentialCollision pcX){
        return () -> {
            if(Physics.isStuck(pcX))
                pcX.getActionIfCollision().run();
            return 0;
        };
    }

    /**
     * Crée un Runnable correspondant au gain de la partie.
     * @return	le Runnable créé
     */
    public static Runnable playerWin(){
        return () -> {
            long timeElapsed = (System.currentTimeMillis() - timestart) / 1000;
            endingScreen = new EndingScreen(true, timeElapsed);
            switchTo(Context.ISOVER);
        };
    }

    /**
     * Crée un Runnable correspondant à la mort du Player
     * @return	le Runnable créé
     */
    public static Runnable playerDeath(){
        return () -> {
            long timeElapsed = (System.currentTimeMillis() - timestart) / 1000;
            endingScreen = new EndingScreen(false, timeElapsed);
            switchTo(Context.ISOVER);
        };
    }

    /**
     * Permet de retourner directement au Menu.
     */
    public static void hardBackToMenu(){
        worldOver = true;
    }

    /**
     * Permet de reprendre la partie.
     */
    public static void backToPlay(){
        switchTo(Context.ISPLAYING);
    }

    /**
     * Change le Context selon que la partie est en cours, en Pause ou finie.
     * @param toContext	le nouveauContext
     */
    private static void switchTo(Context toContext){
        if(toContext == Context.INPAUSE){
            Sound.pause();
            context = Context.INPAUSE;
        }else if(toContext == Context.ISPLAYING){
            Sound.play();
            context = Context.ISPLAYING;
        }else if(toContext == Context.ISOVER){
            context = Context.ISOVER;
        }
    }

    /**
     * Actualise les données du World.
     */
	public void update() {
        switch (context){
            case INPAUSE:
                pauseDisplay.update();
                if(pauseDisplay.getLastButtonClicked() != null) {
                    Future future = poolThread.submit(pauseDisplay.getLastButtonClicked().getAction());
                    do{
                    }while (!future.isDone());
                }
                break;

            case ISPLAYING:
                scroller.translateView();
                if(player.isAlive()){
                    try {
                        poolThread.invokeAll(isStuckRoutines);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    player.update();
                }else{
                    playerDeath().run();
                }

            case ISOVER:
                break;
        }

	}

	/**
	 * Affiche tous les composants du World à l'écran.
	 */
	public void render() {
        switch (context){
            case INPAUSE:
                pauseDisplay.render();
                break;

            case ISPLAYING:
                Graphics.scroll(WorldParameters.getxScroll(), WorldParameters.getyScroll());
                for(Obstacle obstacle : plateau) {
                    obstacle.render();
                }
                for(Coin coin : coinsList)
                    coin.render();
                doorOut.render();

                player.render();
                break;

            case ISOVER:
                endingScreen.render();
                break;
        }

	}

	/**
	 * Récupère et interprète les entrées clavier et souris.
	 */
    public void pollInput(){
        switch (context){
            case INPAUSE:
                if(Mouse.isButtonDown(0))
                    pauseDisplay.receiveClick(Mouse.getX(), Mouse.getY());
                break;
            case ISPLAYING:
                keyCommandsToActionInWorld.forEach((keyToCheck, actionToRunIfPressed) -> {
                    if(Keyboard.isKeyDown(keyToCheck))
                        actionToExecute.add(actionToRunIfPressed);
                });
                break;
            case ISOVER:
                keyCommandsToActionInEndingScreen.forEach((keyToCheck, actionToRunIfPressed) -> {
                    if(Keyboard.isKeyDown(keyToCheck))
                        actionToExecute.add(actionToRunIfPressed);
                });
                break;
        }
        try {
            poolThread.invokeAll(actionToExecute);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actionToExecute.clear();
    }

    /**
     * Lance la musique du World.
     */
    public void playBackgroundSound(){
        Sound.play(WorldParameters.getBackgroundMusic());
    }


    public boolean isInProgress(){
        return !worldOver;
    }
}

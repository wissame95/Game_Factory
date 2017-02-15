package mario.game;

import mario.engine.Graphics;
import mario.engine.Launcher;
import mario.engine.Sound;
import mario.game.world.World;
import mario.game.world.WorldParameters;
<<<<<<< HEAD

import java.io.IOException;

import org.json.JSONException;
=======
>>>>>>> refs/remotes/origin/Wissame
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Game{
    private Menu menu;
	private World world;

    private Context context;

    private Sound soundContext;
    private float soundPosition;

	public Game() {

        menu = new Menu();

<<<<<<< HEAD
		try {
			world = WorldReader.worldFromJson("world_test.json");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
=======
		//level = new World(this,1500, 1000);
        //level = WordReader.worldFromJson("/world/mario_map.json");
>>>>>>> refs/remotes/origin/Wissame

        context = Context.INMENU;

        soundContext = new Sound();

        soundContext.setBackgroundSound(WorldParameters.getPathToBackgroundMusic());
        soundPosition = 0;
    }

    public void pollInput() {

	    if(context == Context.INMENU){
            if (Mouse.isButtonDown(0)) {
                menu.receiveClick(Mouse.getX(), Mouse.getY());
            }
        }else if(context == Context.INGAME){
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            	world.getPlayer().jumpWanted();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            	world.getPlayer().leftWanted();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            	world.getPlayer().rightWanted();
            }

            while (Keyboard.next()) {
                if (Keyboard.getEventKeyState()) {

                } else {
                    if (Keyboard.getEventKey() == Keyboard.KEY_P) {
                        context = Context.INPAUSE;
                    }
                }
            }
        } else if(context == Context.INPAUSE) {
        	while (Keyboard.next()) {
                if (Keyboard.getEventKeyState()) {

                } else {
                    if (Keyboard.getEventKey() == Keyboard.KEY_P) {
                        context = Context.INGAME;
                    }
                }
            }
        }

    }

	public void init() {
<<<<<<< HEAD
		world.init();
=======
		//level.init();
>>>>>>> refs/remotes/origin/Wissame
        soundContext.play(soundPosition);
	}

	public void update() {
        pollInput();
<<<<<<< HEAD
		if(context == Context.INGAME && world.isInProgress()){
			world.update();
            if(!soundContext.isPlaying())
                soundContext.play(soundPosition);
		}else if(!world.isInProgress()){
		    context = Context.INMENU;
=======
		if(context == Context.INGAME){
		    if(level.isInProgress()){
                level.update();
                if(!soundContext.isPlaying())
                    soundContext.play(soundPosition);
            }else if(!level.isInProgress()){
                context = Context.INMENU;
            }

		}else if(context == Context.INMENU){
            if(menu.getLastButtonClicked() != null){
                String actionWanted = menu.getLastButtonClicked().getAction();
                switch (actionWanted){
                    case "start":
                        level = new World(this,1500, 1000);
                        menu.setLastButtonClicked(null);
                        context = Context.INGAME;
                        break;
                }
            }
>>>>>>> refs/remotes/origin/Wissame
        }


		else {
			if(soundContext.isPlaying())
				soundPosition = soundContext.stop();
		}
	}

	public void render() {
	    if(context == Context.INGAME){
	    	world.render();
        }else if(context == Context.INPAUSE) {
			int size = 200;
			int x = (Launcher.width - size) / 2 - WorldParameters.getxScroll();
			int y = (Launcher.height + size) / 2 + WorldParameters.getyScroll();
			Graphics.renderQuad(x, y, size, size, GameTextureMap.textureMap.get("pause"));
			//Il faut creer un objet pause
		}else if(context == Context.INMENU){
            menu.render();
        }
	}

    private enum Context {INGAME, INMENU, INPAUSE}
}
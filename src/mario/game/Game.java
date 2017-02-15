package mario.game;

import mario.engine.Graphics;
import mario.engine.Launcher;
import mario.engine.Sound;
import mario.game.world.World;
import mario.game.world.WorldParameters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class Game{
    private Menu menu;
	private World level;

    private Context context;

    private Sound soundContext;
    private float soundPosition;

    private GameTextureMap textures;

	public Game() {
	    //Creation de toute les textures une seule fois
        try {
            textures = new GameTextureMap();
        } catch (IOException e) {
            e.printStackTrace();
        }

        menu = new Menu(this);

		//level = new World(this,1500, 1000);
        //level = WordReader.worldFromJson("/world/mario_map.json");

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
                level.getPlayer().jumpWanted();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                level.getPlayer().leftWanted();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                level.getPlayer().rightWanted();
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
		//level.init();
        soundContext.play(soundPosition);
	}

	public void update() {
        pollInput();
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
        }


		else {
			if(soundContext.isPlaying())
				soundPosition = soundContext.stop();
		}
	}

	public void render() {
	    if(context == Context.INGAME){
            level.render();
        }else if(context == Context.INPAUSE) {
			int size = 200;
			int x = (Launcher.width - size) / 2 - WorldParameters.getxScroll();
			int y = (Launcher.height + size) / 2 + WorldParameters.getyScroll();
			Graphics.renderQuad(x, y, size, size, textures.textureMap.get("pause"));
			//Il faut creer un objet pause
		}else if(context == Context.INMENU){
            menu.render();
        }
	}

    public GameTextureMap getTextures() {
        return textures;
    }

    private enum Context {INGAME, INMENU, INPAUSE}
}
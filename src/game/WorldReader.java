package game;

import dataMapping.Data;
import exceptions.CameraTypeException;
import game.world.World;
import game.world.WorldParameters;
import game.world.camera.AttachedScroller;
import game.world.camera.Camera;
import game.world.camera.ForceScroller;
import game.world.entities.Door;
import game.world.entities.Obstacle;
import game.world.entities.Player;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.newdawn.slick.opengl.Texture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


class WorldReader {

    /**
	 * Lit un fichier .json et crée le World correspondant.
	 * @param path					le fichier à lire
	 * @return						le World créé
	 * @throws CameraTypeException	si le type de caméra spécifié dans le fichier n'existe pas
	 * @throws IOException			si le fichier est introuvable
	 * @throws JSONException		si le fichier est mal structuré, ou incomplet
	 */
	static World worldFromJSON(String path) throws CameraTypeException, IOException, JSONException {
        System.out.println("Call to worldFromJSON from : "+ Thread.currentThread().getName());
		JSONObject obj = new JSONObject(new String(Files.readAllBytes(Paths.get(path))));

		int width = obj.getInt("width");
		int height = obj.getInt("height");

		JSONObject obj2 = obj.getJSONObject("player");

		int sizeX = obj2.getInt("sizeX");
		int sizeY = obj2.getInt("sizeY");
		int v0 = obj2.getInt("v0");
		int v1 = obj2.getInt("v1");
		int x = obj2.getInt("x");
		int y = obj2.getInt("y");
		Texture[] skin = Data.skinsMap.get(obj2.getString("skin"));

		Player player = new Player(sizeX, sizeY, v0, v1, x, y, skin);

		JSONObject obj3 = obj.getJSONObject("camera");

		Camera camera;

		if(obj3.getString("type").equals("attached")) {
			boolean xAttached = obj3.getBoolean("xAttached");
			boolean yAttached = obj3.getBoolean("yAttached");
			camera = new AttachedScroller(xAttached, yAttached, player);
		} else if(obj3.getString("type").equals("force")) {
			v0 = obj3.getInt("v0");
			v1 = obj3.getInt("v1");
			camera = new ForceScroller(v0, v1);
		} else {
			throw new CameraTypeException();
		}

		JSONObject obj4 = obj.getJSONObject("door");

		sizeX = obj4.getInt("sizeX");
		sizeY = obj4.getInt("sizeY");
		x = obj4.getInt("x");
		y = obj4.getInt("y");
		Texture texture = Data.texturesMap.get(obj4.getString("texture"));

		Door door = new Door(sizeX, sizeY, x, y, texture);

		JSONArray arr = obj.getJSONArray("plateau");

		ArrayList<Obstacle> plateau = new ArrayList<>();

		for (int i = 0; i < arr.length(); i++) {

			sizeX = arr.getJSONObject(i).getInt("sizeX");
			sizeY = arr.getJSONObject(i).getInt("sizeY");
			x = arr.getJSONObject(i).getInt("x");
			y = arr.getJSONObject(i).getInt("y");
			texture = Data.texturesMap.get(arr.getJSONObject(i).getString("texture"));

			plateau.add(new Obstacle(sizeX, sizeY, x, y, texture));

		}

		WorldParameters.setGamma(obj.getDouble("gamma"));
		WorldParameters.setG(obj.getDouble("g"));
		WorldParameters.setDeltaT(1.0 / obj.getDouble("deltaT"));
		WorldParameters.setBackgroundMusic(Data.soundsMap.get(obj.getString("music")));
		WorldParameters.setMAXSPEED(obj.getInt("maxspeed"));
		WorldParameters.setJumpTime(obj.getDouble("jumpTime"));
		WorldParameters.setGainVitesseX(obj.getInt("gainVitesseX"));
		WorldParameters.setGainVitesseY(obj.getInt("gainVitesseY"));

        WorldParameters.setBordBas(0);
        WorldParameters.setBordHaut(height);
        WorldParameters.setBordGauche(0);
        WorldParameters.setBordDroit(width);

        WorldParameters.setxScroll(0);
        WorldParameters.setyScroll(0);

        System.out.println("HERE");
		return new World(player, camera, door, plateau);

	}

}

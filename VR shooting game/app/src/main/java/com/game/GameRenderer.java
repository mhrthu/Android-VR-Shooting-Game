package com.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;
import com.resource.RajawaliVRRenderer;
import com.test.R;

import javax.microedition.khronos.opengles.GL10;

import rajawali.Object3D;
import rajawali.animation.Animation;
import rajawali.animation.SplineTranslateAnimation3D;
import rajawali.curves.CatmullRomCurve3D;
import rajawali.lights.DirectionalLight;
import rajawali.materials.Material;
import rajawali.materials.methods.DiffuseMethod;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.NormalMapTexture;
import rajawali.materials.textures.Texture;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.parser.LoaderAWD;
import rajawali.parser.LoaderOBJ;
import rajawali.terrain.SquareTerrain;
import rajawali.terrain.TerrainGenerator;

// this is for the res files

public class GameRenderer extends RajawaliVRRenderer {
	private SquareTerrain mTerrain;
	protected HeadTracker mHeadTracker;
	protected HeadTransform mHeadTransform;
	protected float[] mHeadViewMatrix;
	protected Matrix4 mHeadViewMatrix4;
	private float[] modelView =  new float[16];
	private float[] tempPosition = new float[16];
	private float[] headView = new float[16];
	private static final float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};
	private static final float YAW_LIMIT = 0.2f;
	private static final float PITCH_LIMIT = 0.2f;
	private Object3D  currentObj;
	private int count = 0;
	private int score = 0;
	private int pathselection = 0;
	LoaderOBJ hellfireobj = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.hellfire_obj);
	LoaderOBJ spaceshipobj = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.spaceship_obj);
	LoaderOBJ ewingobj = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.ewing_obj);
	public GameRenderer(Context context) {
		super(context);
		this.mHeadTracker = super.mHeadTracker;
		this.mHeadTransform = super.mHeadTransform;
		this.mHeadViewMatrix = super.mHeadViewMatrix;
		this.mHeadViewMatrix4 = super.mHeadViewMatrix4;
		//this.mCameraOrientation = new Quaternion();
	}

	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
		mHeadTransform.getHeadView(headView, 0);
		if (isLookingAtObj(currentObj)){
			count ++;
			Log.v("is at center: ", Integer.toString(count));
			if (count >= 100) {
				getCurrentScene().removeChild(currentObj);
						Log.v("remove successed", ":");
				//currentObj.destroy();
				//Object3D  currentObj;
						setScore(10);
						setObject();
						setAnim();
						count = 0;
			}
		} else {
			count = 0;
		}
	}

	@Override
	public void initScene() {
		try {
			hellfireobj.parse();
			spaceshipobj.parse();
			ewingobj.parse();
		} catch(Exception e) {
			e.printStackTrace();
		}
		DirectionalLight light = new DirectionalLight(0.2f, -1f, 0f);
		light.setPower(.7f);
		getCurrentScene().addLight(light);

		light = new DirectionalLight(0.2f, 1f, 0f);
		light.setPower(1f);
		getCurrentScene().addLight(light);

		getCurrentCamera().setFarPlane(1000);

		getCurrentScene().setBackgroundColor(0xdddddd);

		createTerrain();

		try {
			getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx, R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
			setObject();
			setAnim();
			//X: right
			//Y: height
			//Z: back
		} catch(Exception e) {
			e.printStackTrace();
		}

		super.initScene();
	}
	private void setAnim() {
		CatmullRomCurve3D path = choosepath();
		SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(path);
		anim.setDurationMilliseconds(88000);
		anim.setRepeatMode(Animation.RepeatMode.INFINITE);
		anim.setOrientToPath(true);
		anim.setTransformable3D(currentObj);
		getCurrentScene().registerAnimation(anim);
		anim.play();
	}
	public void setObject() {
		int index =(int) ((Math.random()) * 4);
		Log.v("new object added,index ", Integer.toString(index));
		LoaderOBJ loaderobj;
		LoaderAWD awd;
		currentObj = spaceshipobj.getParsedObject();

		try{
			switch (index) {

				case 0:
					loaderobj = spaceshipobj;
					currentObj = loaderobj.getParsedObject();
					currentObj.setScale(0.3);
					break;
				case 1:
					loaderobj = hellfireobj;
					currentObj = loaderobj.getParsedObject();
					currentObj.setScale(0.3);
					//loaderobj = ewingobj;
					break;
				case 2:
					awd= new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.capital);
					awd.parse();
					Material capitalMaterial = new Material();
					capitalMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
					capitalMaterial.setColorInfluence(0);
					capitalMaterial.enableLighting(true);
					capitalMaterial.addTexture(new Texture("capitalTex", R.drawable.hullw));
					capitalMaterial.addTexture(new NormalMapTexture("capitalNormTex", R.drawable.hulln));
					currentObj = awd.getParsedObject();
					currentObj.setMaterial(capitalMaterial);
					break;
				case 3:
					awd= new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.dark_fighter);
					awd.parse();
					Material darkFighterMaterial = new Material();
					darkFighterMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
					darkFighterMaterial.setColorInfluence(0);
					darkFighterMaterial.enableLighting(true);
					darkFighterMaterial.addTexture(new Texture("darkFighterTex", R.drawable.dark_fighter_6_color));

					currentObj = awd.getParsedObject();
					currentObj.setMaterial(darkFighterMaterial);
					break;
				default:
					loaderobj = spaceshipobj;
					currentObj = loaderobj.getParsedObject();
					break;
			}

		}catch(Exception e){

		}

		//currentObj.setY(-2);//height
		//currentObj.setX(1);//right is positive, left is negative
		//currentObj.setRotY(90);
		//currentObj.setZ(-3);//front is negative   back is positive
		//currentObj.setScale(0.3);
		getCurrentScene().addChild(currentObj);
	}
	private CatmullRomCurve3D choosepath(){
		int n =(int) ((Math.random()) * 3);

		while(pathselection==n){
			n =(int) ((Math.random()) * 3);
		}
		pathselection = n;
		CatmullRomCurve3D path = new CatmullRomCurve3D();
		switch (n){
			case 0:{
				path.addPoint(new Vector3(0, -5, -10));//points that object will go through
				path.addPoint(new Vector3(10, -5, 0));
				path.addPoint(new Vector3(0, -4, 8));
				path.addPoint(new Vector3(-16, -6, 0));
				break;
			}
			case 1:{
				path.addPoint(new Vector3(-10, 2, -30));//points that object will go through
				path.addPoint(new Vector3(-10, 0, -20));
				path.addPoint(new Vector3(-10, -2, -0));
				path.addPoint(new Vector3(5, -2, 20));
				path.addPoint(new Vector3(10, 0, 0));
				path.addPoint(new Vector3(10, -2, -10));
				path.addPoint(new Vector3(10, -5, -30));
				break;
			}
			case 2:{
				path.addPoint(new Vector3(-30, 2, 10));//points that object will go through
				path.addPoint(new Vector3(-20, 0, 10));
				path.addPoint(new Vector3(0, -2, 10));
				path.addPoint(new Vector3(20, -2, -5));
				path.addPoint(new Vector3(0, 0, 10));
				path.addPoint(new Vector3(-10, -2, 10));
				path.addPoint(new Vector3(-30, -5, 10));
				break;
			}
			default:{
				path.addPoint(new Vector3(0, 5, 10));//points that object will go through
				path.addPoint(new Vector3(10, 5, 0));
				path.addPoint(new Vector3(0, 4, 8));
				path.addPoint(new Vector3(16, 6, 0));
				path.isClosedCurve(true);
				break;
			}
		}
		path.isClosedCurve(true);
		return path;
	}

	public void createTerrain() {
		//
		// -- Load a bitmap that represents the terrain. Its color values will
		//    be used to generate heights.
		//

		Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.terrain1);

		try {
			SquareTerrain.Parameters terrainParams = SquareTerrain.createParameters(bmp);
			// -- set terrain scale
			//terrainParams.setScale(4f, 54f, 4f); //for terrain.jpg

			terrainParams.setScale(4f, 110f, 4f);
			// -- the number of plane subdivisions
			terrainParams.setDivisions(128);
			// -- the number of times the textures should be repeated
			terrainParams.setTextureMult(4);
			//
			// -- Terrain colors can be set by manually specifying base, middle and
			//    top colors.
			//
			// --  terrainParams.setBasecolor(Color.argb(255, 0, 0, 0));
			//     terrainParams.setMiddleColor(Color.argb(255, 200, 200, 200));
			//     terrainParams.setUpColor(Color.argb(255, 0, 30, 0));
			//
			// -- However, for this example we'll use a bitmap
			//
			//terrainParams.setColorMapBitmap(bmp);
			terrainParams.setBasecolor(Color.argb(0, 0, 0, 0));
			terrainParams.setMiddleColor(Color.argb(122, 122, 122, 200));
			terrainParams.setUpColor(Color.argb(255, 255, 255, 0));
			terrainParams.setColorMapBitmap(bmp);

			//
			// -- create the terrain
			//
			mTerrain = TerrainGenerator.createSquareTerrainFromBitmap(terrainParams);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		// -- The bitmap won't be used anymore, so get rid of it.
		//
		bmp.recycle();

		//
		// -- A normal map material will give the terrain a bit more detail.
		//
		Material material = new Material();
		material.enableLighting(true);
		material.useVertexColors(true);
		material.setDiffuseMethod(new DiffuseMethod.Lambert());
		try {
			Texture groundTexture = new Texture("ground", R.drawable.ground);
			groundTexture.setInfluence(.5f);
			material.addTexture(groundTexture);
			material.addTexture(new NormalMapTexture("groundNormalMap", R.drawable.groundnor));
			material.setColorInfluence(0);
		} catch (TextureException e) {
			e.printStackTrace();
		}

		//
		// -- Blend the texture with the vertex colors
		//
		material.setColorInfluence(.5f);
		mTerrain.setY(-200);
		mTerrain.setMaterial(material);

		getCurrentScene().addChild(mTerrain);
	}

	private boolean isLookingAtObj(Object3D object3D) {

		//	Matrix.multiplyMM(modelView, 0, headView, 0, object3D.getScenePosition(), 0);
		Matrix.multiplyMV(tempPosition, 0, object3D.getModelViewMatrix().getFloatValues(), 0, POS_MATRIX_MULTIPLY_VEC, 0);
		float pitch = (float) Math.atan2(tempPosition[1], -tempPosition[2]);
		float yaw = (float) Math.atan2(tempPosition[0], -tempPosition[2]);
		Boolean bool = Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
		String result = Boolean.toString(bool);
		return bool;
	}
	public void setScore(int plus) {
		score += plus;
	}

	public Integer getScore() {
		return score;
	}
}


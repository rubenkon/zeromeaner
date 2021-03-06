package org.zeromeaner.gui.reskin;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.PropertyConfigurator;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.plaf.ZeroMetalTheme;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.PropertyConstant;
import org.zeromeaner.util.ResourceInputStream;
import org.zeromeaner.util.ResourceOutputStream;
import org.zeromeaner.util.Session;
import org.zeromeaner.util.io.PropertyStore;

import com.esotericsoftware.minlog.Log;

public class StandaloneMain {
	public static ModeList modeManager;

	private static boolean applet;

	public static boolean isApplet() {
		return applet;
	}

	public static void setApplet(boolean applet) {
		StandaloneMain.applet = applet;
	}

	public static void main(String[] args) {
		try {
			init(args).setVisible(true);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void restart() {
		StandaloneMain.saveConfig();
		URL jar = StandaloneGeneralConfigPanel.class.getClassLoader().getClass().getProtectionDomain().getCodeSource().getLocation();
		String path = jar.getPath().replaceAll("!.*", "");
		boolean linux = System.getProperty("os.name").toUpperCase().contains("linux");
		if(linux)
			path = "\"" + path + "\"";
		try {
			Runtime.getRuntime().exec(new String[]{"java", "-jar", path});
			System.exit(0);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void loadGlobalConfig() {
		try {
			InputStream in = new ResourceInputStream("setting/global.cfg");
			Options.GLOBAL_PROPERTIES.load(in);
			in.close();
		} catch(FileNotFoundException fnfe) {
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
		try {
			InputStream in = new ResourceInputStream("setting/swing.cfg");
			Options.GUI_PROPERTIES.load(in);
			in.close();
		} catch(FileNotFoundException fnfe) {
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
		try {
			InputStream in = new ResourceInputStream("setting/runtime.cfg");
			Options.RUNTIME_PROPERTIES.load(in);
			in.close();
		} catch(FileNotFoundException fnfe) {
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public static void saveConfig() {
		try {
			ResourceOutputStream out = new ResourceOutputStream("setting/global.cfg");
			Options.GLOBAL_PROPERTIES.store(out, "zeromeaner global Config");
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		try {
			ResourceOutputStream out = new ResourceOutputStream("setting/swing.cfg");
			Options.GUI_PROPERTIES.store(out, "zeromeaner Swing-frontend Config");
			out.close();
		} catch(IOException e) {
		}
		try {
			ResourceOutputStream out = new ResourceOutputStream("setting/runtime.cfg");
			Options.RUNTIME_PROPERTIES.store(out, "zeromeaner Swing-frontend Config");
			out.close();
		} catch(IOException e) {
		}
	}

	public static StandaloneFrame init(String[] args) throws Exception {
		Log.NONE();
		PropertyConfigurator.configure(StandaloneMain.class.getClassLoader().getResource("org/zeromeaner/config/etc/log_nothing.cfg"));

		if(!isApplet()) {
			System.setProperty("user.dir", System.getProperty("user.home") + File.separator + ".zeromeaner");
			new File(System.getProperty("user.dir")).mkdirs();

			if(args.length == 0 || !"--no-inject".equals(args[0])) {
				File plugins = new File(System.getProperty("user.dir"), "plugins");
				plugins.mkdirs();

				File disabled = new File(plugins, "disabled-plugins");
				disabled.mkdirs();

				try {
					Method addURL = StandaloneMain.class.getClassLoader().getClass().getDeclaredMethod("addURL", URL.class);
					addURL.setAccessible(true);
					for(File p : plugins.listFiles()) {
						if(p.equals(disabled))
							continue;
						System.out.println("Injecting plugin " + p);
						addURL.invoke(StandaloneMain.class.getClassLoader(), p.toURI().toURL());
					}
				} catch(NoSuchMethodException e) {
				}
			}

			Session.setUser(System.getProperty("user.name"));

			try {
				PropertyConfigurator.configure(new ResourceInputStream("config/etc/log_gui.cfg"));
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		if(PropertyStore.get().get("userId") != null)
			Session.setUser(PropertyStore.get().get("userId"));


		MetalLookAndFeel.setCurrentTheme(new ZeroMetalTheme());
		UIManager.setLookAndFeel(new MetalLookAndFeel());

		loadGlobalConfig();

		modeManager = ModeList.getModes();

		StandaloneGameKey.initGlobalGameKeySwing();
		StandaloneGameKey.gamekey[0].loadDefaultKeymap();
		StandaloneGameKey.gamekey[1].loadDefaultKeymap();

		if(Options.GUI_PROPERTIES.subProperties("key.p").getAll().size() == 0) {
			System.out.println("no key prefs?  adding defaults!");
			StandaloneGameKey.gamekey[0].saveConfig(Options.GUI_PROPERTIES);
			StandaloneGameKey.gamekey[1].saveConfig(Options.GUI_PROPERTIES);
		}

		StandaloneGameKey.gamekey[0].loadConfig(Options.GUI_PROPERTIES);
		StandaloneGameKey.gamekey[1].loadConfig(Options.GUI_PROPERTIES);

		StandaloneResourceHolder.load();

		StandaloneFrame frame = new StandaloneFrame();

		return frame;
	}
}

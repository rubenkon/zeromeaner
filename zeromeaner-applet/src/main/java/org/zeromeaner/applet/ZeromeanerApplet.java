package org.zeromeaner.applet;

import static org.zeromeaner.gui.reskin.StandaloneMain.userId;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.PropertyConfigurator;
import org.zeromeaner.gui.reskin.StandaloneFrame;
import org.zeromeaner.gui.reskin.StandaloneGameKey;
import org.zeromeaner.gui.reskin.StandaloneMain;
import org.zeromeaner.gui.reskin.StandaloneResourceHolder;
import org.zeromeaner.plaf.ZeroMetalTheme;
import org.zeromeaner.util.EQInvoker;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.PropertyStore;
import org.zeromeaner.util.ResourceInputStream;

public class ZeromeanerApplet extends JApplet {
	private static ZeromeanerApplet instance;
	public static URL url;

	public static boolean isApplet() {
		return instance != null;
	}
	
	public static ZeromeanerApplet getInstance() {
		return instance;
	}

	private JPanel panel = new JPanel(new GridBagLayout());

	public ZeromeanerApplet() {
		panel.setBackground(new Color(0,0,64));
		panel.setOpaque(true);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}
	
	@Override
	public void init() {

		Runnable laf = new Runnable() {
			@Override
			public void run() {
				try {
					MetalLookAndFeel.setCurrentTheme(new ZeroMetalTheme());
					UIManager.setLookAndFeel(new MetalLookAndFeel());
				} catch(UnsupportedLookAndFeelException e) {
				}
			}
		};
		
		laf.run();
		
		if(EQInvoker.reinvoke(false, this)) {
			validate();
			repaint();
			return;
		}



		instance = this;

		new Thread(new Runnable() {
			@Override
			public void run() {
				startup();
			}
		}).start();;
	}
	
	private void startup() {
		
		url = getDocumentBase();
		if(System.getProperty("zero_url") != null) {
			try {
				url = new URL(System.getProperty("zero_url"));
			} catch(Exception ex) {
			}
		}

		if(userId == null)
			userId = PropertyStore.get().get("userId");
		if(userId == null)
			userId = getParameter("userId");

		while(userId == null || "default".equals(userId)) {
			userId = "none";
			int create = JOptionPane.showConfirmDialog(this, "To save user configuration, such as custom keys, you must create a user id.\nThere is no need to remember a password.\nIf you choose not to create a user ID the default settings will be used.\n\nCreate a user ID now?", "Create User ID?", JOptionPane.YES_NO_OPTION);
			if(create == JOptionPane.YES_OPTION) {
				userId = (String) JOptionPane.showInputDialog(this, "Enter Config ID", "Enter Config ID", JOptionPane.QUESTION_MESSAGE, null, null, "");
				if(userId != null)
					PropertyStore.get().put("userId", userId);
				else
					userId = "default";
			}
		}

		try {

			try {
				PropertyConfigurator.configure(new ResourceInputStream("config/etc/log_applet.cfg"));
			} catch(IOException ioe) {
			}

			StandaloneMain.loadGlobalConfig();

			StandaloneMain.modeManager = ModeList.getModes();

			StandaloneGameKey.initGlobalGameKeySwing();
			StandaloneGameKey.gamekey[0].loadDefaultKeymap();
			StandaloneGameKey.gamekey[1].loadDefaultKeymap();

			StandaloneGameKey.gamekey[0].loadConfig(Options.GUI_PROPERTIES);
			StandaloneGameKey.gamekey[1].loadConfig(Options.GUI_PROPERTIES);

			StandaloneResourceHolder.load();

			final StandaloneFrame frame = new StandaloneFrame();
			frame.setUndecorated(false);

			panel.add(frame.getRootPane(), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
			validate();

			if(url.getQuery() != null) {
				String[] qf = url.getQuery().split("&");
				for(String qpp : qf) {
					final String[] qp = qpp.split("=", 2);
					if("replay".equals(qp[0]) && qp.length > 1) {
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								frame.startReplayGame(qp[1]);
							}
						});
						break;
					}

				}
			}

		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void stop() {
	}

	@Override
	public void destroy() {
		StandaloneMain.saveConfig();
		System.exit(0);
	}
}

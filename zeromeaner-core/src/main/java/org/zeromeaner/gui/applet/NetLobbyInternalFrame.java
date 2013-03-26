package org.zeromeaner.gui.applet;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;

import org.zeromeaner.game.knet.obj.KNetChannelInfo;
import org.zeromeaner.gui.knet.KNetPanel;
import org.zeromeaner.gui.knet.KNetPanelAdapter;
import org.zeromeaner.gui.knet.KNetPanelEvent;

public class NetLobbyInternalFrame extends JInternalFrame {
	private KNetPanel knetPanel;
	
	public NetLobbyInternalFrame() {
		knetPanel = new KNetPanel();
		
		setLayout(new BorderLayout());
		add(knetPanel, BorderLayout.CENTER);

		setSize(800, 300);
		
		AppletMain.instance.desktop.add(this);
		setVisible(true);
		
		setLocation(0, 510);
	}
	
	public void init() {
		knetPanel.init();
		knetPanel.addKNetPanelListener(new KNetPanelAdapter() {
			@Override
			public void knetPanelJoined(KNetPanelEvent e) {
				KNetChannelInfo ci = e.getSource().getChannels().get(e.getChannel().getId()).getChannel();
				NullpoMinoInternalFrame.gameFrame.strModeToEnter = ci.getMode();
			}
			@Override
			public void knetPanelParted(KNetPanelEvent e) {
				NullpoMinoInternalFrame.gameFrame.strModeToEnter = null;
			}
			@Override
			public void knetPanelShutdown(KNetPanelEvent e) {
				setVisible(false);
			}
		});
	}
	
	public void shutdown() {
		knetPanel.shutdown();
	}
	
	public KNetPanel getKnetPanel() {
		return knetPanel;
	}
}
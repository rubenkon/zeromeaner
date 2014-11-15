package org.zeromeaner.gui.reskin;

import java.applet.Applet;
import java.util.Collections;
import java.util.Map;

//import netscape.javascript.JSObject;


public class CookieAccess {
	private static CookieAccess instance = new CookieAccess();
	
	public static CookieAccess getInstance() {
		return instance;
	}
	
	public static void setInstance(CookieAccess instance) {
		CookieAccess.instance = instance;
	}
	
	public static String get(String key) {
		String c = instance.get().get(key);
		if(c == null)
			c = System.getProperty(key);
		return c;
	}
	
	public Map<String, String> get() {
		return get(StandaloneApplet.getInstance());
	}
	
	protected Map<String, String> get(Applet applet) {
		return Collections.emptyMap();
//		try {
//			String data = "";
//			JSObject myBrowser = JSObject.getWindow(applet);
//			JSObject myDocument = (JSObject) myBrowser.getMember("document");
//
//			String myCookie = (String) myDocument.getMember("cookie");
//
//			if (myCookie.length() > 0) {
//				String[] cookies = myCookie.split(";");
//				for (String cookie : cookies) {
//					int pos = cookie.indexOf("=");
//					if (cookie.substring(0, pos).trim().equals("c")) {
//						data = cookie.substring(pos + 1);
//						break;
//					}
//				}
//			}
//			if("".equals(data))
//				return new TreeMap<String, String>();
//			ByteArrayOutputStream bout = new ByteArrayOutputStream();
//			for(int i = 0; i < data.length(); i += 2) {
//				bout.write(Integer.parseInt(data.substring(i, i+2), 16));
//			}
//			return (Map<String, String>) new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray())).readObject();
//		} catch(Throwable t) {
//			return new TreeMap<String, String>();
//		}
	}
	
	public void set(Map<String, String> cookie) {
		set(StandaloneApplet.getInstance(), cookie);
	}
	
	public static void put(String key, String val) {
		Map<String, String> c = instance.get();
		c.put(key, val);
		instance.set(c);
	}

	protected void set(StandaloneApplet applet, Map<String, String> cookie) {
//		try {
//			ByteArrayOutputStream bout = new ByteArrayOutputStream();
//			ObjectOutputStream out = new ObjectOutputStream(bout);
//			out.writeObject(cookie);
//			out.close();
//
//			StringBuilder sb = new StringBuilder();
//			for(byte b : bout.toByteArray()) {
//				sb.append(String.format("%02x", b));
//			}
//			String value = sb.toString();
//			JSObject win = JSObject.getWindow(applet);
//			JSObject doc = (JSObject) win.getMember("document");
//			String data = "c=" + value + "; path=/; expires=Thu, 31-Dec-2019 12:00:00 GMT";
//			doc.setMember("cookie", data);
//		} catch(Throwable t) {
//		}
	}
}

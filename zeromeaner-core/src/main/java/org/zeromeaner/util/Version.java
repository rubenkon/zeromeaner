package org.zeromeaner.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Version {
	private static Version buildVersion;
	private static String buildRevision;
	
	static {
		try {
			Properties p = new Properties();
			p.load(Version.class.getClassLoader().getResourceAsStream("org/zeromeaner/res/version.properties"));
			buildVersion = new Version(p.getProperty("project.version"));
			buildRevision = p.getProperty("git.revision");
			if(buildRevision.isEmpty())
				buildRevision = "IDE";
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public static Version getBuildVersion() {
		return buildVersion;
	}
	
	public static String getBuildRevision() {
		return buildRevision;
	}
	
	private List<Integer> digits = new ArrayList<Integer>();
	private String free;
	private boolean snapshot;
	
	public Version(String version) {
		String[] va = version.split("[\\.\\-]");
		for(String d : va) {
			if(d.equals("SNAPSHOT"))
				snapshot = true;
			else if(d.matches("[0-9]+"))
				digits.add(Integer.parseInt(d));
			else if(free == null)
				free = d;
			else
				free += "-" + d;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(Integer d : digits) {
			sb.append(sep);
			sb.append(d);
			sep = ".";
		}
		if(free != null)
			sb.append("-" + free);
		if(snapshot)
			sb.append("-SNAPSHOT");
		return sb.toString();
	}

	public int digitOrZero(int index) {
		if(digits.size() > index)
			return digits.get(index);
		else
			return 0;
	}
	
	public int getMajor() {
		return digitOrZero(0);
	}
	
	public int getMinor() {
		return digitOrZero(1);
	}
	
	public int getMicro() {
		return digitOrZero(2);
	}
	
	public int getNano() {
		return digitOrZero(3);
	}

	public boolean isSnapshot() {
		return snapshot;
	}

	public String getFree() {
		return free;
	}
}

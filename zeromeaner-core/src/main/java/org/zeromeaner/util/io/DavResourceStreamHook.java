package org.zeromeaner.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.zeromeaner.util.Session;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.util.SardineException;

public class DavResourceStreamHook implements ResourceStreamHook {
	
	protected static final String URL_BASE = "http://www.zeromeaner.org/dav/";
	
	protected static final Pattern NON_DAV = Pattern.compile("^(config|gui|res)/.*");
	
	protected Sardine sardine;
	
	public DavResourceStreamHook() throws SardineException {
		sardine = SardineFactory.begin();
	}

	@Override
	public void addInputHandler(final String resource, PrioritizedHandler<Callable<InputStream>> handlers) {
		if(Session.ANONYMOUS_USER.equals(Session.getUser()))
			return;
		if(NON_DAV.matcher(resource).matches())
			return;
		try {
			sardine.getInputStream(URL_BASE + Session.getUser() + "/" + resource).close();;
		} catch(Exception e) {
			return;
		}
		Callable<InputStream> handler = new Callable<InputStream>() {
			@Override
			public InputStream call() throws Exception {
				return sardine.getInputStream(URL_BASE + Session.getUser() + "/" + resource);
			}
		};
		handlers.add(0, handler);
	}

	@Override
	public void addOutputHandler(final String resource, PrioritizedHandler<Callable<OutputStream>> handlers) {
		if(Session.ANONYMOUS_USER.equals(Session.getUser()))
			return;
		if(NON_DAV.matcher(resource).matches())
			return;
		Callable<OutputStream> handler = new Callable<OutputStream>() {
			@Override
			public OutputStream call() throws Exception {
				return new DavOutputStream(resource);
			}
		};
		handlers.add(0, handler);
	}
	
	protected class DavOutputStream extends ByteArrayOutputStream {
		protected String resource;
		
		public DavOutputStream(String resource) {
			this.resource = resource;
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			sardine.put(URL_BASE + Session.getUser() + "/" + resource, toByteArray());
		}
	}

}

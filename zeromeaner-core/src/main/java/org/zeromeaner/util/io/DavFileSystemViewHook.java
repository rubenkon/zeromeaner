package org.zeromeaner.util.io;

import java.util.concurrent.Callable;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.zeromeaner.util.Options;

public class DavFileSystemViewHook implements FileSystemViewHook {

	@Override
	public void addFileSystemView(final String limit, PrioritizedHandler<Callable<FileSystemView>> handlers) {
		if(!Options.isDavEnabled())
			return;
		Callable<FileSystemView> handler = new Callable<FileSystemView>() {
			@Override
			public FileSystemView call() throws Exception {
				return new DavFileSystemView(limit);
			}
		};
		handlers.add(-1, handler);
	}

	@Override
	public void addFileChooser(final String path, PrioritizedHandler<Callable<JFileChooser>> handlers) {
		if(!Options.isDavEnabled())
			return;
		Callable<JFileChooser> handler = new Callable<JFileChooser>() {
			@Override
			public JFileChooser call() throws Exception {
				return new JFileChooser("", new DavFileSystemView(path));
			}
		};
		handlers.add(-1, handler);
	}

}

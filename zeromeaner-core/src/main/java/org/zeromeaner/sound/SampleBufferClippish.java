package org.zeromeaner.sound;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.io.IOUtils;
import org.zeromeaner.gui.reskin.StandaloneResourceHolder;

public class SampleBufferClippish extends Thread {
	
	private static final byte[] silence;
	static {
		Arrays.fill(silence = new byte[128], (byte) 0x80);
	}
	
	protected AudioFormat format;
	protected SourceDataLine line;
	protected SampleBuffer sample;
	
	protected byte[] sbuf;
	
	protected Runnable emptiedTask;
	
	protected Object sync = new Object();
	
	public SampleBufferClippish(AudioFormat format, SourceDataLine line) {
		this.format = format;
		this.line = line;
		sbuf = new byte[512];
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				synchronized(sync) {
					if(sample != null && sample.getBytes().remaining() == 0) {
						if(emptiedTask != null	)
							emptiedTask.run();
						sample = null;
					}
				}
				Arrays.fill(sbuf, (byte) 0x80);
				int count;
				if(sample != null) {
					if(sample.getBytes().position() == 0)
						line.flush();
					count = Math.min(sample.getBytes().remaining(), sbuf.length);
					sample.getBytes().get(sbuf, 0, count);
				} else
					count = sbuf.length;
				line.write(sbuf, 0, sbuf.length);
				try {
					Thread.sleep(Math.max(0, (long)(count * 1000L / format.getSampleRate()) - 50));
				} catch (InterruptedException e) {
				}
			}
		} catch(RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void setSample(SampleBuffer sample, boolean flush) {
		this.sample = new SampleBuffer(sample.getFormat(), sample.getBytes().duplicate());
		interrupt();
	}
	
	public void setEmptiedTask(Runnable emptiedTask) {
		this.emptiedTask = emptiedTask;
	}
	
	public SourceDataLine getLine() {
		return line;
	}
}

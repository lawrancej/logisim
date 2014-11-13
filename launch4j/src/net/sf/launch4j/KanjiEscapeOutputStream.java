/*
 * Created on Sep 20, 2013
 */
package net.sf.launch4j;

import java.io.OutputStream;
import java.io.IOException;

/**
 * @author toshimm (2013)
 * 
 *         This class makes Japanese Kanji characters in MS932 charcode escaped
 *         in octal form.
 */
public class KanjiEscapeOutputStream extends OutputStream {

	protected OutputStream parent;

	public KanjiEscapeOutputStream(OutputStream out) {
		this.parent = out;
	}

	private final int MASK = 0x000000FF;
	private boolean state = true;

	public void write(int b) throws IOException {
		b = b & MASK;

		if (state) {
			if (0x00 <= b && b <= 0x7f) {
				this.parent.write(b);
			} else {
				this.octprint(b);
				if ((0x81 <= b && b <= 0x9f) || (0xe0 <= b && b <= 0xfc)) {
					this.state = false;
				}
			}
		} else {
			if ((0x40 <= b && b <= 0x7e) || (0x80 <= b && b <= 0xfc)) {
				this.octprint(b);
			} else if (0x00 <= b && b <= 0x7f) {
				this.parent.write(b);
			} else {
				this.octprint(b);
			}
			this.state = true;
		}
	}

	private void octprint(int b) throws IOException {
		String oct = "\\" + String.format("%o", b & MASK);
		for (int i = 0; i < oct.length(); ++i) {
			int bb = oct.charAt(i);
			this.parent.write(bb);
		}
	}
}

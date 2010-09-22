/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.shapes;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.model.Handle;
import com.cburch.draw.model.HandleGesture;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.UnmodifiableList;

public class Curve extends FillableCanvasObject {
	private Location p0;
	private Location p1;
	private Location p2;
	private Bounds bounds;
	
	public Curve(Location end0, Location end1, Location ctrl) {
		this.p0 = end0;
		this.p1 = ctrl;
		this.p2 = end1;
		bounds = CurveUtil.getBounds(toArray(p0), toArray(p1), toArray(p2));
	}
	
	@Override
	public boolean matches(CanvasObject other) {
		if (other instanceof Curve) {
			Curve that = (Curve) other;
			return this.p0.equals(that.p0) && this.p1.equals(that.p1)
				&& this.p2.equals(that.p2) && super.matches(that);
		} else {
			return false;
		}
	}

	@Override
	public int matchesHashCode() {
		int ret = p0.hashCode();
		ret = ret * 31 * 31 + p1.hashCode();
		ret = ret * 31 * 31 + p2.hashCode();
		ret = ret * 31 + super.matchesHashCode();
		return ret;
	}
	
	@Override
	public Element toSvgElement(Document doc) {
		return SvgCreator.createCurve(doc, this);
	}

	public Location getEnd0() {
		return p0;
	}
	
	public Location getEnd1() {
		return p2;
	}
	
	public Location getControl() {
		return p1;
	}
	
	public QuadCurve2D getCurve2D() {
		return new QuadCurve2D.Double(p0.getX(), p0.getY(),
				p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}
	
	@Override
	public String getDisplayName() {
		return Strings.get("shapeCurve");
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.getFillAttributes(getPaintType());
	}
	
	@Override
	public Bounds getBounds() {
		return bounds;
	}
	
	@Override
	public boolean contains(Location loc, boolean assumeFilled) {
		Object type = getPaintType();
		if (assumeFilled && type == DrawAttr.PAINT_STROKE) {
			type = DrawAttr.PAINT_STROKE_FILL;
		}
		if (type != DrawAttr.PAINT_FILL) {
			int stroke = getStrokeWidth();
			double[] q = toArray(loc); 
			double[] p0 = toArray(this.p0);
			double[] p1 = toArray(this.p1);
			double[] p2 = toArray(this.p2);
			double[] p = CurveUtil.findNearestPoint(q, p0, p1, p2);
			if (p == null) return false;
			
			int thr;
			if (type == DrawAttr.PAINT_STROKE) {
				thr = Math.max(Line.ON_LINE_THRESH, stroke / 2);
			} else {
				thr = stroke / 2;
			}
			if (LineUtil.distanceSquared(p[0], p[1], q[0], q[1]) < thr * thr) {
				return true;
			}
		}
		if (type != DrawAttr.PAINT_STROKE) {
			QuadCurve2D curve = getCurve(null);
			if (curve.contains(loc.getX(), loc.getY())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void translate(int dx, int dy) {
		p0 = p0.translate(dx, dy);
		p1 = p1.translate(dx, dy);
		p2 = p2.translate(dx, dy);
		bounds = bounds.translate(dx, dy);
	}
	
	public List<Handle> getHandles() {
		return UnmodifiableList.create(getHandleArray(null));
	}
	
	@Override
	public List<Handle> getHandles(HandleGesture gesture) {
		return UnmodifiableList.create(getHandleArray(gesture));
	}
	
	private Handle[] getHandleArray(HandleGesture gesture) {
		if (gesture == null) {
			return new Handle[] { new Handle(this, p0), new Handle(this, p1),
					new Handle(this, p2) };
		} else {
			Handle g = gesture.getHandle();
			int dx = gesture.getDeltaX();
			int dy = gesture.getDeltaY();
			Handle[] ret = new Handle[3];
			ret[0] = new Handle(this, g.isAt(p0) ? p0.translate(dx, dy) : p0);
			ret[1] = new Handle(this, g.isAt(p1) ? p1.translate(dx, dy) : p1);
			ret[2] = new Handle(this, g.isAt(p2) ? p2.translate(dx, dy) : p2);
			return ret;
		}
	}
	
	@Override
	public boolean canMoveHandle(Handle handle) {
		return true;
	}
	
	@Override
	public Handle moveHandle(HandleGesture gesture) {
		Handle h = gesture.getHandle();
		int dx = gesture.getDeltaX();
		int dy = gesture.getDeltaY();
		Handle ret = null;
		if (h.isAt(p0)) {
			p0 = p0.translate(dx, dy);
			ret = new Handle(this, p0);
		}
		if (h.isAt(p1)) {
			p1 = p1.translate(dx, dy);
			ret = new Handle(this, p1);
		}
		if (h.isAt(p2)) {
			p2 = p2.translate(dx, dy);
			ret = new Handle(this, p2);
		}
		bounds = CurveUtil.getBounds(toArray(p0), toArray(p1), toArray(p2));
		return ret;
	}
	
	@Override
	public void paint(Graphics g, HandleGesture gesture) {
		QuadCurve2D curve = getCurve(gesture);
		if (setForFill(g)) {
			((Graphics2D) g).fill(curve);
		}
		if (setForStroke(g)) {
			((Graphics2D) g).draw(curve);
		}
	}
	
	private QuadCurve2D getCurve(HandleGesture gesture) {
		Handle[] p = getHandleArray(gesture);
		return new QuadCurve2D.Double(p[0].getX(), p[0].getY(),
				p[1].getX(), p[1].getY(), p[2].getX(), p[2].getY());
	}
	
	private static double[] toArray(Location loc) {
		return new double[] { loc.getX(), loc.getY() };
	}
}

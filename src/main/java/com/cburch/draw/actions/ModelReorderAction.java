/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.model.ReorderRequest;
import com.cburch.draw.util.ZOrder;
import static com.cburch.logisim.util.LocaleString.*;

public class ModelReorderAction extends ModelAction {
	public static ModelReorderAction createRaise(CanvasModel model,
			Collection<? extends CanvasObject> objects) {
		List<ReorderRequest> reqs = new ArrayList<ReorderRequest>();
		Map<CanvasObject, Integer> zmap = ZOrder.getZIndex(objects, model);
		for(Map.Entry<CanvasObject, Integer> entry : zmap.entrySet()) {
			CanvasObject obj = entry.getKey();
			int from = entry.getValue().intValue();
			CanvasObject above = ZOrder.getObjectAbove(obj, model, objects);
			if (above != null) {
				int to = ZOrder.getZIndex(above, model);
				if (objects.contains(above)) {
					to--;
				}
				reqs.add(new ReorderRequest(obj, from, to));
			}
		}
		if (reqs.isEmpty()) {
			return null;
		} else {
			Collections.sort(reqs, ReorderRequest.DESCENDING_FROM);
			repairRequests(reqs);
			return new ModelReorderAction(model, reqs);
		}
	}

	public static ModelReorderAction createLower(CanvasModel model,
			Collection<? extends CanvasObject> objects) {
		List<ReorderRequest> reqs = new ArrayList<ReorderRequest>();
		Map<CanvasObject, Integer> zmap = ZOrder.getZIndex(objects, model);
		for(Map.Entry<CanvasObject, Integer> entry : zmap.entrySet()) {
			CanvasObject obj = entry.getKey();
			int from = entry.getValue().intValue();
			CanvasObject above = ZOrder.getObjectBelow(obj, model, objects);
			if (above != null) {
				int to = ZOrder.getZIndex(above, model);
				if (objects.contains(above)) {
					to++;
				}
				reqs.add(new ReorderRequest(obj, from, to));
			}
		}
		if (reqs.isEmpty()) {
			return null;
		} else {
			Collections.sort(reqs, ReorderRequest.ASCENDING_FROM);
			repairRequests(reqs);
			return new ModelReorderAction(model, reqs);
		}
	}
	
	public static ModelReorderAction createRaiseTop(CanvasModel model,
			Collection<? extends CanvasObject> objects) {
		List<ReorderRequest> reqs = new ArrayList<ReorderRequest>();
		Map<CanvasObject, Integer> zmap = ZOrder.getZIndex(objects, model);
		int to = model.getObjectsFromBottom().size() - 1;
		for(Map.Entry<CanvasObject, Integer> entry : zmap.entrySet()) {
			CanvasObject obj = entry.getKey();
			int from = entry.getValue().intValue();
			reqs.add(new ReorderRequest(obj, from, to));
		}
		if (reqs.isEmpty()) {
			return null;
		} else {
			Collections.sort(reqs, ReorderRequest.ASCENDING_FROM);
			repairRequests(reqs);
			return new ModelReorderAction(model, reqs);
		}
	}
	
	public static ModelReorderAction createLowerBottom(CanvasModel model,
			Collection<? extends CanvasObject> objects) {
		List<ReorderRequest> reqs = new ArrayList<ReorderRequest>();
		Map<CanvasObject, Integer> zmap = ZOrder.getZIndex(objects, model);
		int to = 0;
		for(Map.Entry<CanvasObject, Integer> entry : zmap.entrySet()) {
			CanvasObject obj = entry.getKey();
			int from = entry.getValue().intValue();
			reqs.add(new ReorderRequest(obj, from, to));
		}
		if (reqs.isEmpty()) {
			return null;
		} else {
			Collections.sort(reqs, ReorderRequest.ASCENDING_FROM);
			repairRequests(reqs);
			return new ModelReorderAction(model, reqs);
		}
	}
	
	private static void repairRequests(List<ReorderRequest> reqs) {
		for (int i = 0, n = reqs.size(); i < n; i++) {
			ReorderRequest req = reqs.get(i);
			int from = req.getFromIndex();
			int to = req.getToIndex();
			for (int j = 0; j < i; j++) {
				ReorderRequest prev = reqs.get(j);
				int prevFrom = prev.getFromIndex();
				int prevTo = prev.getToIndex();
				if (prevFrom <= from && from < prevTo) {
					from--;
				} else if (prevTo <= from && from < prevFrom) {
					from++;
				}
				if (prevFrom <= to && to < prevTo) {
					to--;
				} else if (prevTo <= to && to < prevFrom) {
					to++;
				}
			}
			if (from != req.getFromIndex() || to != req.getToIndex()) {
				reqs.set(i, new ReorderRequest(req.getObject(), from, to));
			}
		}
		for (int i = reqs.size() - 1; i >= 0; i--) {
			ReorderRequest req = reqs.get(i);
			if (req.getFromIndex() == req.getToIndex()) {
				reqs.remove(i);
			}
		}
	}
	
	private ArrayList<ReorderRequest> requests;
	private ArrayList<CanvasObject> objects;
	private int type;
	
	public ModelReorderAction(CanvasModel model, List<ReorderRequest> requests) {
		super(model);
		this.requests = new ArrayList<ReorderRequest>(requests);
		this.objects = new ArrayList<CanvasObject>(requests.size());
		for (ReorderRequest r : requests) {
			objects.add(r.getObject());
		}
		int type = 0; // 0 = mixed/unknown, -1 = to greater index, 1 = to smaller index
		for (ReorderRequest r : requests) {
			int thisType;
			int from = r.getFromIndex();
			int to = r.getToIndex();
			if (to < from) {
				thisType = -1; 
			} else if (to > from) {
				thisType = 1;
			} else {
				thisType = 0;
			}
			if (type == 2) {
				type = thisType;
			} else if (type != thisType) {
				type = 0;
				break;
			}
		}
		this.type = type;
	}
	
	public List<ReorderRequest> getReorderRequests() {
		return Collections.unmodifiableList(requests);
	}
	
	@Override
	public Collection<CanvasObject> getObjects() {
		return objects;
	}

	@Override
	public String getName() {
		if (type < 0) {
			return _("actionRaise", getShapesName(objects));
		} else if (type > 0) {
			return _("actionLower", getShapesName(objects));
		} else {
			return _("actionReorder", getShapesName(objects));
		}
	}
	
	@Override
	void doSub(CanvasModel model) {
		model.reorderObjects(requests);
	}
	
	@Override
	void undoSub(CanvasModel model) {
		ArrayList<ReorderRequest> inv = new ArrayList<ReorderRequest>(requests.size());
		for (int i = requests.size() - 1; i >= 0; i--) {
			ReorderRequest r = requests.get(i);
			inv.add(new ReorderRequest(r.getObject(), r.getToIndex(),
					r.getFromIndex()));
		}
		model.reorderObjects(inv);
	}
}

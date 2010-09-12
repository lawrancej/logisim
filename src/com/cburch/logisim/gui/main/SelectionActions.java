/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.CircuitTransaction;
import com.cburch.logisim.circuit.CircuitTransactionResult;
import com.cburch.logisim.circuit.ReplacementMap;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.JoinedAction;
import com.cburch.logisim.proj.Project;

public class SelectionActions {
	private SelectionActions() { }
	
	public static Action drop(Selection sel, Collection<Component> comps) {
		HashSet<Component> floating = new HashSet<Component>(sel.getFloatingComponents());
		HashSet<Component> anchored = new HashSet<Component>(sel.getAnchoredComponents());
		ArrayList<Component> toDrop = new ArrayList<Component>();
		ArrayList<Component> toIgnore = new ArrayList<Component>();
		for (Component comp : comps) {
			if (floating.contains(comp)) {
				toDrop.add(comp);
			} else if (anchored.contains(comp)) {
				toDrop.add(comp);
				toIgnore.add(comp);
			}
		}
		int numDrop = toDrop.size() - toIgnore.size();
		if (numDrop == 0) {
			for (Component comp : toIgnore) {
				sel.remove(null, comp);
			}
			return null;
		} else {
			return new Drop(sel, toDrop, numDrop);
		}
	}
	
	public static Action dropAll(Selection sel) {
		return drop(sel, sel.getComponents());
	}

	public static Action clear(Selection sel) {
		return new Delete(sel);
	}
	
	public static Action duplicate(Selection sel) {
		return new Duplicate(sel);
	}

	public static Action cut(Selection sel) {
		return new Cut(sel);
	}

	public static Action copy(Selection sel) {
		return new Copy(sel);
	}

	public static Action paste(Selection sel) {
		return new Paste(sel);
	}
	
	public static Action translate(Selection sel, int dx, int dy, ReplacementMap repl) {
		return new Translate(sel, dx, dy, repl);
	}
	
	private static class Drop extends Action {
		private Selection sel;
		private Component[] drops;
		private int numDrops;
		private SelectionSave before;
		private CircuitTransaction xnReverse;
		
		Drop(Selection sel, Collection<Component> toDrop, int numDrops) {
			this.sel = sel;
			this.drops = new Component[toDrop.size()];
			toDrop.toArray(this.drops);
			this.numDrops = numDrops;
			this.before = SelectionSave.create(sel);
		}

		@Override
		public String getName() {
			return numDrops == 1 ? Strings.get("dropComponentAction")
					: Strings.get("dropComponentsAction");
		}

		@Override
		public void doIt(Project proj) {
			Circuit circuit = proj.getCurrentCircuit();
			CircuitMutation xn = new CircuitMutation(circuit);
			for (Component comp : drops) {
				sel.remove(xn, comp);
			}
			CircuitTransactionResult result = xn.execute();
			xnReverse = result.getReverseTransaction();
		}

		@Override
		public void undo(Project proj) {
			xnReverse.execute();
		}

		@Override
		public boolean shouldAppendTo(Action other) {
			Action last;
			if (other instanceof JoinedAction) last = ((JoinedAction) other).getLastAction();
			else last = other;
			
			SelectionSave otherAfter = null;
			if (last instanceof Paste) {
				otherAfter = ((Paste) last).after;
			} else if (last instanceof Duplicate) {
				otherAfter = ((Duplicate) last).after;
			}
			return otherAfter != null && otherAfter.equals(this.before);
		}
	}

	private static class Delete extends Action {
		private Selection sel;
		private CircuitTransaction xnReverse;

		Delete(Selection sel) {
			this.sel = sel;
		}

		@Override
		public String getName() {
			return Strings.get("deleteSelectionAction");
		}

		@Override
		public void doIt(Project proj) {
			Circuit circuit = proj.getCurrentCircuit();
			CircuitMutation xn = new CircuitMutation(circuit);
			sel.deleteAllHelper(xn);
			CircuitTransactionResult result = xn.execute();
			xnReverse = result.getReverseTransaction();
		}

		@Override
		public void undo(Project proj) {
			xnReverse.execute();
		}
	}

	private static class Duplicate extends Action {
		private Selection sel;
		private CircuitTransaction xnReverse;
		private SelectionSave after;

		Duplicate(Selection sel) {
			this.sel = sel;
		}

		@Override
		public String getName() {
			return Strings.get("duplicateSelectionAction");
		}

		@Override
		public void doIt(Project proj) {
			Circuit circuit = proj.getCurrentCircuit();
			CircuitMutation xn = new CircuitMutation(circuit);
			sel.duplicateHelper(xn);

			CircuitTransactionResult result = xn.execute();
			xnReverse = result.getReverseTransaction();
			after = SelectionSave.create(sel);
		}

		@Override
		public void undo(Project proj) {
			xnReverse.execute();
		}
	}

	private static class Cut extends Action {
		private Action first;
		private Action second;

		Cut(Selection sel) {
			first = new Copy(sel);
			second = new Delete(sel);
		}

		@Override
		public String getName() {
			return Strings.get("cutSelectionAction");
		}

		@Override
		public void doIt(Project proj) {
			first.doIt(proj);
			second.doIt(proj);
		}

		@Override
		public void undo(Project proj) {
			second.undo(proj);
			first.undo(proj);
		}
	}

	private static class Copy extends Action {
		private Selection sel;
		private Clipboard oldClip;

		Copy(Selection sel) {
			this.sel = sel;
		}

		@Override
		public boolean isModification() { return false; }

		@Override
		public String getName() {
			return Strings.get("copySelectionAction");
		}

		@Override
		public void doIt(Project proj) {
			oldClip = Clipboard.get();
			Clipboard.set(sel,
					proj.getFrame().getAttributeTable().getAttributeSet());
		}

		@Override
		public void undo(Project proj) {
			Clipboard.set(oldClip);
		}
	}

	private static class Paste extends Action {
		private Selection sel;
		private CircuitTransaction xnReverse;
		private SelectionSave after;

		Paste(Selection sel) {
			this.sel = sel;
		}

		@Override
		public String getName() {
			return Strings.get("pasteClipboardAction");
		}

		@Override
		public void doIt(Project proj) {
			Clipboard clip = Clipboard.get();
			Circuit circuit = proj.getCurrentCircuit();
			CircuitMutation xn = new CircuitMutation(circuit);
			sel.pasteHelper(xn, clip.getComponents());
			CircuitTransactionResult result = xn.execute();
			xnReverse = result.getReverseTransaction();
			after = SelectionSave.create(sel);
		}

		@Override
		public void undo(Project proj) {
			xnReverse.execute();
		}
	}

	private static class Translate extends Action {
		private Selection sel;
		private int dx;
		private int dy;
		private ReplacementMap replacements;
		private SelectionSave before;
		// private SelectionSave after;
		private CircuitTransaction xnReverse;
		
		Translate(Selection sel, int dx, int dy, ReplacementMap replacements) {
			this.sel = sel;
			this.dx = dx;
			this.dy = dy;
			this.replacements = replacements;
			this.before = SelectionSave.create(sel);
		}

		@Override
		public String getName() {
			return Strings.get("moveSelectionAction");
		}

		@Override
		public void doIt(Project proj) {
			Circuit circuit = proj.getCurrentCircuit();
			CircuitMutation xn = new CircuitMutation(circuit);

			sel.translateHelper(xn, dx, dy);
			if (replacements != null) {
				xn.replace(replacements);
			}

			CircuitTransactionResult result = xn.execute();
			xnReverse = result.getReverseTransaction();
			// after = SelectionSave.create(sel);
		}

		@Override
		public void undo(Project proj) {
			xnReverse.execute();
		}

		@Override
		public boolean shouldAppendTo(Action other) {
			Action last;
			if (other instanceof JoinedAction) last = ((JoinedAction) other).getLastAction();
			else last = other;
			
			SelectionSave otherAfter = null;
			/* I think this makes sense philosophically, but it feels weird...
			if (last instanceof Translate) {
				otherAfter = ((Translate) last).after;
			}
			*/
			if (last instanceof Paste) {
				otherAfter = ((Paste) last).after;
			} else if (last instanceof Duplicate) {
				otherAfter = ((Duplicate) last).after;
			}
			return otherAfter != null && otherAfter.equals(this.before);
		}
	}
}

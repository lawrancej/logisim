/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.util.ArrayList;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import static com.cburch.logisim.util.LocaleString.*;


public class LogisimFileActions {
	private LogisimFileActions() { }

	public static Action addCircuit(Circuit circuit) {
		return new AddCircuit(circuit);
	}

	public static Action removeCircuit(Circuit circuit) {
		return new RemoveCircuit(circuit);
	}
	
	public static Action moveCircuit(AddTool tool, int toIndex) {
		return new MoveCircuit(tool, toIndex);
	}

	public static Action loadLibrary(Library lib) {
		return new LoadLibraries(new Library[] { lib });
	}

	public static Action loadLibraries(Library[] libs) {
		return new LoadLibraries(libs);
	}

	public static Action unloadLibrary(Library lib) {
		return new UnloadLibraries(new Library[] { lib });
	}

	public static Action unloadLibraries(Library[] libs) {
		return new UnloadLibraries(libs);
	}

	public static Action setMainCircuit(Circuit circuit) {
		return new SetMainCircuit(circuit);
	}

	public static Action revertDefaults() {
		return new RevertDefaults();
	}

	private static class AddCircuit extends Action {
		private Circuit circuit;

		AddCircuit(Circuit circuit) {
			this.circuit = circuit;
		}

		@Override
		public String getName() {
			return _("addCircuitAction");
		}

		@Override
		public void doIt(Project proj) {
			proj.getLogisimFile().addCircuit(circuit);
		}

		@Override
		public void undo(Project proj) {
			proj.getLogisimFile().removeCircuit(circuit);
		}
	}

	private static class RemoveCircuit extends Action {
		private Circuit circuit;
		private int index;

		RemoveCircuit(Circuit circuit) {
			this.circuit = circuit;
		}

		@Override
		public String getName() {
			return _("removeCircuitAction");
		}

		@Override
		public void doIt(Project proj) {
			index = proj.getLogisimFile().getCircuits().indexOf(circuit);
			proj.getLogisimFile().removeCircuit(circuit);
		}

		@Override
		public void undo(Project proj) {
			proj.getLogisimFile().addCircuit(circuit, index);
		}
	}

	private static class MoveCircuit extends Action {
		private AddTool tool;
		private int fromIndex;
		private int toIndex;

		MoveCircuit(AddTool tool, int toIndex) {
			this.tool = tool;
			this.toIndex = toIndex;
		}

		@Override
		public String getName() {
			return _("moveCircuitAction");
		}

		@Override
		public void doIt(Project proj) {
			fromIndex = proj.getLogisimFile().getTools().indexOf(tool);
			proj.getLogisimFile().moveCircuit(tool, toIndex);
		}

		@Override
		public void undo(Project proj) {
			proj.getLogisimFile().moveCircuit(tool, fromIndex);
		}
		
		@Override
		public boolean shouldAppendTo(Action other) {
			return other instanceof MoveCircuit
				&& ((MoveCircuit) other).tool == this.tool;
		}
		
		@Override
		public Action append(Action other) {
			MoveCircuit ret = new MoveCircuit(tool, ((MoveCircuit) other).toIndex);
			ret.fromIndex = this.fromIndex;
			return ret.fromIndex == ret.toIndex ? null : ret;
		}
	}

	private static class LoadLibraries extends Action {
		private Library[] libs;

		LoadLibraries(Library[] libs) {
			this.libs = libs;
		}

		@Override
		public String getName() {
			if (libs.length == 1) {
				return _("loadLibraryAction");
			} else {
				return _("loadLibrariesAction");
			}
		}

		@Override
		public void doIt(Project proj) {
			for (int i = 0; i < libs.length; i++) {
				proj.getLogisimFile().addLibrary(libs[i]);
			}
		}

		@Override
		public void undo(Project proj) {
			for (int i = libs.length - 1; i >= 0; i--) {
				proj.getLogisimFile().removeLibrary(libs[i]);
			}
		}
	}
	
	private static class UnloadLibraries extends Action {
		private Library[] libs;

		UnloadLibraries(Library[] libs) {
			this.libs = libs;
		}

		@Override
		public String getName() {
			if (libs.length == 1) {
				return _("unloadLibraryAction");
			} else {
				return _("unloadLibrariesAction");
			}
		}

		@Override
		public void doIt(Project proj) {
			for (int i = libs.length - 1; i >= 0; i--) {
				proj.getLogisimFile().removeLibrary(libs[i]);
			}
		}

		@Override
		public void undo(Project proj) {
			for (int i = 0; i < libs.length; i++) {
				proj.getLogisimFile().addLibrary(libs[i]);
			}
		}
	}

	private static class SetMainCircuit extends Action {
		private Circuit oldval;
		private Circuit newval;

		SetMainCircuit(Circuit circuit) {
			newval = circuit;
		}

		@Override
		public String getName() {
			return _("setMainCircuitAction");
		}

		@Override
		public void doIt(Project proj) {
			oldval = proj.getLogisimFile().getMainCircuit();
			proj.getLogisimFile().setMainCircuit(newval);
		}

		@Override
		public void undo(Project proj) {
			proj.getLogisimFile().setMainCircuit(oldval);
		}
	}
	
	private static class RevertAttributeValue {
		private AttributeSet attrs;
		private Attribute<Object> attr;
		private Object value;
		
		RevertAttributeValue(AttributeSet attrs, Attribute<Object> attr, Object value) {
			this.attrs = attrs;
			this.attr = attr;
			this.value = value;
		}
	}
	
	private static class RevertDefaults extends Action {
		private Options oldOpts;
		private ArrayList<Library> libraries = null;
		private ArrayList<RevertAttributeValue> attrValues;

		RevertDefaults() {
			libraries = null;
			attrValues = new ArrayList<RevertAttributeValue>();
		}

		@Override
		public String getName() {
			return _("revertDefaultsAction");
		}

		@Override
		public void doIt(Project proj) {
			LogisimFile src = ProjectActions.createNewFile(proj);
			LogisimFile dst = proj.getLogisimFile();
			
			copyToolAttributes(src, dst);
			for (Library srcLib : src.getLibraries()) {
				Library dstLib = dst.getLibrary(srcLib.getName());
				if (dstLib == null) {
					String desc = src.getLoader().getDescriptor(srcLib);
					dstLib = dst.getLoader().loadLibrary(desc);
					proj.getLogisimFile().addLibrary(dstLib);
					if (libraries == null) libraries = new ArrayList<Library>();
					libraries.add(dstLib);
				}
				copyToolAttributes(srcLib, dstLib);
			}
			
			Options newOpts = proj.getOptions();
			oldOpts = new Options();
			oldOpts.copyFrom(newOpts, dst);
			newOpts.copyFrom(src.getOptions(), dst);
		}
		
		private void copyToolAttributes(Library srcLib, Library dstLib) {
			for (Tool srcTool : srcLib.getTools()) {
				AttributeSet srcAttrs = srcTool.getAttributeSet();
				Tool dstTool = dstLib.getTool(srcTool.getName());
				if (srcAttrs != null && dstTool != null) {
					AttributeSet dstAttrs = dstTool.getAttributeSet();
					for (Attribute<?> attrBase : srcAttrs.getAttributes()) {
						@SuppressWarnings("unchecked")
						Attribute<Object> attr = (Attribute<Object>) attrBase;
						Object srcValue = srcAttrs.getValue(attr);
						Object dstValue = dstAttrs.getValue(attr);
						if (!dstValue.equals(srcValue)) {
							dstAttrs.setValue(attr, srcValue);
							attrValues.add(new RevertAttributeValue(dstAttrs, attr, dstValue));
						}
					}
				}
			}
		}

		@Override
		public void undo(Project proj) {
			proj.getOptions().copyFrom(oldOpts, proj.getLogisimFile());
			
			for (RevertAttributeValue attrValue : attrValues) {
				attrValue.attrs.setValue(attrValue.attr, attrValue.value);
			}

			if (libraries != null) {
				for (Library lib : libraries) {
					proj.getLogisimFile().removeLibrary(lib);
				}
			}
		}
	}
}

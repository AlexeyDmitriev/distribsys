package name.admitriev.keyvaluestorage;

import java.io.Serializable;

public class ViewInfo implements Serializable {
	public int view = 0;
	public String primary = "";
	public String backup = "";


	public ViewInfo () {

	}

	public ViewInfo(int view, String primary, String backup) {
		this.view = view;
		this.primary = primary;
		this.backup = backup;
	}

	public ViewInfo copy() {
		return new ViewInfo(view, primary, backup);
	}
}

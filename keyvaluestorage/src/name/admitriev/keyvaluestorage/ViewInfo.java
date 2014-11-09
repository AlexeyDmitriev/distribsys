package name.admitriev.keyvaluestorage;

public class ViewInfo
{
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

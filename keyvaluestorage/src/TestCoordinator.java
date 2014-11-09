import name.admitriev.keyvaluestorage.CoordinatorImplementation;
import name.admitriev.keyvaluestorage.ViewInfo;

class TestFailedException extends Exception
{
	TestFailedException(String message)
	{
		super(message);
	}
}

public class TestCoordinator
{
	static void test(ViewInfo info,
	                 String primary,
	                 String backup,
	                 int view,
	                 String description) throws TestFailedException
	{
		if (!primary.equals(info.primary)) {
			System.err.println("Wrong primary: expected " + primary +
			                   ", got " + info.primary);
			throw new TestFailedException(description);
		}
		if (!backup.equals(info.backup)) {
			System.err.println("Wrong backup: expected " + backup +
			                   ", got " + info.backup);
			throw new TestFailedException(description);
		}
		if (view != info.view ) {
			System.err.println("Wrong view number: expected " + view +
			                   ", got " + info.view);
			throw new TestFailedException(description);
		}
		System.err.println("Test passed: " + description);
	}

	public static void main (String[] argv) throws Exception
	{
		CoordinatorImplementation service = new CoordinatorImplementation();
		int longDelay = CoordinatorImplementation.DEAD_PINGS * 2;
		String srv1 = "localhost:10001";
		String srv2 = "localhost:10002";
		String srv3 = "localhost:10003";
		int currentView = 0;
		ViewInfo info = null;

		try {
			// no ready servers
			if (!service.primary().equals(""))
				throw new TestFailedException("no ready servers");
			System.err.println("Test passed: no ready servers");

			// first primary
			for (int i = 0; i < longDelay; ++i) {
				info = service.ping(0, srv1);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			++currentView;
			test(info, srv1, "", currentView, "first primary");

			// first backup
			for (int i = 0; i < longDelay; ++i) {
				service.ping(currentView, srv1);
				info = service.ping(0, srv2);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			++currentView;
			test(info, srv1, srv2, currentView, "first backup");

			// primary fails, backup should take over
			service.ping(2, srv1);
			for (int i = 0; i < longDelay; ++i) {
				info = service.ping(2, srv2);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			++currentView;
			test(info, srv2, "", currentView, "backup takes over");

			// first server restarts, should become backup
			for (int i = 0; i < longDelay; ++i) {
				service.ping(currentView, srv2);
				info = service.ping(0, srv1);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			++currentView;
			test(info, srv2, srv1, currentView, "restarted server becomes backup");

			// primary fails, third server appears,
			// backup should become primary, new server - backup
			service.ping(currentView, srv2);
			for (int i = 0; i < longDelay; ++i) {
				service.ping(currentView, srv1);
				info = service.ping(0, srv3);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			++currentView;
			test(info, srv1, srv3, currentView, "spare server becomes backup");

			// primary quickly restarts, should not be primary anymore
			service.ping(currentView, srv1);
			for (int i = 0; i < longDelay; ++i) {
				service.ping(0, srv1);
				info = service.ping(currentView, srv3);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			++currentView;
			test(info, srv3, srv1, currentView, "primary reboots");
			System.err.println("Test passed: primary reboots");

			// set up a view with just 3 as primary,
			// to prepare for the next test.
			for (int i = 0; i < longDelay; ++i) {
				info = service.ping(currentView, srv3);
				service.tick();
			}
			++currentView;
			test(info, srv3, "", currentView, "primary only");

			// backup appears but primary does not ack
			for (int i = 0; i < longDelay; ++i) {
				service.ping(0, srv1);
				info = service.ping(currentView, srv3);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			++currentView;
			test(info, srv3, srv1, currentView, "primary doesn't ack");

			// primary didn't ack and dies
			// check that backup is not promoted
			for (int i = 0; i < longDelay; ++i) {
				info = service.ping(currentView, srv1);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			test(info, srv3, srv1, currentView, "do not promote backup");

			// primary is OK
			for (int i = 0; i < longDelay; ++i) {
				info = service.ping(currentView, srv3);
				info = service.ping(currentView, srv1);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			test(info, srv3, srv1, currentView, "no changes, primary alive");

			// backup restarts
			for (int i = 0; i < longDelay; ++i) {
				info = service.ping(currentView, srv3);
				info = service.ping(0, srv1);
				if (info.view == currentView + 1) break;
				service.tick();
			}
			++currentView;
			test(info, srv3, srv1, currentView, "backup restarts");

		} catch (TestFailedException e) {
			System.err.println("Test failed: " + e.getMessage());
		}
	}
}

package name.admitriev;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;

public class NoterClient {
	public static void main(String[] args) throws IOException, NotBoundException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Noter noter = (Noter) Naming.lookup("noter");
		while (true) {
			String s = br.readLine();
			if(s == null)
				return;
			noter.add(s);

			System.out.println(noter.allNotes());

		}
	}
}

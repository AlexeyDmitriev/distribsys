package name.admitriev;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

//ls cp mv rm
public class Main {

	private static String token = "0145de72efd1498580bf9f582cfa14e1";

	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			System.err.println("wtf");
			return;
		}

		String resourceURL = null;

		String method = null;
		if(args[0].equals("cp")) {
			String from = URLEncoder.encode(args[1], "UTF-8");
			String to = URLEncoder.encode(args[2], "UTF-8");

			resourceURL = "https://cloud-api.yandex.net/v1/disk/resources/copy?from=" + from + "&path=" + to;

			System.out.println(request(resourceURL, "POST"));

		}
		else if(args[0].equals("mv")) {
			String from = URLEncoder.encode(args[1], "UTF-8");
			String to = URLEncoder.encode(args[2], "UTF-8");

			resourceURL = "https://cloud-api.yandex.net/v1/disk/resources/move?from=" + from + "&path=" + to;

			System.out.println(request(resourceURL, "POST"));
		}
		else if(args[0].equals("rm")) {
			String path = URLEncoder.encode(args[1], "UTF-8");

			resourceURL = "https://cloud-api.yandex.net/v1/disk/resources?path=" + path;

			System.out.println(request(resourceURL, "DELETE"));
		}
		else if(args[0].equals("ls")) {
			String path = URLEncoder.encode(args[1], "UTF-8");

			resourceURL = "https://cloud-api.yandex.net/v1/disk/resources?path=" + path + "&limit=100";

			String json = request(resourceURL, "GET");

			JSONObject obj = new JSONObject(json.toString());
			JSONArray children = obj.getJSONObject("_embedded").getJSONArray("items");

			for(int i = 0, length = children.length(); i < length; ++i) {
				System.out.println(children.getJSONObject(i).getString("name"));
			}

		}
		else
			throw new RuntimeException("trash");

	}

	public static String request(String resourceURL, String method) throws IOException {

		URL requestURL = new URL(resourceURL);


		HttpsURLConnection urlConnection = (HttpsURLConnection) requestURL.openConnection();
		urlConnection.setRequestMethod(method);
		urlConnection.setRequestProperty("Authorization", "OAuth " + token);

		int responseCode = urlConnection.getResponseCode();
		InputStream is;
		if (responseCode / 100 == 2) {
			is = urlConnection.getInputStream();
		}
		else
			throw new IOException("Problem. Error code:" + responseCode);

		if (is != null) {

			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder buffer = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line).append("\n");
			}
			return buffer.toString();
		}

	}
}

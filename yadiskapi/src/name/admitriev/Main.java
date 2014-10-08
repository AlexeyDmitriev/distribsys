package name.admitriev;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

	private static final String token = "";

	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			System.err.println("no args provided");
			return;
		}

		switch (args[0]) {
			case "cp": {
				String from = URLEncoder.encode(args[1], "UTF-8");
				String to = URLEncoder.encode(args[2], "UTF-8");

				String resourceURL = "https://cloud-api.yandex.net/v1/disk/resources/copy?from=" + from + "&path=" + to;

				System.out.println(request(resourceURL, "POST"));

				break;
			}
			case "mv": {
				String from = URLEncoder.encode(args[1], "UTF-8");
				String to = URLEncoder.encode(args[2], "UTF-8");

				String resourceURL = "https://cloud-api.yandex.net/v1/disk/resources/move?from=" + from + "&path=" + to;

				System.out.println(request(resourceURL, "POST"));
				break;
			}
			case "rm": {
				String path = URLEncoder.encode(args[1], "UTF-8");

				String resourceURL = "https://cloud-api.yandex.net/v1/disk/resources?path=" + path;

				System.out.println(request(resourceURL, "DELETE"));
				break;
			}
			case "ls": {
				String path = URLEncoder.encode(args[1], "UTF-8");

				String resourceURL = "https://cloud-api.yandex.net/v1/disk/resources?path=" + path + "&limit=100";

				String json = request(resourceURL, "GET");

				JSONObject obj = new JSONObject(json);
				JSONArray children = obj.getJSONObject("_embedded").getJSONArray("items");

				for (int i = 0, length = children.length(); i < length; ++i) {
					System.out.println(children.getJSONObject(i).getString("name"));
				}

				break;
			}
			case "upload": {
				String localPath = args[1];
				String remotePath = URLEncoder.encode(args[2], "UTF-8");
				String resourceURL = "https://cloud-api.yandex.net/v1/disk/resources/upload?path=" + remotePath;
				byte[] content = Files.readAllBytes(Paths.get(localPath));
				JSONObject reply = new JSONObject(request(resourceURL, "GET"));

				String uploadURL = reply.getString("href");
				System.err.println(uploadURL);

				System.out.println(request(uploadURL, "PUT", content));
				break;
			}
			case "download": {
				String remotePath = args[1];
				String localPath = args[2];
				String resourceURL = "https://cloud-api.yandex.net/v1/disk/resources/download?path=" + remotePath;

				JSONObject reply = new JSONObject(request(resourceURL, "GET"));

				String downloadURL = reply.getString("href");

				String fileContent = request(downloadURL, "GET");
				Files.write(Paths.get(localPath), fileContent.getBytes("UTF-8"));
				break;
			}
			default:
				throw new RuntimeException("wrong operation");
		}

	}

	public static String request(String resourceURL, String method) throws IOException {
		return request(resourceURL, method, null);
	}

	public static String request(String resourceURL, String method, byte[] content) throws IOException {

		URL requestURL = new URL(resourceURL);


		HttpsURLConnection urlConnection = (HttpsURLConnection) requestURL.openConnection();
		urlConnection.setRequestMethod(method);
		urlConnection.setRequestProperty("Authorization", "OAuth " + token);
		if(content != null) {
			urlConnection.setDoOutput(true);
			try (OutputStream outputStream = urlConnection.getOutputStream()) {
				outputStream.write(content);
			}
		}
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
		else
			return null;

	}
}

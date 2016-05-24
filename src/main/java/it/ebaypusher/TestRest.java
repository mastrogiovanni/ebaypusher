package it.ebaypusher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpHost;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import it.ebaypusher.utility.Configurazione;
import it.ebaypusher.utility.Utility;

public class TestRest {

	public static void main(String[] args) throws UnirestException, IOException {
				
		File file = new File("/home/michele/Documents/Progetti/EBayGrifoni/Ultimo/Esempi di file XML e CSV/20160513192006_ADD_catalogo_ballo_ebay.txt.csv");
		
		String token = Configurazione.getText("tokenScambioFileCsv");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Utility.copy(new FileInputStream(file), out);

		out.flush();
		
		StringBuilder builder = new StringBuilder();
		builder.append("--THIS_STRING_SEPARATES\r\n");
		builder.append("Content-Disposition: form-data; name=\"token\"\r\n");
		builder.append(token + "\r\n");
		builder.append("--THIS_STRING_SEPARATES\r\n");
		builder.append("Content-Disposition: form-data; name=\"file\"; filename=\"test.csv\"\r\n");
		builder.append("Content-Type: text/csv\r\n");
		builder.append(out);
		builder.append("\r\n");
		builder.append("--THIS_STRING_SEPARATES--\r\n");
		
		System.out.println(token);

		// Unirest.setProxy(new HttpHost("alpha01.tesoro.it", 8080));

		HttpResponse<InputStream> response = Unirest
			// .post("http://localhost:5555") // https://bulksell.ebay.com/ws/eBayISAPI.dll?FileExchangeUpload")
			.post("https://bulksell.ebay.com/ws/eBayISAPI.dll?FileExchangeUpload")
			.header("Content-Type", "multipart/form-data; boundary=THIS_STRING_SEPARATES")
			.body(builder.toString()).asBinary();
			
			// .field("file", file, "text/csv")
			// .field("token", token, "text/plain").asBinary();
		
		System.out.println(response.getStatus());
		
		Utility.copy(response.getBody(), System.out);

	}

}

package it.ebaypusher;

import java.io.File;

import com.mashape.unirest.http.Unirest;

public class TestRest {

	public static void main(String[] args) {
		
		Unirest
			.post("https://bulksell.ebay.com/ws/eBayISAPI.dll?FileExchangeUpload")
			.field("file", new File("test.csv"))
			.field("token", "token");

	}

}

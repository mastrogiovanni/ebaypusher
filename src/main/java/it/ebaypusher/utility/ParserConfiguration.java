package it.ebaypusher.utility;

public class ParserConfiguration {

	private static ParserConfiguration instance;

	private boolean parseAdd;
	private boolean parseDel;
	private boolean parseMod;
	private boolean parseRev;

	public static ParserConfiguration instance() {
		if (instance == null) {
			instance = new ParserConfiguration();
		}
		return instance;
	}

	private ParserConfiguration() {
		parseAdd = Configurazione.getText("DoParserXML_ADD") != null;
		parseDel = Configurazione.getText("DoParserXML_DEL") != null;
		parseMod = Configurazione.getText("DoParserXML_MOD") != null;
		parseRev = Configurazione.getText("DoParserXML_REV") != null;
	}

	public boolean isParseAdd() {
		return parseAdd;
	}

	public boolean isParseDel() {
		return parseDel;
	}

	public boolean isParseMod() {
		return parseMod;
	}

	public boolean isParseRev() {
		return parseRev;
	}

}

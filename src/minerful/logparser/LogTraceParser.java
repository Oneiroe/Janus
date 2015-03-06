package minerful.logparser;

public interface LogTraceParser {
	enum SenseOfReading {
		ONWARDS,
		BACKWARDS;
		
		public SenseOfReading switchSenseOfReading() {
			return (this.equals(ONWARDS) ? BACKWARDS : ONWARDS);
		}
	}

	SenseOfReading reverse();
	SenseOfReading getSenseOfReading();
	int length();
	LogParser getLogParser();
	boolean isParsing();
	Character parseSubsequentAndEncode();
	boolean isParsingOver();
	boolean stepToSubsequent();
	void init();
}
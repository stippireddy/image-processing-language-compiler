package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;

public class Scanner {

	/**
	 * Kind enum
	 */
	private static final HashMap<String, Kind> textToKeywordMap;

	static {
		textToKeywordMap = new HashMap<String, Kind>(24, 1) {

			{
				// keyword = integer | boolean | image | url | file | frame |
				// while | if | sleep | screenheight | screenwidth
				put("integer", Kind.KW_INTEGER);
				put("boolean", Kind.KW_BOOLEAN);
				put("image", Kind.KW_IMAGE);
				put("url", Kind.KW_URL);
				put("file", Kind.KW_FILE);
				put("frame", Kind.KW_FRAME);
				put("while", Kind.KW_WHILE);
				put("if", Kind.KW_IF);
				put("sleep", Kind.OP_SLEEP);
				put("screenheight", Kind.KW_SCREENHEIGHT);
				put("screenwidth", Kind.KW_SCREENWIDTH);

				// filter_op_keyword = gray | convolve | blur | scale
				put("gray", Kind.OP_GRAY);
				put("convolve", Kind.OP_CONVOLVE);
				put("blur", Kind.OP_BLUR);
				put("scale", Kind.KW_SCALE);

				// image_op_keyword = width | height
				put("width", Kind.OP_WIDTH);
				put("height", Kind.OP_HEIGHT);

				// frame_op_keyword = xloc | yloc | hide | show | move
				put("xloc", Kind.KW_XLOC);
				put("yloc", Kind.KW_YLOC);
				put("hide", Kind.KW_HIDE);
				put("show", Kind.KW_SHOW);
				put("move", Kind.KW_MOVE);

				// boolean_literal = true | false
				put("true", Kind.KW_TRUE);
				put("false", Kind.KW_FALSE);
			}
		};

	}

	public static enum Kind {
		IDENT(""),
		INT_LIT(""),
		KW_INTEGER("integer"),
		KW_BOOLEAN("boolean"),
		KW_IMAGE("image"),
		KW_URL("url"),
		KW_FILE("file"),
		KW_FRAME("frame"),
		KW_WHILE("while"),
		KW_IF("if"),
		KW_TRUE("true"),
		KW_FALSE("false"),
		SEMI(";"),
		COMMA(","),
		LPAREN("("),
		RPAREN(")"),
		LBRACE("{"),
		RBRACE("}"),
		ARROW("->"),
		BARARROW("|->"),
		OR("|"),
		AND("&"),
		EQUAL("=="),
		NOTEQUAL("!="),
		LT("<"),
		GT(">"),
		LE("<="),
		GE(">="),
		PLUS("+"),
		MINUS("-"),
		TIMES("*"),
		DIV("/"),
		MOD("%"),
		NOT("!"),
		ASSIGN("<-"),
		OP_BLUR("blur"),
		OP_GRAY("gray"),
		OP_CONVOLVE("convolve"),
		KW_SCREENHEIGHT("screenheight"),
		KW_SCREENWIDTH("screenwidth"),
		OP_WIDTH("width"),
		OP_HEIGHT("height"),
		KW_XLOC("xloc"),
		KW_YLOC("yloc"),
		KW_HIDE("hide"),
		KW_SHOW("show"),
		KW_MOVE("move"),
		OP_SLEEP("sleep"),
		KW_SCALE("scale"),
		EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}

	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {

		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {

		public IllegalNumberException(String message) {
			super(message);
		}
	}

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {

		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!LinePos.class.isAssignableFrom(obj.getClass())) {
				return false;
			}
			LinePos other = (LinePos) obj;
			return this.line == other.line && this.posInLine == other.posInLine;
		}
	}

	public class Token {

		public final Kind kind;
		public final int pos; // position in input array
		public final int length;

		// returns the text of this Token
		public String getText() {
			if (kind.equals(Kind.EOF)) {
				return Kind.EOF.getText();
			}
			return chars.substring(pos, pos + length);
		}

		// returns a LinePos object representing the line and column of this Token
		LinePos getLinePos() {
			int lineNumber = calculateLineNumber(pos, 0, endingLinePositionArray.length - 1);
			int positionInLine = pos;
			if (!(lineNumber == 0)) {
				positionInLine = pos - endingLinePositionArray[lineNumber - 1] - 1;
			}
			return new LinePos(lineNumber, positionInLine);
		}

		private int calculateLineNumber(int position, int i, int j) {
			if (i >= j) {
				return i;
			}
			int mid = (i + j) / 2;
			if (position > endingLinePositionArray[mid]) {
				return calculateLineNumber(position, mid + 1, j);
			} else if (position < endingLinePositionArray[mid]) {
				return calculateLineNumber(position, i, mid);
			} else {
				return mid;
			}
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/**
		 * Precondition: kind = Kind.INT_LIT, the text can be represented with a Java int. Note that the validity of the
		 * input should have been checked when the Token was created. So the exception should never be thrown.
		 * 
		 * @return int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException {
			return Integer.parseInt(chars.substring(pos, pos + length));
		}

		public boolean isKind(Kind inputKind) {
			return this.kind == inputKind;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Token)) {
				return false;
			}
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (kind != other.kind) {
				return false;
			}
			if (length != other.length) {
				return false;
			}
			if (pos != other.pos) {
				return false;
			}
			return true;
		}

		private Scanner getOuterType() {
			return Scanner.this;
		}
	}

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();

	}

	Integer[] endingLinePositionArray;

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0;
		int startPos = 0;
		int length = chars.length();
		ArrayList<Integer> lineIndices = new ArrayList<>();
		State state = State.START;
		int ch;
		while (pos <= length) {
			ch = pos < length ? chars.charAt(pos) : -1;
			switch (state) {
			case START:
				pos = skipWhiteSpace(pos, lineIndices);
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {

				case -1:
					tokens.add(new Token(Kind.EOF, pos, 0));
					pos++;
					break;

				case '0':
					tokens.add(new Token(Kind.INT_LIT, pos, 1));
					pos++;
					startPos = pos;
					break;

				case ';':
					tokens.add(new Token(Kind.SEMI, pos, 1));
					pos++;
					startPos = pos;
					break;

				case ',':
					tokens.add(new Token(Kind.COMMA, pos, 1));
					pos++;
					startPos = pos;
					break;

				case '(':
					tokens.add(new Token(Kind.LPAREN, pos, 1));
					pos++;
					startPos = pos;
					break;

				case ')':
					tokens.add(new Token(Kind.RPAREN, pos, 1));
					pos++;
					startPos = pos;
					break;

				case '{':
					tokens.add(new Token(Kind.LBRACE, pos, 1));
					pos++;
					startPos = pos;
					break;

				case '}':
					tokens.add(new Token(Kind.RBRACE, pos, 1));
					pos++;
					startPos = pos;
					break;

				case '&':
					tokens.add(new Token(Kind.AND, pos, 1));
					pos++;
					startPos = pos;
					break;

				case '+':
					tokens.add(new Token(Kind.PLUS, pos, 1));
					pos++;
					startPos = pos;
					break;

				case '%':
					tokens.add(new Token(Kind.MOD, pos, 1));
					pos++;
					startPos = pos;
					break;

				case '|':
					state = State.IN_OR;
					startPos = pos;
					pos++;
					break;

				case '-':
					state = State.IN_MINUS;
					startPos = pos;
					pos++;
					break;

				case '>':
					state = State.IN_GT;
					startPos = pos;
					pos++;
					break;

				case '<':
					state = State.IN_LT;
					startPos = pos;
					pos++;
					break;

				case '=':
					state = State.IN_EQUAL;
					startPos = pos;
					pos++;
					break;

				case '!':
					state = State.IN_NOT;
					startPos = pos;
					pos++;
					break;

				case '*':
					tokens.add(new Token(Kind.TIMES, pos, 1));
					pos++;
					startPos = pos;
					break;

				case '/':
					state = State.IN_DIV;
					startPos = pos;
					pos++;
					break;

				default:
					if (Character.isDigit(ch)) {
						state = State.IN_INT;
						startPos = pos;
						pos++;
					} else if (Character.isJavaIdentifierStart(ch)) {
						state = State.IN_IDENT;
						startPos = pos;
						pos++;
					} else {
						throw new IllegalCharException("The character " + ch + " is not recognized by this language");
					}
					break;
				}
				break;

			case IN_INT:
				if (!Character.isDigit(ch)) {
					try {
						Integer.parseInt(chars.substring(startPos, pos));
						tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
						startPos = pos;
						state = State.START;
					} catch (NumberFormatException e) {
						throw new IllegalNumberException("The value of integer literal "
								+ chars.substring(startPos, pos) + " lies beyond the range of this language");
					}
				} else {
					pos++;
				}
				break;

			case IN_IDENT:
				if (!Character.isJavaIdentifierPart(ch)) {
					String identifier = chars.substring(startPos, pos);
					Kind identKind = Kind.IDENT;
					if (textToKeywordMap.containsKey(identifier)) {
						identKind = textToKeywordMap.get(identifier);
					}
					tokens.add(new Token(identKind, startPos, pos - startPos));
					startPos = pos;
					state = State.START;
				} else {
					pos++;
				}
				break;

			case IN_OR:
				if (ch == '-') {
					state = State.IN_OR_MINUS;
					pos++;
				} else {
					tokens.add(new Token(Kind.OR, startPos, pos - startPos));
					startPos = pos;
					state = State.START;
				}
				break;

			case IN_OR_MINUS:
				if (ch == '>') {
					pos++;
					tokens.add(new Token(Kind.BARARROW, startPos, pos - startPos));
				} else {
					tokens.add(new Token(Kind.OR, startPos, 1));
					startPos++;
					tokens.add(new Token(Kind.MINUS, startPos, 1));
				}
				startPos = pos;
				state = State.START;
				break;

			case IN_MINUS:
				if (ch == '>') {
					pos++;
					tokens.add(new Token(Kind.ARROW, startPos, pos - startPos));
				} else {
					tokens.add(new Token(Kind.MINUS, startPos, pos - startPos));
				}
				startPos = pos;
				state = State.START;
				break;

			case IN_GT:
				if (ch == '=') {
					pos++;
					tokens.add(new Token(Kind.GE, startPos, pos - startPos));
				} else {
					tokens.add(new Token(Kind.GT, startPos, pos - startPos));
				}
				startPos = pos;
				state = State.START;
				break;

			case IN_LT:
				if (ch == '=') {
					pos++;
					tokens.add(new Token(Kind.LE, startPos, pos - startPos));
				} else if (ch == '-') {
					pos++;
					tokens.add(new Token(Kind.ASSIGN, startPos, pos - startPos));
				} else {
					tokens.add(new Token(Kind.LT, startPos, pos - startPos));
				}
				startPos = pos;
				state = State.START;
				break;

			case IN_EQUAL:
				if (ch == '=') {
					pos++;
					tokens.add(new Token(Kind.EQUAL, startPos, pos - startPos));
					startPos = pos;
					state = State.START;
				} else {
					throw new IllegalCharException(
							"The literal " + chars.substring(startPos, pos) + " is not recognized by this language");
				}

				break;

			case IN_NOT:
				if (ch == '=') {
					pos++;
					tokens.add(new Token(Kind.NOTEQUAL, startPos, pos - startPos));
				} else {
					tokens.add(new Token(Kind.NOT, startPos, pos - startPos));
				}
				startPos = pos;
				state = State.START;
				break;

			case IN_DIV:
				if (ch == '*') {
					state = State.IN_COMMENT_START;
					pos++;
				} else {
					tokens.add(new Token(Kind.DIV, startPos, pos - startPos));
					startPos = pos;
					state = State.START;
				}
				break;

			case IN_COMMENT_START:
				if (ch == '\n') {
					lineIndices.add(pos);
				} else if (ch == '*') {
					state = State.IN_COMMENT_END;
				} else if (ch == -1) {
					tokens.add(new Token(Kind.EOF, pos, 0));
				}
				pos++;
				break;

			case IN_COMMENT_END:
				pos++;
				if (ch == '/') {
					startPos = pos;
					state = State.START;
				} else if (ch != '*') {
					state = State.IN_COMMENT_START;
				}
				break;

			default:
				break;
			}
		}
		lineIndices.add(--pos);
		endingLinePositionArray = lineIndices.toArray(new Integer[lineIndices.size()]);
		return this;
	}

	private int skipWhiteSpace(int pos, ArrayList<Integer> lineIndices) {
		if (pos == -1) {
			return pos;
		}
		while (pos < chars.length()) {
			char c = chars.charAt(pos);
			if (Character.isWhitespace(c)) {
				if (c == '\n') {
					lineIndices.add(pos);
				}
				pos++;
			} else {
				break;
			}
		}
		return pos;
	}

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	static enum State {
		START("start"),

		IN_INT("inInt"),

		IN_IDENT("inIdent"),

		IN_OR("inOr"),

		IN_OR_MINUS("inOrMinus"),

		IN_MINUS("inMinus"),

		IN_GT("inGT"),

		IN_LT("inLT"),

		IN_EQUAL("inEqual"),

		IN_NOT("inNot"),

		IN_DIV("inDiv"),

		IN_COMMENT_START("inCommentStart"),

		IN_COMMENT_END("inCommentEnd");

		State(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}

	/*
	 * Return the next token in the token list and update the state so that the next call will return the Token..
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state. (So the following call to next will return
	 * the same token.)
	 */
	public Token peek() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the given token.
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		return t.getLinePos();
	}

}

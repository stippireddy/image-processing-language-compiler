package cop5556sp17;

import static cop5556sp17.Scanner.Kind.AND;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.ASSIGN;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.COMMA;
import static cop5556sp17.Scanner.Kind.DIV;
import static cop5556sp17.Scanner.Kind.EOF;
import static cop5556sp17.Scanner.Kind.EQUAL;
import static cop5556sp17.Scanner.Kind.GE;
import static cop5556sp17.Scanner.Kind.GT;
import static cop5556sp17.Scanner.Kind.IDENT;
import static cop5556sp17.Scanner.Kind.KW_BOOLEAN;
import static cop5556sp17.Scanner.Kind.KW_FILE;
import static cop5556sp17.Scanner.Kind.KW_FRAME;
import static cop5556sp17.Scanner.Kind.KW_IMAGE;
import static cop5556sp17.Scanner.Kind.KW_INTEGER;
import static cop5556sp17.Scanner.Kind.KW_URL;
import static cop5556sp17.Scanner.Kind.LBRACE;
import static cop5556sp17.Scanner.Kind.LE;
import static cop5556sp17.Scanner.Kind.LPAREN;
import static cop5556sp17.Scanner.Kind.LT;
import static cop5556sp17.Scanner.Kind.MINUS;
import static cop5556sp17.Scanner.Kind.MOD;
import static cop5556sp17.Scanner.Kind.NOTEQUAL;
import static cop5556sp17.Scanner.Kind.OR;
import static cop5556sp17.Scanner.Kind.PLUS;
import static cop5556sp17.Scanner.Kind.RBRACE;
import static cop5556sp17.Scanner.Kind.RPAREN;
import static cop5556sp17.Scanner.Kind.SEMI;
import static cop5556sp17.Scanner.Kind.TIMES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;

public class Parser {

  /**
   * Exception to be thrown if a syntax error is detected in the input. You will want to provide a
   * useful error message.
   *
   */
  @SuppressWarnings("serial")
  public static class SyntaxException extends Exception {

    public SyntaxException(String message) {
      super(message);
    }
  }

  Scanner scanner;
  Token t;

  Parser(Scanner scanner) {
    this.scanner = scanner;
    t = scanner.nextToken();
  }

  /**
   * parse the input using tokens from the scanner. Check for EOF (i.e. no trailing junk) when
   * finished
   * 
   * @throws SyntaxException
   */
  ASTNode parse() throws SyntaxException {
    ASTNode ast = program();
    matchEOF();
    return ast;
  }

  Expression expression() throws SyntaxException {
    Token firstToken = t;
    Expression e0 = null;
    Expression e1 = null;
    e0 = term();
    while (t.isKind(LT) || t.isKind(LE) || t.isKind(GT) || t.isKind(GE) || t.isKind(EQUAL)
        || t.isKind(NOTEQUAL)) {
      Token op = t;
      consume();
      e1 = term();
      e0 = new BinaryExpression(firstToken, e0, op, e1);
    }
    return e0;
  }

  Expression term() throws SyntaxException {
    Token firstToken = t;
    Expression e0 = null;
    Expression e1 = null;
    e0 = elem();
    while (t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR)) {
      Token op = t;
      consume();
      e1 = elem();
      e0 = new BinaryExpression(firstToken, e0, op, e1);
    }
    return e0;
  }

  Expression elem() throws SyntaxException {
    Token firstToken = t;
    Expression e0 = null;
    Expression e1 = null;
    e0 = factor();
    while (t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)) {
      Token op = t;
      consume();
      e1 = factor();
      e0 = new BinaryExpression(firstToken, e0, op, e1);
    }
    return e0;
  }

  Expression factor() throws SyntaxException {
    Token firstToken = t;
    Expression expression = null;
    Kind kind = t.kind;
    switch (kind) {
      case IDENT: {
        expression = new IdentExpression(firstToken);
        consume();
      }
        break;
      case INT_LIT: {
        expression = new IntLitExpression(firstToken);
        consume();
      }
        break;
      case KW_TRUE:
      case KW_FALSE: {
        expression = new BooleanLitExpression(firstToken);
        consume();
      }
        break;
      case KW_SCREENWIDTH:
      case KW_SCREENHEIGHT: {
        expression = new ConstantExpression(firstToken);
        consume();
      }
        break;
      case LPAREN: {
        consume();
        expression = expression();
        match(RPAREN);
      }
        break;
      default:
        throw new SyntaxException("This is not a valid start for a factor");
    }
    return expression;
  }

  Block block() throws SyntaxException {
    Token firstToken = t;
    ArrayList<Dec> declarations = new ArrayList<>();
    ArrayList<Statement> statements = new ArrayList<>();
    match(LBRACE);
    loop: while (true) {
      Kind kind = t.kind;
      switch (kind) {
        case KW_INTEGER:
        case KW_BOOLEAN:
        case KW_IMAGE:
        case KW_FRAME: {
          declarations.add(dec());
        }
          break;
        case OP_SLEEP:
        case KW_WHILE:
        case KW_IF:
        case IDENT:
        case OP_BLUR:
        case OP_GRAY:
        case OP_CONVOLVE:
        case KW_SHOW:
        case KW_HIDE:
        case KW_MOVE:
        case KW_XLOC:
        case KW_YLOC:
        case OP_WIDTH:
        case OP_HEIGHT:
        case KW_SCALE: {
          statements.add(statement());
        }
          break;
        default:
          break loop;
      }
    }
    match(RBRACE);
    Block block = new Block(firstToken, declarations, statements);
    return block;
  }

  Program program() throws SyntaxException {
    Token firstToken = t;
    ArrayList<ParamDec> paramList = new ArrayList<>();
    match(IDENT);
    Kind kind = t.kind;
    if (kind == KW_URL || kind == KW_FILE || kind == KW_INTEGER || kind == KW_BOOLEAN) {
      paramList.add(paramDec());
      while (t.isKind(COMMA)) {
        consume();
        paramList.add(paramDec());
      }
    }
    Block b = block();
    Program program = new Program(firstToken, paramList, b);
    return program;
  }

  ParamDec paramDec() throws SyntaxException {
    Token firstToken = t;
    match(KW_URL, KW_FILE, KW_INTEGER, KW_BOOLEAN);
    Token ident = t;
    match(IDENT);
    ParamDec paramDec = new ParamDec(firstToken, ident);
    return paramDec;
  }

  Dec dec() throws SyntaxException {
    Token firstToken = t;
    match(KW_INTEGER, KW_BOOLEAN, KW_IMAGE, KW_FRAME);
    Token ident = t;
    match(IDENT);
    Dec dec = new Dec(firstToken, ident);
    return dec;
  }

  Statement statement() throws SyntaxException {
    Token firstToken = t;
    Statement s0 = null;
    Kind kind = t.kind;
    switch (kind) {
      case OP_SLEEP: {
        consume();
        Expression e0 = expression();
        match(SEMI);
        s0 = new SleepStatement(firstToken, e0);
      }
        break;
      case KW_WHILE: {
        consume();
        match(LPAREN);
        Expression e0 = expression();
        match(RPAREN);
        Block b = block();
        s0 = new WhileStatement(firstToken, e0, b);
      }
        break;
      case KW_IF: {
        consume();
        match(LPAREN);
        Expression e0 = expression();
        match(RPAREN);
        Block b = block();
        s0 = new IfStatement(firstToken, e0, b);
      }
        break;
      case IDENT: {
        if (scanner.peek().kind == ASSIGN) {
          IdentLValue identifier = new IdentLValue(firstToken);
          consume();
          match(ASSIGN);
          Expression expression = expression();
          s0 = new AssignmentStatement(firstToken, identifier, expression);
        } else {
          s0 = chain();
        }
        match(SEMI);
      }
        break;
      case OP_BLUR:
      case OP_GRAY:
      case OP_CONVOLVE:
      case KW_SHOW:
      case KW_HIDE:
      case KW_MOVE:
      case KW_XLOC:
      case KW_YLOC:
      case OP_WIDTH:
      case OP_HEIGHT:
      case KW_SCALE: {
        s0 = chain();
        match(SEMI);
      }
        break;
      default:
        throw new SyntaxException("This is not a valid start for a Statement");
    }
    return s0;
  }

  Chain chain() throws SyntaxException {
    Token firstToken = t;
    ChainElem chainElement1 = null;
    ChainElem chainElement2 = null;
    chainElement1 = chainElem();
    Token arrowOp = t;
    match(ARROW, BARARROW);
    chainElement2 = chainElem();
    BinaryChain binaryChain = new BinaryChain(firstToken, chainElement1, arrowOp, chainElement2);
    while (t.isKind(ARROW) || t.isKind(BARARROW)) {
      arrowOp = t;
      consume();
      chainElement2 = chainElem();
      binaryChain = new BinaryChain(firstToken, binaryChain, arrowOp, chainElement2);
    }
    return binaryChain;
  }

  ChainElem chainElem() throws SyntaxException {
    Token firstToken = t;
    ChainElem chainElement = null;
    Kind kind = t.kind;
    switch (kind) {
      case IDENT: {
        chainElement = new IdentChain(firstToken);
        consume();
      }
        break;
      case OP_BLUR:
      case OP_GRAY:
      case OP_CONVOLVE: {
        consume();
        Tuple tuple = arg();
        chainElement = new FilterOpChain(firstToken, tuple);
      }
        break;
      case KW_SHOW:
      case KW_HIDE:
      case KW_MOVE:
      case KW_XLOC:
      case KW_YLOC: {
        consume();
        Tuple tuple = arg();
        chainElement = new FrameOpChain(firstToken, tuple);
      }
        break;

      case OP_WIDTH:
      case OP_HEIGHT:
      case KW_SCALE: {
        consume();
        Tuple tuple = arg();
        chainElement = new ImageOpChain(firstToken, tuple);
      }
        break;
      default:
        throw new SyntaxException("illegal chain element");
    }

    return chainElement;
  }

  Tuple arg() throws SyntaxException {
    Token firstToken = t;
    List<Expression> argList = new ArrayList<>();
    Tuple tuple = new Tuple(firstToken, argList);
    if (t.isKind(LPAREN)) {
      consume();
      argList.add(expression());
      while (t.isKind(COMMA)) {
        consume();
        argList.add(expression());
      }
      match(RPAREN);
    }
    return tuple;
  }

  /**
   * Checks whether the current token is the EOF token. If not, a SyntaxException is thrown.
   * 
   * @return
   * @throws SyntaxException
   */
  private Token matchEOF() throws SyntaxException {
    if (t.isKind(EOF)) {
      return t;
    }
    throw new SyntaxException("expected EOF");
  }

  /**
   * Checks if the current token has the given kind. If so, the current token is consumed and
   * returned. If not, a SyntaxException is thrown.
   * 
   * Precondition: kind != EOF
   * 
   * @param kind
   * @return
   * @throws SyntaxException
   */
  private Token match(Kind kind) throws SyntaxException {
    if (t.isKind(kind)) {
      return consume();
    }
    throw new SyntaxException("saw " + t.kind + " expected " + kind);
  }

  /**
   * Checks if the current token has one of the given kinds. If so, the current token is consumed
   * and returned. If not, a SyntaxException is thrown.
   * 
   * * Precondition: for all given kinds, kind != EOF
   * 
   * @param kinds list of kinds, matches any one
   * @return
   * @throws SyntaxException
   */
  private Token match(Kind... kinds) throws SyntaxException {
    for (Kind kind : kinds) {
      if (t.isKind(kind)) {
        return consume();
      }
    }
    throw new SyntaxException("The expected token kind " + t.kind + " is not in the expected kinds"
        + Arrays.toString(kinds));
  }

  /**
   * Gets the next token and returns the consumed token.
   * 
   * Precondition: t.kind != EOF
   * 
   * @return
   * 
   */
  private Token consume() throws SyntaxException {
    Token tmp = t;
    t = scanner.nextToken();
    return tmp;
  }

}

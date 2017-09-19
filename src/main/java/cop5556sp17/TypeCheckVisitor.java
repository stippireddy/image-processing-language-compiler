package cop5556sp17;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
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
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		TypeName typeOfChain0 = (TypeName) binaryChain.getE0().visit(this, arg);
		ChainElem chain1 = binaryChain.getE1();
		TypeName typeOfChain1 = (TypeName) chain1.visit(this, arg);
		Kind opKind = binaryChain.getArrow().kind;
		if (typeOfChain0 == TypeName.URL && opKind == Kind.ARROW && typeOfChain1 == TypeName.IMAGE) {
			binaryChain.setType(TypeName.IMAGE);
		} else if (typeOfChain0 == TypeName.FILE && opKind == Kind.ARROW && typeOfChain1 == TypeName.IMAGE) {
			binaryChain.setType(TypeName.IMAGE);
		} else if (typeOfChain0 == TypeName.FRAME && opKind == Kind.ARROW && (chain1 instanceof FrameOpChain
				&& (chain1.getFirstToken().isKind(Kind.KW_XLOC) || chain1.getFirstToken().isKind(Kind.KW_YLOC)))) {
			binaryChain.setType(TypeName.INTEGER);
		} else if (typeOfChain0 == TypeName.FRAME && opKind == Kind.ARROW
				&& (chain1 instanceof FrameOpChain
						&& (chain1.getFirstToken().isKind(Kind.KW_SHOW) || chain1.getFirstToken().isKind(Kind.KW_HIDE)
								|| chain1.getFirstToken().isKind(Kind.KW_MOVE)))) {
			binaryChain.setType(TypeName.FRAME);
		} else if (typeOfChain0 == TypeName.IMAGE && opKind == Kind.ARROW && (chain1 instanceof ImageOpChain
				&& (chain1.getFirstToken().isKind(Kind.OP_WIDTH) || chain1.getFirstToken().isKind(Kind.OP_HEIGHT)))) {
			binaryChain.setType(TypeName.INTEGER);
		} else if (typeOfChain0 == TypeName.IMAGE && opKind == Kind.ARROW && typeOfChain1 == TypeName.FRAME) {
			binaryChain.setType(TypeName.FRAME);
		} else if (typeOfChain0 == TypeName.IMAGE && opKind == Kind.ARROW && typeOfChain1 == TypeName.FILE) {
			binaryChain.setType(TypeName.NONE);
		} else if (typeOfChain0 == TypeName.IMAGE && (opKind == Kind.ARROW || opKind == Kind.BARARROW)
				&& (chain1 instanceof FilterOpChain
						&& (chain1.getFirstToken().isKind(Kind.OP_GRAY) || chain1.getFirstToken().isKind(Kind.OP_BLUR)
								|| chain1.getFirstToken().isKind(Kind.OP_CONVOLVE)))) {
			binaryChain.setType(TypeName.IMAGE);
		} else if (typeOfChain0 == TypeName.IMAGE && opKind == Kind.ARROW
				&& (chain1 instanceof ImageOpChain && chain1.getFirstToken().isKind(Kind.KW_SCALE))) {
			binaryChain.setType(TypeName.IMAGE);
		} else if (typeOfChain0 == TypeName.IMAGE && opKind == Kind.ARROW
				&& (chain1 instanceof IdentChain && chain1.getType() == TypeName.IMAGE)) {
			binaryChain.setType(TypeName.IMAGE);
		} else if (typeOfChain0 == TypeName.INTEGER && opKind == Kind.ARROW
				&& (chain1 instanceof IdentChain && chain1.getType() == TypeName.INTEGER)) {
			binaryChain.setType(TypeName.INTEGER);
		} else {
			throw new TypeCheckException("Incompatible chain types encountered for the given binary chain.");
		}
		return binaryChain.getType();
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		TypeName t0 = (TypeName) binaryExpression.getE0().visit(this, arg);
		TypeName t1 = (TypeName) binaryExpression.getE1().visit(this, arg);
		Kind opKind = binaryExpression.getOp().kind;
		switch (opKind) {
		case PLUS:
		case MINUS:
			if (t0 == TypeName.INTEGER && t1 == TypeName.INTEGER) {
				binaryExpression.setType(TypeName.INTEGER);
			} else if (t0 == TypeName.IMAGE && t1 == TypeName.IMAGE) {
				binaryExpression.setType(TypeName.IMAGE);
			} else {
				throw new TypeCheckException("Incompatible expression types encountered for the given operation.");
			}
			break;
		case TIMES:
			if (t0 == TypeName.INTEGER && t1 == TypeName.INTEGER) {
				binaryExpression.setType(TypeName.INTEGER);
			} else if ((t0 == TypeName.INTEGER && t1 == TypeName.IMAGE)
					|| (t0 == TypeName.IMAGE && t1 == TypeName.INTEGER)) {
				binaryExpression.setType(TypeName.IMAGE);
			} else {
				throw new TypeCheckException("Incompatible expression types encountered for the given operation.");
			}
			break;
		case DIV:
		case MOD:
			if (t0 == TypeName.INTEGER && t1 == TypeName.INTEGER) {
				binaryExpression.setType(TypeName.INTEGER);
			} else if (t0 == TypeName.IMAGE && t1 == TypeName.INTEGER) {
				binaryExpression.setType(TypeName.IMAGE);
			} else {
				throw new TypeCheckException("Incompatible expression types encountered for the given operation.");
			}
			break;
		case LT:
		case GT:
		case LE:
		case GE:
			if ((t0 == TypeName.INTEGER && t1 == TypeName.INTEGER)
					|| (t0 == TypeName.BOOLEAN && t1 == TypeName.BOOLEAN)) {
				binaryExpression.setType(TypeName.BOOLEAN);
			} else {
				throw new TypeCheckException("Incompatible expression types encountered for the given operation.");
			}
			break;
		case EQUAL:
		case NOTEQUAL:
			if (t0 == t1) {
				binaryExpression.setType(TypeName.BOOLEAN);
			} else {
				throw new TypeCheckException("Incompatible expression types encountered for the given operation.");
			}
			break;
		case AND:
		case OR:
			if (t0 == TypeName.BOOLEAN && t1 == TypeName.BOOLEAN) {
				binaryExpression.setType(TypeName.BOOLEAN);
			} else {
				throw new TypeCheckException("Incompatible expression types encountered for the given operation.");
			}
			break;
		default:
			throw new TypeCheckException("Incompatible operator expression type encountered.");
		}

		return binaryExpression.getType();
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtab.enterScope();
		for (Dec d : block.getDecs()) {
			visitDec(d, arg);
		}
		for (Statement s : block.getStatements()) {
			s.visit(this, arg);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.setType(TypeName.BOOLEAN);
		return booleanLitExpression.getType();
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		filterOpChain.getArg().visit(this, null);
		if (filterOpChain.getArg().getExprList().size() != 0) {
			throw new TypeCheckException("The length of the expression list is more than expected.");
		}
		filterOpChain.setType(TypeName.IMAGE);
		return filterOpChain.getType();
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Kind opChainKind = frameOpChain.getFirstToken().kind;
		frameOpChain.getArg().visit(this, null);
		int tupleSize = frameOpChain.getArg().getExprList().size();
		if (opChainKind == Kind.KW_SHOW || opChainKind == Kind.KW_HIDE) {
			if (tupleSize != 0) {
				throw new TypeCheckException("The length of the expression list is more than expected.");
			}
			frameOpChain.setType(TypeName.NONE);
		} else if (opChainKind == Kind.KW_XLOC
				|| opChainKind == Kind.KW_YLOC) {
			if (tupleSize != 0) {
				throw new TypeCheckException("The length of the expression list is more than expected.");
			}
			frameOpChain.setType(TypeName.INTEGER);
		} else if (opChainKind == Kind.KW_MOVE) {
			if (tupleSize != 2) {
				throw new TypeCheckException("The length of the expression list is more than expected.");
			}
			frameOpChain.setType(TypeName.NONE);
		} else {
			throw new TypeCheckException("!!!!!!!There is a bug in your parser!!!!!!!");
		}
		return frameOpChain.getType();
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Dec dec = symtab.lookup(identChain.getFirstToken().getText());
		if (dec == null) {
			throw new TypeCheckException("The identifier attempted to be found does not exist in the current scope.");
		} else {
			identChain.setDec(dec);
			identChain.setType(Type.getTypeName(dec.getType()));
		}
		return identChain.getType();
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec dec = symtab.lookup(identExpression.getFirstToken().getText());
		if (dec == null) {
			throw new TypeCheckException("The identifier attempted to be found does not exist in the current scope.");
		} else {
			identExpression.setDec(dec);
			identExpression.setType(Type.getTypeName(dec.getType()));
		}
		return identExpression.getType();
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.getE().visit(this, arg);
		if (!(ifStatement.getE().getType() == TypeName.BOOLEAN)) {
			throw new TypeCheckException("Invalid type encountered for expression.");
		}
		ifStatement.getB().visit(this, arg);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.setType(TypeName.INTEGER);
		return intLitExpression.getType();
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, null);
		if (sleepStatement.getE().getType() != TypeName.INTEGER) {
			throw new TypeCheckException("Inconsistent type thrown. Expected type is Integer.");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		whileStatement.getE().visit(this, arg);
		if (!(whileStatement.getE().getType() == TypeName.BOOLEAN)) {
			throw new TypeCheckException("Invalid type encountered for expression.");
		}
		whileStatement.getB().visit(this, arg);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		boolean isDeclaredProperly = symtab.insert(declaration.getIdent().getText(), declaration);
		if (isDeclaredProperly == false) {
			throw new TypeCheckException("Multiple declarations of a variable in a single scope is not allowed.");
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		ArrayList<ParamDec> paramDecs = program.getParams();
		for (ParamDec paramDec : paramDecs) {
			visitParamDec(paramDec, arg);
		}
		visitBlock(program.getB(), arg);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		visitIdentLValue(assignStatement.getVar(), arg);
		if (assignStatement.getE().getType() != assignStatement.getVar().getType()) {
			throw new TypeCheckException("Incompatible types for expression and declaration.");
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec dec = symtab.lookup(identX.getText());
		if (dec == null) {
			throw new TypeCheckException("");
		} else {
			identX.setDec(dec);
			identX.setType(Type.getTypeName(dec.getType()));
		}
		return identX;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		boolean isParamDeclaredProperly = symtab.insert(paramDec.getIdent().getText(), paramDec);
		if (isParamDeclaredProperly == false) {
			throw new TypeCheckException("Multiple parameter declarations in a single scope is not allowed.");
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setType(TypeName.INTEGER);
		return constantExpression.getType();
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Kind opChainKind = imageOpChain.getFirstToken().kind;
		imageOpChain.getArg().visit(this, null);
		int tupleSize = imageOpChain.getArg().getExprList().size();
		if (opChainKind == Kind.OP_WIDTH || opChainKind == Kind.OP_HEIGHT) {
			if (tupleSize != 0) {
				throw new TypeCheckException("The length of the expression list is more than expected.");
			}
			imageOpChain.setType(TypeName.INTEGER);
		} else if (opChainKind == Kind.KW_SCALE) {
			if (tupleSize != 1) {
				throw new TypeCheckException("The length of the expression list is more than expected.");
			}
			imageOpChain.setType(TypeName.IMAGE);
		}
		return imageOpChain.getType();
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression e : tuple.getExprList()) {
			e.visit(this, arg);
			if (e.getType() != TypeName.INTEGER) {
				throw new TypeCheckException("Expected type is Integer for a tuple entry");
			}
		}
		return null;
	}

}

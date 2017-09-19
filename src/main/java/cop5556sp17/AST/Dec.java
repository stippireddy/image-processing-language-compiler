package cop5556sp17.AST;

import org.objectweb.asm.Label;

import cop5556sp17.Scanner.Token;

public class Dec extends ASTNode {
	
	final Token ident;
	
	private int slot;
	
	Label scopeStart;
	
	Label scopeEnd;

	public Label getScopeStart() {
		return scopeStart;
	}

	public void setScopeStart(Label scopeStart) {
		this.scopeStart = scopeStart;
	}

	public Label getScopeEnd() {
		return scopeEnd;
	}

	public void setScopeEnd(Label scopeEnd) {
		this.scopeEnd = scopeEnd;
	}

	public Dec(Token firstToken, Token ident) {
		super(firstToken);
		this.ident = ident;
	}

	public Token getType() {
		return firstToken;
	}

	public Token getIdent() {
		return ident;
	}

	@Override
	public String toString() {
		return "Dec [ident=" + ident + ", firstToken=" + firstToken + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ident == null) ? 0 : ident.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Dec)) {
			return false;
		}
		Dec other = (Dec) obj;
		if (ident == null) {
			if (other.ident != null) {
				return false;
			}
		} else if (!ident.equals(other.ident)) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitDec(this,arg);
	}
	
	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

}

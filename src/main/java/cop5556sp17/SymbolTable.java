package cop5556sp17;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;

import cop5556sp17.AST.Dec;

public class SymbolTable {

	int currentScope, nextScope;

	Deque<Integer> scopeStack;

	HashMap<String, ArrayList<SymbolTableEntry>> map;


	/**
	 * to be called when block entered
	 */
	public void enterScope() {
		currentScope = nextScope++;
		scopeStack.push(currentScope);
	}

	/**
	 * leaves scope
	 */
	public void leaveScope() {
		scopeStack.pop();
		currentScope = scopeStack.peek();

	}

	public boolean insert(String ident, Dec dec) {
		if (!map.containsKey(ident)) {
			map.put(ident, new ArrayList<SymbolTableEntry>());
		}
		ArrayList<SymbolTableEntry> symbolTableEntryList = map.get(ident);
		for (SymbolTableEntry s : symbolTableEntryList) {
			if (s.getScope() == currentScope) {
				return false;
			}
		}
		symbolTableEntryList.add(new SymbolTableEntry(currentScope, dec));
		return true;
	}

	public Dec lookup(String ident) {
		if (!map.containsKey(ident)) {
			return null;
		} else {
			ArrayList<SymbolTableEntry> symbolTableEntryList = map.get(ident);
			Integer[] scope = scopeStack.toArray(new Integer[scopeStack.size()]);
			for (int i : scope) {
				for (SymbolTableEntry s : symbolTableEntryList) {
					if (s.scope == i) {
						return s.dec;
					}
				}
			}
		}
		return null;
	}

	public SymbolTable() {
		currentScope = 0;
		nextScope = 1;
		scopeStack = new ArrayDeque<Integer>();
		scopeStack.push(currentScope);
		map = new HashMap<String, ArrayList<SymbolTableEntry>>();
	}

	@Override
	public String toString() {
		return "SymbolTable [currentScope=" + currentScope + ", scopeStack="
				+ Arrays.toString(scopeStack.toArray(new Integer[scopeStack.size()])) + ", map=" + map + "]";
	}

	class SymbolTableEntry {

		int scope;

		Dec dec;

		public SymbolTableEntry(int scope, Dec dec) {
			this.scope = scope;
			this.dec = dec;
		}

		public int getScope() {
			return scope;
		}

		public Dec getDec() {
			return dec;
		}

	}
}

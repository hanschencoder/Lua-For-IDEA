/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.sylvanaar.idea.Lua.parser.kahlua;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.parser.LuaPsiBuilder;
import org.jetbrains.annotations.NotNull;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.Prototype;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;


public class KahluaParser implements PsiParser, LuaElementTypes {

	public int nCcalls;

	protected static final String RESERVED_LOCAL_VAR_FOR_CONTROL = "(for control)";
    protected static final String RESERVED_LOCAL_VAR_FOR_STATE = "(for state)";
    protected static final String RESERVED_LOCAL_VAR_FOR_GENERATOR = "(for generator)";
    protected static final String RESERVED_LOCAL_VAR_FOR_STEP = "(for step)";
    protected static final String RESERVED_LOCAL_VAR_FOR_LIMIT = "(for limit)";
    protected static final String RESERVED_LOCAL_VAR_FOR_INDEX = "(for index)";

    // keywords array
    protected static final String[] RESERVED_LOCAL_VAR_KEYWORDS = new String[] {
        RESERVED_LOCAL_VAR_FOR_CONTROL,
        RESERVED_LOCAL_VAR_FOR_GENERATOR,
        RESERVED_LOCAL_VAR_FOR_INDEX,
        RESERVED_LOCAL_VAR_FOR_LIMIT,
        RESERVED_LOCAL_VAR_FOR_STATE,
        RESERVED_LOCAL_VAR_FOR_STEP
    };
    private static final Hashtable RESERVED_LOCAL_VAR_KEYWORDS_TABLE = new Hashtable();
    static {
    	for ( int i=0; i<RESERVED_LOCAL_VAR_KEYWORDS.length; i++ )
        	RESERVED_LOCAL_VAR_KEYWORDS_TABLE.put( RESERVED_LOCAL_VAR_KEYWORDS[i], Boolean.TRUE );
    }

    private static final int EOZ    = (-1);
	private static final int MAXSRC = 80;
	private static final int MAX_INT = Integer.MAX_VALUE-2;
	private static final int UCHAR_MAX = 255; // TODO, convert to unicode CHAR_MAX?
	private static final int LUAI_MAXCCALLS = 200;
    private LuaPsiBuilder builder;

    public KahluaParser(Project project) {
    }

    private static final String LUA_QS(String s) { return "'"+s+"'"; }
	private static final String LUA_QL(Object o) { return LUA_QS(String.valueOf(o)); }

    public static boolean isReservedKeyword(String varName) {
    	return RESERVED_LOCAL_VAR_KEYWORDS_TABLE.containsKey(varName);
    }

	/*
	** Marks the end of a patch list. It is an invalid value both as an absolute
	** address, and as a list link (would link an element to itself).
	*/
	static final int NO_JUMP = (-1);

	/*
	** grep "ORDER OPR" if you change these enums
	*/
	static final int
	  OPR_ADD=0, OPR_SUB=1, OPR_MUL=2, OPR_DIV=3, OPR_MOD=4, OPR_POW=5,
	  OPR_CONCAT=6,
	  OPR_NE=7, OPR_EQ=8,
	  OPR_LT=9, OPR_LE=10, OPR_GT=11, OPR_GE=12,
	  OPR_AND=13, OPR_OR=14,
	  OPR_NOBINOPR=15;

	static final int
		OPR_MINUS=0, OPR_NOT=1, OPR_LEN=2, OPR_NOUNOPR=3;

	/* exp kind */
	static final int
	  VVOID = 0,	/* no value */
	  VNIL = 1,
	  VTRUE = 2,
	  VFALSE = 3,
	  VK = 4,		/* info = index of constant in `k' */
	  VKNUM = 5,	/* nval = numerical value */
	  VLOCAL = 6,	/* info = local register */
	  VUPVAL = 7,       /* info = index of upvalue in `upvalues' */
	  VGLOBAL = 8,	/* info = index of table, aux = index of global name in `k' */
	  VINDEXED = 9,	/* info = table register, aux = index register (or `k') */
	  VJMP = 10,		/* info = instruction pc */
	  VRELOCABLE = 11,	/* info = instruction pc */
	  VNONRELOC = 12,	/* info = result register */
	  VCALL = 13,	/* info = instruction pc */
	  VVARARG = 14;	/* info = instruction pc */

	;

	int current;  /* current character (charint) */
	int linenumber;  /* input line counter */
	int lastline;  /* line of last token `consumed' */
	IElementType t = null;  /* current token */
	IElementType lookahead = null;  /* look ahead token */
	FuncState fs;  /* `FuncState' is private to the parser */
	Reader z;  /* input stream */
	byte[] buff;  /* buffer for tokens */
	int nbuff; /* length of buffer */
	String source;  /* current source name */

	/* ORDER RESERVED */
	final static String luaX_tokens [] = {
	    "and", "break", "do", "else", "elseif",
	    "end", "false", "for", "function", "if",
	    "in", "local", "nil", "not", "or", "repeat",
	    "return", "then", "true", "until", "while",
	    "..", "...", "==", ">=", "<=", "~=",
	    "<number>", "<name>", "<string>", "<eof>",
	};

	final static int
		/* terminal symbols denoted by reserved words */
		TK_AND=257,  TK_BREAK=258, TK_DO=259, TK_ELSE=260, TK_ELSEIF=261,
		TK_END=262, TK_FALSE=263, TK_FOR=264, TK_FUNCTION=265, TK_IF=266,
		TK_IN=267, TK_LOCAL=268, TK_NIL=269, TK_NOT=270, TK_OR=271, TK_REPEAT=272,
		TK_RETURN=273, TK_THEN=274, TK_TRUE=275, TK_UNTIL=276, TK_WHILE=277,
		/* other terminal symbols */
		TK_CONCAT=278, TK_DOTS=279, TK_EQ=280, TK_GE=281, TK_LE=282, TK_NE=283,
		TK_NUMBER=284, TK_NAME=285, TK_STRING=286, TK_EOS=287;

	final static int FIRST_RESERVED = TK_AND;
	final static int NUM_RESERVED = TK_WHILE+1-FIRST_RESERVED;

	final static Hashtable RESERVED = new Hashtable();
	static {
		for ( int i=0; i<NUM_RESERVED; i++ ) {
			String ts = luaX_tokens[i];
			RESERVED.put(ts, new Integer(FIRST_RESERVED+i));
		}
	}

	private boolean isalnum(int c) {
		return (c >= '0' && c <= '9')
			|| (c >= 'a' && c <= 'z')
			|| (c >= 'A' && c <= 'Z')
			|| (c == '_');
		// return Character.isLetterOrDigit(c);
	}

	private boolean isalpha(int c) {
		return (c >= 'a' && c <= 'z')
			|| (c >= 'A' && c <= 'Z');
	}

	private boolean isdigit(int c) {
		return (c >= '0' && c <= '9');
	}

	private boolean isspace(int c) {
		return (c <= ' ');
	}

	public static Prototype compile(int firstByte, Reader z, String name, String source) {
        if (name != null) {
            source = name;
        } else {
            name = "stdin";
            source = "[string \"" + trim(source, MAXSRC) + "\"]";
        }
        KahluaParser lexstate = new KahluaParser(z, firstByte, source);
        FuncState funcstate = new FuncState(lexstate);
        // lexstate.buff = buff;

        /* main func. is always vararg */
        funcstate.isVararg = FuncState.VARARG_ISVARARG;
        funcstate.f.name = name;
        lexstate.next(); /* read first token */
        lexstate.chunk();
        lexstate.check(EMPTY_INPUT);
        lexstate.close_func();
        FuncState._assert(funcstate.prev == null);
        FuncState._assert(funcstate.f.numUpvalues == 0);
        FuncState._assert(lexstate.fs == null);
        return funcstate.f;
    }

	public KahluaParser(Reader stream, int firstByte, String source) {
		this.z = stream;
		this.buff = new byte[32];
        this.lookahead = EMPTY_INPUT; /* no look-ahead token */
        this.fs = null;
        this.linenumber = 1;
        this.lastline = 1;
        this.source = source;
        this.nbuff = 0;   /* initialize buffer */
        this.current = firstByte; /* read first char */
        this.skipShebang();
	}

	void nextChar() {
		try {
 			current = z.read();
		} catch ( IOException e ) {
			e.printStackTrace();
			current = EOZ;
		}
	}

	boolean currIsNewline() {
		return current == '\n' || current == '\r';
	}

	void save_and_next() {
		save( current );
		nextChar();
	}

	void save(int c) {
		if ( buff == null || nbuff + 1 > buff.length )
			buff = FuncState.realloc( buff, nbuff*2+1 );
		buff[nbuff++] = (byte) c;
	}


	String token2str( int token ) {
		if ( token < FIRST_RESERVED ) {
			return iscntrl(token)?
					"char("+((int)token)+")":
					String.valueOf( (char) token );
		} else {
			return luaX_tokens[token-FIRST_RESERVED];
		}
	}

	private static boolean iscntrl(int token) {
		return token < ' ';
	}

	String txtToken(int token) {
		switch ( token ) {
		case TK_NAME:
		case TK_STRING:
		case TK_NUMBER:
			return new String( buff, 0, nbuff );
		default:
			return token2str( token );
		}
	}

	void lexerror( String msg, IElementType token ) {
		String cid = source;
		String errorMessage;
		if ( token != EMPTY_INPUT ) {
			errorMessage = cid+":"+linenumber+": "+msg+" near `"+ token + "`";
		} else {
			errorMessage = cid+":"+linenumber+": "+msg;
		}
		throw new KahluaException(errorMessage);
	}

    private static String trim(String s, int max) {
        if (s.length() > max) {
            return s.substring(0, max - 3) + "...";
        }
        return s;
    }

	void syntaxerror( String msg ) {
		lexerror( msg, t );
	}

	String newstring( byte[] chars, int offset, int len ) {
		try {
			String s = new String(chars, offset, len, "UTF-8");
			return s;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	void inclinenumber() {
		int old = current;
		FuncState._assert( currIsNewline() );
		nextChar(); /* skip '\n' or '\r' */
		if ( currIsNewline() && current != old )
			nextChar(); /* skip '\n\r' or '\r\n' */
		if ( ++linenumber >= MAX_INT )
			syntaxerror("chunk has too many lines");
	}

	private void skipShebang() {
		if ( current == '#' )
			while (!currIsNewline() && current != EOZ)
				nextChar();
	}



	/*
	** =======================================================
	** LEXICAL ANALYZER
	** =======================================================
	*/


	boolean check_next(String set) {
		if (set.indexOf(current) < 0)
			return false;
		save_and_next();
		return true;
	}

	void str2d(String str, Token token) {
        try {
            double d;
            if (str.startsWith("0x")) {
                d = Long.parseLong(str.substring(2), 16);
            } else {
                d = Double.parseDouble(str);
            }
            token.r = d;
        } catch (NumberFormatException e) {
			lexerror("malformed number", NUMBER);
        }
    }

	//
	// TODO: reexamine this source and see if it should be ported differently
	//
	// static void trydecpoint (KahluaParser *ls, SemInfo *seminfo) {
	//	  /* format error: try to update decimal point separator */
	//	  struct lconv *cv = localeconv();
	//	  char old = this.decpoint;
	//	  this.decpoint = (cv ? cv->decimal_point[0] : '.');
	//	  buffreplace(ls, old, this.decpoint);  /* try updated decimal separator */
	//	  if (!luaO_str2d(luaZ_buffer(this.buff), &seminfo->r)) {
	//	    /* format error with correct decimal point: no more options */
	//	    buffreplace(ls, this.decpoint, '.');  /* undo change (for error message) */
	//	    luaX_lexerror(ls, "malformed number", TK_NUMBER);
	//	  }
	//	}
	//
	/*
	void trydecpoint(String str, SemInfo seminfo) {
		NumberFormat nf = NumberFormat.getInstance();
		try {
			Number n = nf.parse(str);
			double d = n.doubleValue();
			seminfo.r = new LDouble(d);
		} catch (ParseException e) {
			lexerror("malformed number", TK_NUMBER);
		}
	}
	*/

	void read_numeral(Token token) {
		FuncState._assert (isdigit(current));
		do {
			save_and_next();
		} while (isdigit(current) || current == '.');
		if (check_next("Ee")) /* `E'? */
			check_next("+-"); /* optional exponent sign */
		while (isalnum(current) || current == '_')
			save_and_next();
		save('\0');
		String str = new String(buff, 0, nbuff);
		str2d(str, token);
	}

	int skip_sep() {
		int count = 0;
		int s = current;
		FuncState._assert (s == '[' || s == ']');
		save_and_next();
		while (current == '=') {
			save_and_next();
			count++;
		}
		return (current == s) ? count : (-count) - 1;
	}

	void read_long_string(Token token, int sep) {
		int cont = 0;
		save_and_next(); /* skip 2nd `[' */
		if (currIsNewline()) /* string starts with a newline? */
			inclinenumber(); /* skip it */
		for (boolean endloop = false; !endloop;) {
			switch (current) {
			case EOZ:
				lexerror((token != null) ? "unfinished long string"
						: "unfinished long comment", EMPTY_INPUT);
				break; /* to avoid warnings */
			case '[': {
				if (skip_sep() == sep) {
					save_and_next(); /* skip 2nd `[' */
					cont++;
				}
				break;
			}
			case ']': {
				if (skip_sep() == sep) {
					save_and_next(); /* skip 2nd `]' */
					endloop = true;
				}
				break;
			}
			case '\n':
			case '\r': {
				save('\n');
				inclinenumber();
				if (token == null)
					nbuff = 0; /* avoid wasting space */
				break;
			}
			default: {
				if (token != null)
					save_and_next();
				else
					nextChar();
			}
			}
		}
		if (token != null)
			token.ts = newstring(buff, 2 + sep, nbuff - 2 * (2 + sep));
	}

	void read_string(int del, Token token) {
		save_and_next();
		while (current != del) {
			switch (current) {
			case EOZ:
				lexerror("unfinished string", EMPTY_INPUT);
				continue; /* to avoid warnings */
			case '\n':
			case '\r':
				lexerror("unfinished string", STRING);
				continue; /* to avoid warnings */
			case '\\': {
				int c;
				nextChar(); /* do not save the `\' */
				switch (current) {
				case 'a': /* bell */
					c = '\u0007';
					break;
				case 'b': /* backspace */
					c = '\b';
					break;
				case 'f': /* form feed */
					c = '\f';
					break;
				case 'n': /* newline */
					c = '\n';
					break;
				case 'r': /* carriage return */
					c = '\r';
					break;
				case 't': /* tab */
					c = '\t';
					break;
				case 'v': /* vertical tab */
					c = '\u000B';
					break;
				case '\n': /* go through */
				case '\r':
					save('\n');
					inclinenumber();
					continue;
				case EOZ:
					continue; /* will raise an error next loop */
				default: {
					if (!isdigit(current))
						save_and_next(); /* handles \\, \", \', and \? */
					else { /* \xxx */
						int i = 0;
						c = 0;
						do {
							c = 10 * c + (current - '0');
							nextChar();
						} while (++i < 3 && isdigit(current));
						if (c > UCHAR_MAX)
							lexerror("escape sequence too large", STRING);
						save(c);
					}
					continue;
				}
				}
				save(c);
				nextChar();
				continue;
			}
			default:
				save_and_next();
			}
		}
		save_and_next(); /* skip delimiter */
		token.ts = newstring(buff, 1, nbuff - 2);
	}

//	int llex(Token token) {
//		nbuff = 0;
//		while (true) {
//			switch (current) {
//			case '\n':
//			case '\r': {
//				inclinenumber();
//				continue;
//			}
//			case '-': {
//				nextChar();
//				if (current != '-')
//					return '-';
//				/* else is a comment */
//				nextChar();
//				if (current == '[') {
//					int sep = skip_sep();
//					nbuff = 0; /* `skip_sep' may dirty the buffer */
//					if (sep >= 0) {
//						read_long_string(null, sep); /* long comment */
//						nbuff = 0;
//						continue;
//					}
//				}
//				/* else short comment */
//				while (!currIsNewline() && current != EOZ)
//					nextChar();
//				continue;
//			}
//			case '[': {
//				int sep = skip_sep();
//				if (sep >= 0) {
//					read_long_string(token, sep);
//					return TK_STRING;
//				} else if (sep == -1)
//					return '[';
//				else
//					lexerror("invalid long string delimiter", TK_STRING);
//			}
//			case '=': {
//				nextChar();
//				if (current != '=')
//					return '=';
//				else {
//					nextChar();
//					return TK_EQ;
//				}
//			}
//			case '<': {
//				nextChar();
//				if (current != '=')
//					return '<';
//				else {
//					nextChar();
//					return TK_LE;
//				}
//			}
//			case '>': {
//				nextChar();
//				if (current != '=')
//					return '>';
//				else {
//					nextChar();
//					return TK_GE;
//				}
//			}
//			case '~': {
//				nextChar();
//				if (current != '=')
//					return '~';
//				else {
//					nextChar();
//					return TK_NE;
//				}
//			}
//			case '"':
//			case '\'': {
//				read_string(current, token);
//				return TK_STRING;
//			}
//			case '.': {
//				save_and_next();
//				if (check_next(".")) {
//					if (check_next("."))
//						return TK_DOTS; /* ... */
//					else
//						return TK_CONCAT; /* .. */
//				} else if (!isdigit(current))
//					return '.';
//				else {
//					read_numeral(token);
//					return TK_NUMBER;
//				}
//			}
//			case EOZ: {
//				return TK_EOS;
//			}
//			default: {
//				if (isspace(current)) {
//					FuncState._assert (!currIsNewline());
//					nextChar();
//					continue;
//				} else if (isdigit(current)) {
//					read_numeral(token);
//					return TK_NUMBER;
//				} else if (isalpha(current) || current == '_') {
//					/* identifier or reserved word */
//					String ts;
//					do {
//						save_and_next();
//					} while (isalnum(current) || current == '_');
//					ts = newstring(buff, 0, nbuff);
//					if ( RESERVED.containsKey(ts) )
//						return ((Integer)RESERVED.get(ts)).intValue();
//					else {
//						token.ts = ts;
//						return TK_NAME;
//					}
//				} else {
//					int c = current;
//					nextChar();
//					return c; /* single-char tokens (+ - / ...) */
//				}
//			}
//			}
//		}
//	}

	void next() {
		lastline = linenumber;
        builder.advanceLexer();
        t = builder.getTokenType();

//        /*
//		if (lookahead != TK_EOS) { /* is there a look-ahead token? */
//			t.set( lookahead ); /* use this one */
//			lookahead = TK_EOS; /* and discharge it */
//		} else
//			t = llex(t); /* read next token */
//    */
	}

	void lookahead() {
//		FuncState._assert (lookahead == TK_EOS);
//		lookahead = llex(lookahead);
	}

	// =============================================================
	// from lcode.h
	// =============================================================


	// =============================================================
	// from lparser.c
	// =============================================================

	boolean hasmultret(int k) {
		return ((k) == VCALL || (k) == VVARARG);
	}

	/*----------------------------------------------------------------------
	name		args	description
	------------------------------------------------------------------------*/

	/*
	 * * prototypes for recursive non-terminal functions
	 */

	void error_expected(IElementType token) {
		syntaxerror(token.toString() + " expected");
	}

	boolean testnext(IElementType c) {
		if (t == c) {
			next();
			return true;
		} else
			return false;
	}

	void check(IElementType c) {
		if (t != c)
			error_expected(c);
	}

	void checknext (IElementType c) {
	  check(c);
	  next();
	}

	void check_condition(boolean c, String msg) {
		if (!(c))
			syntaxerror(msg);
	}


	void check_match(IElementType what, IElementType who, int where) {
		if (!testnext(what)) {
			if (where == linenumber)
				error_expected(what);
			else {
				syntaxerror(what
				+ " expected " + "(to close " + who.toString()
				+ " at line " + where + ")");
			}
		}
	}

	String str_checkname() {
		String ts;

        check(NAME);

		ts = "NAME:TODO";

        next();
		return ts;
	}

	void codestring(ExpDesc e, String s) {
		e.init(VK, fs.stringK(s));
	}

	void checkname(ExpDesc e) {
		codestring(e, str_checkname());
	}


	int registerlocalvar(String varname) {
		FuncState fs = this.fs;
		if (fs.locvars == null || fs.nlocvars + 1 > fs.locvars.length)
			fs.locvars = FuncState.realloc( fs.locvars, fs.nlocvars*2+1 );
		fs.locvars[fs.nlocvars] = varname;
		return fs.nlocvars++;
	}


//
//	#define new_localvarliteral(ls,v,n) \
//	  this.new_localvar(luaX_newstring(ls, "" v, (sizeof(v)/sizeof(char))-1), n)
//
	void new_localvarliteral(String v, int n) {
        new_localvar(v, n);
	}

	void new_localvar(String name, int n) {
		FuncState fs = this.fs;
		fs.checklimit(fs.nactvar + n + 1, FuncState.LUAI_MAXVARS, "local variables");
		fs.actvar[fs.nactvar + n] = (short) registerlocalvar(name);
	}

	void adjustlocalvars(int nvars) {
		FuncState fs = this.fs;
		fs.nactvar = (fs.nactvar + nvars);
	}

	void removevars(int tolevel) {
		FuncState fs = this.fs;
		fs.nactvar = tolevel;
	}

	void singlevar(ExpDesc var) {
		String varname = this.str_checkname();
		FuncState fs = this.fs;
		if (fs.singlevaraux(varname, var, 1) == VGLOBAL)
			var.info = fs.stringK(varname); /* info points to global name */
	}

	void adjust_assign(int nvars, int nexps, ExpDesc e) {
		FuncState fs = this.fs;
		int extra = nvars - nexps;
		if (hasmultret(e.k)) {
			/* includes call itself */
			extra++;
			if (extra < 0)
				extra = 0;
			/* last exp. provides the difference */
			fs.setreturns(e, extra);
			if (extra > 1)
				fs.reserveregs(extra - 1);
		} else {
			/* close last expression */
			if (e.k != VVOID)
				fs.exp2nextreg(e);
			if (extra > 0) {
				int reg = fs.freereg;
				fs.reserveregs(extra);
				fs.nil(reg, extra);
			}
		}
	}

	void enterlevel() {
		if (++nCcalls > LUAI_MAXCCALLS)
			lexerror("chunk has too many syntax levels", EMPTY_INPUT);
	}

	void leavelevel() {
		nCcalls--;
	}

	void pushclosure(FuncState func, ExpDesc v) {
		FuncState fs = this.fs;
		Prototype f = fs.f;
		if (f.prototypes == null || fs.np + 1 > f.prototypes.length)
			f.prototypes = FuncState.realloc( f.prototypes, fs.np*2 + 1 );
		f.prototypes[fs.np++] = func.f;
		v.init(VRELOCABLE, fs.codeABx(FuncState.OP_CLOSURE, 0, fs.np - 1));
		for (int i = 0; i < func.f.numUpvalues; i++) {
			int o = (func.upvalues_k[i] == VLOCAL) ? FuncState.OP_MOVE
					: FuncState.OP_GETUPVAL;
			fs.codeABC(o, 0, func.upvalues_info[i], 0);
		}
	}

	void close_func() {
		FuncState fs = this.fs;
		Prototype f = fs.f;
		f.isVararg = fs.isVararg != 0;

		this.removevars(0);
		fs.ret(0, 0); /* final return */
		f.code = FuncState.realloc(f.code, fs.pc);
		f.lines = FuncState.realloc(f.lines, fs.pc);
		// f.sizelineinfo = fs.pc;
		f.constants = FuncState.realloc(f.constants, fs.nk);
		f.prototypes = FuncState.realloc(f.prototypes, fs.np);
		fs.locvars = FuncState.realloc(fs.locvars, fs.nlocvars);
		// f.sizelocvars = fs.nlocvars;
		fs.upvalues = FuncState.realloc(fs.upvalues, f.numUpvalues);
		// FuncState._assert (CheckCode.checkcode(f));
		FuncState._assert (fs.bl == null);
		this.fs = fs.prev;
//		L.top -= 2; /* remove table and prototype from the stack */
		// /* last token read was anchored in defunct function; must reanchor it
		// */
		// if (fs!=null) ls.anchor_token();
	}

	/*============================================================*/
	/* GRAMMAR RULES */
	/*============================================================*/

	void field(ExpDesc v) {
		/* field -> ['.' | ':'] NAME */
		FuncState fs = this.fs;
		ExpDesc key = new ExpDesc();
		fs.exp2anyreg(v);
		this.next(); /* skip the dot or colon */
		this.checkname(key);
		fs.indexed(v, key);
	}

	void yindex(ExpDesc v) {
		/* index -> '[' expr ']' */
		this.next(); /* skip the '[' */
		this.expr(v);
		this.fs.exp2val(v);
		this.checknext(RBRACK);
	}


 /*
	** {======================================================================
	** Rules for Constructors
	** =======================================================================
	*/
	;


	void recfield(ConsControl cc) {
		/* recfield -> (NAME | `['exp1`]') = exp1 */
		FuncState fs = this.fs;
		int reg = this.fs.freereg;
		ExpDesc key = new ExpDesc();
		ExpDesc val = new ExpDesc();
		int rkkey;
		if (this.t == NAME) {
			fs.checklimit(cc.nh, MAX_INT, "items in a constructor");
			this.checkname(key);
		} else
			/* this.t == '[' */
			this.yindex(key);
		cc.nh++;
		this.checknext(ASSIGN);
		rkkey = fs.exp2RK(key);
		this.expr(val);
		fs.codeABC(FuncState.OP_SETTABLE, cc.t.info, rkkey, fs.exp2RK(val));
		fs.freereg = reg; /* free registers */
	}

	void listfield (ConsControl cc) {
	  this.expr(cc.v);
	  fs.checklimit(cc.na, MAX_INT, "items in a constructor");
	  cc.na++;
	  cc.tostore++;
	}


	void constructor(ExpDesc t) {
		/* constructor -> ?? */
		FuncState fs = this.fs;
		int line = this.linenumber;
		int pc = fs.codeABC(FuncState.OP_NEWTABLE, 0, 0, 0);
		ConsControl cc = new ConsControl();
		cc.na = cc.nh = cc.tostore = 0;
		cc.t = t;
		t.init(VRELOCABLE, pc);
		cc.v.init(VVOID, 0); /* no value (yet) */
		fs.exp2nextreg(t); /* fix it at stack top (for gc) */
		this.checknext(LCURLY);
		do {
			FuncState._assert (cc.v.k == VVOID || cc.tostore > 0);
			if (this.t == RCURLY)
				break;
			fs.closelistfield(cc);
			if (this.t == NAME) {
			  /* may be listfields or recfields */
				this.lookahead();
				if (this.lookahead != ASSIGN) /* expression? */
					this.listfield(cc);
				else
					this.recfield(cc);
				break;
			}
			else if (this.t == LBRACK) { /* constructor_item -> recfield */
				this.recfield(cc);
				break;
			}
			else { /* constructor_part -> listfield */
				this.listfield(cc);
				break;
			}
			
		} while (this.testnext(COMMA) || this.testnext(SEMI));
		this.check_match(RCURLY, LCURLY, line);
		fs.lastlistfield(cc);
		InstructionPtr i = new InstructionPtr(fs.f.code, pc);
		FuncState.SETARG_B(i, luaO_int2fb(cc.na)); /* set initial array size */
		FuncState.SETARG_C(i, luaO_int2fb(cc.nh));  /* set initial table size */
	}

	/*
	** converts an integer to a "floating point byte", represented as
	** (eeeeexxx), where the real value is (1xxx) * 2^(eeeee - 1) if
	** eeeee != 0 and (xxx) otherwise.
	*/
	static int luaO_int2fb (int x) {
	  int e = 0;  /* expoent */
	  while (x >= 16) {
	    x = (x+1) >> 1;
	    e++;
	  }
	  if (x < 8) return x;
	  else return ((e+1) << 3) | (((int)x) - 8);
	}


	/* }====================================================================== */

	void parlist () {
	  /* parlist -> [ param { `,' param } ] */
	  FuncState fs = this.fs;
	  Prototype f = fs.f;
	  int nparams = 0;
	  fs.isVararg = 0;
	  if (this.t != RPAREN) {  /* is `parlist' not empty? */
	    do {
	     if (this.t == NAME) {
	          /* param . NAME */
	          this.new_localvar(this.str_checkname(), nparams++);
	          break;
	        }
	        else if (this.t == ELLIPSIS) {  /* param . `...' */
	          this.next();
	          fs.isVararg |= FuncState.VARARG_ISVARARG;
	          break;
	        }
            else {
	        this.syntaxerror("<name> or " + LUA_QL("...") + " expected");
         }
	    } while ((fs.isVararg==0) && this.testnext(COMMA));
	  }
	  this.adjustlocalvars(nparams);
	  f.numParams = (fs.nactvar - (fs.isVararg & FuncState.VARARG_HASARG));
	  fs.reserveregs(fs.nactvar);  /* reserve register for parameters */
	}


	void body(ExpDesc e, boolean needself, int line) {
		/* body -> `(' parlist `)' chunk END */
		FuncState new_fs = new FuncState(this);
		new_fs.linedefined = line;
		this.checknext(LPAREN);
		if (needself) {
			new_localvarliteral("self", 0);
			adjustlocalvars(1);
		}
		this.parlist();
		this.checknext(RPAREN);
		this.chunk();
		new_fs.lastlinedefined = this.linenumber;
		this.check_match(END, FUNCTION, line);
		this.close_func();
		this.pushclosure(new_fs, e);
	}

	int explist1(ExpDesc v) {
		/* explist1 -> expr { `,' expr } */
		int n = 1; /* at least one expression */
		this.expr(v);
		while (this.testnext(COMMA)) {
			fs.exp2nextreg(v);
			this.expr(v);
			n++;
		}
		return n;
	}


	void funcargs(ExpDesc f) {
		FuncState fs = this.fs;
		ExpDesc args = new ExpDesc();
		int base, nparams;
		int line = this.linenumber;

		if (this.t == LPAREN) { /* funcargs -> `(' [ explist1 ] `)' */
			if (line != this.lastline)
				this.syntaxerror("ambiguous syntax (function call x new statement)");
			this.next();
			if (this.t == RPAREN) /* arg list is empty? */
				args.k = VVOID;
			else {
				this.explist1(args);
				fs.setmultret(args);
			}
			this.check_match(RPAREN, LPAREN, line);
		//	break;
		}
        else if (this.t == LCURLY) {
		 /* funcargs -> constructor */
			this.constructor(args);

		}
		else if (this.t == STRING) { /* funcargs -> STRING */
			//this.codestring(args, this.t.ts);
			this.next(); /* must use `seminfo' before `next' */

		}
        else {
			this.syntaxerror("function arguments expected");
			
		}
		
		FuncState._assert (f.k == VNONRELOC);
		base = f.info; /* base register for call */
		if (hasmultret(args.k))
			nparams = FuncState.LUA_MULTRET; /* open call */
		else {
			if (args.k != VVOID)
				fs.exp2nextreg(args); /* close last argument */
			nparams = fs.freereg - (base + 1);
		}
		f.init(VCALL, fs.codeABC(FuncState.OP_CALL, base, nparams + 1, 2));
		fs.fixline(line);
		fs.freereg = base+1;  /* call remove function and arguments and leaves
							 * (unless changed) one result */
	}


	/*
	** {======================================================================
	** Expression parsing
	** =======================================================================
	*/

	void prefixexp(ExpDesc v) {
		/* prefixexp -> NAME | '(' expr ')' */

		if (this.t == LPAREN) {
			int line = this.linenumber;
			this.next();
			this.expr(v);
			this.check_match(RPAREN, LPAREN, line);
			fs.dischargevars(v);
			return;
		}
        else if (this.t == NAME) {
			this.singlevar(v);
			return;
		}
        this.syntaxerror("unexpected symbol");
        return;
	}


	void primaryexp(ExpDesc v) {
		/*
		 * primaryexp -> prefixexp { `.' NAME | `[' exp `]' | `:' NAME funcargs |
		 * funcargs }
		 */
		FuncState fs = this.fs;
		this.prefixexp(v);
		for (;;) {

			if (this.t == DOT) { /* field */
				this.field(v);
				break;
			}
			else if (this.t ==  LBRACK) { /* `[' exp1 `]' */
				ExpDesc key = new ExpDesc();
				fs.exp2anyreg(v);
				this.yindex(key);
				fs.indexed(v, key);
				break;
			}
			else if (this.t ==  COLON) { /* `:' NAME funcargs */
				ExpDesc key = new ExpDesc();
				this.next();
				this.checkname(key);
				fs.self(v, key);
				this.funcargs(v);
				break;
			}
			else if (this.t ==  LPAREN
			 ||this.t == STRING
			 ||this.t == LCURLY) { /* funcargs */
				fs.exp2nextreg(v);
				this.funcargs(v);
				break;
			}
			else {
				return;
			}
        }
	}


	void simpleexp(ExpDesc v) {
		/*
		 * simpleexp -> NUMBER | STRING | NIL | true | false | ... | constructor |
		 * FUNCTION body | primaryexp
		 */

		if (this.t == NUMBER) {
			v.init(VKNUM, 0);
			//TODO v.setNval(this.t.r);
		}
		else if (this.t == STRING) {
			//TODO this.codestring(v, this.t.ts);

		}
		else if (this.t == NIL) {
			v.init(VNIL, 0);

		}
		else if (this.t == TRUE) {
			v.init(VTRUE, 0);

		}
		else if (this.t == FALSE) {
			v.init(VFALSE, 0);

		}
		else if (this.t == ELLIPSIS) { /* vararg */
			FuncState fs = this.fs;
			this.check_condition(fs.isVararg!=0, "cannot use " + LUA_QL("...")
					+ " outside a vararg function");
			fs.isVararg &= ~FuncState.VARARG_NEEDSARG; /* don't need 'arg' */
			v.init(VVARARG, fs.codeABC(FuncState.OP_VARARG, 0, 1, 0));

		}
		else if (this.t == LCURLY)  { /* constructor */
			this.constructor(v);
			return;
		}
		else if (this.t == FUNCTION) {
			this.next();
			this.body(v, false, this.linenumber);
			return;
		}
		else {
			this.primaryexp(v);
			return;
		}
		this.next();
	}


    int getunopr(IElementType op) {
        if (op == NOT)
            return OPR_NOT;
        if (op == MINUS)
            return OPR_MINUS;
        if (op == GETN)
            return OPR_LEN;

        return OPR_NOUNOPR;
    }


    int getbinopr(IElementType op) {

        if (op == PLUS)
            return OPR_ADD;
        if (op == MINUS)
            return OPR_SUB;
        if (op == MULT)
            return OPR_MUL;
        if (op == DIV)
            return OPR_DIV;
        if (op == MOD)
            return OPR_MOD;
        if (op == EXP)
            return OPR_POW;
        if (op == CONCAT)
            return OPR_CONCAT;
        if (op == NE)
            return OPR_NE;
        if (op == EQ)
            return OPR_EQ;
        if (op == LT)
            return OPR_LT;
        if (op == LE)
            return OPR_LE;
        if (op == GT)
            return OPR_GT;
        if (op == GE)
            return OPR_GE;
        if (op == AND)
            return OPR_AND;
        if (op == OR)
            return OPR_OR;

        return OPR_NOBINOPR;
    }

	static final int[] priorityLeft = {
		   6, 6, 7, 7, 7,  /* `+' `-' `/' `%' */
		   10, 5,                 /* power and concat (right associative) */
		   3, 3,                  /* equality and inequality */
		   3, 3, 3, 3,  /* order */
		   2, 1,                    /* logical (and/or) */
	};

	static final int[]  priorityRight = {  /* ORDER OPR */
		   6, 6, 7, 7, 7,  /* `+' `-' `/' `%' */
		   9, 4,                 /* power and concat (right associative) */
		   3, 3,                  /* equality and inequality */
		   3, 3, 3, 3,  /* order */
		   2, 1                   /* logical (and/or) */
	};

	static final int UNARY_PRIORITY	= 8;  /* priority for unary operators */


	/*
	** subexpr -> (simpleexp | unop subexpr) { binop subexpr }
	** where `binop' is any binary operator with a priority higher than `limit'
	*/
	int subexpr(ExpDesc v, int limit) {
		int op;
		int uop;
		this.enterlevel();
		uop = getunopr(this.t);
		if (uop != OPR_NOUNOPR) {
			this.next();
			this.subexpr(v, UNARY_PRIORITY);
			fs.prefix(uop, v);
		} else
			this.simpleexp(v);
		/* expand while operators have priorities higher than `limit' */
		op = getbinopr(this.t);
		while (op != OPR_NOBINOPR && priorityLeft[op] > limit) {
			ExpDesc v2 = new ExpDesc();
			int nextop;
			this.next();
			fs.infix(op, v);
			/* read sub-expression with higher priority */
			nextop = this.subexpr(v2, priorityRight[op]);
			fs.posfix(op, v, v2);
			op = nextop;
		}
		this.leavelevel();
		return op; /* return first untreated operator */
	}

	void expr(ExpDesc v) {
		this.subexpr(v, 0);
        next();
	}

	/* }==================================================================== */



	/*
	** {======================================================================
	** Rules for Statements
	** =======================================================================
	*/


	boolean block_follow (IElementType token) {
        if (token == ELSE || token == ELSEIF || token == UNTIL || token == EMPTY_INPUT)
            return true;

        return false;
	}


	void block () {
	  /* block -> chunk */
	  FuncState fs = this.fs;
	  BlockCnt bl = new BlockCnt();
	  fs.enterblock(bl, false);
	  this.chunk();
	  FuncState._assert(bl.breaklist == NO_JUMP);
	  fs.leaveblock();
	}


	;


	/*
	** check whether, in an assignment to a local variable, the local variable
	** is needed in a previous assignment (to a table). If so, save original
	** local value in a safe place and use this safe copy in the previous
	** assignment.
	*/
	void check_conflict (LHS_assign lh, ExpDesc v) {
		FuncState fs = this.fs;
		int extra = fs.freereg;  /* eventual position to save local variable */
		boolean conflict = false;
		for (; lh!=null; lh = lh.prev) {
			if (lh.v.k == VINDEXED) {
				if (lh.v.info == v.info) {  /* conflict? */
					conflict = true;
					lh.v.info = extra;  /* previous assignment will use safe copy */
				}
				if (lh.v.aux == v.info) {  /* conflict? */
					conflict = true;
					lh.v.aux = extra;  /* previous assignment will use safe copy */
				}
			}
		}
		if (conflict) {
			fs.codeABC(FuncState.OP_MOVE, fs.freereg, v.info, 0); /* make copy */
			fs.reserveregs(1);
		}
	}


	void assignment (LHS_assign lh, int nvars) {
		ExpDesc e = new ExpDesc();
		this.check_condition(VLOCAL <= lh.v.k && lh.v.k <= VINDEXED,
	                      "syntax error");
		if (this.testnext(COMMA)) {  /* assignment -> `,' primaryexp assignment */
		    LHS_assign nv = new LHS_assign();
		    nv.prev = lh;
		    this.primaryexp(nv.v);
		    if (nv.v.k == VLOCAL)
		      this.check_conflict(lh, nv.v);
		    this.assignment(nv, nvars+1);
		}
		else {  /* assignment . `=' explist1 */
		    int nexps;
		    this.checknext(ASSIGN);
		    nexps = this.explist1(e);
		    if (nexps != nvars) {
		      this.adjust_assign(nvars, nexps, e);
		      if (nexps > nvars)
		        this.fs.freereg -= nexps - nvars;  /* remove extra values */
	    }
	    else {
	    	fs.setoneret(e);  /* close last expression */
	    	fs.storevar(lh.v, e);
	    	return;  /* avoid default */
	    }
	  }
	  e.init(VNONRELOC, this.fs.freereg-1);  /* default assignment */
	  fs.storevar(lh.v, e);
	}


	int cond() {
		/* cond -> exp */
		ExpDesc v = new ExpDesc();
		/* read condition */
		this.expr(v);
		/* `falses' are all equal here */
		if (v.k == VNIL)
			v.k = VFALSE;
		fs.goiftrue(v);
		return v.f;
	}


	void breakstat() {
		FuncState fs = this.fs;
		BlockCnt bl = fs.bl;
		boolean upval = false;
		while (bl != null && !bl.isbreakable) {
			upval |= bl.upval;
			bl = bl.previous;
		}
		if (bl == null)
			this.syntaxerror("no loop to break");
		if (upval)
			fs.codeABC(FuncState.OP_CLOSE, bl.nactvar, 0, 0);
		bl.breaklist = fs.concat(bl.breaklist, fs.jump());
	}


	void whilestat (int line) {
		/* whilestat -> WHILE cond DO block END */
		FuncState fs = this.fs;
		int whileinit;
		int condexit;
		BlockCnt bl = new BlockCnt();
		this.next();  /* skip WHILE */
		whileinit = fs.getlabel();
		condexit = this.cond();
		fs.enterblock(bl, true);
		this.checknext(DO);
		this.block();
		fs.patchlist(fs.jump(), whileinit);
		this.check_match(END, WHILE, line);
		fs.leaveblock();
		fs.patchtohere(condexit);  /* false conditions finish the loop */
	}

	void repeatstat(int line) {
		/* repeatstat -> REPEAT block UNTIL cond */
		int condexit;
		FuncState fs = this.fs;
		int repeat_init = fs.getlabel();
		BlockCnt bl1 = new BlockCnt();
		BlockCnt bl2 = new BlockCnt();
		fs.enterblock(bl1, true); /* loop block */
		fs.enterblock(bl2, false); /* scope block */
		this.next(); /* skip REPEAT */
		this.chunk();
		this.check_match(UNTIL, REPEAT, line);
		condexit = this.cond(); /* read condition (inside scope block) */
		if (!bl2.upval) { /* no upvalues? */
			fs.leaveblock(); /* finish scope */
			fs.patchlist(condexit, repeat_init); /* close the loop */
		} else { /* complete semantics when there are upvalues */
			this.breakstat(); /* if condition then break */
			fs.patchtohere(condexit); /* else... */
			fs.leaveblock(); /* finish scope... */
			fs.patchlist(fs.jump(), repeat_init); /* and repeat */
		}
		fs.leaveblock(); /* finish loop */
	}


	int exp1() {
		ExpDesc e = new ExpDesc();
		int k;
		this.expr(e);
		k = e.k;
		fs.exp2nextreg(e);
		return k;
	}


	void forbody(int base, int line, int nvars, boolean isnum) {
		/* forbody -> DO block */
		BlockCnt bl = new BlockCnt();
		FuncState fs = this.fs;
		int prep, endfor;
		this.adjustlocalvars(3); /* control variables */
		this.checknext(DO);
		prep = isnum ? fs.codeAsBx(FuncState.OP_FORPREP, base, NO_JUMP) : fs.jump();
		fs.enterblock(bl, false); /* scope for declared variables */
		this.adjustlocalvars(nvars);
		fs.reserveregs(nvars);
		this.block();
		fs.leaveblock(); /* end of scope for declared variables */
		fs.patchtohere(prep);
		endfor = (isnum) ? fs.codeAsBx(FuncState.OP_FORLOOP, base, NO_JUMP) : fs
				.codeABC(FuncState.OP_TFORLOOP, base, 0, nvars);
		fs.fixline(line); /* pretend that `Lua.OP_FOR' starts the loop */
		fs.patchlist((isnum ? endfor : fs.jump()), prep + 1);
	}


	void fornum(String varname, int line) {
		/* fornum -> NAME = exp1,exp1[,exp1] forbody */
		FuncState fs = this.fs;
		int base = fs.freereg;
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_INDEX, 0);
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_LIMIT, 1);
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_STEP, 2);
		this.new_localvar(varname, 3);
		this.checknext(ASSIGN);
		this.exp1(); /* initial value */
		this.checknext(COMMA);
		this.exp1(); /* limit */
		if (this.testnext(COMMA))
			this.exp1(); /* optional step */
		else { /* default step = 1 */
			fs.codeABx(FuncState.OP_LOADK, fs.freereg, fs.numberK(1));
			fs.reserveregs(1);
		}
		this.forbody(base, line, 1, true);
	}


	void forlist(String indexname) {
		/* forlist -> NAME {,NAME} IN explist1 forbody */
		FuncState fs = this.fs;
		ExpDesc e = new ExpDesc();
		int nvars = 0;
		int line;
		int base = fs.freereg;
		/* create control variables */
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_GENERATOR, nvars++);
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_STATE, nvars++);
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_CONTROL, nvars++);
		/* create declared variables */
		this.new_localvar(indexname, nvars++);
		while (this.testnext(COMMA))
			this.new_localvar(this.str_checkname(), nvars++);
		this.checknext(IN);
		line = this.linenumber;
		this.adjust_assign(3, this.explist1(e), e);
		fs.checkstack(3); /* extra space to call generator */
		this.forbody(base, line, nvars - 3, false);
	}


	void forstat(int line) {
		/* forstat -> FOR (fornum | forlist) END */
		FuncState fs = this.fs;
		String varname;
		BlockCnt bl = new BlockCnt();
		fs.enterblock(bl, true); /* scope for loop and control variables */
		this.next(); /* skip `for' */
		varname = this.str_checkname(); /* first variable name */
		if (this.t == ASSIGN) {
			this.fornum(varname, line);
        } else if (this.t == COMMA) {
            this.forlist(varname);
        }
        else {
            this.syntaxerror(LUA_QL("=") + " or " + LUA_QL("in") + " expected");
        }
		this.check_match(END, FOR, line);
		fs.leaveblock(); /* loop scope (`break' jumps to this point) */
	}


	int test_then_block() {
		/* test_then_block -> [IF | ELSEIF] cond THEN block */
		int condexit;
		this.next(); /* skip IF or ELSEIF */
		condexit = this.cond();
		this.checknext(THEN);
		this.block(); /* `then' part */
		return condexit;
	}


	void ifstat(int line) {
		/* ifstat -> IF cond THEN block {ELSEIF cond THEN block} [ELSE block]
		 * END */
		FuncState fs = this.fs;
		int flist;
		int escapelist = NO_JUMP;
		flist = test_then_block(); /* IF cond THEN block */
		while (this.t == ELSEIF) {
			escapelist = fs.concat(escapelist, fs.jump());
			fs.patchtohere(flist);
			flist = test_then_block(); /* ELSEIF cond THEN block */
		}
		if (this.t == ELSE) {
			escapelist = fs.concat(escapelist, fs.jump());
			fs.patchtohere(flist);
			this.next(); /* skip ELSE (after patch, for correct line info) */
			this.block(); /* `else' part */
		} else
			escapelist = fs.concat(escapelist, flist);
		fs.patchtohere(escapelist);
		this.check_match(END, IF, line);
	}

	void localfunc() {
		ExpDesc v = new ExpDesc();
		ExpDesc b = new ExpDesc();
		FuncState fs = this.fs;
		this.new_localvar(this.str_checkname(), 0);
		v.init(VLOCAL, fs.freereg);
		fs.reserveregs(1);
		this.adjustlocalvars(1);
		this.body(b, false, this.linenumber);
		fs.storevar(v, b);
		/* debug information will only see the variable after this point! */
	}


	void localstat() {
		/* stat -> LOCAL NAME {`,' NAME} [`=' explist1] */
		int nvars = 0;
		int nexps;
		ExpDesc e = new ExpDesc();
		do {
			this.new_localvar(this.str_checkname(), nvars++);
		} while (this.testnext(COMMA));
		if (this.testnext(ASSIGN))
			nexps = this.explist1(e);
		else {
			e.k = VVOID;
			nexps = 0;
		}
		this.adjust_assign(nvars, nexps, e);
		this.adjustlocalvars(nvars);
	}


	boolean funcname(ExpDesc v) {
		/* funcname -> NAME {field} [`:' NAME] */
		boolean needself = false;
		this.singlevar(v);
		while (this.t == DOT)
			this.field(v);
		if (this.t == COLON) {
			needself = true;
			this.field(v);
		}
		return needself;
	}


	void funcstat(int line) {
		PsiBuilder.Marker func = builder.mark();

        /* funcstat -> FUNCTION funcname body */
		boolean needself;
		ExpDesc v = new ExpDesc();
		ExpDesc b = new ExpDesc();
		this.next(); /* skip FUNCTION */
		needself = this.funcname(v);
		this.body(b, needself, line);

        func.done(FUNCTION_DEFINITION);
		fs.storevar(v, b);
		fs.fixline(line); /* definition `happens' in the first line */
	}


	void exprstat() {
		/* stat -> func | assignment */
		FuncState fs = this.fs;
		LHS_assign v = new LHS_assign();
		this.primaryexp(v.v);
		if (v.v.k == VCALL) /* stat -> func */
			FuncState.SETARG_C(fs.getcodePtr(v.v), 1); /* call statement uses no results */
		else { /* stat -> assignment */
			v.prev = null;
			this.assignment(v, 1);
		}
	}

	void retstat() {
		/* stat -> RETURN explist */
		FuncState fs = this.fs;
		ExpDesc e = new ExpDesc();
		int first, nret; /* registers with returned values */
		this.next(); /* skip RETURN */
		if (block_follow(this.t) || this.t == SEMI)
			first = nret = 0; /* return no values */
		else {
			nret = this.explist1(e); /* optional return values */
			if (hasmultret(e.k)) {
				fs.setmultret(e);
				if (e.k == VCALL && nret == 1) { /* tail call? */
					FuncState.SET_OPCODE(fs.getcodePtr(e), FuncState.OP_TAILCALL);
					FuncState._assert (FuncState.GETARG_A(fs.getcode(e)) == fs.nactvar);
				}
				first = fs.nactvar;
				nret = FuncState.LUA_MULTRET; /* return all values */
			} else {
				if (nret == 1) /* only one single value? */
					first = fs.exp2anyreg(e);
				else {
					fs.exp2nextreg(e); /* values must go to the `stack' */
					first = fs.nactvar; /* return all `active' values */
					FuncState._assert (nret == fs.freereg - first);
				}
			}
		}
		fs.ret(first, nret);
	}


	boolean statement() {
		int line = this.linenumber; /* may be needed for error messages */
		
		if (this.t == IF)  { /* stat -> ifstat */
			this.ifstat(line);
			return false;
		}
		if (this.t == WHILE) { /* stat -> whilestat */
			this.whilestat(line);
			return false;
		}
		if (this.t == DO) { /* stat -> DO block END */
			this.next(); /* skip DO */
			this.block();
			this.check_match(END, DO, line);
			return false;
		}
		if (this.t == FOR) { /* stat -> forstat */
			this.forstat(line);
			return false;
		}
		if (this.t == REPEAT) { /* stat -> repeatstat */
			this.repeatstat(line);
			return false;
		}
		if (this.t == FUNCTION) {
			this.funcstat(line); /* stat -> funcstat */
			return false;
		}
		if (this.t == LOCAL) { /* stat -> localstat */
			this.next(); /* skip LOCAL */
			if (this.testnext(FUNCTION)) /* local function? */
				this.localfunc();
			else
				this.localstat();
			return false;
		}
		if (this.t == RETURN) { /* stat -> retstat */
			this.retstat();
			return true; /* must be last statement */
		}
		if (this.t == BREAK) { /* stat -> breakstat */
			this.next(); /* skip BREAK */
			this.breakstat();
			return true; /* must be last statement */
		}

			this.exprstat();
			return false; /* to avoid warnings */
	}

	void chunk() {
		/* chunk -> { stat [`;'] } */
		boolean islast = false;
		this.enterlevel();
		while (!islast && !block_follow(this.t)) {
			islast = this.statement();
			this.testnext(SEMI);
			FuncState._assert (this.fs.f.maxStacksize >= this.fs.freereg
					&& this.fs.freereg >= this.fs.nactvar);
			this.fs.freereg = this.fs.nactvar; /* free registers */
		}
		this.leavelevel();
	}

	/* }====================================================================== */


    @NotNull
    @Override
    public ASTNode parse(IElementType root, PsiBuilder builder) {
        String name="todo:name";
         if (name != null) {
            source = name;
        } else {
            name = "stdin";
            source = "[string \"" + trim(source, MAXSRC) + "\"]";
        }
        KahluaParser lexstate = new KahluaParser(z, 0, source);
        FuncState funcstate = new FuncState(lexstate);
        // lexstate.buff = buff;

        /* main func. is always vararg */
        funcstate.isVararg = FuncState.VARARG_ISVARARG;
        funcstate.f.name = name;

        final LuaPsiBuilder psiBuilder = new LuaPsiBuilder(builder);
        final PsiBuilder.Marker rootMarker = psiBuilder.mark();

        lexstate.builder = psiBuilder;
        lexstate.t = psiBuilder.getTokenType();
//        lexstate.next(); /* read first token */
        lexstate.chunk();
       // lexstate.check(EMPTY_INPUT);
        lexstate.close_func();

        FuncState._assert(funcstate.prev == null);
        FuncState._assert(funcstate.f.numUpvalues == 0);
        FuncState._assert(lexstate.fs == null);


      //  return funcstate.f;
        if (root != null)
            rootMarker.done(root);
        
        return builder.getTreeBuilt();
    }
}
/* The following code was generated by JFlex 1.4.1 on 4/15/10 12:56 AM */

package com.sylvanaar.idea.Lua.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.1
 * on 4/15/10 12:56 AM from the specification file
 * <tt>lua.flex</tt>
 */
class _LuaLexer implements FlexLexer, LuaTokenTypes {
  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int XLONGSTRING = 1;
  public static final int XSTRINGA = 6;
  public static final int XLONGCOMMENT = 4;
  public static final int XSHORTCOMMENT = 3;
  public static final int YYINITIAL = 0;
  public static final int XSHORTCOMMENT_BEGIN = 7;
  public static final int XSTRINGQ = 5;
  public static final int XLONGSTRING_BEGIN = 2;

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\1\1\3\1\0\1\1\1\2\22\0\1\1\1\42\1\37"+
    "\1\41\1\0\1\47\1\0\1\40\1\51\1\52\1\46\1\10\1\56"+
    "\1\36\1\11\1\50\12\6\1\60\1\57\1\44\1\12\1\43\2\0"+
    "\4\4\1\7\25\4\1\13\1\62\1\53\1\61\1\4\1\0\1\14"+
    "\1\17\1\31\1\16\1\21\1\27\1\4\1\34\1\26\1\4\1\22"+
    "\1\24\1\4\1\15\1\23\1\33\1\4\1\20\1\25\1\32\1\30"+
    "\1\5\1\35\3\4\1\54\1\0\1\55\1\45\uff81\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\10\0\1\1\1\2\2\3\1\4\1\2\1\5\1\6"+
    "\1\7\1\10\1\11\15\4\1\12\1\13\1\14\1\15"+
    "\1\16\1\17\1\1\1\20\1\21\1\22\1\23\1\24"+
    "\1\25\1\26\1\27\1\30\1\31\1\32\1\33\2\34"+
    "\1\35\1\36\1\37\2\40\1\41\1\42\1\43\1\41"+
    "\1\44\1\43\1\41\2\45\2\0\1\46\1\47\1\0"+
    "\1\50\3\4\1\51\4\4\1\52\1\4\1\53\1\54"+
    "\7\4\1\55\1\56\1\57\1\60\1\61\1\0\1\62"+
    "\1\0\1\63\2\0\1\5\1\0\1\5\1\64\1\65"+
    "\1\66\1\67\3\4\1\70\3\4\1\71\5\4\1\55"+
    "\1\72\3\4\1\73\4\4\1\74\1\75\1\4\1\76"+
    "\3\4\1\77\1\100\1\4\1\101\1\102\1\103\1\104"+
    "\1\105\2\4\1\106";

  private static int [] zzUnpackAction() {
    int [] result = new int[151];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\63\0\146\0\231\0\314\0\377\0\u0132\0\u0165"+
    "\0\u0198\0\u01cb\0\u01fe\0\u0198\0\u0231\0\u0264\0\u0297\0\u0198"+
    "\0\u02ca\0\u02fd\0\u0330\0\u0363\0\u0396\0\u03c9\0\u03fc\0\u042f"+
    "\0\u0462\0\u0495\0\u04c8\0\u04fb\0\u052e\0\u0561\0\u0594\0\u05c7"+
    "\0\u05fa\0\u0198\0\u0198\0\u062d\0\u0660\0\u0693\0\u06c6\0\u0198"+
    "\0\u0198\0\u0198\0\u0198\0\u0198\0\u0198\0\u0198\0\u0198\0\u0198"+
    "\0\u0198\0\u0198\0\u0198\0\u0198\0\u06f9\0\u0198\0\u0198\0\u0198"+
    "\0\u0198\0\u072c\0\u0198\0\u0198\0\u075f\0\u0792\0\u0198\0\u07c5"+
    "\0\u07f8\0\u0198\0\u082b\0\u085e\0\u0891\0\u08c4\0\u0198\0\u0330"+
    "\0\u0198\0\u08f7\0\u092a\0\u095d\0\u0231\0\u0990\0\u09c3\0\u09f6"+
    "\0\u0a29\0\u0231\0\u0a5c\0\u0231\0\u0231\0\u0a8f\0\u0ac2\0\u0af5"+
    "\0\u0b28\0\u0b5b\0\u0b8e\0\u0bc1\0\u0bf4\0\u0198\0\u0198\0\u0198"+
    "\0\u0198\0\u06f9\0\u0198\0\u072c\0\u0198\0\u0c27\0\u082b\0\u0c5a"+
    "\0\u0c5a\0\u0c8d\0\u0198\0\u0231\0\u0231\0\u0231\0\u0cc0\0\u0cf3"+
    "\0\u0d26\0\u0231\0\u0d59\0\u0d8c\0\u0dbf\0\u0231\0\u0df2\0\u0e25"+
    "\0\u0e58\0\u0e8b\0\u0ebe\0\u0198\0\u0198\0\u0ef1\0\u0f24\0\u0f57"+
    "\0\u0f8a\0\u0fbd\0\u0ff0\0\u1023\0\u1056\0\u0231\0\u0231\0\u1089"+
    "\0\u0231\0\u10bc\0\u10ef\0\u1122\0\u0231\0\u0231\0\u1155\0\u0231"+
    "\0\u0231\0\u0231\0\u0231\0\u0231\0\u1188\0\u11bb\0\u0231";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[151];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\15"+
    "\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27"+
    "\1\30\1\31\1\15\1\32\1\33\1\15\1\34\1\35"+
    "\1\36\1\15\1\37\2\15\1\40\1\41\1\42\1\43"+
    "\1\44\1\11\1\45\1\46\1\47\1\50\1\51\1\52"+
    "\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62"+
    "\1\63\1\11\53\64\1\65\7\64\3\66\1\14\57\66"+
    "\2\67\2\70\57\67\53\71\1\72\7\71\2\73\2\74"+
    "\33\73\1\75\22\73\1\76\2\73\2\77\34\73\1\100"+
    "\21\73\1\101\3\102\1\0\32\102\1\103\24\102\64\0"+
    "\1\12\3\0\1\12\60\0\1\14\63\0\4\15\4\0"+
    "\22\15\26\0\1\12\2\0\1\15\1\16\2\15\4\0"+
    "\22\15\33\0\1\17\1\104\1\0\1\105\7\0\1\104"+
    "\52\0\1\106\63\0\1\107\62\0\1\110\1\111\53\0"+
    "\4\15\4\0\1\15\1\112\20\15\31\0\4\15\4\0"+
    "\7\15\1\113\2\15\1\114\7\15\31\0\4\15\4\0"+
    "\7\15\1\115\12\15\31\0\4\15\4\0\4\15\1\116"+
    "\15\15\31\0\4\15\4\0\5\15\1\117\14\15\31\0"+
    "\4\15\4\0\1\15\1\120\6\15\1\121\11\15\31\0"+
    "\4\15\4\0\4\15\1\122\15\15\31\0\4\15\4\0"+
    "\7\15\1\123\12\15\31\0\4\15\4\0\1\15\1\124"+
    "\11\15\1\125\6\15\31\0\4\15\4\0\1\126\6\15"+
    "\1\127\4\15\1\130\5\15\31\0\4\15\4\0\1\15"+
    "\1\131\20\15\31\0\4\15\4\0\4\15\1\132\13\15"+
    "\1\133\1\15\31\0\4\15\4\0\20\15\1\134\1\15"+
    "\63\0\1\135\66\0\1\136\32\0\1\137\62\0\1\140"+
    "\62\0\1\141\62\0\1\142\40\0\1\143\21\0\1\144"+
    "\40\0\1\145\46\0\1\73\26\0\1\73\1\0\1\73"+
    "\5\0\3\73\1\0\2\73\6\0\1\73\2\0\1\73"+
    "\4\0\2\73\12\0\1\73\6\0\1\73\40\0\1\73"+
    "\25\0\1\73\1\0\1\73\5\0\3\73\1\0\2\73"+
    "\6\0\1\73\2\0\1\73\5\0\1\73\12\0\1\73"+
    "\6\0\1\73\13\0\1\146\22\0\1\147\32\0\1\150"+
    "\1\0\1\151\25\0\1\151\32\0\1\152\65\0\1\153"+
    "\55\0\4\15\4\0\2\15\1\154\17\15\31\0\4\15"+
    "\4\0\16\15\1\155\3\15\31\0\4\15\4\0\10\15"+
    "\1\156\11\15\31\0\4\15\4\0\5\15\1\157\14\15"+
    "\31\0\4\15\4\0\16\15\1\160\1\161\2\15\31\0"+
    "\4\15\4\0\2\15\1\162\17\15\31\0\4\15\4\0"+
    "\11\15\1\163\10\15\31\0\4\15\4\0\15\15\1\164"+
    "\4\15\31\0\4\15\4\0\10\15\1\165\11\15\31\0"+
    "\4\15\4\0\4\15\1\166\15\15\31\0\4\15\4\0"+
    "\1\15\1\167\20\15\31\0\4\15\4\0\16\15\1\170"+
    "\3\15\31\0\4\15\4\0\14\15\1\171\5\15\31\0"+
    "\4\15\4\0\5\15\1\172\14\15\31\0\4\15\4\0"+
    "\12\15\1\173\7\15\40\0\1\174\22\0\1\135\36\0"+
    "\1\146\1\175\55\0\1\150\62\0\1\152\1\104\11\0"+
    "\1\104\45\0\4\15\4\0\1\176\21\15\31\0\4\15"+
    "\4\0\14\15\1\177\5\15\31\0\4\15\4\0\5\15"+
    "\1\200\14\15\31\0\4\15\4\0\5\15\1\201\14\15"+
    "\31\0\4\15\4\0\1\202\21\15\31\0\4\15\4\0"+
    "\11\15\1\203\10\15\31\0\4\15\4\0\15\15\1\204"+
    "\4\15\31\0\4\15\4\0\12\15\1\205\7\15\31\0"+
    "\4\15\4\0\5\15\1\206\14\15\31\0\4\15\4\0"+
    "\1\15\1\207\20\15\31\0\4\15\4\0\10\15\1\210"+
    "\11\15\31\0\4\15\4\0\6\15\1\211\13\15\31\0"+
    "\4\15\4\0\4\15\1\212\15\15\31\0\4\15\4\0"+
    "\1\213\21\15\31\0\4\15\4\0\12\15\1\214\7\15"+
    "\31\0\4\15\4\0\10\15\1\215\11\15\31\0\4\15"+
    "\4\0\5\15\1\216\14\15\31\0\4\15\4\0\16\15"+
    "\1\217\3\15\31\0\4\15\4\0\10\15\1\220\11\15"+
    "\31\0\4\15\4\0\5\15\1\221\14\15\31\0\4\15"+
    "\4\0\1\15\1\222\20\15\31\0\4\15\4\0\16\15"+
    "\1\223\3\15\31\0\4\15\4\0\13\15\1\224\6\15"+
    "\31\0\4\15\4\0\12\15\1\225\7\15\31\0\4\15"+
    "\4\0\7\15\1\226\12\15\31\0\4\15\4\0\1\15"+
    "\1\227\20\15\25\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[4590];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;
  private static final char[] EMPTY_BUFFER = new char[0];
  private static final int YYEOF = -1;
  private static java.io.Reader zzReader = null; // Fake

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\10\0\1\11\2\1\1\11\3\1\1\11\21\1\2\11"+
    "\4\1\15\11\1\1\4\11\1\1\2\11\2\1\1\11"+
    "\2\1\1\11\1\1\2\0\1\1\1\11\1\0\1\11"+
    "\24\1\4\11\1\0\1\11\1\0\1\11\2\0\1\1"+
    "\1\0\1\1\1\11\20\1\2\11\32\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[151];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** this buffer may contains the current text array to be matched when it is cheap to acquire it */
  private char[] zzBufferArray;

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
    int yyline, yychar, yycolumn;

    ExtendedSyntaxStrCommentHandler longCommentOrStringHandler = new ExtendedSyntaxStrCommentHandler();


  _LuaLexer(java.io.Reader in) {
      this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  _LuaLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 136) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

  public final int getTokenStart(){
    return zzStartRead;
  }

  public final int getTokenEnd(){
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end,int initialState){
    zzBuffer = buffer;
    zzBufferArray = com.intellij.util.text.CharArrayUtil.fromSequenceWithoutCopying(buffer);
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzPushbackPos = 0;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  public void reset(CharSequence buffer, int initialState){
    reset(buffer, 0, buffer.length(), initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBufferArray != null ? zzBufferArray[zzStartRead+pos]:zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() {
    if (!zzEOFDone) {
      zzEOFDone = true;
    
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;
    char[] zzBufferArrayL = zzBufferArray;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      yychar+= zzMarkedPosL-zzStartRead;

      boolean zzR = false;
      for (zzCurrentPosL = zzStartRead; zzCurrentPosL < zzMarkedPosL;
                                                             zzCurrentPosL++) {
        switch (zzBufferL.charAt(zzCurrentPosL)) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn++;
        }
      }

      if (zzR) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL.charAt(zzMarkedPosL) == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof) 
            zzPeek = false;
          else 
            zzPeek = zzBufferL.charAt(zzMarkedPosL) == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = zzLexicalState;


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL.charAt(zzCurrentPosL++);
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL.charAt(zzCurrentPosL++);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 44: 
          { return IF;
          }
        case 71: break;
        case 70: 
          { return FUNCTION;
          }
        case 72: break;
        case 51: 
          { if (longCommentOrStringHandler.isCurrentExtQuoteStart(yytext())) {
                       yybegin(YYINITIAL); longCommentOrStringHandler.resetCurrentExtQuoteStart(); return LONGCOMMENT_END;
                       }  else { yypushback(yytext().length()-1); }
                        return LONGCOMMENT;
          }
        case 73: break;
        case 27: 
          { return EXP;
          }
        case 74: break;
        case 36: 
          { yybegin(YYINITIAL);return WRONG;
          }
        case 75: break;
        case 15: 
          { return LT;
          }
        case 76: break;
        case 40: 
          { longCommentOrStringHandler.setCurrentExtQuoteStart(yytext().toString()); yybegin( XLONGSTRING_BEGIN ); return LONGSTRING_BEGIN;
          }
        case 77: break;
        case 56: 
          { return END;
          }
        case 78: break;
        case 38: 
          { return CONCAT;
          }
        case 79: break;
        case 16: 
          { return MULT;
          }
        case 80: break;
        case 2: 
          { return WS;
          }
        case 81: break;
        case 50: 
          { if (longCommentOrStringHandler.isCurrentExtQuoteStart(yytext())) {
                       yybegin(YYINITIAL); longCommentOrStringHandler.resetCurrentExtQuoteStart(); return LONGSTRING_END;
                       } else { yypushback(yytext().length()-1); }
                        return LONGSTRING;
          }
        case 82: break;
        case 3: 
          { return NEWLINE;
          }
        case 83: break;
        case 9: 
          { return LBRACK;
          }
        case 84: break;
        case 21: 
          { return RBRACK;
          }
        case 85: break;
        case 13: 
          { return GETN;
          }
        case 86: break;
        case 68: 
          { return REPEAT;
          }
        case 87: break;
        case 52: 
          { return ELLIPSIS;
          }
        case 88: break;
        case 22: 
          { return LCURLY;
          }
        case 89: break;
        case 37: 
          { yybegin(XSHORTCOMMENT); return SHORTCOMMENT;
          }
        case 90: break;
        case 5: 
          { return NUMBER;
          }
        case 91: break;
        case 17: 
          { return MOD;
          }
        case 92: break;
        case 23: 
          { return RCURLY;
          }
        case 93: break;
        case 64: 
          { return FALSE;
          }
        case 94: break;
        case 24: 
          { return COMMA;
          }
        case 95: break;
        case 7: 
          { return DOT;
          }
        case 96: break;
        case 43: 
          { return IN;
          }
        case 97: break;
        case 42: 
          { return OR;
          }
        case 98: break;
        case 25: 
          { return SEMI;
          }
        case 99: break;
        case 18: 
          { return DIV;
          }
        case 100: break;
        case 34: 
          { yybegin(YYINITIAL); return WRONG;
          }
        case 101: break;
        case 30: 
          { return SHORTCOMMENT;
          }
        case 102: break;
        case 4: 
          { return NAME;
          }
        case 103: break;
        case 12: 
          { yybegin(XSTRINGA); return STRING;
          }
        case 104: break;
        case 26: 
          { return COLON;
          }
        case 105: break;
        case 54: 
          { return NOT;
          }
        case 106: break;
        case 53: 
          { return AND;
          }
        case 107: break;
        case 31: 
          { yybegin(YYINITIAL); return SHORTCOMMENT;
          }
        case 108: break;
        case 46: 
          { yybegin( XSHORTCOMMENT ); return SHEBANG;
          }
        case 109: break;
        case 32: 
          { return LONGCOMMENT;
          }
        case 110: break;
        case 8: 
          { return ASSIGN;
          }
        case 111: break;
        case 47: 
          { return GE;
          }
        case 112: break;
        case 35: 
          { yybegin(YYINITIAL); return STRING;
          }
        case 113: break;
        case 1: 
          { return WRONG;
          }
        case 114: break;
        case 45: 
          { yypushback(yylength()); yybegin( XSHORTCOMMENT_BEGIN ); return advance();
          }
        case 115: break;
        case 69: 
          { return ELSEIF;
          }
        case 116: break;
        case 65: 
          { return UNTIL;
          }
        case 117: break;
        case 61: 
          { return THEN;
          }
        case 118: break;
        case 41: 
          { return DO;
          }
        case 119: break;
        case 14: 
          { return GT;
          }
        case 120: break;
        case 63: 
          { return LOCAL;
          }
        case 121: break;
        case 28: 
          { return LONGSTRING;
          }
        case 122: break;
        case 59: 
          { return ELSE;
          }
        case 123: break;
        case 29: 
          { yypushback(1); yybegin(XLONGSTRING); return advance();
          }
        case 124: break;
        case 67: 
          { return RETURN;
          }
        case 125: break;
        case 66: 
          { return WHILE;
          }
        case 126: break;
        case 49: 
          { return NE;
          }
        case 127: break;
        case 10: 
          { return MINUS;
          }
        case 128: break;
        case 11: 
          { yybegin(XSTRINGQ);  return STRING;
          }
        case 129: break;
        case 55: 
          { return NIL;
          }
        case 130: break;
        case 33: 
          { return STRING;
          }
        case 131: break;
        case 60: 
          { return TRUE;
          }
        case 132: break;
        case 57: 
          { return FOR;
          }
        case 133: break;
        case 19: 
          { return LPAREN;
          }
        case 134: break;
        case 62: 
          { return BREAK;
          }
        case 135: break;
        case 20: 
          { return RPAREN;
          }
        case 136: break;
        case 39: 
          { return EQ;
          }
        case 137: break;
        case 6: 
          { return PLUS;
          }
        case 138: break;
        case 58: 
          { longCommentOrStringHandler.setCurrentExtQuoteStart(yytext().toString());   yybegin(XLONGCOMMENT); return LONGCOMMENT_BEGIN;
          }
        case 139: break;
        case 48: 
          { return LE;
          }
        case 140: break;
        default:
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            zzDoEOF();
            return null;
          }
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}

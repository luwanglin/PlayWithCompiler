import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个简单的手写的词法分析器。
 * 能够为后面的简单计算器、简单脚本语言产生Token。
 */
public class SimpleLexerNew {
    public static void main(String[] args) {
        SimpleLexerNew lexer = new SimpleLexerNew();
        String script = "int age=45";
        System.out.println("parse : " + script);
        SimpleTokenReaderNew tokenReaderNew = lexer.tokenize(script);
        dump(tokenReaderNew);

        //测试inta的解析
        script = "inta age = 45;";
        System.out.println("\nparse :" + script);
        tokenReaderNew = lexer.tokenize(script);
        dump(tokenReaderNew);

        //测试in的解析
        script = "in age = 45;";
        System.out.println("\nparse :" + script);
        tokenReaderNew = lexer.tokenize(script);
        dump(tokenReaderNew);

        //测试>=的解析
        script = "age >= 45;";
        System.out.println("\nparse :" + script);
        tokenReaderNew = lexer.tokenize(script);
        dump(tokenReaderNew);

        //测试>的解析
        script = "age > 45;";
        System.out.println("\nparse :" + script);
        tokenReaderNew = lexer.tokenize(script);
        dump(tokenReaderNew);
    }

    /**
     * 保存解析出来的token列表
     */
    private List<Token> tokens = null;


    /**
     * 临时保存token的文本
     */
    private StringBuilder currentTokenText = null;


    /**
     * 当前正在解析的token
     */
    private SimpleTokeNew currentToken = null;

    /**
     * 解析字符串，形成Token。这是一个有限状态自动机，在不同的状态中迁移
     */
    public SimpleTokenReaderNew tokenize(String code) {
        tokens = new ArrayList<>();
        CharArrayReader reader = new CharArrayReader(code.toCharArray());
        currentToken = new SimpleTokeNew();
        currentTokenText = new StringBuilder();
        int inCh = 0;
        char ch = 0;
        DfaState state = DfaState.Initial;
        try {
            while ((inCh = reader.read()) != -1) {
                ch = (char) inCh;
                switch (state) {
                    case Initial:
                        state = initToken(ch);// 重新确定后续状态
                        break;
                    case Id:
                        if (isAlpha(ch) || isDigit(ch)) {
                            currentTokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case GT:
                        if (ch == '=') {
                            state = DfaState.GE;
                            currentToken.type = TokenType.GE;
                            currentTokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                    case GE:
                    case Assignment:
                    case Plus:
                    case Minus:
                    case Star:
                    case Slash:
                    case SemiColon:
                    case LeftParen:
                    case RightParen:
                        state = initToken(ch);//退出当前状态，并保存Token
                        break;
                    case IntLiteral:
                        if (isDigit(ch)) {
                            currentTokenText.append(ch);//继续保持在数字字面量状态
                        } else {
                            state = initToken(ch); //退出当前状态，并保存Token
                        }
                        break;
                    case Id_int1:
                        if (ch == 'n') {
                            state = DfaState.Id_int2;
                            currentTokenText.append(ch);
                        } else if (isDigit(ch) || isAlpha(ch)) {
                            state = DfaState.Id; // 切换为Id状态
                            currentTokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int2:
                        if (ch == 't') {
                            state = DfaState.Id_int3;
                            currentTokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaState.Id;
                            currentTokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int3:
                        if (isBlank(ch)) {
                            currentToken.type = TokenType.Int; // 关键字int
                            state = initToken(ch);
                        } else {
                            state = DfaState.Id; // 切换为Id状态
                            currentTokenText.append(ch);
                        }
                        break;
                    default:
                }
            }
            // 把最后一个token送进去
            if (currentTokenText.length() > 0) {
                initToken(ch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SimpleTokenReaderNew(tokens);
    }

    private DfaState initToken(char ch) {
        if (currentTokenText.length() > 0) {
            // 完成一个token的解析，保存到token列表中
            currentToken.text = currentTokenText.toString();
            tokens.add(currentToken);
            currentToken = new SimpleTokeNew();
            currentTokenText = new StringBuilder();
        }
        DfaState newState = DfaState.Initial;
        if (isAlpha(ch)) { // 第一个字符是字母
            if (ch == 'i') {
                newState = DfaState.Id_int1; // 可能是关键字int
            } else {
                newState = DfaState.Id; // 进行ID状态
            }
            currentToken.type = TokenType.Identifier;
            currentTokenText.append(ch);
        } else if (isDigit(ch)) { //第一个字符是数字
            newState = DfaState.IntLiteral;
            currentToken.type = TokenType.IntLiteral;
            currentTokenText.append(ch);
        } else if (ch == '>') {
            //第一个字符是大于号
            newState = DfaState.GT;
            currentToken.type = TokenType.GT;
            currentTokenText.append(ch);
        } else if (ch == '+') {
            newState = DfaState.Plus;
            currentToken.type = TokenType.Plus;
            currentTokenText.append(ch);
        } else if (ch == '-') {
            newState = DfaState.Minus;
            currentToken.type = TokenType.Minus;
            currentTokenText.append(ch);
        } else if (ch == '*') {
            newState = DfaState.Star;
            currentToken.type = TokenType.Star;
            currentTokenText.append(ch);
        } else if (ch == '/') {
            newState = DfaState.Slash;
            currentToken.type = TokenType.Slash;
            currentTokenText.append(ch);
        } else if (ch == ';') {
            newState = DfaState.SemiColon;
            currentToken.type = TokenType.SemiColon;
            currentTokenText.append(ch);
        } else if (ch == '(') {
            newState = DfaState.LeftParen;
            currentToken.type = TokenType.LeftParen;
            currentTokenText.append(ch);
        } else if (ch == ')') {
            newState = DfaState.RightParen;
            currentToken.type = TokenType.RightParen;
            currentTokenText.append(ch);
        } else if (ch == '=') {
            newState = DfaState.Assignment;
            currentToken.type = TokenType.Assignment;
            currentTokenText.append(ch);
        } else {
            newState = DfaState.Initial; // skip all unknown patterns
        }
        return newState;
    }

    public static void dump(SimpleTokenReaderNew tokenReaderNew) {
        System.out.println("text\ttype");
        Token token;
        while ((token = tokenReaderNew.read()) != null) {
            System.out.println(token.getText() + "\t\t" + token.getType());
        }
    }

    /**
     * Token的一个简单实现。只有类型和文本值两个属性。
     */
    final class SimpleTokeNew implements Token {
        private TokenType type;

        private String text = null;

        @Override
        public TokenType getType() {
            return type;
        }

        @Override
        public String getText() {
            return text;
        }
    }


    /**
     * 一个简单的Token流。是把一个Token列表进行了封装。
     */
    class SimpleTokenReaderNew implements TokenReader {
        List<Token> tokenList;
        int pos = 0;

        public SimpleTokenReaderNew(List<Token> tokens) {
            this.tokenList = tokens;
        }

        @Override
        public Token read() {
            if (pos < tokenList.size()) {
                return tokenList.get(pos++);
            }
            return null;
        }

        @Override
        public Token peek() {
            if (pos < tokenList.size()) {
                return tokenList.get(pos);
            }
            return null;
        }

        @Override
        public void unread() {
            if (pos > 0) {
                pos--;
            }
        }

        @Override
        public int getPosition() {
            return pos;
        }

        @Override
        public void setPosition(int position) {
            if (position >= 0 && position < tokenList.size()) {
                pos = position;
            }
        }
    }

    enum DfaState {
        Initial,
        Id,
        Id_int1,
        Id_int2,
        Id_int3,
        IntLiteral,
        GT,
        Plus,
        Minus,
        Star,
        Slash, SemiColon, LeftParen, RightParen, Assignment, GE,


    }

    //是否是字母
    public boolean isAlpha(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    //是否是数字
    public boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    //是否是空白字符
    private boolean isBlank(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }
}

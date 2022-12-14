package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;

    private String input;
    private int index;

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        input = FileUtils.readFile(path);
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        index = 0;
    }

    private void skipNoneToken() {
        while (index < input.length() && input.substring(index, index+1).matches("[ \t\n\r]")) {
            index++;
        }
    }

    private void eatIntConst() {
        while (index < input.length() && Character.isDigit(input.charAt(index))) {
            index++;
        }
    }

    private void eatSymbolId() {
        while (index < input.length() && Character.isLetter(input.charAt(index))) {
            index++;
        }
    }
    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        index = 0;
        ArrayList<Token> res = new ArrayList<>();
        while (index < input.length()) {
//            System.out.println("index " + index + " char " + input.charAt(index));
            if (input.substring(index, index+1).matches("[ \t\n\r]")) {
                // skip all blank
                skipNoneToken();
                continue;
            }
            switch (input.charAt(index)) {
                case '+':
                    res.add(Token.simple(TokenKind.fromString("+")));
                    index++;
                    break;
                case '-':
                    res.add(Token.simple(TokenKind.fromString("-")));
                    index++;
                    break;
                case '*':
                    res.add(Token.simple(TokenKind.fromString("*")));
                    index++;
                    break;
                case '/':
                    res.add(Token.simple(TokenKind.fromString("/")));
                    index++;
                    break;
                case '(':
                    res.add(Token.simple(TokenKind.fromString("(")));
                    index++;
                    break;
                case ')':
                    res.add(Token.simple(TokenKind.fromString(")")));
                    index++;
                    break;
                case '=':
                    res.add(Token.simple(TokenKind.fromString("=")));
                    index++;
                    break;
                case ';':
                    res.add(Token.simple(TokenKind.fromString("Semicolon")));
                    index++;
                    break;
                case 'i':
                    if (index < input.length() - 2 && "int".equals(input.substring(index, index+3)) &&
                            (index == input.length() - 3 || !Character.isLetter(input.charAt(index+3)))) {
                        // if the token is 'int'
                        res.add(Token.simple(TokenKind.fromString("int")));
                        index += 3;
                        break;
                    }
                    // else go to default
                case 'r':
                    if (index < input.length() - 5 && "return".equals(input.substring(index, index+6)) &&
                            (index == input.length() - 6 || !Character.isLetter(input.charAt(index+6)))) {
                        // if the token is 'return'
                        res.add(Token.simple(TokenKind.fromString("return")));
                        index += 6;
                        break;
                    }
                    // else go to default
                default:
                    if (Character.isLetter(input.charAt(index))) {
                        // the token is id
                        int startIndex = index;
                        eatSymbolId();
                        res.add(Token.normal(TokenKind.fromString("id"), input.substring(startIndex, index)));
                        if (!symbolTable.has(input.substring(startIndex, index))) {
                            symbolTable.add(input.substring(startIndex, index));
                        }
                    } else if (Character.isDigit(input.charAt(index))) {
                        // the token is intConst
                        int startIndex = index;
                        eatIntConst();
                        res.add(Token.normal(TokenKind.fromString("IntConst"), input.substring(startIndex, index)));
                    } else {
                        System.out.println("index " + index + " char " + input.charAt(index));
                        throw new RuntimeException("syntax error");
                    }
            }
        }
        res.add(Token.eof());
        return res;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}

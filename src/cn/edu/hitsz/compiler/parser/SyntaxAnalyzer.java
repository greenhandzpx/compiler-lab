package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.*;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.ArrayList;
import java.util.List;

//TODO: 实验二: 实现 LR 语法分析驱动程序

/**
 * LR 语法分析驱动程序
 * <br>
 * 该程序接受词法单元串与 LR 分析表 (action 和 goto 表), 按表对词法单元流进行分析, 执行对应动作, 并在执行动作时通知各注册的观察者.
 * <br>
 * 你应当按照被挖空的方法的文档实现对应方法, 你可以随意为该类添加你需要的私有成员对象, 但不应该再为此类添加公有接口, 也不应该改动未被挖空的方法,
 * 除非你已经同助教充分沟通, 并能证明你的修改的合理性, 且令助教确定可能被改动的评测方法. 随意修改该类的其它部分有可能导致自动评测出错而被扣分.
 */
public class SyntaxAnalyzer {
    private final SymbolTable symbolTable;
    private final List<ActionObserver> observers = new ArrayList<>();


    public SyntaxAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * 注册新的观察者
     *
     * @param observer 观察者
     */
    public void registerObserver(ActionObserver observer) {
        observers.add(observer);
        observer.setSymbolTable(symbolTable);
    }

    /**
     * 在执行 shift 动作时通知各个观察者
     *
     * @param currentStatus 当前状态
     * @param currentToken  当前词法单元
     */
    public void callWhenInShift(Status currentStatus, Token currentToken) {
        for (final var listener : observers) {
            listener.whenShift(currentStatus, currentToken);
        }
    }

    /**
     * 在执行 reduce 动作时通知各个观察者
     *
     * @param currentStatus 当前状态
     * @param production    待规约的产生式
     */
    public void callWhenInReduce(Status currentStatus, Production production) {
        for (final var listener : observers) {
//            System.out.println("call when reduce callback");
            listener.whenReduce(currentStatus, production);
        }
    }

    /**
     * 在执行 accept 动作时通知各个观察者
     *
     * @param currentStatus 当前状态
     */
    public void callWhenInAccept(Status currentStatus) {
        for (final var listener : observers) {
            listener.whenAccept(currentStatus);
        }
    }

    private ArrayList<Token> tokens = new ArrayList<>();

    public void loadTokens(Iterable<Token> tokens) {
        // TODO: 加载词法单元
        // 你可以自行选择要如何存储词法单元, 譬如使用迭代器, 或是栈, 或是干脆使用一个 list 全存起来
        // 需要注意的是, 在实现驱动程序的过程中, 你会需要面对只读取一个 token 而不能消耗它的情况,
        // 在自行设计的时候请加以考虑此种情况
        tokens.forEach(token -> this.tokens.add(token));
    }

    private LRTable table;

    public void loadLRTable(LRTable table) {
        // TODO: 加载 LR 分析表
        // 你可以自行选择要如何使用该表格:
        // 是直接对 LRTable 调用 getAction/getGoto, 抑或是直接将 initStatus 存起来使用
        this.table = table;
    }

    private ArrayList<Status> statusStack = new ArrayList<>();
    public static ArrayList<Symbol> symbolStack = new ArrayList<>();

    public void run() {
        // TODO: 实现驱动程序
        // 你需要根据上面的输入来实现 LR 语法分析的驱动程序
        // 请分别在遇到 Shift, Reduce, Accept 的时候调用上面的 callWhenInShift, callWhenInReduce, callWhenInAccept
        // 否则用于为实验二打分的产生式输出可能不会正常工作
        Status status = table.getInit();
        statusStack.add(status);
        int index = 0;
        while (index < tokens.size()) {
            Token token = tokens.get(index);
            System.out.println("token " + token.toString() + " status " + status.toString());
            index++;
            Action action = table.getAction(status, token);
            switch (action.getKind()) {
                case Shift:
                    // TODO: not sure status should be the one after shift or before shift
                    status = action.getStatus();
                    statusStack.add(status);
                    symbolStack.add(new Symbol(token));
                    Symbol symbol = symbolStack.get(symbolStack.size()-1);
                    callWhenInShift(status, token);
                    break;
                case Reduce:
                    Production production = action.getProduction();
                    System.out.println("generate " + production.toString());
                    index--;
//                    symbolStack.add(new Symbol(token));
                    System.out.println("top " + symbolStack.get(symbolStack.size()-1).getNonTerminal());
                    System.out.println("symbol stack size " + symbolStack.size());
                    callWhenInReduce(status, production);
//                    symbolStack.remove(symbolStack.size()-1);
                    for (Term term: production.body()) {
//                        assert(term.equals(tokenStack.get(tokenStack.size()-1)));
                        symbolStack.remove(symbolStack.size()-1);
                        statusStack.remove(statusStack.size()-1);
                    }
                    symbolStack.add(new Symbol(production.head()));

                    status = statusStack.get(statusStack.size() - 1);
                    status = table.getGoto(status, production.head());
                    statusStack.add(status);
                    break;
                case Accept:
                    callWhenInAccept(status);
                    return;
                case Error:
                    System.out.println("syntax error");
                    return;
                default:
            }
        }

    }
}

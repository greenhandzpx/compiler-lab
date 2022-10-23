package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;

public class Symbol{
    Token token;
    NonTerminal nonTerminal;
    SourceCodeType sourceCodeType;
    IRValue irValue;

    private Symbol(Token token, NonTerminal nonTerminal, SourceCodeType sourceCodeType,
                   IRValue irValue){
        this.token = token;
        this.nonTerminal = nonTerminal;
        this.sourceCodeType = sourceCodeType;
        this.irValue = irValue;
//        System.out.println("get a token ");
//        System.out.println(this.token);
    }

    public Symbol(Token token){
        this(token, null, null, null);
    }

    public Symbol(NonTerminal nonTerminal){
        this(null, nonTerminal, null, null);
    }

    public boolean isToken(){
        return this.token != null;
    }

    public boolean isNonterminal(){
        return this.nonTerminal != null;
    }

    public Token getToken() { return this.token; }

    public NonTerminal getNonTerminal() { return this.nonTerminal; }

    public SourceCodeType getSourceCodeType() { return this.sourceCodeType; }

    public IRValue getIrValue() { return this.irValue; }
}

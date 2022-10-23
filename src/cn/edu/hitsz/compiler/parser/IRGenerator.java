package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Term;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    private final ArrayList<Instruction> irList = new ArrayList<>();
    private final ArrayList<IRValue> irStack = new ArrayList<>();
    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        if ("IntConst".equals(currentToken.getKind().toString())) {
            irStack.add(IRImmediate.of(Integer.parseInt(currentToken.getText())));
        } else if ("id".equals(currentToken.getKind().toString())) {
            System.out.println("ir stack: add an id " + currentToken.getText());
            irStack.add(IRVariable.named(currentToken.getText()));
        } else {
            irStack.add(IRVariable.named(currentToken.getKindId()));
        }
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        switch (production.index()) {
            case 6 -> {
                // S -> id = E
                irList.add(Instruction.createMov((IRVariable) irStack.get(irStack.size() - 3),
                        irStack.get(irStack.size() - 1)));
                for (Term term: production.body()) {
                    irStack.remove(irStack.size() - 1);
                }
                irStack.add(IRVariable.named(production.head().getTermName()));
            }
            case 7 -> {
                // S -> return E
                irList.add(Instruction.createRet(irStack.get(irStack.size() - 1)));
                for (Term term: production.body()) {
                    irStack.remove(irStack.size() - 1);
                }
                irStack.add(IRVariable.named(production.head().getTermName()));
            }
            case 8 -> {
                // E -> E + A
                IRVariable tmp = IRVariable.temp();
                irList.add(Instruction.createAdd(tmp, irStack.get(irStack.size() - 3),
                        irStack.get(irStack.size() - 1)));
                for (Term term: production.body()) {
                    irStack.remove(irStack.size() - 1);
                }
                System.out.println("add a tmp " + tmp);
                irStack.add(tmp);
            }
            case 9 -> {
                // E -> E - A
                IRVariable tmp = IRVariable.temp();
                irList.add(Instruction.createSub(tmp, irStack.get(irStack.size() - 3),
                        irStack.get(irStack.size() - 1)));
                for (Term term: production.body()) {
                    irStack.remove(irStack.size() - 1);
                }
                System.out.println("add a tmp " + tmp);
                irStack.add(tmp);
            }
            case 11 -> {
                // A -> A * B
                IRVariable tmp = IRVariable.temp();
                irList.add(Instruction.createMul(tmp, irStack.get(irStack.size() - 3),
                        irStack.get(irStack.size() - 1)));
                System.out.println("A -> A * B " + irStack.get(irStack.size()-2).toString());
                for (Term term: production.body()) {
                    irStack.remove(irStack.size() - 1);
                }
                System.out.println("add a tmp " + tmp);
                irStack.add(tmp);
            }
            case 4 -> {
                // S -> D id
                IRValue id = irStack.get(irStack.size()-1);
                for (Term term: production.body()) {
                    irStack.remove(irStack.size() - 1);
                }
                irStack.add(id);
            }
            case 10 -> {
                // E -> A
//                irStack.remove(irStack.size()-1);
            }
            case 12 -> {
                // A -> B
            }
            case 13 -> {
                // B -> (E)
                IRValue E = irStack.get(irStack.size()-2);
                for (Term term: production.body()) {
                    irStack.remove(irStack.size() - 1);
                }
                irStack.add(E);
            }
            case 14 -> {
                // B -> id
            }
            case 15 -> {
                // B -> IntConst
            }
            default -> {
                for (Term term : production.body()) {
                    irStack.remove(irStack.size() - 1);
                }
                irStack.add(IRVariable.named(production.head().getTermName()));
            }
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
//        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
//        throw new NotImplementedException();
    }

    public List<Instruction> getIR() {
        // TODO
        return irList;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}


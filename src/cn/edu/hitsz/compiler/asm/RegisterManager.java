package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.utils.BMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RegisterManager {
    private final BMap<IRVariable, RV32Register> variableRV32RegisterBMap = new BMap<>();

    private HashSet<RV32Register> registersUnused = new HashSet<>();

    private List<Instruction> irList;

    private int irIndex = 0;

    public RegisterManager() {
        registersUnused.addAll(Arrays.asList(RV32Register.values()));
    }

    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        irList = originInstructions;
    }

    public boolean varHasReg(IRVariable variable) {
        return variableRV32RegisterBMap.containsKey(variable);
    }

    public RV32Register getRegByVar(IRVariable variable) {
        return variableRV32RegisterBMap.getByKey(variable);
    }

    public RV32Register allocateRegister(IRVariable irVariable) {
        if (variableRV32RegisterBMap.containsKey(irVariable)) {
            return variableRV32RegisterBMap.getByKey(irVariable);
        }
        if (!registersUnused.isEmpty()) {
            // there is any reg that isn't used yet
            RV32Register res = registersUnused.iterator().next();
            registersUnused.remove(res);
            variableRV32RegisterBMap.replace(irVariable, res);
            return res;
        }
        // no, then fetch one from the variable that needn't use any more
        // check the variables in the irs after this ir one by one
        HashSet<IRVariable> variableSet = new HashSet<>();
        for (int i = irIndex; i < irList.size(); ++i) {
            Instruction ir = irList.get(i);
            if (ir.getKind().isReturn()) {
                if (ir.getReturnValue().isIRVariable()) {
                    IRVariable v = (IRVariable) ir.getReturnValue();
                    if (variableRV32RegisterBMap.containsKey(v)) {
                        variableSet.add(v);
                    }
                }
                continue;
            }
            if (variableRV32RegisterBMap.containsKey(ir.getResult())) {
                variableSet.add(ir.getResult());
            }
            if (ir.getKind().isBinary()) {
                if (ir.getLHS().isIRVariable()) {
                    IRVariable v = (IRVariable) ir.getLHS();
                    if (variableRV32RegisterBMap.containsKey(v)) {
                        variableSet.add(v);
                    }
                }
                if (ir.getRHS().isIRVariable()) {
                    IRVariable v = (IRVariable) ir.getRHS();
                    if (variableRV32RegisterBMap.containsKey(v)) {
                        variableSet.add(v);
                    }
                }
            } else if (ir.getKind().isUnary()) {
                if (ir.getFrom().isIRVariable()) {
                    IRVariable v = (IRVariable) ir.getFrom();
                    if (variableRV32RegisterBMap.containsKey(v)) {
                        variableSet.add(v);
                    }
                }
            }
        }
        for (IRVariable v: variableRV32RegisterBMap.keySet()) {
            if (!variableSet.contains(v)) {
                RV32Register reg = variableRV32RegisterBMap.getByKey(v);
                variableRV32RegisterBMap.replace(irVariable, reg);
                return reg;
            }
        }
        throw new RuntimeException("no available register for this value");
    }

    public void step() {
        irIndex++;
    }


}

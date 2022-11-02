package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {

    private List<Instruction> irList;
    private List<Instruction> processedIrList;
    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        irList = originInstructions;
    }

    List<Instruction> preProcess() {
        ArrayList<Instruction> res = new ArrayList<>();
        for (Instruction ir: irList) {
            if (ir.getKind().isReturn() || ir.getKind().isUnary()) {
                // TODO
                res.add(ir);
                continue;
            }
            if (ir.getKind().isBinary()) {
                if (ir.getLHS().isIRVariable()) {
                    // add a, b, c
                    // add a, b, 1
                    res.add(ir);
                    continue;
                }
                if (ir.getRHS().isIRVariable()) {
                    // add a, 1, c
                    switch (ir.getKind()) {
                        case ADD -> res.add(Instruction.createAdd(ir.getResult(), ir.getRHS(), ir.getLHS()));
                        case SUB -> {
                            Instruction tmpIr = Instruction.createMov(IRVariable.temp(), ir.getLHS());
                            res.add(tmpIr);
                            res.add(Instruction.createSub(ir.getResult(), tmpIr.getResult(), ir.getRHS()));
                        }
                        case MUL -> res.add(Instruction.createMul(ir.getResult(), ir.getRHS(), ir.getLHS()));
                        default -> throw new RuntimeException();
                    }
                } else {
                    // add a, 1, 2
                    int lhs = ((IRImmediate) ir.getLHS()).getValue();
                    int rhs = ((IRImmediate) ir.getRHS()).getValue();
                    res.add(Instruction.createMov(ir.getResult(), new IRImmediate(lhs + rhs)));
                }
            }
        }
        processedIrList = res;
        return res;
    }

    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        List<Instruction> processedIR = preProcess();
        System.out.println("preprocessed:");
        for (Instruction ir: processedIR) {
            System.out.println(ir);
        }
        RegisterManager registerManager = new RegisterManager();
        registerManager.loadIR(processedIR);
        for (Instruction ir: processedIR) {
            switch (ir.getKind()) {
                case RET -> {
                    if (ir.getReturnValue().isIRVariable()) {
                        ir.setReturnValueReg(registerManager.allocateRegister((IRVariable) ir.getReturnValue()));
                    }
                }
                case ADD, MUL, SUB -> {
                    ir.setResultReg(registerManager.allocateRegister(ir.getResult()));
                    ir.setLHSReg(registerManager.allocateRegister((IRVariable) ir.getLHS()));
                    if (ir.getRHS().isIRVariable()) {
                        ir.setRHSReg(registerManager.allocateRegister((IRVariable) ir.getRHS()));
                    }
                }
                case MOV -> {
                    ir.setResultReg(registerManager.allocateRegister(ir.getResult()));
                    if (ir.getFrom().isIRVariable()) {
                        ir.setFromReg(registerManager.allocateRegister((IRVariable) ir.getFrom()));
                    }
                }
            }
            registerManager.step();
        }
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        ArrayList<String> res = new ArrayList<>();
        res.add(".text");
        res.addAll(processedIrList.stream().map(Instruction::toRV32String).toList());
        FileUtils.writeLines(path, res);
    }
}


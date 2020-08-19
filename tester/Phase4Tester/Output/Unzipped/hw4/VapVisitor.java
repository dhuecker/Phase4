

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

import java.util.List;
import java.util.ArrayList;

public class VapVisitor<E extends Throwable> extends Visitor<E> {

    VFunction currentFunc;
    List<String> vapBuffer;
    int stackPointerSize;


    public void setData(VFunction cFunc) {
        this.currentFunc = cFunc;
        vapBuffer = new ArrayList<>();
        stackPointerSize = (cFunc.stack.out * 4) + (cFunc.stack.local * 4) + 8;
        for (int a = 0; a <= cFunc.body.length + cFunc.labels.length; a++) {
            vapBuffer.add("");
        }
    }

    public void printVapBuffer() {
        insertLabels();

        String onStart = "";
        onStart += "sw $fp -8($sp)\n";
        onStart += "move $fp $sp\n";
        onStart += "subu $sp $sp " + stackPointerSize + "\n";
        onStart += "sw $ra -4($fp)";

        vapBuffer.add(0, onStart);

        for (String lineTemp : vapBuffer) {
            System.out.println(lineTemp);
        }
    }

    /*
        Helper functions
    */

    int getRelativeLoc(int sourceLoc) {
        return (sourceLoc - currentFunc.sourcePos.line) - 1;
    }

    void insertLabels() {
        for (int a = 0; a < currentFunc.labels.length; a++) {
            vapBuffer.set(getRelativeLoc(currentFunc.labels[a].sourcePos.line), currentFunc.labels[a].ident + ":");
        }
    }

    void addLine(int loc, String line) {
        vapBuffer.set(loc,line);
    }

    /*
        Visitor functions
    */

    public void visit(VAssign x) throws E {
        String currentLine = "";
        int relativeLoc = getRelativeLoc(x.sourcePos.line);

        String dReg = x.dest.toString();

        if (x.source instanceof VVarRef) {
            currentLine += "move " + dReg + " " + x.source.toString();
        } else if (x.source instanceof VLitInt) {
            currentLine += "li " + dReg + " " + x.source.toString();
        } else {
            currentLine += "la " + dReg + " " + x.source.toString().replace(":", "");
        }

        addLine(relativeLoc, currentLine);
    }

    public void visit(VCall x) throws E {
        String currentLine = "";
        int relativeLoc = getRelativeLoc(x.sourcePos.line);

        if (x.addr instanceof VAddr.Var) {
            currentLine += "jalr " + x.addr.toString();
        } else {
            currentLine += "jal " + x.addr.toString().replace(":","");
        }

        addLine(relativeLoc, currentLine);
    }

    public void visit(VBuiltIn x) throws E {
        String currentLine = "";
        int relativeLoc = getRelativeLoc(x.sourcePos.line);

        switch (x.op.name) {
            case "Add":
                if (x.args[0] instanceof VOperand.Static && !(x.args[1] instanceof VOperand.Static)) {
                    currentLine += "addi " + x.dest.toString() + " " + x.args[1].toString() + " " + x.args[0].toString();
                } else if (x.args[0] instanceof VOperand.Static) {
                    currentLine += "li $t9 " + x.args[1].toString() + "\n";
                    currentLine += "addi $t9 $t9 " + x.args[0].toString() + "\n";
                    currentLine += "move " + x.dest.toString() + " $t9\n";
                } else {
                    currentLine += "add " + x.dest.toString() + " " + x.args[0].toString() + " " + x.args[1].toString();
                }
                break;
            case "Sub":
                if (x.args[0] instanceof  VOperand.Static && !(x.args[1] instanceof VOperand.Static)) {
                    int valTemp = Integer.parseInt(x.args[0].toString()) * -1;
                    currentLine += "addi " + x.dest.toString() + " " + x.args[1].toString() + " " + valTemp;
                } else if (x.args[0] instanceof VOperand.Static) {
                    currentLine += "li $t9 " + x.args[0].toString() + "\n";
                    currentLine += "li $t8 " + x.args[1].toString() + "\n";
                    currentLine += "subu " + x.dest.toString() + " $t9 $t8\n";
                } else {
                    currentLine += "sub " + x.dest.toString() + " " + x.args[0].toString() + " " + x.args[1].toString();
                }
                break;
            case "LtS":
            case "Lt":
                if (x.args[1] instanceof VOperand.Static) {
                    currentLine += "slti " + x.dest.toString() + " " + x.args[0].toString() + " " + x.args[1].toString();
                } else if (x.args[0] instanceof VOperand.Static){
                    currentLine += "li $t9 " + x.args[0].toString() + "\n";
                    currentLine += "slt " + x.dest.toString() + " $t9 " + x.args[1].toString();
                } else {
                    currentLine += "slt " + x.dest.toString() + " " + x.args[0].toString() + " " + x.args[1].toString();
                }
                break;
            case "MulS":
                if (x.args[0] instanceof VOperand.Static) {
                    currentLine += "li $t9 " + x.args[0].toString() + "\n";
                    currentLine += "mul " + x.dest.toString() + " $t9 " + x.args[1].toString();
                } else {
                    currentLine += "mul " + x.dest.toString() + " " + x.args[0].toString() + " " + x.args[1].toString();
                }
                break;
            case "PrintIntS":
                if (x.args[0] instanceof VVarRef) {
                    currentLine += "move $a0 " + x.args[0].toString() + "\n";
                } else {
                    currentLine += "li $a0 " + x.args[0].toString() + "\n";
                }
                currentLine += "jal _print";
                break;
            case "HeapAllocZ":
                if (x.args[0] instanceof VOperand.Static) {
                    currentLine += "li $a0 " + x.args[0].toString() + "\n";
                } else {
                    currentLine += "move $a0 " + x.args[0].toString() + "\n";
                }
                currentLine += "jal _heapAlloc\n";
                currentLine += "move " + x.dest.toString() + " $v0";
                break;
            case "Error":
                currentLine += "la $a0 _str0\n";
                currentLine += "j _error";
                break;
            default:
                currentLine += "(" + x.op.name + ") UNDEFINED";
                break;
        }

        addLine(relativeLoc, currentLine);
    }

    public void visit(VMemWrite x) throws E {
        String currentLine = "";
        int relativeLoc = getRelativeLoc(x.sourcePos.line);

        String src = x.source.toString().replace(":", "");
        String destTemp;
        String offsetTemp = "0";
        if (x.dest instanceof VMemRef.Global) {
            destTemp = ((VMemRef.Global) x.dest).base.toString();
            offsetTemp = Integer.toString(((VMemRef.Global) x.dest).byteOffset);
        } else {
            destTemp = "$sp";
            offsetTemp = Integer.toString((((VMemRef.Stack) x.dest).index) * 4);
        }

        if (x.source instanceof VVarRef) {
            currentLine += "sw " + x.source.toString() + " " + (offsetTemp) + "(" + destTemp + ")";
        } else {
            currentLine += "la $t9 " + src + "\n";
            currentLine += "sw $t9 " + offsetTemp + "(" + destTemp + ")";
        }

        addLine(relativeLoc, currentLine);
    }

    public void visit(VMemRead x) throws E {
        String currentLine = "";
        int relativeLoc = getRelativeLoc(x.sourcePos.line);

        String src = "";
        String offsetTemp = "0";

        if (x.source instanceof VMemRef.Global) {
            src = ((VMemRef.Global) x.source).base.toString();
            offsetTemp = Integer.toString(((VMemRef.Global) x.source).byteOffset);
        } else if (x.source instanceof VMemRef.Stack) {
            // Stack
            if (((VMemRef.Stack) x.source).region == VMemRef.Stack.Region.In) {
                src = "$fp";
            } else {
                src = "$sp";
            }
            offsetTemp = Integer.toString(((VMemRef.Stack) x.source).index * 4);
        }

        currentLine += "lw " + x.dest.toString() + " " + offsetTemp + "(" + src + ")";

        addLine(relativeLoc, currentLine);
    }

    public void visit(VBranch x) throws E {
        String currentLine = "";
        int relativeLoc = getRelativeLoc(x.sourcePos.line);

        if (x.positive) {
            currentLine += "bnez " + x.value.toString() + " " + x.target.ident;
        } else {
            currentLine += "beqz " + x.value.toString() + " " + x.target.ident;
        }

        addLine(relativeLoc, currentLine);
    }

    public void visit(VGoto x) throws E {
        String currentLine = "";
        int relativeLoc = getRelativeLoc(x.sourcePos.line);

        currentLine += "j " + x.target.toString().substring(1);

        addLine(relativeLoc, currentLine);
    }

    public void visit(VReturn x) throws E {
        String currentLine = "";
        int relativeLoc = getRelativeLoc(x.sourcePos.line);

        if (x.value != null) {
            if (x.value instanceof VVarRef) {
                currentLine += "move $v0 " + x.value.toString() + "\n";
            } else if (x.value instanceof VOperand.Static) {
                currentLine += "li $v0 " + x.value.toString() + "\n";
            }
        }

        // on exit
        currentLine += "lw $ra -4($fp)\n";
        currentLine += "lw $fp -8($fp)\n";
        currentLine += "addu $sp $sp " + stackPointerSize + "\n";

        currentLine += "jr $ra";

        addLine(relativeLoc, currentLine);
    }
}

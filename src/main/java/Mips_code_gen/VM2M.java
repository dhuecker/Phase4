package Mips_code_gen;

import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;

public class VM2M {
    public static void main(String[] args) {
        inSelection();
    }

    public static void inSelection() {
        try {
            VaporProgram vtree = ParseVapMInput.parseVapor(System.in, System.err);

            // Translate data
            System.out.println(".data");
            for (int a = 0; a < vtree.dataSegments.length; a++) {
                System.out.println(vtree.dataSegments[a].ident + ":");
                for (int b = 0; b < vtree.dataSegments[a].values.length; b++) {
                    String labelTemp = vtree.dataSegments[a].values[b].toString().replace(":","");
                    System.out.println("    " + labelTemp);
                }
            }
            System.out.println("");

            // Translate text
            System.out.println(".text");

            // Jump-and-link to Main
            System.out.println("jal Main\nli $v0 10\nsyscall\n");

            // Translate function below
            MipsVisitor<Exception> vVisitor = new MipsVisitor<>();
            for (int a = 0; a < vtree.functions.length; a++) {
                VFunction currentF = vtree.functions[a];
                vVisitor.setData(currentF);


                System.out.println(currentF.ident + ":");

                for (int b = 0; b < currentF.body.length; b++) {
                    currentF.body[b].accept(vVisitor);
                }

                vVisitor.printVapBuffer();

                System.out.println();
            }

            // Translate syscalls & data
            System.out.println("\n_print:\nli $v0 1\nsyscall\nla $a0 _newline\nli $v0 4\nsyscall\njr $ra");
            System.out.println("\n_error:\nli $v0 4\nsyscall\nli $v0 10\nsyscall");
            System.out.println("\n_heapAlloc:\nli $v0 9\nsyscall\njr $ra");
            System.out.println("\n.data");
            System.out.println(".align 0");
            System.out.println("_newline: .asciiz \"\\n\"");
            System.out.println("_str0: .asciiz \"null pointer\\n\"");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace(System.out);
        }
    }
} //end VM2M class
